# Docker Api
Makes using docker commands easier in Jenkins pipelines.

## Example Usage
```groovy
@Library('DockerApi')
import org.avalancs.docker.*
// Global Pipeline Library set up in Manage Jenkins > System

node() {
    ...
    Docker docker = new Docker(this) // pass pipeline script as 'this' so steps can be used
    docker.run(image: 'nginx:stable-alpine-slim') { containerId ->
       docker.exec(containerId: containerId, command: 'ls -la') 
    }
}
```