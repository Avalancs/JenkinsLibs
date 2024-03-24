plugins {
    groovy
    id("JenkinsLibs.groovy-conventions")
}

group = "org.avalancs"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.pebble.template)
    // Locally mock out Jenkins classes
    implementation(project(":JenkinsMock"))
}