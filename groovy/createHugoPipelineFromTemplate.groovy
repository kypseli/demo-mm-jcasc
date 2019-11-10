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
</org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject>
"""

def p = j.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes("UTF-8")));
