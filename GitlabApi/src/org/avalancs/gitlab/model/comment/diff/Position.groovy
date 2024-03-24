package org.avalancs.gitlab.model.comment.diff

class Position {
    String base_sha;
    String start_sha;
    String head_sha;
    String old_path;
    String new_path;
    String position_type;
    long old_line;
    long new_line;
    // TODO: line_range
}
