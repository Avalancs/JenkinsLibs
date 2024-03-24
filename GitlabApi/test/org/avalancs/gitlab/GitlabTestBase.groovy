package org.avalancs.gitlab

import org.junit.jupiter.api.BeforeAll

abstract class GitlabTestBase {
    protected static String baseUrl = "https://yourcompany.yourgitlab.com";
    protected static String accessToken;
    protected static GitlabAuth auth;

    @BeforeAll
    static void setUp_TestBase() {
        accessToken = new File("test-resources/test_api_key.txt").text;
        auth = new GitlabAuth(baseUrl, accessToken);
    }
}
