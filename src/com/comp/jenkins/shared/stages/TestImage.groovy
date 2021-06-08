package com.comp.jenkins.shared.stages

/**
 * Stage for testing a docker image
 */
class TestImage extends Stage {
    String dockerArgs;

    @Override
    String getName() {
        return "Test Image"
    }

    @Override
    String getDescription() {
        return "Test a image"
    }

    // Currently tests it nginx version and if nginx conf file is there or not
    @Override
    protected void executeCoreLogic() {
        exitStatus = sh(script: "docker run ${pipelineOptions.docker_image} ${dockerArgs}", returnStatus: true)
        if (exitStatus != 0) {
            error("Component Test Failed - Docker run exited with status code of ${exitStatus}")
        }
        pipelineOptions['stageHook'].call("POST", "Test Image")
    }
}
