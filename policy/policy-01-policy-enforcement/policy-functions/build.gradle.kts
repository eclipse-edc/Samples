plugins {
    `java-library`
    id("application")
}

dependencies {
    api(libs.edc.data.plane.spi)
    api(libs.edc.json.ld.spi)

    implementation(libs.edc.control.plane.core)

}
