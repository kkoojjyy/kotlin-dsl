plugins {
    application
    kotlin("jvm") version "1.3.10"
}

application {
    mainClassName = "experiment.MainKt"
}

dependencies {

    implementation(project(":gradle-api"))

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
}

repositories {
    jcenter()
}
