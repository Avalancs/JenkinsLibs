package org.avalancs.docker

import groovy.util.logging.Slf4j

@Slf4j
class Docker implements Serializable {
    /** Docker command. Specify in constructor if not 'docker' */
    String dockerCommand;

    /** The jenkins pipeline script object, so we can call regular pipeline steps like sh */
    def steps;

    /** Use docker buildx build instead of docker build (legacy builder) */
    boolean useBuildX;

    /**
     * @param steps the pipeline script object
     * @param dockerCommand the path to the docker executable, only specify if not 'docker'
     */
    Docker(def steps, String dockerCommand = 'docker', boolean useBuildX = true) {
        this.steps = steps;
        this.dockerCommand = dockerCommand;
        this.useBuildX = useBuildX;
    }

    /**
     * @param registry name of registry in docker login command
     * @param credentialsId Jenkins credential id of type UsernamePassword
     */
    void loginToRegistry(String registry, String credentialsId) {
        steps.withCredentials([new org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordMultiBinding('dockerUsername', 'dockerPassword', credentialsId)]) {
            // steps.env.dockerUsername if you want to use it as a groovy variable
            steps.sh(script: "echo \$dockerPassword | $dockerCommand login ${registry} -u \$dockerUsername --password-stdin", label: "Log in to docker registry: $registry");
        }
    }

//region docker build
    void build(Map args) {
        build(
            args.get('dockerfile') as String,
            args.get('tags') as List<String>,
            args.get('buildArgs') as Map<String, String>,
            args.get('additionalArgs') as List<String>
        );
    }

    void build(String dockerFile = null, List<String> tags = null, Map<String, String> buildArgs = null, List<String> additionalArgs = null) {
        steps.sh(script: buildCommand(dockerFile, tags, buildArgs, additionalArgs), label: 'Docker build');
    }

    String buildCommand(String dockerFile = null, List<String> tags = null, Map<String, String> buildArgs = null, List<String> additionalArgs = null) {
        StringBuilder sb = new StringBuilder("$dockerCommand " + (useBuildX ? 'buildx build ' : 'build '));
        if(dockerFile != null) {
            sb.append('-f "' + dockerFile + '" ');
        }
        if(tags != null && !tags.isEmpty()) {
            tags.each {
                sb.append('-t ').append(it).append(' ');
            }
        }
        if(buildArgs != null && !buildArgs.isEmpty()) {
            buildArgs.each {
                sb.append('--build-arg "').append(it.key).append('=').append(it.value).append('" ');
            }
        }

        if(additionalArgs != null && !additionalArgs.isEmpty()) {
            additionalArgs.each {
                sb.append(it).append(' ');
            }
        }

        sb.append('.'); // run from current directory
        return sb.toString();
    }
//endregion

    void addToNetwork(String containerId, String network) {
        steps.sh(script: "$dockerCommand network connect $network $containerId", label: "Connect container to network: $network");
    }

//region docker run
    String run(Map args) {
        run(
            args.get('image') as String,
            args.get('name') as String,
            args.get('user') as String,
            args.get('entrypoint') as String,
            args.get('envVars') as Map<String, String>,
            args.get('ports') as List<String>,
            args.get('mounts') as List<String>,
            args.get('volumes') as Map<String, String>,
            args.getOrDefault('removeOnExit', false) as boolean,
            args.get('workingDirectory') as String,
            args.get('networks') as List<String>,
            args.get('otherArgs') as List<String>
        );
    }

    /**
     * https://docs.docker.com/reference/cli/docker/container/run/
     * The {@code volumes} and {@code mounts} parameters don't actually refer to volumes and bind mounts, both can be used for both, check out the linked documentation
     */
    String run(String image, String name = null, String user = null, String entrypoint = null, Map<String, String> envVars = null, List<String> ports = null, List<String> mounts = null,
             Map<String, String> volumes, boolean removeOnExit = false, String workingDirectory = null, List<String> networks = null, List<String> otherArgs = null) {
        String containerId = steps.sh(script: runCommand(image, name, user, entrypoint, envVars, ports, mounts, volumes, removeOnExit, workingDirectory, otherArgs), returnStdout: true, label: 'Docker run ' + image) as String;
        containerId = containerId.trim();

        if(networks != null) {
            networks.each {
                addToNetwork(containerId, it);
            }
        }

        return containerId;
    }

    /**
     * Runs the container, and stops it after the passed closure have finished running.
     * @param args See {@link #run(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.List, java.util.Map)}
     * @param closure code to run while the container is running in the background
     */
    void run(Map args, Closure closure) {
        // TODO: if name argument is given, check if container is already running!
        String containerId = run(args);
        try {
            closure.call(containerId)
        } finally {
            stopRemoveIgnoreError(containerId);
        }
    }

