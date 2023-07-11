plugins {
  id("base-conventions")
  id("java-library")
  id("net.kyori.indra")
  id("com.diffplug.spotless")
}

indra {
  javaVersions {
    target(11)
  }
}

repositories {
  if (!ci.get()) mavenLocal()
  mavenCentral()
}
