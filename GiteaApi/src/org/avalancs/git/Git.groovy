package org.avalancs.git

/**
 * Common git operations you would do on the terminal
 * GIT PUSH IS PURPOSEFULLY NOT INCLUDED. YOU HAVE TO DO IT LIKE THIS IN THE PIPELINE BUILD SO THE PASSWORD GETS STARRED OUT:
 * <pre>{@code
withCredentials([usernamePassword(credentialsId: 'YOUR_CREDENTIAL_ID', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
    sh(encoding: 'UTF-8', script:
        """git config --local credential.helper "!f() { echo username=${env.GIT_USERNAME}; echo password=${env.GIT_PASSWORD}; }; f"
        git push origin HEAD:${branchToPush}""");
 }
 }</pre>
 */
class Git {
    def script;
    boolean linux;
    String encoding = 'UTF-8';

    /**
     * @param script pass {@code this] from the pipeline script, so steps like sh and bat can be used in this library
     * @param linux true: use sh step, false uses bat step
     */
    Git(def script, boolean linux) {
        this.script = script;
        this.linux = linux;
    }

    /**
     * @param branch name of branch to check
     * @param label optional: passed to sh or bat step
     * @return is branch name valid?
     */
    boolean checkIfBranchNameIsValid(String branch, String label = null) {
        def options = createOptions(label);
        return terminalStatus("git check-ref-format --branch '$branch'".toString(), options) == 0;
    }

    /**
     * Resets file modifications using git reset HEAD -- (list offiles)
     * @param files list of file/folder names
     * @param label optional: passed to sh or bat step
     */
    boolean resetFiles(List<String> files, String label = null) {
        def options = createOptions(label);
        files = files.collect{ '"' + it + '"' }; // quote so if path contains spaces it still works
        return terminalStatus('git checkout HEAD -- ' + files.join(' '), options) == 0;
    }

    /**
     * @param label optional: passed to sh or bat step
     * @return Where there any changes compared to HEAD revision (WARNING: this stages all files to be able to check!)
     */
    boolean wereThereChanges(String label = null) {
        def options = createOptions(label);
        terminalStdout('git add -A .', [:]);
        String anythingToCommit = terminalStdout('git status --porcelain=v1', options)?.trim();
        return anythingToCommit != null && anythingToCommit != '';
    }

    /**
     * Create new branch with given name and checks it out afterwards
     * @param label optional: passed to sh or bat step
     * @return was it successful?
     */
    boolean createBranchAndCheckout(String branchName, String label = null) {
        def options = createOptions(label);
        return terminalStatus("git checkout -b ${branchName}".toString(), options) == 0;
    }

    /**
     * @param label optional: passed to sh or bat step
     * @return did commiting succeed?
     */
    boolean commit(String commitMessage, String label = null) {
        def options = createOptions(label);
        return terminalStatus("""git commit -m "${commitMessage}" """.toString(), options) == 0;
    }

    /**
     * Between two references returns the first common ancestor<br/>
     * {@code ref1} and {@code ref2} can be commit hash, branch, tag as well!<br/><br/>
     * Do not forget that locally only the branch exists which was used during cloning!<br/>
     * Use origin/master or origin/... to avoid that error!
     * @param label optional: passed to sh or bat step
     * @return commit hash
     */
    String findFirstCommonAncestor(String ref1, String ref2, String label = null) {
        def options = createOptions(label);
        return terminalStdout("git merge-base $ref1 $ref2", options);
    }

    /**
     * @param label optional: passed to sh or bat step
     * @return the commit hash of the HEAD reference
     */
    String getCurrentCommitHash(String label = null) {
        def options = createOptions(label);
        return terminalStdout('git rev-list --max-count=1 HEAD', options);
    }

    /**
     * @param label optional: passed to sh or bat step
     * @return Does given branch exist in local repository? Dioes not check remote repository. Sensitive to lower/upper case!
     */
    boolean checkIfBranchExists(String branch, String label = null) {
        def options = createOptions(label);
        String result = terminalStdout('git branch --list ' + branch, options);
        if(result?.trim() == branch) {
            return true;
        }
        return false;
    }

    /**
     * @param branch include "origin/" in the name
     * @param label optional: passed to sh or bat step
     * @return Does given branch exist in remote repository? Sensitive to lower/upper case!
     */
    boolean checkIfRemoteBranchExists(String branch, String label = null) {
        def options = createOptions(label);
        String result = terminalStdout('git branch --list -r ' + branch, options);
        if(result?.trim() == branch) {
            return true;
        }
        return false;
    }

    /**
     * If branch does not exist locally, creates one from origin/branch automatically
     * @param label optional: passed to sh or bat step
     * @return did it succeed?
     */
    boolean changeBranch(String branch, String label = null) {
        def options = createOptions(label);
        // check if branch exists locally
        if(checkIfBranchExists(branch)) {
            return terminalStatus('git checkout ' + branch, options) == 0;
        }

        String remoteBranch = branch;
        // if the name does not contain the remote repository, search with origin
        if(!remoteBranch.contains("/")) {
            remoteBranch = 'origin/' + remoteBranch;
        }
        if(checkIfRemoteBranchExists(remoteBranch)) {
            return terminalStatus('git checkout -b ' + branch + ' ' + remoteBranch, options) == 0;
        }

        println 'Could not find the following branch locally or remotely: ' + branch;
        return false;
    }

    /** Run command with given options and return stdout */
    protected String terminalStdout(String command, Map options) {
        def scriptOptions = [
            'script': command,
            'returnStdout': true,
            'encoding': encoding
        ];
        options.each {
            scriptOptions.put(it.key as String, it.value);
        }
        if(linux) {
            return script.sh(scriptOptions) as String;
        } else {
            return script.bat(scriptOptions) as String;
        }
    }

    /** Run command with given options and return exit code */
    protected int terminalStatus(String command, Map options) {
        def scriptOptions = [
                'script': command,
                'returnStatus': true,
                'encoding': encoding
        ];
        options.each {
            scriptOptions.put(it.key as String, it.value);
        }
        if(linux) {
            return script.sh(scriptOptions) as int;
        } else {
            return script.bat(scriptOptions) as int;
        }
    }

    /** Merge map with passed-in label parameter */
    protected static Map createOptions(String label = null, Map others = null) {
        def options = [:];
        if(label != null) {
            options['label'] = label;
        }
        if(others != null) {
            options.putAll(others);
        }
        return options;
    }
}
