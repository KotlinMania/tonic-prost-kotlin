// port-lint: source codec.rs
package io.github.kotlinmania.tonicprost

/**
 * Common protobuf message interface for prost-encoded models.
 */
public interface Message {
    /**
     * Encodes this message into the target buffer.
     */
    public fun encode(buf: EncodeBuf)

    /**
     * Returns the encoded length in bytes of this message.
     */
    public fun encodedLen(): Int
}

/**
 * Status error representing gRPC response outcomes.
 */
public class Status(
    public val code: Code,
    public val message: String,
) {
    public enum class Code(
        public val value: Int,
    ) {
        OK(0),
        CANCELLED(1),
        UNKNOWN(2),
        INVALID_ARGUMENT(3),
        DEADLINE_EXCEEDED(4),
        NOT_FOUND(5),
        ALREADY_EXISTS(6),
        PERMISSION_DENIED(7),
        RESOURCE_EXHAUSTED(8),
        FAILED_PRECONDITION(9),
        ABORTED(10),
        OUT_OF_RANGE(11),
        UNIMPLEMENTED(12),
        INTERNAL(13),
        UNAVAILABLE(14),
        DATA_LOSS(15),
        UNAUTHENTICATED(16),
    }

    public companion object {
        public const val GRPC_STATUS: String = "grpc-status"
        public const val GRPC_MESSAGE: String = "grpc-message"
        public const val HEADER_SIZE: Int = 5
        public const val LEN: Int = 10000
        public const val MAX_MESSAGE_SIZE: Int = 2 * 1024 * 1024

        public fun ok(): Status = Status(Code.OK, "")

        public fun internal(message: String): Status = Status(Code.INTERNAL, message)

        public fun outOfRange(message: String): Status = Status(Code.OUT_OF_RANGE, message)

        public fun invalidArgument(message: String): Status = Status(Code.INVALID_ARGUMENT, message)

        public fun resourceExhausted(message: String): Status = Status(Code.RESOURCE_EXHAUSTED, message)

        public fun fromDecodeError(error: Throwable): Status =
            internal(error.message ?: "Protobuf decode error")
    }

    override fun toString(): String = "Status(code=$code, message='$message')"

    override fun equals(other: Any?): Boolean =
        other is Status && other.code == code && other.message == message

    override fun hashCode(): Int = 31 * code.hashCode() + message.hashCode()
}

/**
 * Buffer settings controlling allocation and growth per RPC.
 */
public data class BufferSettings(
    public val initialCapacity: Int = 1024,
    public val maxCapacity: Int = 4 * 1024 * 1024,
) {
    public companion object {
        public fun default(): BufferSettings = BufferSettings()
    }
}

/**
 * Buffer used during encoding of protobuf payloads.
 */
public class EncodeBuf(
    initialCapacity: Int = 32,
) {
    private val buffer: ArrayList<Byte> = ArrayList(initialCapacity)

    public fun put(bytes: ByteArray) {
        for (b in bytes) {
            buffer.add(b)
        }
    }

    public fun put(byte: Byte) {
        buffer.add(byte)
    }

    public fun putU8(value: Int) {
        buffer.add(value.toByte())
    }

    public fun putU32(value: Long) {
        buffer.add(((value ushr 24) and 0xFFL).toByte())
        buffer.add(((value ushr 16) and 0xFFL).toByte())
        buffer.add(((value ushr 8) and 0xFFL).toByte())
        buffer.add((value and 0xFFL).toByte())
    }

    public fun reserve(additional: Int) {
        buffer.ensureCapacity(buffer.size + additional)
    }

    public fun toByteArray(): ByteArray = buffer.toByteArray()

    public fun size(): Int = buffer.size
}

/**
 * Buffer used during decoding of protobuf payloads.
 */
public class DecodeBuf(
    private val data: ByteArray,
    private var position: Int = 0,
) {
    public fun remaining(): Int = data.size - position

    public fun chunk(): ByteArray = data.copyOfRange(position, data.size)

    public fun advance(cnt: Int) {
        require(cnt <= remaining()) { "Cannot advance past end of buffer: cnt=$cnt, remaining=${remaining()}" }
        position += cnt
    }

    public fun getU8(): Int {
        require(remaining() >= 1) { "Buffer underflow" }
        return data[position++].toInt() and 0xFF
    }

    public fun getU32(): Long {
        require(remaining() >= 4) { "Buffer underflow" }
        val b0 = (data[position++].toLong() and 0xFFL) shl 24
        val b1 = (data[position++].toLong() and 0xFFL) shl 16
        val b2 = (data[position++].toLong() and 0xFFL) shl 8
        val b3 = data[position++].toLong() and 0xFFL
        return b0 or b1 or b2 or b3
    }
}

