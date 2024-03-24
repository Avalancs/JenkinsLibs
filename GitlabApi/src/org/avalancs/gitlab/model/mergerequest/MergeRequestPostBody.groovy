package org.avalancs.gitlab.model.mergerequest

class MergeRequestPostBody {
    /** project id */
    long id;
    String source_branch;
    String target_branch
    /** Title of the merge request */
    String title;
    /** Description of MR */
    String description;
    /** Labels for the merge request, as a comma-separated list. Optional */
    String labels;
    Boolean remove_source_branch;
    Boolean squash;
    /** optional */
    Long assignee_id;
    List<Long> reviewer_ids;

    void validate() {
        if(id == 0) {
            throw new RuntimeException("You must set project id for merge request!");
        }
        if(source_branch == null) {
            throw new RuntimeException("You must set source_branch for merge request!");
        }
        if(target_branch == null) {
            throw new RuntimeException("You must set target_branch for merge request!");
        }
        if(title == null) {
            throw new RuntimeException("You must set title for merge request!");
        }
    }
}
