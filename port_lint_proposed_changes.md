# port-lint Proposed Changes

**Generated:** 2026-08-31
**Source:** tmp/tonic-prost
**Target:** src/commonMain/kotlin

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/tonicprost/Codec.kt` | `// port-lint: source codec.rs` | `// port-lint: source codec.rs` | `codec.rs` | `port-lint provenance header matched only after fallback normalization: 'codec.rs' vs expected 'codec.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/tonicprost/CodecTest.kt` | `// port-lint: tests codec.rs` | `// port-lint: tests codec.rs` | `codec.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:codec.rs' vs expected 'codec.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/tonicprost/Lib.kt` | `// port-lint: source lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'lib.rs' vs expected 'lib.rs'` |
