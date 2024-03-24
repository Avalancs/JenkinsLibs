package org.avalancs.gitlab.model.project

class ProjectResult {
    long id;
    String description;
    String name;
    String name_with_namespace;
    String path;
    String path_with_namespace;
    String created_at;
    String default_branch;
    String ssh_url_to_repo;
    String http_url_to_repo;
    String web_url;
    Namespace namespace;
    boolean empty_repo;
    boolean archived;
    String visibility;
    boolean issues_enabled;
    boolean merge_requests_enabled;
    boolean wiki_enabled;
    boolean jobs_enabled;
    boolean container_registry_enabled;
    boolean can_create_merge_request_in;
    String creator_id;
}
