package org.avalancs.gitlab.model.mergerequest

import org.avalancs.gitlab.model.comment.Author

class MergeRequestResult {
    long id;
    long iid;
    long project_id;
    String title;
    String description;
    String state;
    String created_at;
    String updated_at;
    String target_branch;
    String source_branch;
    Author author;
    List<String> assignees;
    String assignee;
    List<String> reviewers;
    long source_project_id;
    long target_project_id;
    boolean draft;
    String merge_status;
    String detailed_merge_status;
    String sha;
    String web_url;
    boolean has_conflicts;
}
