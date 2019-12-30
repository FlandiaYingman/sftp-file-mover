import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "tech.flandia_yingm"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.jcraft:jsch:0.1.55")

    testCompile("org.junit.jupiter:junit-jupiter-api:5.6.0-M1")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.6.0-M1")

    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("io.github.microutils:kotlin-logging:1.7.7")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}