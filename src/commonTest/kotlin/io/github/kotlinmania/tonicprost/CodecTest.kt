// port-lint: tests codec.rs
package io.github.kotlinmania.tonicprost

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodecTest {
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
        assertEquals(Status.Code.OUT_OF_RANGE, status.code())
        assertEquals("Message limit exceeded", status.message())
        assertEquals("Status(code=OUT_OF_RANGE, message='Message limit exceeded')", status.toString())
    }

    @Test
    fun testBufferSettings() {
        val settings = BufferSettings(initialCapacity = 512, maxCapacity = 2048)
        assertEquals(512, settings.initialCapacity)
        assertEquals(2048, settings.maxCapacity)
    }
}
