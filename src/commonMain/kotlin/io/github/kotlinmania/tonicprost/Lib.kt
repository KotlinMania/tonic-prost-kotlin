// port-lint: source lib.rs
/**
 * Prost codec implementation for tonic.
 *
 * This crate provides the [ProstCodec] for encoding and decoding protobuf
 * messages using the prost serialization library.
 *
 * # Example
 *
 * ```kotlin
 * val codec = ProstCodec.new<Message, Message> { buf ->
 *     // decode protobuf message from buffer
 *     Result.success(msg)
 * }
 * ```
 */
package io.github.kotlinmania.tonicprost

/**
 * Metadata and re-exports for the `tonic-prost` codec module.
 *
 * This package provides [ProstCodec], [ProstDecoder], and [ProstEncoder] for encoding
 * and decoding protobuf messages using the prost serialization library.
 */
public object TonicProst {
    public const val MODULE_NAME: String = "tonic-prost"
    public const val VERSION: String = "0.13.1"
}
