package org.avalancs.gitlab

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GitlabAuthTest {
    String baseUrl = "https://yourcompany.yourgitlab.com";
    String accessToken = "aabbccdd";

    @Test
    void testAuth_trimSlashFromEndOfUrl() {
        GitlabAuth auth = new GitlabAuth(baseUrl + '///', accessToken);

        Assertions.assertEquals(baseUrl, auth.getBaseUrl());
    }
}
