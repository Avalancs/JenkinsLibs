package org.avalancs.gitlab.api

@Grab(group='com.google.code.gson', module='gson', version='2.10.1')
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import groovy.util.logging.Slf4j
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpPut
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.StringEntity

import java.lang.reflect.Type

@Slf4j
class MergeRequest {
    /** https://docs.gitlab.com/ee/api/merge_requests.html#create-mr */
    static org.avalancs.gitlab.model.mergerequest.MergeRequestResult createMR(org.avalancs.gitlab.GitlabAuth auth, long projectId, org.avalancs.gitlab.model.mergerequest.MergeRequestPostBody mrPostBody) {
        mrPostBody.validate();
        Gson gson = new Gson();

        // POST /projects/:id/merge_requests
        HttpPost request = auth.createPost('/projects/' + projectId + '/merge_requests');
        final StringEntity postBody = new StringEntity(gson.toJson(mrPostBody));
        request.setEntity(postBody);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            return gson.fromJson(response, org.avalancs.gitlab.model.mergerequest.MergeRequestResult.class) as org.avalancs.gitlab.model.mergerequest.MergeRequestResult;
        } finally {
            client?.close();
        }
    }

    /** https://docs.gitlab.com/ee/api/notes.html#list-all-merge-request-notes */
    static List<org.avalancs.gitlab.model.comment.Note> getNotes(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid) {
        // GET /projects/:id/merge_requests/:merge_request_iid/notes
        HttpGet request = auth.createGet('/projects/' + projectId + '/merge_requests/' + mergeRequestIid + '/notes');

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<org.avalancs.gitlab.model.comment.Note>>(){}.getType();
            return gson.fromJson(response, listType) as List<org.avalancs.gitlab.model.comment.Note>;
        } finally {
            client?.close();
        }
    }

    /** https://docs.gitlab.com/ee/api/discussions.html#list-project-merge-request-discussion-items */
    static List<org.avalancs.gitlab.model.comment.Discussion> getDiscussions(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid) {
        // GET /projects/:id/merge_requests/:merge_request_iid/discussions
        HttpGet request = auth.createGet('/projects/' + projectId + '/merge_requests/' + mergeRequestIid + '/discussions');

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            Type listType = new TypeToken<ArrayList<org.avalancs.gitlab.model.comment.Discussion>>(){}.getType();
            return new Gson().fromJson(response, listType) as List<org.avalancs.gitlab.model.comment.Discussion>;
        } finally {
            client?.close();
        }
    }

    /**
     * Adds a new Note to the merge request. If you want to create a thread use {@link MergeRequest#addDiscussion(org.avalancs.gitlab.GitlabAuth, long, long, java.lang.String)}
     * @return the created {@link org.avalancs.gitlab.model.comment.Note}
     */
    static org.avalancs.gitlab.model.comment.Note addComment(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, String message) {
        if(message.length() > 1000000) {
            throw new IllegalArgumentException("GitLab documentation states that message cannot be longer than 1,000,000 characters!");
        }

        Map<String, String> postBody = new HashMap<>();
        postBody.put("body", message);

        // POST /projects/:id/merge_requests/:merge_request_iid/notes
        HttpPost request = auth.createPost('/projects/' + projectId + '/merge_requests/' + mergeRequestIid + '/notes', postBody);

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            return new Gson().fromJson(response, org.avalancs.gitlab.model.comment.Note.class) as org.avalancs.gitlab.model.comment.Note;
        } finally {
            client?.close();
        }
    }

    static org.avalancs.gitlab.util.MRThread addDiscussion(org.avalancs.gitlab.util.MRThread thread, String message) {
        return addDiscussion(thread.auth, thread.projectId, thread.mergeRequestIid, message);
    }

    /**
     * Add a new thread to an existing MR
     */
    static org.avalancs.gitlab.util.MRThread addDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, String message) {
        // POST /projects/:id/merge_requests/:merge_request_iid/discussions
        HttpPost request = auth.createPost('/projects/' + projectId + '/merge_requests/' + mergeRequestIid + '/discussions?body=' + URLEncoder.encode(message, "UTF-8"));

        CloseableHttpClient client = null;
        try {
            log.debug('Creating new thread on merge request: Project Id: ' + projectId + ', Merge Request IID: ' + mergeRequestIid)
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            org.avalancs.gitlab.model.comment.Discussion discussion = new Gson().fromJson(response, org.avalancs.gitlab.model.comment.Discussion.class) as org.avalancs.gitlab.model.comment.Discussion;
            return new org.avalancs.gitlab.util.MRThread(auth: auth, projectId: projectId, mergeRequestIid: mergeRequestIid, discussion: discussion);
        } finally {
            client?.close();
        }
    }

    static org.avalancs.gitlab.model.comment.Note replyToDiscussion(org.avalancs.gitlab.util.MRThread thread, String message) {
        return replyToDiscussion(thread.auth, thread.projectId, thread.mergeRequestIid, thread.discussion, message);
    }

    static org.avalancs.gitlab.model.comment.Note replyToDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, org.avalancs.gitlab.model.comment.Discussion discussion, String message) {
        return replyToDiscussion(auth, projectId, mergeRequestIid, discussion.id, message);
    }

    /** https://docs.gitlab.com/ee/api/discussions.html#add-note-to-existing-merge-request-thread */
    static org.avalancs.gitlab.model.comment.Note replyToDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, String discussionId, String message) {
        // POST /projects/:id/merge_requests/:merge_request_iid/discussions/:discussion_id/notes
        HttpPost request = auth.createPost('/projects/' + projectId + '/merge_requests/' + mergeRequestIid + '/discussions/' + discussionId +  '/notes?body=' + URLEncoder.encode(message, "UTF-8"));

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            return new Gson().fromJson(response, org.avalancs.gitlab.model.comment.Note.class) as org.avalancs.gitlab.model.comment.Note;
        } finally {
            client?.close();
        }
    }

    static org.avalancs.gitlab.model.comment.Discussion resolveDiscussion(org.avalancs.gitlab.util.MRThread thread, boolean resolve) {
        return resolveDiscussion(thread.auth, thread.projectId, thread.mergeRequestIid, thread.discussion, resolve);
    }

    /**
     * https://docs.gitlab.com/ee/api/discussions.html#resolve-a-merge-request-thread
     * @param resolve resolve or un-resolve discussion
     */
    static org.avalancs.gitlab.model.comment.Discussion resolveDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, org.avalancs.gitlab.model.comment.Discussion discussion, boolean resolve) {
        return resolveDiscussion(auth, projectId, mergeRequestIid, discussion.id, resolve);
    }

    /**
     * https://docs.gitlab.com/ee/api/discussions.html#resolve-a-merge-request-thread
     * @param resolve resolve or un-resolve discussion
     */
    static org.avalancs.gitlab.model.comment.Discussion resolveDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, String discussionId, boolean resolve) {
        // PUT /projects/:id/merge_requests/:merge_request_iid/discussions/:discussion_id
        HttpPut request = auth.createPut('/projects/' + projectId + '/merge_requests/' + mergeRequestIid + '/discussions/' + discussionId + '?resolved=' + (resolve ? 'true' : 'false'));

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            return new Gson().fromJson(response, org.avalancs.gitlab.model.comment.Discussion.class) as org.avalancs.gitlab.model.comment.Discussion;
        } finally {
            client?.close();
        }
    }

    /**
     * see {@link MergeRequest#resolveDiscussion(org.avalancs.gitlab.GitlabAuth, long, long, java.lang.String, boolean)}
     */
    static org.avalancs.gitlab.model.comment.Discussion resolveDiscussion(org.avalancs.gitlab.util.MRThread thread) {
        return resolveDiscussion(thread.auth, thread.projectId, thread.mergeRequestIid, thread.discussion);
    }

    /**
     * see {@link MergeRequest#resolveDiscussion(org.avalancs.gitlab.GitlabAuth, long, long, java.lang.String, boolean)}
     */
    static org.avalancs.gitlab.model.comment.Discussion resolveDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, String discussionId) {
        return resolveDiscussion(auth, projectId, mergeRequestIid, discussionId, true);
    }

    /**
     * see {@link MergeRequest#resolveDiscussion(org.avalancs.gitlab.GitlabAuth, long, long, java.lang.String, boolean)}
     */
    static org.avalancs.gitlab.model.comment.Discussion resolveDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, org.avalancs.gitlab.model.comment.Discussion discussion) {
        return resolveDiscussion(auth, projectId, mergeRequestIid, discussion.id, true);
    }

    /**
     * see {@link MergeRequest#resolveDiscussion(org.avalancs.gitlab.GitlabAuth, long, long, java.lang.String, boolean)}
     */
    static org.avalancs.gitlab.model.comment.Discussion unresolveDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, String discussionId) {
        return resolveDiscussion(auth, projectId, mergeRequestIid, discussionId, false);
    }

    /**
     * see {@link MergeRequest#resolveDiscussion(org.avalancs.gitlab.GitlabAuth, long, long, java.lang.String, boolean)}
     */
    static org.avalancs.gitlab.model.comment.Discussion unresolveDiscussion(org.avalancs.gitlab.util.MRThread thread) {
        return resolveDiscussion(thread.auth, thread.projectId, thread.mergeRequestIid, thread.discussion, false);
    }

    /**
     * see {@link MergeRequest#resolveDiscussion(org.avalancs.gitlab.GitlabAuth, long, long, java.lang.String, boolean)}
     */
    static org.avalancs.gitlab.model.comment.Discussion unresolveDiscussion(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, org.avalancs.gitlab.model.comment.Discussion discussion) {
        return resolveDiscussion(auth, projectId, mergeRequestIid, discussion.id, false);
    }

    static org.avalancs.gitlab.util.NoteAndDiscussion findDiscussionForNote(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, org.avalancs.gitlab.model.comment.Note note) {
        List<org.avalancs.gitlab.model.comment.Discussion> discussions = getDiscussions(auth, projectId, mergeRequestIid);
        for(org.avalancs.gitlab.model.comment.Discussion d : discussions) {
            if(d.notes.any {it.id == note.id }) {
                return new org.avalancs.gitlab.util.NoteAndDiscussion(note, d);
            }
        }

        return null;
    }

    /**
     * Find the last message on the MR with the given text. If it's a standalone comment then return a {@link org.avalancs.gitlab.model.comment.Note},
     * else return the message thread as {@link org.avalancs.gitlab.model.comment.Discussion}.
     * @return {@link org.avalancs.gitlab.util.NoteAndDiscussion} with either Note or Discussion filled if the message is found, or both fields null if not found
     */
    static org.avalancs.gitlab.util.NoteAndDiscussion getNoteOrDiscussionForLastMessage(org.avalancs.gitlab.GitlabAuth auth, long projectId, long mergeRequestIid, String message) {
        // TODO: getNotes() and getDiscussions() returns only the first 20, need to implement pagination later!!!
        List<org.avalancs.gitlab.model.comment.Note> notes = getNotes(auth, projectId, mergeRequestIid);
        org.avalancs.gitlab.model.comment.Note found;
        for(org.avalancs.gitlab.model.comment.Note n : notes) {
            if(n.body == message) {
                log.debug("Found message with Note id: " + n.id);
                found = n;
                break;
            }
        }

        if(found == null) {
            return new org.avalancs.gitlab.util.NoteAndDiscussion();
        }

        org.avalancs.gitlab.util.NoteAndDiscussion result = findDiscussionForNote(auth, projectId, mergeRequestIid, found);
        if(result != null) {
            return result;
        }
        // no discussion? return the note just so compiler won't complain
        return new org.avalancs.gitlab.util.NoteAndDiscussion(found);
    }

    /**
     * Syntactic sugar for builds, will only reply to MR if thread != null and re-throws exception
     * @param thread MR thread parameters
     * @param message message to reply with if the body throws an exception
     * @param body code to run
     */
    static void withMrThread(org.avalancs.gitlab.util.MRThread thread, String message, Closure body) throws Exception {
        try {
            body.call();
        } catch (ex) {
            if(thread != null) {
                log.debug("Replying to MR because of failure: " + thread.getMRdetails());
                replyToDiscussion(thread, message);
            }
            throw ex;
        }
    }
}
