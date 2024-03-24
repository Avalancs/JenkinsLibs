# Jenkins Libs
Monorepo for Jenkins global pipeline libraryies made by Avalancs. Check the readme file in each subfolder for the description of that particular library.

# What are shared libraries?
[Jenkins article on shared libraries, read first](https://www.jenkins.io/doc/book/pipeline/shared-libraries/)

## Structure of projects
Jenkins pipeline libraries [have to follow a directory structure](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#directory-structure).
For our use case that means source code has to be under `src/` directories.

To make local testing possible I have added Gradle to the project. While Jenkins will only read the `src` directory for source files
in Gradle I can also reference other projects and write JUnit tests. The tests are kept separate under the `test` directory.

To make your library inherit this configuration just apply the `id("JenkinsLibs.groovy-conventions")` plugin, which has it's source in the `buildSrc` folder.

Each project should have a `readme.md` file with a snippet on what imports you need in Jenkins!

You will need Groovy 2.4.x to compile because that is what Jenkins is using. You can download the sdk [here](https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-sdk-2.4.21.zip)

### Importing/Mocking Jenkins classes
Since most libraries do not need the entirety of Jenkins as dependency to work, I made the `JenkinsMock` project where
I created some classes with the same package and name, so the other libraries could compile locally for testing.

If you need more classes to be mocked feel free to extend this project, or in each project you can create a `mockSrc` folder
which will be used for local compilation, but Jenkins will ignore it.

If you don't actually need to store a variable by type you can use the groovy `def` keyword.
For example we store the pipeline `steps` variable with `def` so we can access pipeline steps like `steps.sh`.

## External libraries
If you want to include a library from Maven central, you need a `@Grab('group:artifact:version')` annotation inside the source files AND
also declare it in `gradle/libs.versions.toml` and inside the `build.gradle.kts` file of the project.
When you update a version don't forget to update at both places!

## How to use on Jenkins
[See here](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries). With the following additional steps:
* Since this is a monorepo, the `src` folder for any projects is not at the root, so for example the `Template` lib will have
  `Library Path (optional)` on the UI set to `Template/`.
* Do not enable `Load implicitly` if this will not be used in all builds!
* Check `Allow default version to be overridden` so branches can be tested