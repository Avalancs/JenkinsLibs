package org.avalancs.gitlab.model.comment.diff

class DiffNote extends org.avalancs.gitlab.model.comment.Note {
    String commit_id;
    boolean resolved;
    boolean resolvable;
    String resolved_by;
}
