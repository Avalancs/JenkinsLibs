package org.avalancs.gitlab

import groovy.util.logging.Slf4j
@Grab(group='org.apache.httpcomponents.client5', module='httpclient5', version='5.3.1')
import org.apache.hc.client5.http.classic.methods.HttpDelete
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpPut
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.message.BasicNameValuePair

@Slf4j
class GitlabAuth {
    String baseUrl;
    String apiVersionUrl = "/api/v4";
    private String accessToken;

    GitlabAuth(String baseUrl, String accessToken) {
        while(baseUrl.endsWith('/')) {
            baseUrl = baseUrl.substring(0, baseUrl.length()-1);
        }
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
    }

    HttpGet createGet(String subUrl) {
        String fullUrl = baseUrl + apiVersionUrl + subUrl;
        log.debug("GET request to URL: " + fullUrl);
        HttpGet request = new HttpGet(fullUrl);
        request.addHeader('PRIVATE-TOKEN', accessToken);
        return request;
    }

    HttpPut createPut(String subUrl) {
        String fullUrl = baseUrl + apiVersionUrl + subUrl;
        log.debug("PUT request to URL: " + fullUrl);
        HttpPut request = new HttpPut(fullUrl);
        request.addHeader('PRIVATE-TOKEN', accessToken);
        return request;
    }

    HttpPost createPost(String subUrl) {
        return createPost(subUrl, null);
    }

    HttpPost createPost(String subUrl, Map<String, String> params) {
        String fullUrl = baseUrl + apiVersionUrl + subUrl;
        log.debug('POST request to URL: ' + fullUrl);
        HttpPost request = new HttpPost(fullUrl);
        request.addHeader('PRIVATE-TOKEN', accessToken);

        if(params != null) {
            final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            params.each {
                postParams.add(new BasicNameValuePair(it.key, it.value));
                log.debug('POST Param ' + it.key + ': ' + it.value);
            }
            request.setEntity(new UrlEncodedFormEntity(postParams));
        }

        return request;
    }

    HttpDelete createDelete(String subUrl) {
        String fullUrl = baseUrl + apiVersionUrl + subUrl;
        log.debug("DELETE request to URL: " + fullUrl);
        HttpDelete request = new HttpDelete(fullUrl);
        request.addHeader('PRIVATE-TOKEN', accessToken);
        return request;
    }
}
