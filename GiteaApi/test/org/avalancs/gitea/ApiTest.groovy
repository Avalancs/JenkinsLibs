package org.avalancs.gitea

import org.avalancs.gitea.util.PullRequest
import org.avalancs.gitea.util.Repository
import org.avalancs.gitea.util.WebhookEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import static org.junit.jupiter.api.Assumptions.*

class ApiTest  {
    Api gitea, giteaWithToken;
    WebhookParser parser;
    // Insert your own Gitea url and user
    static final String url = 'https://gitea.somecompany.com';
    static final String username = '***';
    static final String password = '***';
    static final String token = '***';

    /** If user did not fill out credentials then don't run the tests */
    void assumeFilled() {
        assumeFalse('https://gitea.somecompany.com' == url);
        assumeFalse('***' == username);
        assumeFalse('***' == password);
        assumeFalse('***' == token);
    }

    @BeforeEach
    void setUp() {
        gitea = new Api(url, username, password);
        giteaWithToken = new Api(url, token);
        parser = new WebhookParser();
    }

    @Test
    void testPullRequest() {
        WebhookEvent event = parser.parse(newPullRequestJson);
        assertNotNull(event.repo);
        assertNotNull(event.pullRequest);
        assertEquals('opened', event.action);
        assertTrue(event.isNewPullRequest());
    }

    @Test
    void testUjPush() {
        WebhookEvent event = parser.parse(newPushJson);
        assertNotNull(event.repo);
        assertNotNull(event.push);
        assertTrue(event.isPush());
    }

    @Test
    void testUjCommentPRre() {
        WebhookEvent event = parser.parse(commentOnPullRequestJson);
        assertNotNull(event.repo);
        assertNotNull(event.comment);
        assertEquals('created', event.action);
        assertTrue(event.isNewPRComment());
    }

    @Test
    void testCreateRepoFromCloneUrl() {
        String fullName = 'Tardis/PullRequestPipeline';
        String html = url + "/" + fullName;
        String cloneUrl = html + '.git';
        Repository repo = gitea.createRepoFromCloneUrl(cloneUrl);
        assertNotNull(repo);
        assertEquals(fullName, repo.fullName);
        assertEquals(html, repo.htmlUrl);
        assertEquals(cloneUrl, repo.cloneUrl);
    }

    @Test
    @Disabled
    void testPullRequestGet() {
        assumeFilled();
        WebhookEvent event = parser.parse(commentOnPullRequestJson);
        assertNotNull(event.repo);
        assertNotNull(event.comment);
        assertEquals('created', event.action);
        PullRequest pr = gitea.getPullRequestForComment (event.repo, event.comment);
        println pr.dump();
    }

    @Test
    @Disabled
    void testUjCommentIrasPRre() {
        assumeFilled();
        WebhookEvent event = parser.parse(commentOnPullRequestJson);
        assertNotNull(event.repo);
        assertNotNull(event.comment);
        assertEquals('created', event.action);
        PullRequest pr = gitea.getPullRequestForRepo(event.repo, event.comment.pullRequestId) as PullRequest;
        assertNotNull(pr);
        assertNotNull(gitea.addCommentToPullRequest(event.repo, event.comment.issueId, "Elindultak a tesztek"));
    }

    @Test
    @Disabled
    void testUjKomment() {
        assumeFilled();
        Repository repo = new Repository('Teszt_Org/Frontend', '', '');
        assertNotNull(gitea.addCommentToPullRequest(repo, '7', "Ez csak egy teszt"));
    }

    @Test
    @Disabled
    void testGetBranches() {
        assumeFilled();
        Repository repo = new Repository('Multihaz/NarniaBackend', '', '');
        List<String> branches = gitea.getBranchesForRepo(repo);
        assertNotNull(branches);
        println branches.toString();
    }

    @Test
    @Disabled
    void testGetTags() {
        assumeFilled();
        Repository repo = new Repository('Multihaz/NarniaBackend', '', '');
        List<String> tags = gitea.getTagsForRepo(repo);
        assertNotNull(tags);
        println tags.toString();
    }

