plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)

    kotlin("plugin.spring") version "1.8.22"
    kotlin("plugin.jpa") version "1.8.22"
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
}

dependencies {
    // Spring Boot 依赖
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    // Spring 状态机依赖
    implementation("org.springframework.statemachine:spring-statemachine-starter:3.2.0")
    implementation("org.springframework.statemachine:spring-statemachine-redis:1.2.14.RELEASE")

    // MyBatis-Plus 依赖
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.3.2")

    // 数据库依赖
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.alibaba:druid-spring-boot-starter:1.2.9")

    // JSON处理
    implementation("com.alibaba:fastjson:2.0.32")

    // Kotlin 相关依赖
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 测试依赖
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}
