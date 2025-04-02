plugins {
    java
    application
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("ru.mephi.db.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    testImplementation(libs.junit)
    implementation(kotlin("stdlib-jdk8"))
}
kotlin {
    jvmToolchain(21)
}