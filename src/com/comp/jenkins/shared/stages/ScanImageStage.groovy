package com.comp.jenkins.shared.stages

/**
 * Stage for scanning a Docker image for security vulnerabilities using Aqua Security API.
 */
class ScanImageStage extends Stage {

    @Override
    String getName() {
        return "Scan Image"
    }

    @Override
    String getDescription() {
        return "Scans a Docker image for security vulnerabilities using Aqua Security API."
    }

    @Override
    protected void executeCoreLogic() {
        pipelineOptions['stageHook'].call("PRE", "Scan Image")
        try {
            pipelineScript.aqua locationType: 'local', localImage: "${pipelineOptions.docker_image}", hideBase: false, notCompliesCmd: '', onDisallowed: 'ignore', showNegligible: false, customFlags: '-D'
        } catch (Exception e) {
            pipelineScript.echo "AQUA: Ignoring all errors in the pipeline during this phase"
        }
        pipelineOptions['stageHook'].call("POST", "Scan Image")
    }
}
