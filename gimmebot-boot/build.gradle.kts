plugins {
    kotlin("jvm") version "1.5.0"
    `java-library`
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.32"
}

group = "com.github.gimme.gimmebot"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Project
    api(project(":gimmebot-core"))

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Test
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:3.6.28")
    testImplementation("org.mockito:mockito-inline:3.6.28")

    // Other
    implementation("org.apache.commons:commons-lang3:3.11")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "14"
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-progressive")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
            val javadocJar by tasks.creating(Jar::class) {
                dependsOn(dokkaHtml)
                archiveClassifier.set("javadoc")
                from(dokkaHtml.outputDirectory)
            }
            artifact(javadocJar)

            from(components["java"])
        }
    }
}
