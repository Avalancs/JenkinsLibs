plugins {
    groovy
    id("JenkinsLibs.groovy-conventions")
}

group = "org.avalancs"
version = "1.0-SNAPSHOT"

dependencies {
    // Locally mock out Jenkins classes
    implementation(project(":JenkinsMock"))
}