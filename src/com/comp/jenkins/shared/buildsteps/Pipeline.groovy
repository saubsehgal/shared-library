package com.comp.jenkins.shared.buildsteps

import com.cloudbees.groovy.cps.NonCPS
import com.comp.jenkins.Util

class Pipeline implements Serializable {
    Map options;
    private scm
    private final script

    def Pipeline(scm, Map options, script){
        this.script = script
        this.scm = scm;
        this.options = parseOptions(options);
    }

    @NonCPS
    def parseOptions(Map options){

        options.stageHook = Util.ifVariableIsNullReturnThisDefaultValue(options.stageHook, script.runPipeline_stageHook.&call)
        options.targetBranch = Util.ifVariableIsNullReturnThisDefaultValue(options.targetBranch, "master")
        options.enable_cd = Util.ifVariableIsNullReturnThisDefaultValue(options.enable_cd, false)
        options.in_development = Util.ifVariableIsNullReturnThisDefaultValue(options.in_development, false)
        options.mergeWithPrimary = Util.ifVariableIsNullReturnThisDefaultValue(options.mergeWithPrimary, false)

        return options
    }

    def stageCheckoutSCM(){

        this.options['stageHook'].call("PRE", "Build")
//        this.dockerCleanup("dangling=true")
        // Checkout the current commit
        script.checkout this.scm
    }

    def dockerCleanup(filter) {
        def pruneFilterUntil = System.getProperty("docker.prune.filter", "30m")
        println("Cleanup with docker filter of: ${pruneFilterUntil}, control this with system property 'docker.prune.filter'")
        String dockerPruneCommand = """
        docker container prune -f --filter until=${pruneFilterUntil}
        docker volume prune -f
        image_ids="\$(docker images -q --filter '${filter}' | sort -u)"
        if [ "\$image_ids" ];then
            docker rmi -f "\$image_ids"
        fi
        docker image prune -f -a --filter until=${pruneFilterUntil}
        docker system df
        echo
        docker container ls --all --size --filter status=exited --filter status=dead --format '{{json .Image}} : {{json .Size}}'
        """
        script.sh(returnStatus: true, script: dockerPruneCommand)
    }

    def stageHelmBuild(){
        // TODO
     }


    def createDeploymentStage(colour) {
        //TODO
    }


    def helmPushChart(){

        //TODO
    }
}
