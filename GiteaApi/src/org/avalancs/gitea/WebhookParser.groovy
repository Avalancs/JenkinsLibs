package org.avalancs.gitea

import org.avalancs.gitea.util.PullRequest
import org.avalancs.gitea.util.Push
import org.avalancs.gitea.util.Repository
import org.avalancs.gitea.util.WebhookEvent
import groovy.json.JsonSlurperClassic
@Grab(group='org.apache.commons', module='commons-lang3', version='3.14.0')
import org.apache.commons.lang3.StringUtils
import org.avalancs.gitea.util.Comment

/** Parser for Gitea webhooks, to figure out if it's a merge during a pull request */
class WebhookParser {
    static WebhookEvent parse(String inputJson) {
        if (inputJson == null || inputJson == '') {
            return null;
        }

        def parsedMessage = new JsonSlurperClassic().parseText(inputJson)
        if (!parsedMessage instanceof Map) {
            return null;
        }
        Map<String, String> message = parsedMessage as Map<String, String>;
        WebhookEvent event = new WebhookEvent();
        event.action = message['action'];
        event.repo = new Repository(message['repository']['full_name'] as String,
                message['repository']['clone_url'] as String,
                message['repository']['html_url'] as String);

        if (message.containsKey('pusher')) { // push happened
            event.push = new Push();
            event.push.ref = message['ref'];
        } else if (message.containsKey('pull_request')) {
            event.pullRequest = new PullRequest();
            event.pullRequest.htmlUrl = message['pull_request']['url'];
            event.pullRequest.id = message['pull_request']['number'];
            event.pullRequest.headBranch = message['pull_request']['head']['ref'];
            event.pullRequest.baseBranch = message['pull_request']['base']['ref'];
            event.pullRequest.state = message['pull_request']['state'];
        } else if (event.action == 'created' && message.containsKey('comment') && // somebody commented on a Pull Request
                message['comment'].containsKey('pull_request_url')) {
            event.comment = new Comment();
            event.comment.body = message['comment']['body'];
            event.comment.htmlUrl = message['comment']['html_url'];
            event.comment.pullRequestUrl = message['comment']['pull_request_url'];
            event.comment.pullRequestId = StringUtils.substringAfterLast(event.comment.pullRequestUrl, '/');
            event.comment.issueUrl = message['issue']['url'];
            event.comment.issueId = message['issue']['number'];
        } else {
            return null; // event not implemented for our purposes
        }

        return event;
    }
}
