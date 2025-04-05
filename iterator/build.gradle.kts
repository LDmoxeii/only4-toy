plugins {
    kotlin("jvm") version "2.1.10"
}

group = "com.only4"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // https://mvnrepository.com/artifact/cn.hutool/hutool-core
    implementation("cn.hutool:hutool-core:5.8.36")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
