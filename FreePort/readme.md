# Free Port finder for Jenkins Global Library
Find a free port on the machine the build is running.

The source for the one-liner: https://stackoverflow.com/questions/46883466/find-unused-port-in-jenkins-pipeline-library

## Example Usage
Assuming you named the library `FreePort` in the Jenkins settings:
```groovy
@Library('FreePort')
import org.avalancs.freeport

node() {
    int port = PortFinder.getFreePort()
    ... // use port variable however you want
}
```