    @Test
    @Disabled
    void getContentsOfFile() {
        assumeFilled();
        Repository repo = new Repository('Joomla-Tools/CHJoomDev', '', '');
        String readme = gitea.getContentsOfFile(repo, ".gitignore");
        assertNotNull(readme);
        println readme;
    }

    @Test
    @Disabled
    void getContentsOfFileOtherBranch() {
        assumeFilled();
        Repository repo = new Repository('Joomla-Tools/ElesToGitRepok', '', '');
        String readme = gitea.getContentsOfFile(repo, "ftpk.json", "dbsave");
        assertNotNull(readme);
        println readme;
    }

    final String newPullRequestJson = '''
        {
          "secret": "",
          "action": "opened",
          "number": 8,
          "pull_request": {
            "id": 8,
            "url": "http://localhost:3000/Teszt_Org/TesztRepo/pulls/8",
            "number": 8,
            "user": {
              "id": 1,
              "login": "tesztelek",
              "full_name": "Teszt Elek",
              "email": "tesztelek@something.com",
              "avatar_url": "http://localhost:3000/avatars/0de56207be8d0b8c67f6e1a015379633",
              "language": "en-US",
              "is_admin": true,
              "last_login": "2019-08-29T11:10:30+02:00",
              "created": "2019-08-27T13:01:55+02:00",
              "username": "tesztelek"
            },
            "title": ".",
            "body": "",
            "labels": [],
            "milestone": null,
            "assignee": null,
            "assignees": null,
            "state": "open",
            "comments": 0,
            "html_url": "http://localhost:3000/Teszt_Org/TesztRepo/pulls/8",
            "diff_url": "http://localhost:3000/Teszt_Org/TesztRepo/pulls/8.diff",
            "patch_url": "http://localhost:3000/Teszt_Org/TesztRepo/pulls/8.patch",
            "mergeable": true,
            "merged": false,
            "merged_at": null,
            "merge_commit_sha": null,
            "merged_by": null,
            "base": {
              "label": "master",
              "ref": "master",
              "sha": "c41cfd068391acb181f9c7f1ef8b02d6aa60e9b1",
              "repo_id": 1,
              "repo": {
                "id": 1,
                "owner": {
                  "id": 2,
                  "login": "Teszt_Org",
                  "full_name": "",
                  "email": "",
                  "avatar_url": "http://localhost:3000/avatars/2",
                  "language": "",
                  "is_admin": false,
                  "last_login": "1970-01-01T01:00:00+01:00",
                  "created": "2019-08-27T13:03:14+02:00",
                  "username": "Teszt_Org"
                },
                "name": "TesztRepo",
                "full_name": "Teszt_Org/TesztRepo",
                "description": "",
                "empty": false,
                "private": false,
                "fork": false,
                "parent": null,
                "mirror": false,
                "size": 452,
                "html_url": "http://localhost:3000/Teszt_Org/TesztRepo",
                "ssh_url": "tesztelek@localhost:Teszt_Org/TesztRepo.git",
                "clone_url": "http://localhost:3000/Teszt_Org/TesztRepo.git",
                "website": "",
                "stars_count": 0,
                "forks_count": 0,
                "watchers_count": 1,
                "open_issues_count": 0,
                "default_branch": "master",
                "archived": false,
                "created_at": "2019-08-27T13:04:54+02:00",
                "updated_at": "2019-08-29T11:24:32+02:00",
                "permissions": {
                  "admin": false,
                  "push": false,
                  "pull": false
                },
                "has_issues": false,
                "has_wiki": false,
                "has_pull_requests": true,
                "ignore_whitespace_conflicts": false,
                "allow_merge_commits": true,
                "allow_rebase": true,
                "allow_rebase_explicit": true,
                "allow_squash_merge": true,
                "avatar_url": ""
              }
            },
            "head": {
              "label": "newbranch",
              "ref": "newbranch",
              "sha": "2569dc5b206564f37063f9416e6dfba7b8296dca",
              "repo_id": 1,
              "repo": {
                "id": 1,
                "owner": {
                  "id": 2,
                  "login": "Teszt_Org",
                  "full_name": "",
                  "email": "",
                  "avatar_url": "http://localhost:3000/avatars/2",
                  "language": "",
                  "is_admin": false,
                  "last_login": "1970-01-01T01:00:00+01:00",
                  "created": "2019-08-27T13:03:14+02:00",
                  "username": "Teszt_Org"
                },
                "name": "TesztRepo",
                "full_name": "Teszt_Org/TesztRepo",
                "description": "",
                "empty": false,
                "private": false,
                "fork": false,
                "parent": null,
                "mirror": false,
                "size": 452,
                "html_url": "http://localhost:3000/Teszt_Org/TesztRepo",
                "ssh_url": "tesztelek@localhost:Teszt_Org/TesztRepo.git",
                "clone_url": "http://localhost:3000/Teszt_Org/TesztRepo.git",
                "website": "",
                "stars_count": 0,
                "forks_count": 0,
                "watchers_count": 1,
                "open_issues_count": 0,
                "default_branch": "master",
                "archived": false,
                "created_at": "2019-08-27T13:04:54+02:00",
                "updated_at": "2019-08-29T11:24:32+02:00",
                "permissions": {
                  "admin": false,
                  "push": false,
                  "pull": false
                },
                "has_issues": false,
                "has_wiki": false,
                "has_pull_requests": true,
                "ignore_whitespace_conflicts": false,
                "allow_merge_commits": true,
                "allow_rebase": true,
                "allow_rebase_explicit": true,
                "allow_squash_merge": true,
                "avatar_url": ""
              }
            },
            "merge_base": "c41cfd068391acb181f9c7f1ef8b02d6aa60e9b1",
            "due_date": null,
            "created_at": "2019-08-29T11:26:41+02:00",
            "updated_at": "2019-08-29T11:26:41+02:00",
            "closed_at": null
          },
          "repository": {
            "id": 1,
            "owner": {
              "id": 2,
              "login": "Teszt_Org",
              "full_name": "",
              "email": "",
              "avatar_url": "http://localhost:3000/avatars/2",
              "language": "",
              "is_admin": false,
              "last_login": "1970-01-01T01:00:00+01:00",
              "created": "2019-08-27T13:03:14+02:00",
              "username": "Teszt_Org"
            },
            "name": "TesztRepo",
            "full_name": "Teszt_Org/TesztRepo",
            "description": "",
            "empty": false,
            "private": false,
            "fork": false,
            "parent": null,
            "mirror": false,
            "size": 452,
            "html_url": "http://localhost:3000/Teszt_Org/TesztRepo",
            "ssh_url": "tesztelek@localhost:Teszt_Org/TesztRepo.git",
            "clone_url": "http://localhost:3000/Teszt_Org/TesztRepo.git",
            "website": "",
            "stars_count": 0,
            "forks_count": 0,
            "watchers_count": 1,
            "open_issues_count": 0,
            "default_branch": "master",
            "archived": false,
            "created_at": "2019-08-27T13:04:54+02:00",
            "updated_at": "2019-08-29T11:24:32+02:00",
            "permissions": {
              "admin": true,
              "push": true,
              "pull": true
            },
            "has_issues": false,
            "has_wiki": false,
            "has_pull_requests": true,
            "ignore_whitespace_conflicts": false,
            "allow_merge_commits": true,
            "allow_rebase": true,
            "allow_rebase_explicit": true,
            "allow_squash_merge": true,
            "avatar_url": ""
          },
          "sender": {
            "id": 1,
            "login": "tesztelek",
            "full_name": "Teszt Elek",
            "email": "tesztelek@something.com",
            "avatar_url": "http://localhost:3000/avatars/0de56207be8d0b8c67f6e1a015379633",
            "language": "en-US",
            "is_admin": true,
            "last_login": "2019-08-29T11:10:30+02:00",
            "created": "2019-08-27T13:01:55+02:00",
            "username": "tesztelek"
          }
        }
    '''

