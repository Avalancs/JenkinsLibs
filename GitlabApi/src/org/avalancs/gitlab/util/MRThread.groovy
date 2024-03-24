package org.avalancs.gitlab.util
/**
 * Helper class to be able to pass an MR thread to {@link org.avalancs.gitlab.api.MergeRequest} class to make calls less verbose
 */
class MRThread {
    org.avalancs.gitlab.GitlabAuth auth;
    long projectId;
    long mergeRequestIid;
    org.avalancs.gitlab.model.comment.Discussion discussion;

    String getMRdetails() {
        // do not print auth as that contains password/token!
        return 'Project id: ' + projectId + ', Merge Request IID: ' + mergeRequestIid + ', Discussion id: ' + discussion.id;
    }
}
