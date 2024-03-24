plugins {
    groovy
}

repositories {
    mavenCentral()
}

// Assume Java 11 or newer is running on Jenkins
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

// cannot use version catalog inside convention plugin yet, need to use type-unsafe accessor...
val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // Groovy 2.4.x is running on Jenkins
    versionCatalog.findLibrary("groovy").ifPresent {
        implementation(it)
    }

    // Logback for logging
    versionCatalog.findBundle("logback").ifPresent {
        implementation(it)
    }

    // Use JUnit 5 for tests
    versionCatalog.findLibrary("junit-bom").ifPresent {
        testImplementation(platform(it))
    }
    versionCatalog.findLibrary("junit-jupiter").ifPresent {
        testImplementation(it)
    }
    versionCatalog.findLibrary("mockito").ifPresent {
        testImplementation(it)
    }
}

// Jenkins global pipeline libraries will look for the source code under the 'src' folder.
// mockSrc it there if we want to add any more code to use locally, but not on Jenkins itself
sourceSets {
    main {
        java {
            setSrcDirs(listOf<String>()) // no java source
        }
        groovy {
            setSrcDirs(listOf("src", "mockSrc"))
        }
    }

    test {
        java {
            setSrcDirs(listOf<String>()); // no java source
        }
        groovy {
            setSrcDirs(listOf("test"))
        }
        resources {
            setSrcDirs(listOf("test-resources"))
        }
    }
}

// use JUnit 5
tasks.withType<Test>() {
    useJUnitPlatform()
}

// Enable incremental compilation for Groovy
tasks.withType<GroovyCompile>().configureEach {
    options.isIncremental = true
    options.incrementalAfterFailure = true
}