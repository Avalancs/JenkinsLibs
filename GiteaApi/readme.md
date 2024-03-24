# GiteaApi
A Jenkins pipeline library that enables you to do Gitea Api calls (currently mostly related to querying branches and merge requests)

## Example usage
Assuming you named the library `GiteaApi` in Jenkins settings:
```groovy
@Library('GiteaApi')
import org.avalancs.gitea.*
import org.avalancs.git.Git

node() {
    Api gitea = null
    Git git = new Git(this, true) // false for windows
    withCredentials([usernamePassword(credentialsId: '...', passwordVariable: 'passwordVar', usernameVariable: 'usernameVar')]) {
        gitea = new Api('https://your.gitea.com', env.usernameVar, env.passwordVar);
    }
    
    // pass clone url of repo
    List<String> branches = gitea.getBranchesForRepo('https://your.gitea.com/organization/repo.git')
    List<String> tags = gitea.getTagsForRepo('https://your.gitea.com/organization/repo.git')
    
    // regular git commands
    dir('path/to/git/repo') {
        if(git.checkIfBranchNameIsValid('Invalid branch name')) {
            echo('the branch name you have chosen is invalid in git!')
        }
        if(git.wereThereChanges()) {
            git.commit('Commit message')
            // no built-in method for push yet, since you'd have to pass in credentials
            withCredentials([usernamePassword(credentialsId: 'YOUR_CREDENTIAL_ID', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                sh(encoding: 'UTF-8', script:
                    """git config --local credential.helper "!f() { echo username=${env.GIT_USERNAME}; echo password=${env.GIT_PASSWORD}; }; f"
        git push origin HEAD:yourbranch""");
            }
        }
    }
}
```