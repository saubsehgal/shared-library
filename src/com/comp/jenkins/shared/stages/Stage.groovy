package com.comp.jenkins.shared.stages


/**
 * Jenkins Stage wrapper with additional logic.
 * <p>
 * This abstract class contains common stage logic.
 * Specific behaviour can be added by creating subclasses of this class.
 */
abstract class Stage implements Serializable {
    /** Scripted Jenkins pipeline executing this stage. */
    protected Script pipelineScript

    /** Map with options from the pipeline executing this stage. */
    protected Map pipelineOptions

    /** Reference to pipeline which has many methods needed by stages. */
    protected buildPipeline


    /**
     * Executes stages common logic and calls {@link #executeCoreLogic()}.
     *
     * @throws Exception generic exception for all kinds of failures
     */
    void execute() throws Exception {
        pipelineOptions['stage_statuses'][pipelineScript.env.STAGE_NAME] = "FAILURE"
        executeCoreLogic()
        pipelineOptions['stage_statuses'][pipelineScript.env.STAGE_NAME] = "SUCCESS"
    }

    abstract String getName()

    abstract String getDescription()

    /**
     * Executes stage's specific logic.
     */
    protected abstract void executeCoreLogic()
}
