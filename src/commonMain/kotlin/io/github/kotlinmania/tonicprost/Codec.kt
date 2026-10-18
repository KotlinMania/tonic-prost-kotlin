// port-lint: source codec.rs
package io.github.kotlinmania.tonicprost

import io.github.kotlinmania.prost.DecodeError
import io.github.kotlinmania.prost.Message
import io.github.kotlinmania.tonic.Status
import io.github.kotlinmania.tonic.codec.BufferSettings
import io.github.kotlinmania.tonic.codec.Codec
import io.github.kotlinmania.tonic.codec.DecodeBuf
import io.github.kotlinmania.tonic.codec.Decoder
import io.github.kotlinmania.tonic.codec.EncodeBuf
import io.github.kotlinmania.tonic.codec.Encoder

/**
 * A [Codec] that implements `application/grpc+proto` via the prost library.
 */
class ProstCodec<T : Message, U : Message>(
    private val bufferSettings: BufferSettings = BufferSettings.default(),
) : Codec<T, U> {
    internal var defaultInstance: (() -> U)? = null
    internal var decodeFn: ((DecodeBuf) -> Result<U>)? = null

    internal constructor(
        bufferSettings: BufferSettings = BufferSettings.default(),
        defaultInstance: () -> U,
    ) : this(bufferSettings) {
        this.defaultInstance = defaultInstance
    }

    internal constructor(
        bufferSettings: BufferSettings = BufferSettings.default(),
        decodeFn: (DecodeBuf) -> Result<U>,
    ) : this(bufferSettings) {
        this.decodeFn = decodeFn
    }

    companion object {
        /**
         * Configure a ProstCodec with encoder/decoder buffer settings. This is used to control
         * how memory is allocated and grows per RPC.
         */
        fun <T : Message, U : Message> new(): ProstCodec<T, U> =
            ProstCodec()

        internal fun <T : Message, U : Message> new(defaultInstance: () -> U): ProstCodec<T, U> =
            ProstCodec(defaultInstance = defaultInstance)

        internal fun <T : Message, U : Message> new(decodeFn: (DecodeBuf) -> Result<U>): ProstCodec<T, U> =
            ProstCodec(decodeFn = decodeFn)

        fun <T : Message, U : Message> default(): ProstCodec<T, U> =
            new()

        internal fun <T : Message, U : Message> default(defaultInstance: () -> U): ProstCodec<T, U> =
            new(defaultInstance)

        internal fun <T : Message, U : Message> default(decodeFn: (DecodeBuf) -> Result<U>): ProstCodec<T, U> =
            new(decodeFn)

        /**
         * A tool for building custom codecs based on prost encoding and decoding.
         * See the codec buffers example for one possible way to use this.
         */
        fun <T : Message> rawEncoder(
            bufferSettings: BufferSettings = BufferSettings.default(),
        ): ProstEncoder<T> = ProstEncoder(bufferSettings)

        /**
         * A tool for building custom codecs based on prost encoding and decoding.
         * See the codec buffers example for one possible way to use this.
         */
        fun <U : Message> rawDecoder(
            bufferSettings: BufferSettings = BufferSettings.default(),
        ): ProstDecoder<U> = ProstDecoder(bufferSettings)

        internal fun <U : Message> rawDecoder(
            bufferSettings: BufferSettings = BufferSettings.default(),
            defaultInstance: () -> U,
        ): ProstDecoder<U> = ProstDecoder(bufferSettings, defaultInstance)

        internal fun <U : Message> rawDecoder(
            bufferSettings: BufferSettings = BufferSettings.default(),
            decodeFn: (DecodeBuf) -> Result<U>,
        ): ProstDecoder<U> = ProstDecoder(bufferSettings, decodeFn)
    }

    override fun encoder(): Encoder<T> = ProstEncoder(BufferSettings.default())

    override fun decoder(): Decoder<U> {
        val def = defaultInstance
        val fn = decodeFn
        return when {
            def != null -> ProstDecoder(BufferSettings.default(), def)
            fn != null -> ProstDecoder(BufferSettings.default(), fn)
            else -> ProstDecoder(BufferSettings.default())
        }
    }
}

