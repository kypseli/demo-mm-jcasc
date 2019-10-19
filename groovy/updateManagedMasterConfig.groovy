import com.cloudbees.opscenter.server.model.OperationsCenter

String needleMasterName = "REPLACE_BRANCH_NAME"

def mm = OperationsCenter
       .getInstance()
       .getConnectedMasters()
       .find { it.name==needleMasterName }

if(mm) {
    def newYaml = """
---
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: "nginx"
    
---
kind: StatefulSet
spec:
  template:
    metadata:
      annotations:
          cluster-autoscaler.kubernetes.io/safe-to-evict: "false"
    spec:
      containers:
      - name: jenkins
        env:
          # With the help of SECRETS environment variable
          # we point Jenkins Configuration as Code plugin the location of the secrets
          - name: SECRETS
            value: /var/jenkins_home/mm-secrets
          - name: CASC_JENKINS_CONFIG
            value: https://raw.githubusercontent.com/kypseli/demo-mm-jcasc/REPLACE_BRANCH_NAME/jcasc.yml
        volumeMounts:
        - name: mm-secrets
          mountPath: /var/jenkins_home/mm-secrets
          readOnly: true
      volumes:
      - name: mm-secrets
        secret:
          secretName: mm-secrets
      nodeSelector:
        type: master
      automountServiceAccountToken: false
      securityContext:
        runAsUser: 1000
        fsGroup: 1000  
      """
   def mmConfig = mm.configuration
   if(mmConfig.yaml != newYaml) {
        mmConfig.yaml = newYaml
        // mmConfig provides a lot of configuration options. See the sister script in this directory for enumeration of those properties.
        mm.configuration = mmConfig
        mm.save()
        println("Saved configuration. Restarting master.")
        mm.restartAction(false) // the false here causes a graceful shutdown. Specifying true would force the termination of the pod.
   }
}
