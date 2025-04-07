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
}
