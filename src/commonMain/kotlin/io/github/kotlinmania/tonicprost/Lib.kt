// port-lint: source lib.rs
package io.github.kotlinmania.tonicprost

/**
 * Prost codec implementation for tonic.
 *
 * This module provides the [ProstCodec], [ProstDecoder], and [ProstEncoder] for encoding
 * and decoding protobuf messages using the prost serialization model.
 */
public object TonicProst {
    public const val MODULE_NAME: String = "tonic_prost"
    public const val VERSION: String = "0.13.1"
}
