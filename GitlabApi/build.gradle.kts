plugins {
    groovy
    id("JenkinsLibs.groovy-conventions")
}

group = "org.avalancs"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.bundles.logback)
    implementation(libs.httpclient5);
    implementation(libs.gson);
}