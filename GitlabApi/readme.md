# GitlabApi
Call Gitlab Apis and get back structured responses.

You can use it together with the [GitLab plugin](https://plugins.jenkins.io/gitlab-plugin/). E.g.: when a merge request is opened then you can clone the project (you can get the clone url and branch from the Gitlab plugin's build variables), run tests or other analysis and send back a comment on the MR (using merge request iid).

## Example Usage
```groovy
@Library('GitlabApi')
import org,avalancs.gitlab.*
import org,avalancs.gitlab.api.*
import org,avalancs.gitlab.model.comment.*
// Global Pipeline Library set up in Manage Jenkins > System

node() {
    ...
    GitlabAuth auth
    withCredentials([string(credentialsId: 'GITLAB_API_TOKEN_PLAIN', variable: 'gitlab_api_token')]) {
        auth = new GitlabAuth('https://gitlab.yourinstance.com', gitlab_api_token)
    }
    projectID = Long.parseLong(env.gitlabMergeRequestTargetProjectId)   // env var from Gitlab plugin
    MRiid = Long.parseLong(env.gitlabMergeRequestIid)                   // env var from Gitlab plugin
    ...
}
```