    final String newPushJson = '''
    {
      "secret": "",
      "ref": "refs/heads/master",
      "before": "18b7ccf743924db6d02e23f7cefd7bd73f51ebc6",
      "after": "7ce96230dd0ff3d24402c0d2b86f728b941856cd",
      "compare_url": "http://localhost:3000/Teszt_Org/TesztRepo/compare/18b7ccf743924db6d02e23f7cefd7bd73f51ebc6...7ce96230dd0ff3d24402c0d2b86f728b941856cd",
      "commits": [
        {
          "id": "7ce96230dd0ff3d24402c0d2b86f728b941856cd",
          "message": ".\\n",
          "url": "http://localhost:3000/Teszt_Org/TesztRepo/commit/7ce96230dd0ff3d24402c0d2b86f728b941856cd",
          "author": {
            "name": "teszt.elek",
            "email": "teszt.elek@testing.com",
            "username": ""
          },
          "committer": {
            "name": "teszt.elek",
            "email": "teszt.elek@testing.com",
            "username": ""
          },
          "verification": null,
          "timestamp": "2019-08-28T16:35:12+02:00",
          "added": null,
          "removed": null,
          "modified": null
        }
      ],
      "head_commit": null,
      "repository": {
        "id": 1,
        "owner": {
          "id": 2,
          "login": "Teszt_Org",
          "full_name": "",
          "email": "",
          "avatar_url": "http://localhost:3000/avatars/2",
          "language": "",
          "is_admin": false,
          "last_login": "1970-01-01T01:00:00+01:00",
          "created": "2019-08-27T13:03:14+02:00",
          "username": "Teszt_Org"
        },
        "name": "TesztRepo",
        "full_name": "Teszt_Org/TesztRepo",
        "description": "",
        "empty": false,
        "private": false,
        "fork": false,
        "parent": null,
        "mirror": false,
        "size": 244,
        "html_url": "http://localhost:3000/Teszt_Org/TesztRepo",
        "ssh_url": "tesztelek@localhost:Teszt_Org/TesztRepo.git",
        "clone_url": "http://localhost:3000/Teszt_Org/TesztRepo.git",
        "website": "",
        "stars_count": 0,
        "forks_count": 0,
        "watchers_count": 1,
        "open_issues_count": 0,
        "default_branch": "master",
        "archived": false,
        "created_at": "2019-08-27T13:04:54+02:00",
        "updated_at": "2019-08-28T16:35:14+02:00",
        "permissions": {
          "admin": false,
          "push": false,
          "pull": false
        },
        "has_issues": false,
        "has_wiki": false,
        "has_pull_requests": true,
        "ignore_whitespace_conflicts": false,
        "allow_merge_commits": true,
        "allow_rebase": true,
        "allow_rebase_explicit": true,
        "allow_squash_merge": true,
        "avatar_url": ""
      },
      "pusher": {
        "id": 1,
        "login": "tesztelek",
        "full_name": "Teszt Elek",
        "email": "tesztelek@something.com",
        "avatar_url": "http://localhost:3000/avatars/0de56207be8d0b8c67f6e1a015379633",
        "language": "en-US",
        "is_admin": true,
        "last_login": "2019-08-28T14:36:50+02:00",
        "created": "2019-08-27T13:01:55+02:00",
        "username": "tesztelek"
      },
      "sender": {
        "id": 1,
        "login": "tesztelek",
        "full_name": "Teszt Elek",
        "email": "tesztelek@something.com",
        "avatar_url": "http://localhost:3000/avatars/0de56207be8d0b8c67f6e1a015379633",
        "language": "en-US",
        "is_admin": true,
        "last_login": "2019-08-28T14:36:50+02:00",
        "created": "2019-08-27T13:01:55+02:00",
        "username": "tesztelek"
      }
    }
    '''

