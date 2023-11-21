import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.2.0-RC1"
    id("io.spring.dependency-management") version "1.1.3"
    id("maven-publish")
    signing
    id("io.codearte.nexus-staging") version "0.30.0"
    kotlin("jvm") version "1.9.20-RC"
    kotlin("plugin.spring") version "1.9.20-RC"
}

group = "ru.dankos"
version = "0.6.0-SNAPSHOT"

extra["tinkoffSdkVersion"] = "1.6"
extra["kotlinLoggingVersion"] = "3.0.5"
extra["kotlinCoroutinesVersion"] = "1.7.3"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    //kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${property("kotlinCoroutinesVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinCoroutinesVersion")}")
    implementation("io.github.microutils:kotlin-logging:${property("kotlinLoggingVersion")}")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    //tinkoff-sdk
    implementation("ru.tinkoff.piapi:java-sdk-core:${property("tinkoffSdkVersion")}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootJar> {
    enabled = false
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    username = System.getenv("MAVEN_USERNAME")
    password = System.getenv("MAVEN_PASSWORD")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "io.github.dankosik"
            artifactId = "invest-api-java-sdk-starter"
            version = "0.0.1"
            from(components["java"])
            pom {
                packaging = "jar"
                name.set("Invest API java sdk starter")
                url.set("https://github.com/Dankosik/invest-api-java-sdk-starter")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://opensource.org/license/mit/")
                    }
                }

                scm {
                    connection.set("https://github.com/dankosik/invest-api-java-sdk-starter.git")
                    developerConnection.set("scm:git@github.com:dankosik/invest-api-java-sdk-starter.git")
                    url.set("https://github.com/dankosik/invest-api-java-sdk-starter")
                }

                developers {
                    developer {
                        id.set("Dankosik")
                        name.set("Daniil Koryto")
                        email.set("daniil.koryto@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}