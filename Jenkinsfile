library 'kypseli@master'
def podYaml = libraryResource 'podtemplates/kubectl.yml'
pipeline {
  agent {
    kubernetes {
      label 'create-mm'
      defaultContainer 'jnlp'
      yaml podYaml
    }
  }
  options { 
    buildDiscarder(logRotator(numToKeepStr: '5'))
    preserveStashes(buildCount: 5)
  }
  stages {
    stage('Create Managed Master from Groovy') {
      when {
        not {
          branch "master"
        }
      }
      steps {
        echo "preparing Jenkins CLI"
        sh 'curl -O http://cjoc/cjoc/jnlpJars/jenkins-cli.jar'
        sh("sed -i 's#REPLACE_BRANCH_NAME#${BRANCH_NAME}#' groovy/createManagedMaster.groovy")
        withCredentials([usernamePassword(credentialsId: 'cli-username-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh """
            alias cli='java -jar jenkins-cli.jar -s \'http://cjoc/cjoc/\' -auth $USERNAME:$PASSWORD'
            cli groovy = < groovy/createManagedMaster.groovy
          """
          sleep 60
        }
      }
    }
    stage('Reload JCasC') {
      steps {
        echo "preparing to reload JCasc for ${BRANCH_NAME}"
        sh("wget -O master-jcasc.yml https://raw.githubusercontent.com/kypseli/demo-mm-jcasc/master/jcasc.yml")
        sh("yq m -a -i jcasc.yml master-jcasc.yml")
        container('kubectl') {   
          sh("kubectl cp jcasc.yml core-demo/${BRANCH_NAME}-0:/var/jenkins_home/")
        }
        echo "preparing Jenkins CLI"
        sh 'curl -O http://managed-masters-ops.core-demo.svc.cluster.local/managed-masters-ops/jnlpJars/jenkins-cli.jar'
        withCredentials([usernamePassword(credentialsId: 'cli-username-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh """
            alias cli='java -jar jenkins-cli.jar -s \'http://${BRANCH_NAME}.core-demo.svc.cluster.local/${BRANCH_NAME}/\' -auth $USERNAME:$PASSWORD'
            cli reload-jcasc-configuration
          """
        }
      }
    }
  }
}