    final String commentOnPullRequestJson = '''
    {
      "secret": "",
      "action": "created",
      "issue": {
        "id": 34,
        "url": "http://localhost:3000/api/v1/repos/Teszt_Org/Frontend2/issues/9",
        "number": 9,
        "user": {
          "id": 1,
          "login": "tesztelek",
          "full_name": "Teszt Elek",
          "email": "tesztelek@something.com",
          "avatar_url": "http://localhost:3000/avatars/0de56207be8d0b8c67f6e1a015379633",
          "language": "en-US",
          "is_admin": true,
          "last_login": "2019-09-03T10:24:59+02:00",
          "created": "2019-08-27T13:01:55+02:00",
          "username": "tesztelek"
        },
        "title": ".",
        "body": "",
        "labels": [],
        "milestone": null,
        "assignee": null,
        "assignees": null,
        "state": "open",
        "comments": 2,
        "created_at": "2019-09-03T12:27:33+02:00",
        "updated_at": "2019-09-03T13:42:48+02:00",
        "closed_at": null,
        "due_date": null,
        "pull_request": {
          "merged": false,
          "merged_at": null
        }
      },
      "comment": {
        "id": 233,
        "html_url": "http://localhost:3000/Teszt_Org/Frontend2/pulls/9#issuecomment-233",
        "pull_request_url": "http://localhost:3000/Teszt_Org/Frontend2/pulls/9",
        "issue_url": "",
        "user": {
          "id": 1,
          "login": "tesztelek",
          "full_name": "Teszt Elek",
          "email": "tesztelek@something.com",
          "avatar_url": "http://localhost:3000/avatars/0de56207be8d0b8c67f6e1a015379633",
          "language": "en-US",
          "is_admin": true,
          "last_login": "2019-09-03T10:24:59+02:00",
          "created": "2019-08-27T13:01:55+02:00",
          "username": "tesztelek"
        },
        "body": "Retest",
        "created_at": "2019-09-03T13:42:48+02:00",
        "updated_at": "2019-09-03T13:42:48+02:00"
      },
      "repository": {
        "id": 6,
        "owner": {
          "id": 2,
          "login": "Teszt_Org",
          "full_name": "",
          "email": "",
          "avatar_url": "http://localhost:3000/avatars/2",
          "language": "",
          "is_admin": false,
          "last_login": "1970-01-01T01:00:00+01:00",
          "created": "2019-08-27T13:03:14+02:00",
          "username": "Teszt_Org"
        },
        "name": "Frontend2",
        "full_name": "Teszt_Org/Frontend2",
        "description": "",
        "empty": false,
        "private": false,
        "fork": false,
        "parent": null,
        "mirror": false,
        "size": 3439,
        "html_url": "http://localhost:3000/Teszt_Org/Frontend2",
        "ssh_url": "tesztelek@localhost:Teszt_Org/Frontend2.git",
        "clone_url": "http://localhost:3000/Teszt_Org/Frontend2.git",
        "website": "",
        "stars_count": 0,
        "forks_count": 0,
        "watchers_count": 1,
        "open_issues_count": 0,
        "default_branch": "master",
        "archived": false,
        "created_at": "2019-09-02T17:22:22+02:00",
        "updated_at": "2019-09-02T17:32:12+02:00",
        "permissions": {
          "admin": true,
          "push": true,
          "pull": true
        },
        "has_issues": true,
        "has_wiki": true,
        "has_pull_requests": true,
        "ignore_whitespace_conflicts": false,
        "allow_merge_commits": true,
        "allow_rebase": true,
        "allow_rebase_explicit": true,
        "allow_squash_merge": true,
        "avatar_url": ""
      },
      "sender": {
        "id": 1,
        "login": "tesztelek",
        "full_name": "Teszt Elek",
        "email": "tesztelek@something.com",
        "avatar_url": "http://localhost:3000/avatars/0de56207be8d0b8c67f6e1a015379633",
        "language": "en-US",
        "is_admin": true,
        "last_login": "2019-09-03T10:24:59+02:00",
        "created": "2019-08-27T13:01:55+02:00",
        "username": "tesztelek"
      }
    }
    '''
}
