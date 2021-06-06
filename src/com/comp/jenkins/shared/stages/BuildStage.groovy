package com.comp.jenkins.shared.stages


/**
 * Stage for building the main Docker image.
 */
class BuildStage extends Stage {

    /** Arguments for the main Docker image's build command. */
    String dockerArgs

    @Override
    String getName() {
        return "Build Image"
    }

    @Override
    String getDescription() {
        return "Builds the Docker image."
    }

    @Override
    protected void executeCoreLogic() {
        def build_args_list = []
        build_args_list.add("--build-arg 'COMMIT_ID=${pipelineOptions.commit_id}'")
        build_args_list.add(dockerArgs)
        build_args_list.add("-t ${pipelineOptions.docker_image} .")
        pipelineScript.dockerBuild(build_args_list.join(" "), pipelineOptions.image_label)
        pipelineOptions['stageHook'].call("POST", "Build")
    }
}
