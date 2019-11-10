import hudson.model.*;
import jenkins.model.*;

import com.cloudbees.hudson.plugins.folder.*;

import java.util.logging.Logger

Logger logger = Logger.getLogger("createHugoPipelineFromTemplate.groovy")

def j = Jenkins.instance

def name = 'hugo-cloud-run'
logger.info("creating $name job")
def job = j.getItem(name)
if (job != null) {
  logger.info("job $name already existed so deleting")
  job.delete()
}
println "--> creating $name"

def configXml = """
<org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject plugin="workflow-multibranch@2.21">
  <actions/>
  <properties>
    <com.cloudbees.pipeline.governance.templates.classic.multibranch.GovernanceMultibranchPipelinePropertyImpl plugin="cloudbees-workflow-template@3.3">
      <instance>
        <model>workshopCatalog/hugo</model>
        <values class="tree-map">
          <entry>
            <string>clusterNameMaster</string>
            <string>core-labs-cb-sa</string>
          </entry>
          <entry>
            <string>clusterNamePR</string>
            <string></string>
          </entry>
          <entry>
            <string>deployTypeMaster</string>
            <string>gke</string>
          </entry>
          <entry>
            <string>deployTypePR</string>
            <string>managed</string>
          </entry>
          <entry>
            <string>gcpProject</string>
            <string>core-workshop</string>
          </entry>
          <entry>
            <string>gcpRegionMaster</string>
            <string>us-east4-b</string>
          </entry>
          <entry>
            <string>gcpRegionPR</string>
            <string>us-central1</string>
          </entry>
          <entry>
            <string>githubCredentialId</string>
            <string>cbdays-github-token</string>
          </entry>
          <entry>
            <string>name</string>
            <string>hugo-cloud-run</string>
          </entry>
          <entry>
            <string>namespaceMaster</string>
            <string>cloud-run</string>
          </entry>
          <entry>
            <string>namespacePR</string>
            <string></string>
          </entry>
          <entry>
            <string>projectName</string>
            <string>hugo-cloud-run</string>
          </entry>
          <entry>
            <string>repo</string>
            <string>blog</string>
          </entry>
          <entry>
            <string>repoOwner</string>
            <string>cloudbees-days</string>
          </entry>
        </values>
      </instance>
    </com.cloudbees.pipeline.governance.templates.classic.multibranch.GovernanceMultibranchPipelinePropertyImpl>
  </properties>
  <folderViews class="jenkins.branch.MultiBranchProjectViewHolder" plugin="branch-api@2.5.4">
    <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
  </folderViews>
  <healthMetrics>
    <com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric plugin="cloudbees-folder@6.9">
      <nonRecursive>false</nonRecursive>
    </com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric>
    <com.cloudbees.hudson.plugins.folder.health.AverageChildHealthMetric plugin="cloudbees-folders-plus@3.8"/>
    <com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric plugin="cloudbees-folders-plus@3.8">
      <success>true</success>
      <failure>true</failure>
      <unstable>true</unstable>
      <unbuilt>true</unbuilt>
      <countVirginJobs>false</countVirginJobs>
    </com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric>
    <com.cloudbees.hudson.plugins.folder.health.ProjectEnabledHealthMetric plugin="cloudbees-folders-plus@3.8"/>
  </healthMetrics>
  <icon class="jenkins.branch.MetadataActionFolderIcon" plugin="branch-api@2.5.4">
    <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
  </icon>
  <orphanedItemStrategy class="com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy" plugin="cloudbees-folder@6.9">
    <pruneDeadBranches>true</pruneDeadBranches>
    <daysToKeep>-1</daysToKeep>
    <numToKeep>-1</numToKeep>
  </orphanedItemStrategy>
  <triggers>
    <com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger plugin="cloudbees-folder@6.9">
      <spec>H H/4 * * *</spec>
      <interval>86400000</interval>
    </com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger>
  </triggers>
  <disabled>false</disabled>
  <sources>
    <jenkins.branch.BranchSource plugin="branch-api@2.5.4">
      <source class="org.jenkinsci.plugins.github_branch_source.GitHubSCMSource" plugin="github-branch-source@2.5.8">
        <id>hugo-cloud-run</id>
        <apiUri>https://api.github.com</apiUri>
        <credentialsId>cbdays-github-token</credentialsId>
        <repoOwner>cloudbees-days</repoOwner>
        <repository>blog</repository>
        <traits>
          <org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait>
            <strategyId>1</strategyId>
          </org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait>
          <org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait>
            <strategyId>1</strategyId>
          </org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait>
          <org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait>
            <strategyId>1</strategyId>
            <trust class="org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission"/>
          </org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait>
        </traits>
      </source>
      <strategy class="jenkins.branch.DefaultBranchPropertyStrategy">
        <properties class="java.util.Arrays$ArrayList">
          <a class="jenkins.branch.BranchProperty-array"/>
        </properties>
      </strategy>
    </jenkins.branch.BranchSource>
  </sources>
  <factory class="com.cloudbees.pipeline.governance.templates.classic.multibranch.FromTemplateBranchProjectFactory" plugin="cloudbees-workflow-template@3.3">
    <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
    <catalogName>workshopCatalog</catalogName>
    <templateDirectory>hugo</templateDirectory>
    <markerFile>.hugo</markerFile>
  </factory>
</org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject>
"""

def p = j.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes("UTF-8")));
