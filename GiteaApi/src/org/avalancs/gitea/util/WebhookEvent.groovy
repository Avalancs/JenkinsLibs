package org.avalancs.gitea.util

class WebhookEvent {
    String action
    Repository repo
    PullRequest pullRequest
    Comment comment
    Push push

    boolean isNewPullRequest() {
        return action == 'opened' && pullRequest != null
    }

    boolean isPullRequest() {
        return pullRequest != null
    }

    boolean isNewPRComment() {
        return action == 'created' && comment != null && comment.pullRequestUrl != null
    }

    boolean isPush() {
        return push != null
    }

    /** translate pull request event to Hungarian message, just ignore...  */
    String getEsemeny() {
        if(isNewPullRequest()) {
            return "Újonnan nyitott Pull Request"
        } else if(isNewPRComment()) {
            return "Új komment Pull Request-re"
        } else if(isPush()) {
            return "Push történt"
        } else {
            return null
        }
    }
}
