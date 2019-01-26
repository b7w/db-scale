import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20"
}

group = "me.b7w"
version = "1.0-SNAPSHOT"

val kotlinVersion by extra("1.3.20")
val vertxVersion by extra("3.6.2")
val junitVersion by extra("4.12")


repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    compile("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    compile("io.vertx:vertx-core:${vertxVersion}")
    compile("io.vertx:vertx-lang-kotlin:${vertxVersion}")
    compile("io.vertx:vertx-lang-kotlin-coroutines:${vertxVersion}")
    compile("io.vertx:vertx-web:${vertxVersion}")
    compile("io.vertx:vertx-web-client:${vertxVersion}")
    compile("io.vertx:vertx-config:${vertxVersion}")
    compile("io.reactiverse:reactive-pg-client:0.11.2")

    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("org.apache.logging.log4j:log4j-to-slf4j:2.11.1")
    compile("org.slf4j:jul-to-slf4j:1.7.25")

    testCompile("junit:junit:${junitVersion}")
    testCompile("org.jetbrains.kotlin:kotlin-test:${kotlinVersion}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
