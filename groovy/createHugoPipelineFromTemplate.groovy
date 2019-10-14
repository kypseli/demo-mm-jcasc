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
  <properties>
    <com.cloudbees.pipeline.governance.templates.classic.multibranch.GovernanceMultibranchPipelinePropertyImpl plugin="cloudbees-workflow-template@3.3">
      <instance>
        <model>workshopCatalog/hugo</model>
        <values class="tree-map">
          <entry>
            <string>gcpProject</string>
            <string>core-workshop</string>
          </entry>
          <entry>
            <string>gcpRegion</string>
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
  <factory class="com.cloudbees.pipeline.governance.templates.classic.multibranch.FromTemplateBranchProjectFactory" plugin="cloudbees-workflow-template@3.3">
    <owner class="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject" reference="../.."/>
    <catalogName>workshopCatalog</catalogName>
    <templateDirectory>hugo</templateDirectory>
    <markerFile>.hugo</markerFile>
  </factory>
</org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject>
"""

def p = j.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes("UTF-8")));