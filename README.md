# tonic-prost-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Ftonic--prost--kotlin-blue.svg)](https://github.com/KotlinMania/tonic-prost-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/tonic-prost-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/tonic-prost-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/tonic-prost-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/tonic-prost-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`hyperium/tonic`](https://github.com/hyperium/tonic).

**Original Project:** This port is based on [`hyperium/tonic`](https://github.com/hyperium/tonic). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `hyperium/tonic`

> The text below is reproduced and lightly edited from [`https://github.com/hyperium/tonic`](https://github.com/hyperium/tonic). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

![](https://github.com/hyperium/tonic/raw/master/.github/assets/tonic-banner.svg?sanitize=true)


A rust implementation of [gRPC], a high performance, open source, general
RPC framework that puts mobile and HTTP/2 first.

> **Note**: tonic's [master](https://github.com/hyperium/tonic) branch is
> currently preparing breaking changes. For the most recently *released* code,
> look to the [0.14.x branch](https://github.com/hyperium/tonic/tree/v0.14.x).

[`tonic`] is a gRPC over HTTP/2 implementation focused on high performance, interoperability, and flexibility. This library was created to have first class support of async/await and to act as a core building block for production systems written in Rust.

[![Crates.io](https://img.shields.io/crates/v/tonic)](https://crates.io/crates/tonic)
[![Documentation](https://docs.rs/tonic/badge.svg)](https://docs.rs/tonic)
[![Crates.io](https://img.shields.io/crates/l/tonic)](LICENSE)


[Examples] | [Website] | [Docs] | [Chat][discord]

## Overview

[`tonic`] is composed of three main components: the generic gRPC implementation, the high performance HTTP/2
implementation and the codegen powered by [`prost`]. The generic implementation can support any HTTP/2
implementation and any encoding via a set of generic traits. The HTTP/2 implementation is based on [`hyper`],
a fast HTTP/1.1 and HTTP/2 client and server built on top of the robust [`tokio`] stack. The codegen
contains the tools to build clients and servers from [`protobuf`] definitions.

## Features

- Bi-directional streaming
- High performance async io
- Interoperability
- TLS backed by [`rustls`]
- Load balancing
- Custom metadata
- Authentication
- Health Checking

## Getting Started

- The [`helloworld`][helloworld-tutorial] tutorial provides a basic example of using `tonic`, perfect for first time users!
- The [`routeguide`][routeguide-tutorial] tutorial provides a complete example of using `tonic` and all its features.

Examples can be found in [`examples`] and for more complex scenarios [`interop`]
may be a good resource as it shows examples of many of the gRPC features.

### Rust Version

`tonic`'s MSRV is `1.88`.

### Dependencies

[`tonic-build`] uses `protoc` [Protocol Buffers compiler] in some APIs which compile Protocol Buffers resource files such as [`tonic_build::compile_protos()`].

[Protocol Buffers compiler]: https://protobuf.dev/downloads/
[`tonic_build::compile_protos()`]: https://docs.rs/tonic-build/latest/tonic_build/fn.compile_protos.html

## Getting Help

First, see if the answer to your question can be found in the API documentation.
If the answer is not there, there is an active community in
the [Tonic Discord channel][discord]. We would be happy to try to answer your
question. If that doesn't work, try opening an [issue] with the question.

[issue]: https://github.com/hyperium/tonic/issues/new/choose

## Project Layout

- [`tonic`]: Generic gRPC and HTTP/2 client/server implementation.
- [`tonic-build`]: [`prost`] based service codegen.
- [`tonic-types`]: [`prost`] based grpc utility types including support for gRPC Well Known Types.
- [`tonic-health`]: Implementation of the standard [gRPC health checking service][healthcheck].
  Also serves as an example of both unary and response streaming.
- [`tonic-reflection`]: A tonic based gRPC reflection implementation.
- [`examples`]: Example gRPC implementations showing off tls, load balancing and bi-directional streaming.
- [`interop`]: Interop tests implementation.

## Contributing

:balloon: Thanks for your help improving the project! We are so happy to have
you! We have a [contributing guide][guide] to help you get involved in the Tonic
project.

[guide]: CONTRIBUTING.md

## License

This project is licensed under the [MIT license](https://github.com/hyperium/tonic/blob/HEAD/LICENSE).

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in Tonic by you, shall be licensed as MIT, without any additional
terms or conditions.


[gRPC]: https://grpc.io
[`tonic`]: ./tonic
[`tonic-build`]: ./tonic-build
[`tonic-types`]: ./tonic-types
[`tonic-health`]: ./tonic-health
[`tonic-reflection`]: ./tonic-reflection
[`examples`]: ./examples
[`interop`]: ./interop
[`tokio`]: https://github.com/tokio-rs/tokio
[`hyper`]: https://github.com/hyperium/hyper
[`prost`]: https://github.com/tokio-rs/prost
[`protobuf`]: https://protobuf.dev/
[`rustls`]: https://github.com/rustls/rustls
[`interop`]: https://github.com/hyperium/tonic/tree/master/interop
[Examples]: https://github.com/hyperium/tonic/tree/master/examples
[Website]: https://github.com/hyperium/tonic
[Docs]: https://docs.rs/tonic
[discord]: https://discord.gg/6yGkFeN
[routeguide-tutorial]: https://github.com/hyperium/tonic/blob/master/examples/routeguide-tutorial.md
[helloworld-tutorial]: https://github.com/hyperium/tonic/blob/master/examples/helloworld-tutorial.md
[healthcheck]: https://grpc.io/docs/guides/health-checking/

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:tonic-prost-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`hyperium/tonic`](https://github.com/hyperium/tonic). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the tonic authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`hyperium/tonic`](https://github.com/hyperium/tonic) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
