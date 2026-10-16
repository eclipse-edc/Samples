plugins {
    `java-library`
    id("application")
}

dependencies {
    api(libs.edc.core.spi)
    api(libs.edc.control.plane.spi)

    implementation(libs.edc.control.plane.core)

}