    String runCommand(String image, String name = null, String user = null, String entrypoint = null, Map<String, String> envVars = null, List<String> ports = null, List<String> mounts = null, Map<String, String> volumes = null, boolean removeOnExit = false, String workingDirectory = null, List<String> otherArgs = null) {
        StringBuilder sb = new StringBuilder(dockerCommand + ' run -d -q ');
        if(name != null) {
            sb.append('--name "').append(name).append('" ');
        }
        if(user != null) {
            sb.append('-u "').append(user).append('" ');
        }
        if(entrypoint != null) {
            sb.append('--entrypoint ').append('"' + entrypoint + '"').append(' ');
        }
        if(envVars != null && !envVars.isEmpty()) {
            envVars.each {
                sb.append('--env ').append(it.key).append('="').append(it.value).append('" ');
            }
        }
        if(ports != null && !ports.isEmpty()) {
            ports.each {
                sb.append('-p "').append(it).append('" ');
            }
        }
        if(mounts != null && !mounts.isEmpty()) {
            mounts.each {
                sb.append('--mount "').append(it).append('" ');
            }
        }
        if(volumes != null && !volumes.isEmpty()) {
            volumes.each {
                sb.append('-v "').append(it.key).append(':').append(it.value).append('" ');
            }
        }
        if(removeOnExit) {
            sb.append('--rm ');
        }
        if(workingDirectory != null) {
            sb.append('-w "').append(workingDirectory).append('" ');
        }
        if(otherArgs != null && !otherArgs.isEmpty()) {
            otherArgs.each {
                sb.append(it).append(' ');
            }
        }
        sb.append(image);
        return sb.toString();
    }
//endregion

//region docker exec
    String exec(Map args) {
        return exec(
            args.get('containerId') as String,
            args.get('command') as String,
            args.get('user') as String,
            args.get('workDir') as String,
            args.get('envArgs') as Map<String, String>,
            args.get('additionalArgs') as List<String>
        );
    }

    String exec(String containerId, String command, String user=null, String workDir=null, Map<String, String> envArgs = null, List<String> additionalArgs = null) {
        return steps.sh(script: execCommand(containerId, command, user, workDir, envArgs, additionalArgs),
            label: "Running command in docker container"
        );
    }

    String execCommand(String containerId, String command, String user=null, String workDir=null, Map<String, String> envArgs = null, List<String> additionalArgs = null) {
        if(containerId == null) {
            throw new IllegalArgumentException("containerId cannot be null!");
        }
        if(command == null) {
            throw new IllegalArgumentException("command cannot be null!");
        }

        StringBuilder sb = new StringBuilder(dockerCommand + ' exec ');
        if(user != null) {
            sb.append('-u ').append(user).append(' ');
        }
        if(workDir != null) {
            sb.append('-w "').append(workDir).append('" ');
        }
        if(envArgs != null && !envArgs.isEmpty()) {
            envArgs.each {
                sb.append('-e "').append(it.key).append('=').append(it.value).append('" ');
            }
        }
        if(additionalArgs != null && !additionalArgs.isEmpty()) {
            additionalArgs.each {
                sb.append(it).append(' ');
            }
        }

        // documentation says do not quote command, because it will not work
        sb.append(containerId).append(' ').append(command);
        return sb.toString();
    }
//endregion

//region docker cp
    /**
     * Use docker cp to copy a file/directory into the container
     * @param containerPath if not specified then the {@code hostPath} will be used
     */
    void copyToContainer(String containerId, String hostPath, String containerPath = null) {
        if(containerPath == null) {
            containerPath = hostPath;
        }

        steps.sh(script: dockerCopyCommand(containerId, hostPath, containerPath, true), label: 'Copy ' + hostPath + ' to container ' + containerId);
    }

    /** Use docker cp to copy a file/directory from the container */
    void copyFromContainer(String containerId, String containerPath, String hostPath) {
        steps.sh(script: dockerCopyCommand(containerId, containerPath, hostPath, false), label: 'Copy ' + containerPath + ' from container ' + containerId);
    }

    String dockerCopyCommand(String containerId, String from, String to, boolean hostToContainer) {
        if(containerId == null) {
            throw new IllegalArgumentException("containerId cannot be null!");
        }
        if(from == null) {
            throw new IllegalArgumentException("from cannot be null!");
        }
        if(to == null) {
            throw new IllegalArgumentException("to cannot be null!");
        }

        StringBuilder sb = new StringBuilder(dockerCommand + ' cp ');
        if(hostToContainer) {
            sb.append('"').append(from).append('" "').append(containerId).append(':').append(to).append('"');
        } else {
            sb.append('"').append(containerId).append(':').append(from).append('" "').append(to).append('"');
        }
        return sb.toString();
    }
//endregion

    void stop(String containerId) {
        steps.sh(script: dockerCommand + ' stop ' + containerId, label: 'Docker stop ' + containerId);
    }

    void stopRemoveIgnoreError(String containerId) {
        steps.sh(script: dockerCommand + ' rm -f ' + containerId + ' &>/dev/null', label: 'Docker remove ' + containerId);
    }

    void push(String tag) {
        if(tag == null) {
            throw new IllegalArgumentException('Tag should not be null!');
        }
        steps.sh(script: dockerCommand + ' push ' + tag, label: 'Docker push ' + tag);
    }
    void push(List<String> tags) {
        if(tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException('Tags should not be empty!');
        }

        // can only push one image at a time
        tags.each {
            push(it);
        }
    }
}

/**
 * TODO: Data gained from `docker image` commands
 */
class DockerImage {
    String ID;
    String Repository;
    String Tag;
    String CreatedAt;
    String CreatedSince;
    String Digest;
    String VirtualSize;
}
