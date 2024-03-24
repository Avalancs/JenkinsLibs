package org.avalancs.docker

import org.jenkinsci.StepsMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class DockerTest {
    Docker docker, customDocker;

    @BeforeEach
    void setUp() {
        docker = new Docker(new StepsMock());
        customDocker = new Docker(new StepsMock(), '/opt/docker')
    }

    @Test
    void dockerBuildTest() {
        String cmd = docker.buildCommand('project/builder/Dockerfile', ['avalancs/project-builder:latest', 'avalancs/project-builder:latest-stable'], [uid: '1', guid: '2'], ['--otherArg', '--anotherArg']);
        println cmd;
        assertEquals('docker buildx build -f "project/builder/Dockerfile" -t avalancs/project-builder:latest -t avalancs/project-builder:latest-stable --build-arg "uid=1" --build-arg "guid=2" --otherArg --anotherArg .', cmd);
    }

    @Test
    void dockerBuildLegacyTest() {
        String cmd = new Docker(new StepsMock(), 'docker', false).buildCommand('project/builder/Dockerfile', ['avalancs/project-builder:latest', 'avalancs/project-builder:latest-stable'], [uid: '1', guid: '2'], ['--otherArg', '--anotherArg']);
        println cmd;
        assertEquals('docker build -f "project/builder/Dockerfile" -t avalancs/project-builder:latest -t avalancs/project-builder:latest-stable --build-arg "uid=1" --build-arg "guid=2" --otherArg --anotherArg .', cmd);
    }

    @Test
    void dockerBuildWithCustomDockerTest() {
        String cmd = customDocker.buildCommand();
        println cmd;
        assertEquals('/opt/docker buildx build .', cmd);
    }

    @Test
    void dockerBuildLegacyWithCustomDockerTest() {
        String cmd = new Docker(new StepsMock(), '/opt/docker', false).buildCommand();
        println cmd;
        assertEquals('/opt/docker build .', cmd);
    }

    @Test
    void dockerRunTest() {
        String cmd = docker.runCommand('avalancs/project-builder:latest', 'builder', 'root', 'cat',
            [envVar:'envVarValue', otherEnvVar: 'otherEnvValue'],
            ['100:100'],
            ['/tmp:/tmp:ro'],
            [dataVolume: '/data'], true,
            '/workDir',
            ['--otherArg', '--anotherArg']);
        println cmd;
        assertEquals('docker run -d -q --name "builder" -u "root" --entrypoint "cat" --env envVar="envVarValue" --env otherEnvVar="otherEnvValue" -p "100:100" --mount "/tmp:/tmp:ro" -v "dataVolume:/data" --rm -w "/workDir" --otherArg --anotherArg avalancs/project-builder:latest', cmd);
    }

    @Test
    void dockerRunWithCustomDockerTest() {
        String cmd = customDocker.runCommand('avalancs/project-builder:latest');
        println cmd;
        assertEquals('/opt/docker run -d -q avalancs/project-builder:latest', cmd);
    }

    @Test
    void dockerExecTest() {
        String cmd = docker.execCommand('builder', 'cat', 'root', '/workDir', [envVar:'envVarValue', otherEnvVar: 'otherEnvValue'], ['--otherArg', '--anotherArg']);
        println cmd;
        assertEquals('docker exec -u root -w "/workDir" -e "envVar=envVarValue" -e "otherEnvVar=otherEnvValue" --otherArg --anotherArg builder cat', cmd);
    }

    @Test
    void dockerExecWithCustomDockerTest() {
        String cmd = customDocker.execCommand('builder', 'cat');
        println cmd;
        assertEquals('/opt/docker exec builder cat', cmd);
    }

    @Test
    void dockerCopyToContainerTest() {
        String cmd = docker.dockerCopyCommand('builder', './project', '/workspace', true);
        println cmd;
        assertEquals('docker cp "./project" "builder:/workspace"', cmd);
    }

    @Test
    void dockerCopyToContainerWithCustomDockerTest() {
        String cmd = customDocker.dockerCopyCommand('builder', './project', '/workspace', true);
        println cmd;
        assertEquals('/opt/docker cp "./project" "builder:/workspace"', cmd);
    }

    @Test
    void dockerCopyFromContainerTest() {
        String cmd = docker.dockerCopyCommand('builder', '/workspace/frontend/dist', 'frontend', false);
        println cmd;
        assertEquals('docker cp "builder:/workspace/frontend/dist" "frontend"', cmd);
    }

    @Test
    void dockerCopyFromContainerWithCustomDockerTest() {
        String cmd = customDocker.dockerCopyCommand('builder', '/workspace/frontend/dist', 'frontend', false);
        println cmd;
        assertEquals('/opt/docker cp "builder:/workspace/frontend/dist" "frontend"', cmd);
    }
}
