  organizationFolder('dev/bee-cd') {
    description('bee-cd GitHub Organization')
    displayName('bee-cd GitHub Organization')
    triggers {
      periodic(2400)
    }
    organizations {
      github {
        repoOwner("bee-cd")
        apiUri("https://api.github.com")
        credentialsId('beedemo-dev-github-username-pat')
        traits {
          publicRepoPullRequestFilterTrait()
        }
      }
    }
    configure {
        def traits = it / navigators / 'org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator' / traits
        traits << 'org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait' {
            strategyId 1
        }
        traits << 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait' {
            strategyId 2
            trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustEveryone')
        }
        traits << 'org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait' {
            strategyId 2
        }
    }
    projectFactories {
      workflowMultiBranchProjectFactory {
        // Relative location within the checkout of your Pipeline script.
        scriptPath("Jenkinsfile")
      }
    }
  }