/**
 * An [Encoder] that knows how to encode `T`.
 */
class ProstEncoder<T : Message>(
    private val bufferSettings: BufferSettings = BufferSettings.default(),
) : Encoder<T> {
    companion object {
        /**
         * Get a new encoder with explicit buffer settings.
         */
        fun <T : Message> new(
            bufferSettings: BufferSettings = BufferSettings.default(),
        ): ProstEncoder<T> = ProstEncoder(bufferSettings)
    }

    override fun encode(item: T, dst: EncodeBuf): Result<Unit> {
        val bytes = item.encodeToByteArray()
        dst.reserve(bytes.size)
        dst.put(bytes)
        return Result.success(Unit)
    }

    override fun bufferSettings(): BufferSettings = bufferSettings
}

/**
 * A [Decoder] that knows how to decode `U`.
 */
class ProstDecoder<U : Message>(
    private val bufferSettings: BufferSettings = BufferSettings.default(),
) : Decoder<U> {
    internal var defaultInstance: (() -> U)? = null
    internal var decodeFn: ((DecodeBuf) -> Result<U>)? = null

    internal constructor(
        bufferSettings: BufferSettings = BufferSettings.default(),
        defaultInstance: () -> U,
    ) : this(bufferSettings) {
        this.defaultInstance = defaultInstance
    }

    internal constructor(
        bufferSettings: BufferSettings = BufferSettings.default(),
        decodeFn: (DecodeBuf) -> Result<U>,
    ) : this(bufferSettings) {
        this.decodeFn = decodeFn
    }

    companion object {
        /**
         * Get a new decoder with explicit buffer settings.
         */
        fun <U : Message> new(
            bufferSettings: BufferSettings = BufferSettings.default(),
        ): ProstDecoder<U> = ProstDecoder(bufferSettings)

        internal fun <U : Message> new(
            bufferSettings: BufferSettings = BufferSettings.default(),
            defaultInstance: () -> U,
        ): ProstDecoder<U> = ProstDecoder(bufferSettings, defaultInstance)

        internal fun <U : Message> new(
            bufferSettings: BufferSettings = BufferSettings.default(),
            decodeFn: (DecodeBuf) -> Result<U>,
        ): ProstDecoder<U> = ProstDecoder(bufferSettings, decodeFn)
    }

    override fun decode(src: DecodeBuf): Result<U?> {
        if (!src.hasRemaining()) return Result.success(null)
        val fn = decodeFn
        if (fn != null) {
            return fn(src).map { it }
        }
        val def =
            defaultInstance
                ?: return Result.failure(IllegalStateException("No decoder or defaultInstance provided"))
        val chunk = src.chunk()
        return io.github.kotlinmania.prost.decode(chunk, def).fold(
            onSuccess = { msg ->
                src.advance(chunk.size)
                Result.success(msg)
            },
            onFailure = { err ->
                Result.failure(fromDecodeError(err).asException())
            },
        )
    }

    override fun bufferSettings(): BufferSettings = bufferSettings
}

/**
 * Map Protobuf parse errors to an INTERNAL status code.
 */
fun fromDecodeError(error: DecodeError): Status =
    Status.internal(error.message)

/**
 * Map Protobuf parse errors to an INTERNAL status code.
 */
fun fromDecodeError(error: Throwable): Status =
    Status.internal(error.message ?: "Protobuf decode error")

/**
 * Associated encode type for [Codec].
 */
typealias Encode<T> = Any?

/**
 * Associated decode type for [Codec].
 */
typealias Decode<U> = Any?

/**
 * Associated encoder type for [Codec].
 */
typealias Encoder<T> = ProstEncoder<T>

/**
 * Associated decoder type for [Codec].
 */
typealias Decoder<U> = ProstDecoder<U>

/**
 * Associated item type for [Encoder] and [Decoder].
 */
typealias Item<T> = Any?

/**
 * Associated error type for [Encoder] and [Decoder].
 */
typealias Error = Status

/**
 * Associated data type.
 */
typealias Data = ByteArray
