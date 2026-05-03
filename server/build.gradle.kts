plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    application
}

group = "com.example.voteapp.server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    
    // DB
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:6.0.0")
    
    // Firebase/JWT
    implementation("com.google.firebase:firebase-admin:9.4.0")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.12")
    
    // Test
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.exposed:exposed-test-utils:0.55.0")
    testImplementation("com.h2database:h2:2.3.232") // In-mem for tests
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xcontext-receivers"))
    }
}

application {
    mainClass.set("com.example.voteapp.server.ApplicationKt")
    
    project.extra.apply {
        set("ktor.deployment.port", 8080)
    }
}

ktor {
    fatJar {
        archiveClassifier.set("fatJar")
    }
}

