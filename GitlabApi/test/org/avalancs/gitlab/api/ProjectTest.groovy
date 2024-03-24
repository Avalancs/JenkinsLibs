package org.avalancs.gitlab.api


import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull

class ProjectTest extends org.avalancs.gitlab.GitlabTestBase {
    @Test
    void testGetAllProjects() {
        List<org.avalancs.gitlab.model.project.ProjectResult> projects = Project.getAllProjects(auth);
        assertNotNull(projects);
        projects.each {
            println(it.dump());
        }
    }

    @Test
    void testGetProjectById() {
        org.avalancs.gitlab.model.project.ProjectResult project = Project.getProjectById(auth, 4);
        assertNotNull(project);
        println(project.dump());
    }

    @Test
    void testSearchProjectsByName() {
        List<org.avalancs.gitlab.model.project.ProjectResult> projects = Project.searchProjectsByName(auth, "YourProjectName");
        assertNotNull(projects);
        projects.each {
            println(it.dump());
        }
    }
}
