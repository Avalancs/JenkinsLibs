package org.avalancs.gitea.util

class PullRequest {
    String id
    String headBranch
    String baseBranch
    String htmlUrl
    String state

    boolean isOpen() {
        return state == 'open'
    }
}
