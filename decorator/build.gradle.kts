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
// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.4")}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
