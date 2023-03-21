plugins {
    `java-library`
    id("application")
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    api("$groupId:control-plane-spi:$edcVersion")

    implementation("$groupId:data-plane-core:$edcVersion")

    implementation(libs.opentelemetry.annotations)
}