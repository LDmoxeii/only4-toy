plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    // https://mvnrepository.com/artifact/com.h2database/h2
    testImplementation("com.h2database:h2:2.3.232")

    // 测试依赖
    testImplementation(kotlin("test"))
}
