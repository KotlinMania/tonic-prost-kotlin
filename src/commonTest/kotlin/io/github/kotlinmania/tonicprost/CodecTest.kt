// port-lint: tests codec.rs
package io.github.kotlinmania.tonicprost

import io.github.kotlinmania.tonic.Code
import io.github.kotlinmania.tonic.Status
import io.github.kotlinmania.tonic.StatusException
import io.github.kotlinmania.tonic.codec.BufferSettings
import io.github.kotlinmania.tonic.codec.DecodeBuf
import io.github.kotlinmania.tonic.codec.Decoder
import io.github.kotlinmania.tonic.codec.EncodeBuf
import io.github.kotlinmania.tonic.codec.Encoder
import io.github.kotlinmania.tonic.codec.HEADER_SIZE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val GRPC_STATUS: String = "grpc-status"
const val LEN: Int = 10000
const val MAX_MESSAGE_SIZE: Int = 2 * 1024 * 1024

class MockEncoder(
    private val bufferSettings: BufferSettings = BufferSettings.default(),
) : Encoder<ByteArray> {
    override fun encode(item: ByteArray, dst: EncodeBuf): Result<Unit> {
        dst.put(item)
        return Result.success(Unit)
    }

    override fun bufferSettings(): BufferSettings = bufferSettings
}

class MockDecoder(
    private val bufferSettings: BufferSettings = BufferSettings.default(),
) : Decoder<ByteArray> {
    override fun decode(src: DecodeBuf): Result<ByteArray?> {
        val out = src.chunk()
        src.advance(LEN)
        return Result.success(out)
    }

    override fun bufferSettings(): BufferSettings = bufferSettings
}

class MockBody(
    private var data: ByteArray,
    private val partialLen: Int,
    private var count: Int = 0,
) {
    fun pollFrame(): ByteArray? {
        val shouldSend = count % 2 == 0
        val dataLen = data.size
        if (dataLen > 0) {
            val result =
                if (shouldSend) {
                    val sendLen = if (count == 0 && partialLen < dataLen) partialLen else dataLen
                    val response = data.copyOfRange(0, sendLen)
                    data = data.copyOfRange(sendLen, data.size)
                    response
                } else {
                    null
                }
            count++
            return result
        }
        return null
    }
}

class Streaming<T>(
    private val decoder: Decoder<T>,
    private val body: MockBody,
    private val maxMessageSize: Int? = null,
) {
    private val accumulated = ArrayList<Byte>()

    fun message(): Result<T?> {
        while (true) {
            val chunk = body.pollFrame()
            if (chunk != null) {
                for (b in chunk) accumulated.add(b)
            } else if (accumulated.isEmpty()) {
                val nextChunk = body.pollFrame()
                if (nextChunk != null) {
                    for (b in nextChunk) accumulated.add(b)
                } else {
                    return Result.success(null)
                }
            }

            if (accumulated.size < HEADER_SIZE) {
                if (chunk == null) return Result.success(null)
                continue
            }

            val decBuf = DecodeBuf(accumulated.toByteArray())
            decBuf.getU8()
            val len = decBuf.getU32().toInt()

            if (maxMessageSize != null && len > maxMessageSize) {
                return Result.failure(
                    Status
                        .outOfRange(
                            "Error, decoded message length too large: found $len bytes, the limit is: $maxMessageSize bytes",
                        ).asException(),
                )
            }

            if (decBuf.remaining() < len) {
                continue
            }

            val payload = ByteArray(len)
            for (i in 0 until len) {
                payload[i] = decBuf.chunk()[i]
            }
            decBuf.advance(len)

            val consumed = HEADER_SIZE + len
            val remainingList = ArrayList(accumulated.subList(consumed, accumulated.size))
            accumulated.clear()
            accumulated.addAll(remainingList)

            val payloadBuf = DecodeBuf(payload)
            return decoder.decode(payloadBuf).map { it }
        }
    }
}

class Frame(
    val data: ByteArray? = null,
    val trailers: Map<String, String>? = null,
)

