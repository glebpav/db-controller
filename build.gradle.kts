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

listOf(
    "AnnotationProcessor",
    "Implementation",
    "RuntimeOnly",
    "CompileOnly"
).forEach { suffix ->
    configurations.named("integrationTest$suffix") {
        extendsFrom(configurations.getByName("test$suffix"))
    }
}

fun configureTestTask(task: Test) = task.apply {
    useJUnitPlatform()

    filter.includeTestsMatching("*Test")
    testLogging.events("passed", "skipped", "failed")

    outputs.upToDateWhen { false }

    doFirst {
        val agentJar = configurations.testRuntimeClasspath.get()
            .files
            .firstOrNull { it.name.contains("byte-buddy-agent") }
        if (agentJar != null) {
            jvmArgs("-javaagent:${agentJar.absolutePath}")
        } else {
            logger.warn("Byte Buddy agent not found on testRuntimeClasspath.")
        }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    configureTestTask(this)
}

tasks.test {
    configureTestTask(this)
}

// ===========================
//     Dependencies
// ===========================
dependencies {
    // Lombok
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // ANTLR
    antlr(libs.antlr)
    implementation(libs.antlr.runtime)

    // Dagger
    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
    testAnnotationProcessor(libs.dagger.compiler)

    // Misc
    implementation(libs.jetbrains.annotations)
    implementation(libs.jansi)

    // JUnit
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    implementation(libs.apiguardian)

    // Mockito
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.assertJ.core)
}
