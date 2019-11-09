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

if(OperationsCenter.getInstance().getConnectedMasters().any { it?.getName()==masterName }) {
    println "Master with this name already exists."
    return
}

Map props = [
//    allowExternalAgents: false, //boolean
//    clusterEndpointId: "default", //String
//    cpus: 1.0, //Double
      disk: 50, //Integer //
//    domain: "test-custom-domain-1", //String
//    envVars: "", //String
      fsGroup: "1000", //String
//    image: "", //String -- set this up in Operations Center Docker Image configuration
      javaOptions: "-XshowSettings:vm -XX:MaxRAMFraction=1 -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -Dhudson.slaves.NodeProvisioner.initialDelay=0 -Djenkins.install.runSetupWizard=false ", //String
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
      yaml:"""
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
      securityContext:
        runAsUser: 1000
        fsGroup: 1000  
      """
]

def configuration = new KubernetesMasterProvisioning()
props.each { key, value ->
    configuration."$key" = value
}
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
