package org.avalancs.gitea


import org.avalancs.gitea.util.PullRequest
import org.avalancs.gitea.util.Repository
import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic
import org.avalancs.gitea.util.Comment

import java.nio.charset.StandardCharsets

/** subset of Gitea api */
class Api {
    String baseUrl;
    String username;
    String password;
    String token;

    /**
     * @param baseUrl URL of your Gitea instance
     * @param username user who will be used for API calls
     */
    Api(String baseUrl, String username, String password) {
        if(baseUrl == null) {
            throw new IllegalArgumentException('baseUrl is a mandatory parameter!');
        }
        if(password == null || username == null) {
            throw new IllegalArgumentException('username and password are mandatory parameters!');
        }
        this.baseUrl = baseUrl;
        if(!this.baseUrl.endsWith("/")) {
            this.baseUrl = this.baseUrl + "/";
        }
        this.username = username;
        this.password = password;
    }

    /**
     * @param baseUrl URL of your Gitea instance
     * @param token the access_token which will drive authentication
     */
    Api(String baseUrl, String token) {
        if(baseUrl == null) {
            throw new IllegalArgumentException('baseUrl is a mandatory parameter!');
        }
        if(token == null) {
            throw new IllegalArgumentException("token is a mandatory parameter!");
        }
        this.baseUrl = baseUrl;
        if(!this.baseUrl.endsWith("/")) {
            this.baseUrl = this.baseUrl + "/";
        }
        this.token = token;
    }

    PullRequest getPullRequestForComment(Repository repo, Comment comment) {
        return getPullRequestForRepo(repo, comment.pullRequestId);
    }

    PullRequest getPullRequestForRepo(Repository repo, String pullRequestId) {
        String url = baseUrl + "api/v1/repos/${repo.fullName}/pulls/${pullRequestId}";
        String responseStr = doHttpUrlConnectionAction(url, true, true, null);
        def response = new JsonSlurperClassic().parseText(responseStr);
        PullRequest pullRequest = new PullRequest();
        pullRequest.htmlUrl = response['url'];
        pullRequest.id = response['number'];
        pullRequest.headBranch = response['head']['ref'];
        pullRequest.baseBranch = response['base']['ref'];
        pullRequest.state = response['state'];
        return pullRequest;
    }

    String addCommentToPullRequest(Repository repo, String issueNumber, String message) {
        String url = baseUrl + "api/v1/repos/${repo.fullName}/issues/${issueNumber}/comments"; // TODO: URLEncode? documentation claims repo name cannot contain invalid characters
        return doHttpUrlConnectionAction(url, false, true, JsonOutput.toJson([body: message ]))
    }

    List<String> getBranchesForRepo(Repository repo) {
        String url = baseUrl + "api/v1/repos/${repo.fullName}/branches";
        String result = doHttpUrlConnectionAction(url, true, true, null);
        def branches = new JsonSlurperClassic().parseText(result);
        if(!branches instanceof List) {
            throw new RuntimeException("Could not parse branches!");
        } else {
            return (branches as List).collect{ it.name } as List<String>;
        }
    }

    /** WRANING: http urls will be replaced with https automatically! */
    List<String> getBranchesForRepo(String cloneURL, boolean replaceWithHTTPS = true) {
        // People keep forgetting, so I'll replace it silently
        if(cloneURL.startsWith("http://") && replaceWithHTTPS) {
            cloneURL = cloneURL.replace("http://", "https://");
        }
        return getBranchesForRepo(createRepoFromCloneUrl(cloneURL));
    }

    List<String> getTagsForRepo(Repository repo) {
        String url = baseUrl + "api/v1/repos/${repo.fullName}/tags";
        String result = doHttpUrlConnectionAction(url, true, true, null);
        def Tags = new JsonSlurperClassic().parseText(result);
        if(!Tags instanceof List) {
            throw new RuntimeException("Nem sikerült a tag-eket parse-olni!");
        } else {
            return (Tags as List).collect{ it.name } as List<String>;
        }
    }

