package org.avalancs.gitlab.util


import org.avalancs.gitlab.model.comment.Discussion

class NoteAndDiscussion {
    org.avalancs.gitlab.model.comment.Note note;
    Discussion discussion;

    NoteAndDiscussion() {}

    NoteAndDiscussion(org.avalancs.gitlab.model.comment.Note note) {
        this.note = note;
    }

    NoteAndDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    NoteAndDiscussion(org.avalancs.gitlab.model.comment.Note note, Discussion discussion) {
        this.note = note
        this.discussion = discussion
    }

    boolean isEmpty() {
        return note == null && discussion == null;
    }
}
