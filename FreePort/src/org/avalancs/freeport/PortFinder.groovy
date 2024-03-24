package org.avalancs.freeport

import com.cloudbees.groovy.cps.NonCPS

class PortFinder {
    @NonCPS
    /** A groovy one-liner to find a free port on the current machine */
    static int getFreePort() {
        return new ServerSocket(0).withCloseable { socket -> socket.getLocalPort() }
    }
}