    List<String> getTagsForRepo(String cloneURL) {
        return getTagsForRepo(createRepoFromCloneUrl(cloneURL));
    }

    String getContentsOfFile(Repository repo, String filePath, String branch) {
        filePath = queryStrEncode(filePath);
        String url = baseUrl + "api/v1/repos/${repo.fullName}/contents/${filePath}";
        if(branch != null) {
            url = url + '?ref=' + queryStrEncode(branch);
        }
        String responseStr = doHttpUrlConnectionAction(url, true, true, null);
        def response = new JsonSlurperClassic().parseText(responseStr);
        byte[] decoded = (response['content'] as String).decodeBase64();
        return new String(decoded);
    }

    String getContentsOfFile(Repository repo, String filePath) {
        return getContentsOfFile(repo, filePath, null);
    }

    String getContentsOfFile(String cloneURL, String filePath, String branch) {
        return getContentsOfFile(createRepoFromCloneUrl(cloneURL), filePath, branch);
    }

    String getContentsOfFile(String cloneURL, String filePath) {
        return getContentsOfFile(createRepoFromCloneUrl(cloneURL), filePath, null);
    }

    Repository createRepoFromCloneUrl(String cloneUrl) {
        if(!cloneUrl.startsWith(baseUrl)) {
            throw new IllegalArgumentException("The clone URL does not start with the baseURL-el! (http/https matters)!\nExpected: " + baseUrl + "\nClone Url: " + cloneUrl);
        }

        String fullName = cloneUrl.substring(baseUrl.length()); // remote trailing slash
        fullName = fullName.replaceFirst(/\.git/, "");
        return new Repository(fullName, cloneUrl, cloneUrl.replaceFirst(/\.git/, ""));
    }

    protected String getBasicAuth() {
        String hash = "${username}:${password}".bytes.encodeBase64().toString();
        return "Basic ${hash}";
    }

    // https://alvinalexander.com/blog/post/java/how-open-url-read-contents-httpurl-connection-java
    // Because Jenkins cannot use groovy's HttpBuilder, and the Jenkins http plugin cannot be tested locally
    // I'll implement the call myself...
    protected String doHttpUrlConnectionAction(String urlStr, boolean isGetMethod, boolean isAuthenticated, String requestBody) throws Exception {
        // If we're using access token, append to end of url
        if(isAuthenticated && token != null) {
            if(urlStr.contains("?")) {
                urlStr = urlStr + "&access_token=" + token;
            } else {
                urlStr = urlStr + "?access_token=" + token;
            }
        }

        try {
            // create the HttpURLConnection
            URL url = new URI(urlStr).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(isGetMethod ? "GET" : "POST");

            // If we're using username & password then add Authorization header
            if(isAuthenticated && username != null && password != null) {
                connection.setRequestProperty('authorization', getBasicAuth())
            }

            connection.setRequestProperty("Content-Type", "application/json"); // Gitea only uses json for communication
            connection.setRequestProperty("Charset", "UTF-8");
            if (!isGetMethod) {
                connection.setDoOutput(true);
                byte[] requestData = requestBody.bytes
                connection.setRequestProperty( "Content-Length", "${requestData.length}");
                connection.outputStream.write(requestData)
            }

            // give it 15 seconds to respond
            connection.setReadTimeout(15 * 1000);
            connection.connect();

            // read the output from the server
            if(connection.responseCode >= 200 && connection.responseCode < 300) {
                return connection.inputStream.getText('UTF-8');
            } else {
                // remove access token from url if we need to report an error
                String urlEscaped = urlStr.replaceAll(/(access_token=)(?:\w+)(&?)/, '$1...$2');
                throw new RuntimeException(
"""Error during Gitea API call: ${urlEscaped}
code: ${connection.getResponseCode()}
message: ${connection.getResponseMessage()}""");
            }
        } finally {}
    }

    protected static String queryStrEncode(String that) {
        return URLEncoder.encode(that, StandardCharsets.UTF_8.name());
    }
}
