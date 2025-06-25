plugins {
    java
    application
    idea
    antlr
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

configurations {
    maybeCreate("integrationTestAnnotationProcessor")
}

dependencies {
    // Lombok
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    "integrationTestAnnotationProcessor"(libs.lombok)

    // ANTLR
    antlr(libs.antlr)
    implementation(libs.antlr.runtime)

    // Dagger
    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
    testAnnotationProcessor(libs.dagger.compiler)
    "integrationTestAnnotationProcessor"(libs.dagger.compiler)

    // Misc
    implementation(libs.jetbrains.annotations)
    implementation(libs.jansi)

    // JUnit
    testImplementation(libs.junit5.jupiter.api)
    testRuntimeOnly(libs.junit5.jupiter.engine)
    testImplementation(libs.junit4)

    // Mockito
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.assertJ.core)
}

// ===========================
//     ANTLR
// ===========================
val generatedSourcesPath = "src/main/generated"
sourceSets["main"].java.srcDir(file(generatedSourcesPath))
idea.module.generatedSourceDirs.add(file(generatedSourcesPath))

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf(
        "-visitor",
        "-listener",
        "-package", "ru.mephi.sql.parser",
        "-Xexact-output-dir"
    )
    outputDirectory = file("$generatedSourcesPath/ru/mephi/sql/parser")
}
tasks.clean {
    doLast {
        file(generatedSourcesPath).deleteRecursively()
    }
}

tasks.compileJava {
    dependsOn(tasks.generateGrammarSource)
}
// ===========================
//     Testing
// ===========================
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
    }
}

fun configureTestTask(task: Test) {
    task.useJUnitPlatform()
    task.filter {
        includeTestsMatching("*Test")
    }
    task.testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    outputs.upToDateWhen { false }
    mustRunAfter("test")

    configureTestTask(this)
}

tasks.test {
    configureTestTask(this)
}

