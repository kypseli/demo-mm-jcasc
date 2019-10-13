pipeline {
  agent { label 'master' }
  stages {
    stage('Update Managed Master from Groovy') {
      steps {
        echo "preparing Jenkins CLI to update ${BRANCH_NAME}"
        sh 'curl -O http://cjoc/cjoc/jnlpJars/jenkins-cli.jar'
        sh("sed -i 's#REPLACE_BRANCH_NAME#${BRANCH_NAME}#' groovy/updateManagedMasterConfig.groovy")
        withCredentials([usernamePassword(credentialsId: 'cli-username-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh """
            alias cli='java -jar jenkins-cli.jar -s \'http://cjoc/cjoc/\' -auth $USERNAME:$PASSWORD'
            cli groovy = < groovy/updateManagedMasterConfig.groovy
          """
        }
      }
    }    
    stage('reload jcasc') {
      steps {
        echo "preparing to reload JCasc for ${BRANCH_NAME}"
        echo "preparing Jenkins CLI"
        sh 'curl -O http://teams-ops.core-demo.svc.cluster.local/teams-ops/jnlpJars/jenkins-cli.jar'
        withCredentials([usernamePassword(credentialsId: 'cli-username-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh """
            alias cli='java -jar jenkins-cli.jar -s \'http://teams-${BRANCH_NAME}.core-demo.svc.cluster.local/teams-${BRANCH_NAME}/\' -auth $USERNAME:$PASSWORD'
            cli reload-jcasc-configuration
          """
        }
      }
    }
  }
}