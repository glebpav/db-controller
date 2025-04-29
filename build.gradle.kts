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
    testAnnotationProcessor(libs.lombok)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
    testAnnotationProcessor(libs.dagger.compiler)

    implementation(libs.jetbrains.annotations)

    implementation(libs.jansi)

    testImplementation(libs.junit)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)

    testImplementation(libs.assertJ.core)
}

tasks.test {
    useJUnit()
    filter {
        includeTestsMatching("*Test")
    }
    testLogging {
        events("passed", "skipped", "failed")
    }
}
