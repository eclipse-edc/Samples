plugins {
    `java-library`
    id("application")
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    api("$groupId:data-plane-spi:$edcVersion")

    implementation("$groupId:control-plane-core:$edcVersion")

    implementation(project(":policy:policy-02-provision:policy-provision"))
}
