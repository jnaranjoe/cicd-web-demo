import jenkins.model.*
import hudson.security.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import hudson.util.Secret
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import hudson.plugins.git.BranchSpec

println "=== [INIT.GROOVY] Starting Jenkins Custom Configuration ==="

def instance = Jenkins.getInstance()

// 1. Create admin user
println "=== [INIT.GROOVY] Configuring Security Realm and Admin User ==="
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "admin")
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
instance.save()
println "=== [INIT.GROOVY] Admin user created (admin/admin) ==="

// 2. Create GitHub credentials
println "=== [INIT.GROOVY] Creating GitHub Credentials ==="
def domain = Domain.global()
def store = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def githubToken = System.getenv("GITHUB_TOKEN") ?: ""
def githubCreds = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "github-creds",
    "GitHub Credentials with PAT",
    "jnaranjoe",
    githubToken
)

// Check if credential already exists to avoid duplicates
def existingCreds = CredentialsProvider.lookupCredentials(
    StandardUsernamePasswordCredentials.class,
    instance,
    hudson.security.ACL.SYSTEM,
    new java.util.ArrayList<DomainRequirement>()
)
def exists = existingCreds.any { it.getId() == "github-creds" }

if (!exists) {
    store.addCredentials(domain, githubCreds)
    println "=== [INIT.GROOVY] GitHub credentials 'github-creds' created successfully ==="
} else {
    println "=== [INIT.GROOVY] GitHub credentials 'github-creds' already exist ==="
}

// 3. Create Pipeline Job
println "=== [INIT.GROOVY] Creating Pipeline Job 'CICD-Web-Demo' ==="
def jobName = "CICD-Web-Demo"

if (instance.getItem(jobName) == null) {
    def remoteConfigs = [new UserRemoteConfig(
        "https://github.com/jnaranjoe/cicd-web-demo.git",
        null, // name
        null, // refspec
        "github-creds" // credentialsId
    )]
    def branchSpecs = [new BranchSpec("*/main")]
    def gitScm = new GitSCM(
        remoteConfigs,
        branchSpecs,
        false, // doGenerateSubmoduleConfigurations
        null, // submoduleCfg
        null, // browser
        null, // gitTool
        [] // extensions
    )
    def flowDefinition = new CpsScmFlowDefinition(gitScm, "Jenkinsfile")
    flowDefinition.setLightweight(true) // Use lightweight checkout
    
    def project = instance.createProject(WorkflowJob.class, jobName)
    project.setDefinition(flowDefinition)
    project.save()
    println "=== [INIT.GROOVY] Job '${jobName}' created successfully ==="
} else {
    println "=== [INIT.GROOVY] Job '${jobName}' already exists ==="
}

println "=== [INIT.GROOVY] Jenkins Custom Configuration completed successfully ==="
