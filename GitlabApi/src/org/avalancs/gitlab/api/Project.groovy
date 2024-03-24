package org.avalancs.gitlab.api

@Grab(group='com.google.code.gson', module='gson', version='2.10.1')
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import groovy.util.logging.Slf4j
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.message.BasicNameValuePair
import org.apache.hc.core5.net.URIBuilder

import java.lang.reflect.Type

@Slf4j
class Project {
    /** https://archives.docs.gitlab.com/15.11/ee/api/projects.html#list-all-projects */
    static List<org.avalancs.gitlab.model.project.ProjectResult> getAllProjects(org.avalancs.gitlab.GitlabAuth auth) {
        // GET /projects
        HttpGet request = auth.createGet('/projects');

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<org.avalancs.gitlab.model.project.ProjectResult>>(){}.getType();
            return gson.fromJson(response, listType) as List<org.avalancs.gitlab.model.project.ProjectResult>;
        } finally {
            client?.close();
        }
    }

    /** https://archives.docs.gitlab.com/15.11/ee/api/projects.html#list-all-projects */
    static org.avalancs.gitlab.model.project.ProjectResult getProjectById(org.avalancs.gitlab.GitlabAuth auth, long projectId) {
        // GET /projects/:id
        HttpGet request = auth.createGet('/projects/' + projectId);

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            Gson gson = new Gson();
            return gson.fromJson(response, org.avalancs.gitlab.model.project.ProjectResult.class);
        } finally {
            client?.close();
        }
    }

    /** https://archives.docs.gitlab.com/15.11/ee/api/projects.html#search-for-projects-by-name */
    static List<org.avalancs.gitlab.model.project.ProjectResult> searchProjectsByName(org.avalancs.gitlab.GitlabAuth auth, String projectName) {
        // GET /projects
        HttpGet request = auth.createGet('/projects');
        URI uri = new URIBuilder(request.getUri()).addParameters([new BasicNameValuePair("search", projectName)]).build();
        request.setUri(uri);

        CloseableHttpClient client = null;
        try {
            client = HttpClients.createDefault();
            String response = client.execute(request, new BasicHttpClientResponseHandler());
            log.debug(response);

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<org.avalancs.gitlab.model.project.ProjectResult>>(){}.getType();
            return gson.fromJson(response, listType) as List<org.avalancs.gitlab.model.project.ProjectResult>;
        } finally {
            client?.close();
        }
    }
}
