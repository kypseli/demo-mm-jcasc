package com.cloudbees.opscenter.server.model

// This will only work when run against an Operations Center instance.

// WARNING!!!
//
// This is HIGHLY EXPERIMENTAL and should NOT be run against a production system. No guarantees of support are provided.
//
// WARNING!!!

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import com.cloudbees.masterprovisioning.kubernetes.KubernetesMasterProvisioning
import com.cloudbees.opscenter.server.model.ManagedMaster
import com.cloudbees.opscenter.server.properties.ConnectedMasterLicenseServerProperty
import com.cloudbees.opscenter.server.model.OperationsCenter


String masterName = "REPLACE_BRANCH_NAME"

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
            value: /var/jenkins_home/jcasc.yml
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
      securityContext:
        runAsUser: 1000
        fsGroup: 1000  
      """

Map props = [
//    allowExternalAgents: false, //boolean
//    clusterEndpointId: "default", //String
//    cpus: 1.0, //Double
      disk: 30, //Integer //
//    domain: "test-custom-domain-1", //String
//    envVars: "", //String
      fsGroup: "1000", //String
//    image: "custom-image-name", //String -- set this up in Operations Center Docker Image configuration
      javaOptions: "-XshowSettings:vm -XX:MaxRAMFraction=1 -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -Dhudson.slaves.NodeProvisioner.initialDelay=0 -Djenkins.install.runSetupWizard=false", //String
//    jenkinsOptions:"", //String
//    kubernetesInternalDomain: "cluster.local", //String
//    livenessInitialDelaySeconds: 300, //Integer
//    livenessPeriodSeconds: 10, //Integer
//    livenessTimeoutSeconds: 10, //Integer
      memory: 3060, //Integer
      namespace: "REPLACE_BRANCH_NAME", //String
//    ratio: 0.7, //Double
      storageClassName: "ssd", //String
//    terminationGracePeriodSeconds: 1200, //Integer
      yaml: newYaml
]

def configuration = new KubernetesMasterProvisioning()
props.each { key, value ->
    configuration."$key" = value
}

if(OperationsCenter.getInstance().getConnectedMasters().any { it?.getName()==masterName }) {
    println "Master with this name already exists. Performing update."
    def mm = OperationsCenter
       .getInstance()
       .getConnectedMasters()
       .find { it.name==masterName }
    def mmConfig = mm.configuration
    if(mmConfig.yaml != newYaml) {
        mmConfig.yaml = newYaml
        // mmConfig provides a lot of configuration options.
        mm.configuration = mmConfig
        mm.save()
        println("Saved configuration. Restarting master.")
        mm.restartAction(false) // the false here causes a graceful shutdown. Specifying true would force the termination of the pod.
        sleep 400
    }
} else {
    def j = Jenkins.instance
    def mmFolder = j.getItemByFullName("managed-masters")
    ManagedMaster master = mmFolder.createProject(ManagedMaster.class, masterName)

    println "Set config..."
    master.setConfiguration(configuration)
    master.properties.replace(new ConnectedMasterLicenseServerProperty(null))

    println "Save..."
    master.save()

    println "Run onModified..."
    master.onModified()

    println "Provision and start..."
    master.provisionAndStartAction();
}
