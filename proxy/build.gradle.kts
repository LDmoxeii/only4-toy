plugins {
    kotlin("jvm") version "2.1.10"
}

group = "com.only4"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // https://mvnrepository.com/artifact/com.h2database/h2
    testImplementation("com.h2database:h2:2.3.232")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
