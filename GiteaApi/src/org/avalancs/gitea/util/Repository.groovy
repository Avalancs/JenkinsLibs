package org.avalancs.gitea.util

class Repository {
    String fullName
    String cloneUrl
    String htmlUrl

    Repository(String fullName, String cloneUrl, String htmlUrl) {
        this.fullName = fullName
        this.cloneUrl = cloneUrl
        this.htmlUrl = htmlUrl
    }
}
