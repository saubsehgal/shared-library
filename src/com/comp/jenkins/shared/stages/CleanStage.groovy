package com.comp.jenkins.shared.stages

class CleanStage extends Stage {

    @Override
    String getName() {
        return "Clean"
    }

    @Override
    String getDescription() {
        return null
    }

    @Override
    protected void executeCoreLogic() {

        pipelineScript.cleanWs()
        buildPipeline.dockerCleanup("label=build=${pipelineOptions.image_label}")
        pipelineScript.currentBuild.result = pipelineScript.currentBuild.result ?: 'SUCCESS'
    }
}
