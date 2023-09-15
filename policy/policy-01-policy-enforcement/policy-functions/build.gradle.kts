plugins {
    `java-library`
    id("application")
}

dependencies {
    api(libs.edc.data.plane.spi)

    implementation(libs.edc.control.plane.core)

}
