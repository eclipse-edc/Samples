plugins {
    `java-library`
    id("application")
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    api(libs.edc.control.plane.core)

    implementation(libs.edc.data.plane.core)

    implementation(libs.opentelemetry.annotations)
}