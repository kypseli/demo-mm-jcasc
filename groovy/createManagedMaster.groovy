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


String masterName = "test"

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
//    image: "custom-image-name", //String -- set this up in Operations Center Docker Image configuration
      javaOptions: "-XshowSettings:vm -XX:MaxRAMFraction=1 -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -Dhudson.slaves.NodeProvisioner.initialDelay=0 -Djenkins.install.runSetupWizard=false ", //String
//    jenkinsOptions:"", //String
//    kubernetesInternalDomain: "cluster.local", //String
//    livenessInitialDelaySeconds: 300, //Integer
//    livenessPeriodSeconds: 10, //Integer
//    livenessTimeoutSeconds: 10, //Integer
      memory: 3060, //Integer
      namespace: "cloud-run", //String
      nodeSelectors: type=master, //String
//    ratio: 0.7, //Double
      storageClassName: "ssd", //String
//    systemProperties:"", //String
//    terminationGracePeriodSeconds: 1200, //Integer
      yaml:"---\nkind: Ingress\nmetadata:\n  annotations:\n    kubernetes.io/ingress.class:\
      \ \"nginx\"\n    \n---\nkind: StatefulSet\nspec:\n  template:\n    metadata:\n\
      \      annotations:\n          cluster-autoscaler.kubernetes.io/safe-to-evict:\
      \ \"false\"\n    spec:\n      containers:\n      - name: jenkins\n        env:\n\
      \          # With the help of SECRETS environment variable\n          # we point\
      \ Jenkins Configuration as Code plugin the location of the secrets\n       \
      \   - name: SECRETS\n            value: /var/jenkins_home/mm-secrets\n     \
      \     - name: CASC_JENKINS_CONFIG\n            value: https://raw.githubusercontent.com/kypseli/workshop-mm-jcasc/master/jcasc.yml\n\
      \        volumeMounts:\n        - name: mm-secrets\n          mountPath: /var/jenkins_home/mm-secrets\n\
      \          readOnly: true\n      volumes:\n      - name: mm-secrets\n      \
      \  secret:\n          secretName: mm-secrets\n      nodeSelector:\n        type:\
      \ master\n      securityContext:\n        runAsUser: 1000\n        fsGroup:\
      \ 1000\n" //String
]

def configuration = new KubernetesMasterProvisioning()
props.each { key, value ->
    configuration."$key" = value
}

def j = Jenkins.instance
ManagedMaster master = j.createProject(ManagedMaster.class, masterName)

println "Set config..."
master.setConfiguration(configuration)
master.properties.replace(new ConnectedMasterLicenseServerProperty(null))

println "Save..."
master.save()

println "Run onModified..."
master.onModified()

println "Provision and start..."
master.provisionAndStartAction();
