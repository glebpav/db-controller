plugins {
    java
    application
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

    implementation(libs.jetbrains.annotations)

    implementation(libs.jansi)

    testImplementation(libs.junit)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")

    testImplementation(libs.assertJ.core)
}

tasks.test {
    useJUnitPlatform()
    filter {
        includeTestsMatching("*Test")
    }
    testLogging {
        events("passed", "skipped", "failed")
    }
}
