package com.comp.jenkins.shared.stages

/**
 * Stage for tagging and publishing Docker images.
 */
class PublishImageStage extends Stage {

    @Override
    String getName() {
        return "Publish Image"
    }

    @Override
    String getDescription() {
        return "Tags and publishes Docker images."
    }

    @Override
    protected void executeCoreLogic() {
        pipelineOptions['stageHook'].call("PRE", "Publish Image")
        def REPO_IMAGE = "demo_registry/${pipelineOptions.repositoryName}"
        pipelineScript.sh "docker tag ${pipelineOptions.docker_image} ${REPO_IMAGE}:${pipelineOptions.commit_id}"
        pipelineScript.sh "docker tag ${pipelineOptions.docker_image} ${REPO_IMAGE}:latest"
        pipelineScript.sh "docker image push ${REPO_IMAGE}:${pipelineOptions.commit_id}"
        pipelineScript.sh "docker image push ${REPO_IMAGE}:latest"
        pipelineOptions['stageHook'].call("POST", "Publish Image")
    }
}
