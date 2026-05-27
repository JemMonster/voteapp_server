plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    application
}

group = "com.example.voteapp.server"
version = "1.0-SNAPSHOT"



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
    
    // Flyway
    implementation("org.flywaydb:flyway-core:10.24.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.24.0")

    // Test
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("io.ktor:ktor-server-test-host:2.3.7")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.exposed:exposed-test-utils:0.55.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.h2database:h2:2.2.224")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

}

tasks.withType<Test> {
    useJUnitPlatform()
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