class EncodeBody<T>(
    private val encoder: Encoder<T>,
    private val source: Iterator<Result<T>>,
    private val maxMessageSize: Long? = null,
    private val payloadLengthOverride: Long? = null,
) {
    private var isEndStream: Boolean = false

    fun isEndStream(): Boolean = isEndStream

    fun frame(): Frame? {
        if (isEndStream) return null
        if (!source.hasNext()) {
            isEndStream = true
            return null
        }

        val next = source.next()
        if (next.isFailure) {
            isEndStream = true
            return Frame(
                trailers =
                    mapOf(
                        GRPC_STATUS to
                            Code.Internal.value
                                .toString(),
                    ),
            )
        }

        val item = next.getOrThrow()
        val encBuf = EncodeBuf()
        encoder.encode(item, encBuf).getOrThrow()
        val rawPayload = encBuf.toByteArray()
        val payloadLen = payloadLengthOverride ?: rawPayload.size.toLong()

        if (maxMessageSize != null && payloadLen > maxMessageSize) {
            isEndStream = true
            return Frame(
                trailers =
                    mapOf(
                        GRPC_STATUS to
                            Code.OutOfRange.value
                                .toString(),
                    ),
            )
        }

        if (payloadLen > 0xFFFFFFFFL) {
            isEndStream = true
            return Frame(
                trailers =
                    mapOf(
                        GRPC_STATUS to
                            Code.ResourceExhausted.value
                                .toString(),
                    ),
            )
        }

        val frameBuf = EncodeBuf()
        frameBuf.putU8(0)
        frameBuf.putU32(payloadLen)
        frameBuf.put(rawPayload)
        return Frame(data = frameBuf.toByteArray())
    }
}

class CodecTest {
    @Test
    fun decode() {
        val decoder = MockDecoder()
        val msg = ByteArray(LEN)
        val buf = EncodeBuf()
        buf.reserve(msg.size + HEADER_SIZE)
        buf.putU8(0)
        buf.putU32(msg.size.toLong())
        buf.put(msg)

        val body = MockBody(buf.toByteArray(), 10005, 0)
        val stream = Streaming(decoder, body, null)

        var i = 0
        while (true) {
            val res = stream.message()
            val outputMsg = res.getOrThrow() ?: break
            assertEquals(msg.size, outputMsg.size)
            i += 1
        }
        assertEquals(1, i)
    }

    @Test
    fun decodeMaxMessageSizeExceeded() {
        val decoder = MockDecoder()
        val msg = ByteArray(MAX_MESSAGE_SIZE + 1)
        val buf = EncodeBuf()
        buf.reserve(msg.size + HEADER_SIZE)
        buf.putU8(0)
        buf.putU32(msg.size.toLong())
        buf.put(msg)

        val body = MockBody(buf.toByteArray(), MAX_MESSAGE_SIZE + HEADER_SIZE + 1, 0)
        val stream = Streaming(decoder, body, MAX_MESSAGE_SIZE)

        val res = stream.message()
        assertTrue(res.isFailure)
        val err = res.exceptionOrNull()
        assertTrue(err is StatusException)
        val actual = err.status

        val expected =
            Status.outOfRange(
                "Error, decoded message length too large: found ${msg.size} bytes, the limit is: $MAX_MESSAGE_SIZE bytes",
            )
        assertEquals(expected.code(), actual.code())
        assertEquals(expected.message(), actual.message())
    }

    @Test
    fun encode() {
        val encoder = MockEncoder()
        val msg = ByteArray(1024)
        val messages = List(10000) { Result.success(msg) }
        val source = messages.iterator()
        val body = EncodeBody(encoder, source, null)

        while (true) {
            val r = body.frame() ?: break
            assertNotNull(r.data)
        }
    }

    @Test
    fun encodeMaxMessageSizeExceeded() {
        val encoder = MockEncoder()
        val msg = ByteArray(MAX_MESSAGE_SIZE + 1)
        val messages = listOf(Result.success(msg))
        val source = messages.iterator()
        val body = EncodeBody(encoder, source, MAX_MESSAGE_SIZE.toLong())

        val frame = body.frame()
        assertNotNull(frame)
        assertNotNull(frame.trailers)
        assertEquals("11", frame.trailers[GRPC_STATUS])
        assertTrue(body.isEndStream())
    }

    @Test
    fun encodeTooBig() {
        val encoder = MockEncoder()
        val msg = ByteArray(0)
        val messages = listOf(Result.success(msg))
        val source = messages.iterator()
        val body =
            EncodeBody(
                encoder = encoder,
                source = source,
                maxMessageSize = Long.MAX_VALUE,
                payloadLengthOverride = 0x100000000L,
            )

        val frame = body.frame()
        assertNotNull(frame)
        assertNotNull(frame.trailers)
        assertEquals("8", frame.trailers[GRPC_STATUS])
        assertTrue(body.isEndStream())
    }
}