/**
 * Associated encode type for [Codec].
 */
public typealias Encode<T> = Any?

/**
 * Associated decode type for [Codec].
 */
public typealias Decode<U> = Any?

/**
 * Associated item type for [Encoder] and [Decoder].
 */
public typealias Item<T> = Any?

/**
 * Associated error type for [Encoder] and [Decoder].
 */
public typealias Error = Status

/**
 * Associated data frame payload type.
 */
public typealias Data = ByteArray

/**
 * Codec contract for protobuf encoding and decoding.
 */
public interface Codec<T, U> {
    public fun encoder(): Encoder<T>

    public fun decoder(): Decoder<U>
}

/**
 * Encoder interface for serializing messages.
 */
public interface Encoder<T> {
    public fun encode(item: T, buf: EncodeBuf): Result<Unit>

    public fun bufferSettings(): BufferSettings
}

/**
 * Decoder interface for deserializing messages.
 */
public interface Decoder<U> {
    public fun decode(buf: DecodeBuf): Result<U?>

    public fun bufferSettings(): BufferSettings
}

/**
 * A [Codec] that implements `application/grpc+proto` via the prost library.
 */
public class ProstCodec<T : Message, U>(
    private val decodeFn: (DecodeBuf) -> Result<U>,
) : Codec<T, U> {
    public companion object {
        /**
         * Configure a ProstCodec with encoder/decoder buffer settings. This is used to control
         * how memory is allocated and grows per RPC.
         */
        public fun <T : Message, U> new(decodeFn: (DecodeBuf) -> Result<U>): ProstCodec<T, U> =
            ProstCodec(decodeFn)

        public fun <T : Message, U> default(decodeFn: (DecodeBuf) -> Result<U>): ProstCodec<T, U> =
            new(decodeFn)

        /**
         * A tool for building custom codecs based on prost encoding and decoding.
         */
        public fun <T : Message> rawEncoder(
            bufferSettings: BufferSettings = BufferSettings.default(),
        ): ProstEncoder<T> = ProstEncoder(bufferSettings)

        /**
         * A tool for building custom codecs based on prost encoding and decoding.
         */
        public fun <U> rawDecoder(
            bufferSettings: BufferSettings = BufferSettings.default(),
            decodeFn: (DecodeBuf) -> Result<U>,
        ): ProstDecoder<U> = ProstDecoder(bufferSettings, decodeFn)
    }

    override fun encoder(): Encoder<T> = ProstEncoder(BufferSettings.default())

    override fun decoder(): Decoder<U> = ProstDecoder(BufferSettings.default(), decodeFn)
}

/**
 * An [Encoder] that knows how to encode `T`.
 */
public class ProstEncoder<T : Message>(
    private val bufferSettings: BufferSettings = BufferSettings.default(),
) : Encoder<T> {
    public companion object {
        /**
         * Get a new encoder with explicit buffer settings.
         */
        public fun <T : Message> new(
            bufferSettings: BufferSettings = BufferSettings.default(),
        ): ProstEncoder<T> = ProstEncoder(bufferSettings)
    }

    override fun encode(item: T, buf: EncodeBuf): Result<Unit> =
        runCatching {
            item.encode(buf)
        }

    override fun bufferSettings(): BufferSettings = bufferSettings
}

/**
 * A [Decoder] that knows how to decode `U`.
 */
public class ProstDecoder<U>(
    private val bufferSettings: BufferSettings = BufferSettings.default(),
    private val decodeFn: (DecodeBuf) -> Result<U>,
) : Decoder<U> {
    public companion object {
        /**
         * Get a new decoder with explicit buffer settings.
         */
        public fun <U> new(
            bufferSettings: BufferSettings = BufferSettings.default(),
            decodeFn: (DecodeBuf) -> Result<U>,
        ): ProstDecoder<U> = ProstDecoder(bufferSettings, decodeFn)
    }

    override fun decode(buf: DecodeBuf): Result<U?> {
        if (buf.remaining() == 0) return Result.success(null)
        return decodeFn(buf).map { it }
    }

    override fun bufferSettings(): BufferSettings = bufferSettings
}

/**
 * Helper to poll a chunk of data from an input buffer with a partial length limit.
 */
public fun pollFrame(
    data: ByteArray,
    position: Int,
    partialLen: Int,
): ByteArray? {
    if (position >= data.size) return null
    val sendLen = minOf(partialLen, data.size - position)
    return data.copyOfRange(position, position + sendLen)
}

public fun fromDecodeError(error: Throwable): Status = Status.fromDecodeError(error)
