package com.comp.jenkins.shared.stages

/**
 * Stage for Docker purge and code checkout.
 */
class ScmCheckoutStage extends Stage {

    @Override
    String getName() {
        return "SCM Checkout"
    }

    @Override
    String getDescription() {
        return "Removes stopped Docker containers and checks out code from version control."
    }

    @Override
    protected void executeCoreLogic() {
        buildPipeline.stageCheckoutSCM()
        pipelineOptions.commit_id = pipelineScript.sh(returnStdout: true, script: "git rev-parse --short=7 HEAD").trim()
        pipelineOptions.git_remote = pipelineScript.sh(returnStdout: true, script: 'git config remote.origin.url').trim()
        pipelineOptions['stageHook'].call("POST", "SCM")
    }
}
