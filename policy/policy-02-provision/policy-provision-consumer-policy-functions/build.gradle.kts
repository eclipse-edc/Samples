plugins {
    `java-library`
    id("application")
}

dependencies {
    api(libs.edc.data.plane.spi)

    implementation(libs.edc.control.plane.core)

    implementation(project(":policy:policy-02-provision:policy-provision"))
}
