import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("maven-publish")
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
}

group = "io.github.dankosik"
version = "0.6.1-beta27"

extra["tinkoffSdkVersion"] = "1.6"
extra["kotlinLoggingVersion"] = "3.0.5"
extra["kotlinCoroutinesVersion"] = "1.7.3"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
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
    implementation("io.github.microutils:kotlin-logging:${property("kotlinLoggingVersion")}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${property("kotlinCoroutinesVersion")}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinCoroutinesVersion")}")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions")

    //tinkoff-sdk
    api("ru.tinkoff.piapi:java-sdk-core:${property("tinkoffSdkVersion")}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootJar> {
    enabled = false
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.properties["ossrhUsername"].toString())
            password.set(project.properties["ossrhPassword"].toString())
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "invest-api-java-sdk-starter"
            from(components["java"])

            pom {
                name.set("Invest API java sdk starter")
                url.set("https://github.com/Dankosik/invest-api-java-sdk-starter")
                description.set("Starter for RussianInvestments/invest-api-java-sdk is a convenient tool that allows you to" +
                        " quickly and easily integrate RussianInvestments/invest-api-java-sdk for accessing market data and trading into your Spring Boot applications." +
                        " This starter provides easy configuration and automatic setup, allowing you to focus on developing functionality instead of spending time on setting up the integration with RussianInvestments/invest-api-java-sdk.")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://opensource.org/license/mit/")
                    }
                }
                developers {
                    developer {
                        id.set("Dankosik")
                        name.set("Daniil Koryto")
                        email.set("daniil.koryto@gmail.com")
                    }
                }
                scm {
                    connection.set("https://github.com/dankosik/invest-api-java-sdk-starter.git")
                    developerConnection.set("scm:git@github.com:dankosik/invest-api-java-sdk-starter.git")
                    url.set("https://github.com/dankosik/invest-api-java-sdk-starter")
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
                username = project.properties["ossrhUsername"].toString()
                password = project.properties["ossrhPassword"].toString()
            }
        }
    }
}


signing {
        val signingKey: String = project.properties["signing.key"].toString()
        val signingPassword: String = project.properties["signing.password"].toString()
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
}