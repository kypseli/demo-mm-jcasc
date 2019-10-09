pipeline {
  agent { label 'default-jnlp' }
  stages {
    stage('reload jcasc') {
      steps {
        echo "preparing to reload JCasc for ${BRANCH_NAME}"
        echo "preparing Jenkins CLI"
        sh 'curl -O http://teams-ops.cje.svc.cluster.local/teams-ops/jnlpJars/jenkins-cli.jar'
        withCredentials([usernamePassword(credentialsId: 'cli-username-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh """
            alias cli='java -jar jenkins-cli.jar -s \'http://teams-${BRANCH_NAME}.cje.svc.cluster.local/teams-${BRANCH_NAME}/\' -auth $USERNAME:$PASSWORD'
            cli reload-jcasc-configuration
          """
        }
      }
    }
  }
}
