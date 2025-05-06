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

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
    testAnnotationProcessor(libs.dagger.compiler)

    implementation(libs.jetbrains.annotations)

    implementation(libs.jansi)

    antlr(libs.antlr)

    testImplementation(libs.junit)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)

    testImplementation(libs.assertJ.core)
}

val generatedSourcesPath = "src/main/generated"
sourceSets["main"].java.srcDir(file(generatedSourcesPath))
idea.module.generatedSourceDirs.add(file(generatedSourcesPath))

tasks.generateGrammarSource {
    arguments.addAll(listOf("-package", "ru.mephi.sql.parser"))

    val antlrOutput = layout.buildDirectory.dir("generated-src/antlr/main")

    doLast {
        val destinationDir = file("$generatedSourcesPath/ru/mephi/sql/parser")
        println("Copying generated grammar lexer/parser files to main directory.")
        println("To: $destinationDir")

        copy {
            from(antlrOutput.get().asFile)
            into(destinationDir)
        }

        antlrOutput.get().asFile.parentFile.deleteRecursively()
    }

    outputs.dir(generatedSourcesPath)
}

tasks.clean {
    doLast {
        file(generatedSourcesPath).deleteRecursively()
    }
}

tasks.compileJava {
    dependsOn(tasks.generateGrammarSource)
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
