<div align="center">

# KBeam
[![CI](https://img.shields.io/github/actions/workflow/status/aecsocket/kbeam/build.yml)](https://github.com/aecsocket/kbeam/actions/workflows/build.yml)
[![Release](https://img.shields.io/maven-central/v/io.github.aecsocket/kbeam?label=release)](https://central.sonatype.com/artifact/io.github.aecsocket/kbeam)
[![Snapshot](https://img.shields.io/nexus/s/io.github.aecsocket/kbeam?label=snapshot&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://central.sonatype.com/artifact/io.github.aecsocket/kbeam)

Generic data structure implementations and algorithms for Kotlin

### [GitHub](https://github.com/aecsocket/kbeam) · [Dokka](https://aecsocket.github.io/kbeam/dokka)

</div>

Provides general tools for manipulating, representing and storing data as well as data flow. The
name is inspired by the [Rust `crossbeam` crate](https://crates.io/crates/crossbeam), which focuses
on concurrent data structures.

There is no user documentation; see the Dokka page to get API docs.

## Usage

See the version badges for the latest release and snapshot builds.

```kotlin
repositories {
  mavenCentral()
  // for snapshot builds
  // maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  implementation("io.github.aecsocket", "kbeam", "VERSION")
}
```
