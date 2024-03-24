package org.avalancs.gitlab.model.comment

class Note {
    long id;
    String body;
    Attachment attachment;
    Author author;
    Date created_at;
    Date updated_at;
    NoteType noteable_type;
    long project_id;
    long noteable_iid;
}
