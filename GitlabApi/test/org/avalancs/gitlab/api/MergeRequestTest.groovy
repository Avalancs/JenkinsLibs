package org.avalancs.gitlab.api


import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.avalancs.gitlab.GitlabTestBase
import org.avalancs.gitlab.model.comment.Discussion
import org.avalancs.gitlab.model.comment.Note
import org.avalancs.gitlab.model.mergerequest.MergeRequestResult
import org.avalancs.gitlab.util.MRThread
import org.avalancs.gitlab.util.NoteAndDiscussion

import static org.junit.jupiter.api.Assertions.*

class MergeRequestTest extends GitlabTestBase {
    static final long testProjectId = 22;
    static final long testMRIid = 1;

    @Test
    void testGetNotes() {
        List<Note> comments = MergeRequest.getNotes(auth, testProjectId, testMRIid);

        assertNotNull(comments);
        for(Note c : comments) {
            println c.dump();
        }
        assertFalse(comments.isEmpty());
    }

    @Test
    void testDiscussion() {
        List<Discussion> discussions = MergeRequest.getDiscussions(auth, testProjectId, testMRIid);

        assertNotNull(discussions);
        for(Discussion d : discussions) {
            println d.dump();
        }
        assertFalse(discussions.isEmpty());
    }

    @Test
    @Disabled
    void testAddComment() {
        Note comment = MergeRequest.addComment(auth, testProjectId, testMRIid, 'Once you add a comment you cannot make it a thread. You need to use a different APi for that...');
        assertNotNull(comment);
        assertNotNull(comment.id);
    }

    @Test
    @Disabled
    void testAddThread() {
        MRThread thread = MergeRequest.addDiscussion(auth, testProjectId, testMRIid, 'This is the damned thread api...');
        assertNotNull(thread);
        assertNotNull(thread.discussion.id);
    }

    @Test
    @Disabled
    void testReplyToDiscussion() {
        MergeRequest.replyToDiscussion(auth, testProjectId, testMRIid, 'f1741e500cad34105ba00824f62c3e78f87f7085', 'Merge Request reply test!');
    }

    @Test
    @Disabled
    void testResolveDiscussion() {
        MergeRequest.resolveDiscussion(auth, testProjectId, testMRIid, 'f1741e500cad34105ba00824f62c3e78f87f7085');
    }

    @Test
    @Disabled
    void testUnResolveDiscussion() {
        MergeRequest.unresolveDiscussion(auth, testProjectId, testMRIid, 'f1741e500cad34105ba00824f62c3e78f87f7085');
    }

    /** What Jenkins will be doing */
    @Test
    @Disabled
    void addThreadThenResolveWithReply() {
        MRThread thread = MergeRequest.addDiscussion(auth, testProjectId, testMRIid, 'Build will start at: ...');
        assertNotNull(thread);
        assertNotNull(thread.discussion.id);

        Note reply = MergeRequest.replyToDiscussion(thread, 'Build finished successfully!');
        assertNotNull(reply);
        assertNotNull(reply.id);

        MergeRequest.resolveDiscussion(thread, true);
    }

    @Test
    @Disabled
    void testGetNoteOrDiscussionForLastMessage_StandaloneNote() {
        NoteAndDiscussion result = MergeRequest.getNoteOrDiscussionForLastMessage(auth, testProjectId, testMRIid, 'standalone message');
        assertNotNull(result);
        assertNotNull(result.note);
        assertNull(result.discussion);
        println result.note.dump();
    }

    @Test
    void testGetNoteOrDiscussionForLastMessage_NotFoundNote() {
        NoteAndDiscussion result = MergeRequest.getNoteOrDiscussionForLastMessage(auth, testProjectId, testMRIid, 'nonexistent message');
        assertNotNull(result);
        assertNull(result.note);
        assertNull(result.discussion);
    }

    @Test
    @Disabled
    void testGetNoteOrDiscussionForLastMessage_FoundDiscussion() {
        NoteAndDiscussion result = MergeRequest.getNoteOrDiscussionForLastMessage(auth, testProjectId, testMRIid, 'reply 1');
        assertNotNull(result);
        assertNull(result.note);
        assertNotNull(result.discussion);
        println result.discussion.dump();
    }

    @Test
    @Disabled
    void testCreateMR() {
        long projectId = 999;
        org.avalancs.gitlab.model.mergerequest.MergeRequestPostBody mrp = new org.avalancs.gitlab.model.mergerequest.MergeRequestPostBody();
        mrp.id = projectId;
        mrp.source_branch = "2023.11.23-9";
        mrp.target_branch = "main";
        mrp.title = "GitLabApi Test MR";
        mrp.description = "Testing the GitLabApi for Jenkins, please ignore";
        MergeRequestResult result = MergeRequest.createMR(auth, projectId, mrp);
        assertNotNull(result);
        println(result.dump());
    }
}
