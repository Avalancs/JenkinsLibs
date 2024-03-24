package org.jenkinsci

class StepsMock {
    void sh(Map args) {
        println('sh step with script: ' + args.getOrDefault('script', ''));
    }

    void withCredentials(List ignored) {}
}
