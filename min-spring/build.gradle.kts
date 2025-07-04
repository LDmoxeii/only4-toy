plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    // https://mvnrepository.com/artifact/org.apache.tomcat.embed/tomcat-embed-core
    implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.5")
    // https://mvnrepository.com/artifact/org.slf4j/jul-to-slf4j
    implementation("org.slf4j:jul-to-slf4j:2.0.17")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.18")
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.56")

    // 测试依赖
    testImplementation(kotlin("test"))
}
