import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*


plugins {
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("com.google.protobuf") version "0.8.18"

}

group = "com.example.reactiveproject"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_18

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.github.sirayan.genericdb-mongo:generic-db:0.0.3")
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation ("org.mongodb:mongodb-driver-sync:4.6.0")
    testImplementation(kotlin("test"))

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("io.projectreactor:reactor-test")
    implementation("io.projectreactor:reactor-tools:3.4.21")

    implementation("io.github.lognet:grpc-spring-boot-starter:4.5.5")
    implementation("com.salesforce.servicelibs:reactor-grpc:1.2.3")
    implementation("com.salesforce.servicelibs:reactor-grpc-stub:1.2.3")
//    implementation("com.google.protobuf:protobuf-kotlin:3.21.3")
    api("io.grpc:grpc-protobuf:1.48.1")
//    api("com.google.protobuf:protobuf-java-util:3.21.3")
//    api("com.google.protobuf:protobuf-kotlin:3.21.3")
//    api("io.grpc:grpc-kotlin-stub:1.3.0")
    api("io.grpc:grpc-stub:1.48.1")
//    runtimeOnly("io.grpc:grpc-netty:1.48.1")

    implementation("io.nats:jnats:2.15.6")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive:2.7.2")
    implementation("io.lettuce:lettuce-core:6.2.0.RELEASE")

    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation ("org.mockito:mockito-core:4.0.0")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.48.1"
        }
//        id("grpckt") {
//            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.2.1:jdk7@jar"
//        }
        id("reactorGrpc") {
            // Download from the repository.
            artifact = "com.salesforce.servicelibs:reactor-grpc:1.2.3"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
//                id("grpckt")
                id("reactorGrpc")
            }
//            it.builtins {
//                id("kotlin")
//            }
        }
    }
}
sourceSets {
    main {
        java.srcDirs("src/main/kotlin", "${buildDir.absolutePath}/generated/source/proto/main")
        resources.srcDir("src/main/resources")
        proto.srcDir("src/main/proto")
    }
}
tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}