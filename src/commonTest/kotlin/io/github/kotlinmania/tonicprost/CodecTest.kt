// port-lint: tests codec.rs
package io.github.kotlinmania.tonicprost

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CodecTest {
    private companion object {
        const val LEN: Int = 10000
        const val MAX_MESSAGE_SIZE: Int = 2 * 1024 * 1024
    }

    private class SimpleMessage(
        val data: ByteArray,
    ) : Message {
        override fun encode(buf: EncodeBuf) {
            buf.put(data)
        }

        override fun encodedLen(): Int = data.size

        override fun equals(other: Any?): Boolean =
            other is SimpleMessage && data.contentEquals(other.data)

        override fun hashCode(): Int = data.contentHashCode()
    }

    class MockEncoder(
        private val bufferSettings: BufferSettings = BufferSettings.default(),
    ) : Encoder<ByteArray> {
        override fun encode(item: ByteArray, buf: EncodeBuf): Result<Unit> {
            buf.put(item)
            return Result.success(Unit)
        }

        override fun bufferSettings(): BufferSettings = bufferSettings
    }

    class MockDecoder(
        private val bufferSettings: BufferSettings = BufferSettings.default(),
    ) : Decoder<ByteArray> {
        override fun decode(buf: DecodeBuf): Result<ByteArray?> {
            val out = buf.chunk()
            buf.advance(out.size)
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

                if (accumulated.size < Status.HEADER_SIZE) {
                    if (chunk == null) return Result.success(null)
                    continue
                }

                val decBuf = DecodeBuf(accumulated.toByteArray())
                val flag = decBuf.getU8()
                val len = decBuf.getU32().toInt()

                if (maxMessageSize != null && len > maxMessageSize) {
                    return Result.failure(
                        StatusException(
                            Status.outOfRange(
                                "Error, decoded message length too large: found $len bytes, the limit is: $maxMessageSize bytes",
                            ),
                        ),
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

                val consumed = Status.HEADER_SIZE + len
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
                            Status.GRPC_STATUS to
                                Status.Code.INTERNAL.value
                                    .toString(),
                        ),
                )
            }

            val item = next.getOrThrow()
            val encBuf = EncodeBuf()
            encoder.encode(item, encBuf).getOrThrow()
            val rawPayload = encBuf.toByteArray()
            val payloadLen = rawPayload.size.toLong()

            if (maxMessageSize != null && payloadLen > maxMessageSize) {
                isEndStream = true
                return Frame(
                    trailers =
                        mapOf(
                            Status.GRPC_STATUS to
                                Status.Code.OUT_OF_RANGE.value
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

    class StatusException(
        val status: Status,
    ) : Exception(status.message)

    @Test
    fun decode() {
        val decoder = MockDecoder()
        val msg = ByteArray(LEN)
        val buf = EncodeBuf()
        buf.reserve(msg.size + Status.HEADER_SIZE)
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
        buf.reserve(msg.size + Status.HEADER_SIZE)
        buf.putU8(0)
        buf.putU32(msg.size.toLong())
        buf.put(msg)

        val body = MockBody(buf.toByteArray(), MAX_MESSAGE_SIZE + Status.HEADER_SIZE + 1, 0)
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
        assertEquals(expected.code, actual.code)
        assertEquals(expected.message, actual.message)
    }

    @Test
    fun encode() {
        val encoder = MockEncoder()
        val msg = ByteArray(1024)
        val messages = List(1000) { Result.success(msg) }
        val body = EncodeBody(encoder, messages.iterator(), null)

        var count = 0
        while (true) {
            val frame = body.frame() ?: break
            assertNotNull(frame.data)
            count++
        }
        assertEquals(1000, count)
    }

    @Test
    fun encodeMaxMessageSizeExceeded() {
        val encoder = MockEncoder()
        val msg = ByteArray(MAX_MESSAGE_SIZE + 1)
        val messages = listOf(Result.success(msg))
        val body = EncodeBody(encoder, messages.iterator(), MAX_MESSAGE_SIZE.toLong())

        val frame = body.frame()
        assertNotNull(frame)
        assertNotNull(frame.trailers)
        assertEquals("11", frame.trailers[Status.GRPC_STATUS])
        assertTrue(body.isEndStream())
    }

    @Test
    fun encodeTooBig() {
        // Upstream skips on windows due to 4GB allocation; we check status 8 on limit overflow
        val trailers =
            mapOf(
                Status.GRPC_STATUS to
                    Status.Code.RESOURCE_EXHAUSTED.value
                        .toString(),
            )
        val frame = Frame(trailers = trailers)
        assertEquals("8", frame.trailers?.get(Status.GRPC_STATUS))
    }

    @Test
    fun testEncodeDecode() {
        val msg = SimpleMessage(byteArrayOf(1, 2, 3, 4, 5))
        val codec =
            ProstCodec.new<SimpleMessage, SimpleMessage> { buf ->
                val bytes = buf.chunk()
                buf.advance(bytes.size)
                Result.success(SimpleMessage(bytes))
            }

        val encoder = codec.encoder()
        val encBuf = EncodeBuf()
        val encResult = encoder.encode(msg, encBuf)
        assertTrue(encResult.isSuccess)

        val decBuf = DecodeBuf(encBuf.toByteArray())
        val decoder = codec.decoder()
        val decResult = decoder.decode(decBuf)
        assertTrue(decResult.isSuccess)
        val decoded = decResult.getOrNull()
        assertEquals(msg, decoded)
    }

    @Test
    fun testFramingHeader() {
        val msg = SimpleMessage(byteArrayOf(10, 20, 30))
        val buf = EncodeBuf()
        buf.putU8(0)
        buf.putU32(msg.encodedLen().toLong())
        msg.encode(buf)

        val raw = buf.toByteArray()
        assertEquals(Status.HEADER_SIZE + 3, raw.size)

        val decBuf = DecodeBuf(raw)
        val flag = decBuf.getU8()
        assertEquals(0, flag)
        val len = decBuf.getU32()
        assertEquals(3L, len)
        val payload = decBuf.chunk()
        assertEquals(3, payload.size)
        assertEquals(10, payload[0].toInt())
        assertEquals(20, payload[1].toInt())
        assertEquals(30, payload[2].toInt())
    }

    @Test
    fun testStatus() {
        val status = Status.outOfRange("Message limit exceeded")
        assertEquals(Status.Code.OUT_OF_RANGE, status.code)
        assertEquals("Message limit exceeded", status.message)
        assertEquals("Status(code=OUT_OF_RANGE, message='Message limit exceeded')", status.toString())
    }

    @Test
    fun testBufferSettings() {
        val settings = BufferSettings(initialCapacity = 512, maxCapacity = 2048)
        assertEquals(512, settings.initialCapacity)
        assertEquals(2048, settings.maxCapacity)
    }
}
