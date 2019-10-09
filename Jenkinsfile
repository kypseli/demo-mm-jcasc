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
      steps {
        echo "setting up K8s objects"
        sh("sed -i 's#BRANCH_NAME#${BRANCH_NAME}#' k8s/mm.yml")
        container('kubectl') {
          sh """
          kubectl get secret jenkins-agent -n core-demo -o yaml \
            | sed s/"namespace: core-demo"/"namespace: ${BRANCH_NAME}"/\
            | kubectl apply -n ${BRANCH_NAME} -f -
          """
          sh "kubectl -n ${BRANCH_NAME} apply -f k8s/mm.yml"
        }
        echo "preparing Jenkins CLI"
        sh 'curl -O http://cjoc/cjoc/jnlpJars/jenkins-cli.jar'
        sh("sed -i 's#BRANCH_NAME#${BRANCH_NAME}#' groovy/createManagedMaster.groovy")
        withCredentials([usernamePassword(credentialsId: 'cli-username-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh """
            alias cli='java -jar jenkins-cli.jar -s \'http://cjoc/cjoc/\' -auth $USERNAME:$PASSWORD'
            cli groovy = < groovy/createManagedMaster.groovy
          """
        }
      }
    }
  }
}
