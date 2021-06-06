import com.comp.jenkins.shared.buildsteps.Pipeline
import com.comp.jenkins.shared.stages.BuildStage
import com.comp.jenkins.shared.stages.CleanStage
import com.comp.jenkins.shared.stages.PublishImageStage
import com.comp.jenkins.shared.stages.ScanImageStage
import com.comp.jenkins.shared.stages.ScmCheckoutStage
import com.comp.jenkins.shared.stages.Stage
import com.comp.jenkins.shared.stages.TestImage


def call(Map options = [:]) {

    def pipeline = new Pipeline(scm, options, this)
    options = pipeline.options
    def exceptions = []

    if (options.repositoryName == null) {
        error("Missing mandatory parameter: repositoryName")
    }

    println("Pipeline skip settings: options.skipDocker=${options.skipDocker}, options.skipHelm=${options.skipHelm}")
    if (options.skipHelm && options.skipDocker) {
        error("Cannot skipHelm and skipDocker")
    }

    def TARGET_ENVIRONMENTS = ["dev" : "dev"]
    
    def REPO_NAME = options.repositoryName
    println("repo name=${options.repositoryName}")

    println(${REPO_NAME})

    def REPO = "local/${REPO_NAME}"
    String COMMIT_ID = ""
    def GIT_REMOTE = ""
    def ENVIRONMENT = options.targetEnvironment ? options.targetEnvironment : "dev"
    def ERROR_MESSAGE = ""
    def ERROR_STACK_TRACE = ""
    def IMAGE_LABEL = env.JOB_NAME + env.BUILD_NUMBER
    options.environment = ENVIRONMENT
    options.image_label = IMAGE_LABEL
    options.repo = REPO

    node("docker-build-agent") {
        try {

            def scmCheckoutStage = new ScmCheckoutStage(pipelineScript: this, pipelineOptions: options, buildPipeline: pipeline)
            stage(scmCheckoutStage.name) {
                executeStage(scmCheckoutStage, exceptions)
                COMMIT_ID = options.commit_id
                GIT_REMOTE = options.git_remote
            }

            options.docker_image = "${options.repo}:${options.commit_id}"
            // Build Docker Image
            def buildStage = new BuildStage(pipelineScript: this, pipelineOptions: options, dockerArgs: dockerArgs)
            stage(buildStage.name) {
                executeStage(buildStage, exceptions)
            }

            // Build Docker Image
            def testImageStage = new TestImage(pipelineScript: this, pipelineOptions: options,
                dockerArgs: "/bin/bash -c \"nginx -v && test -f nginx.conf\"")
            stage(testImageStage.name) {
                executeStage(buildStage, exceptions)
            }

            // Scan Docker Image
            def scanImageStage = new ScanImageStage(pipelineScript: this, pipelineOptions: options)
            stage(scanImageStage.name) {
                executeStage(scanImageStage, exceptions)
            }

            if (BRANCH_NAME == options.primaryBranch) {
                def publishImageStage = new PublishImageStage(pipelineScript: this, pipelineOptions: options)
                stage(publishImageStage.name) {
                    executeStage(publishImageStage, exceptions)
                }
            }

            // Deploy to Environment only when on master branch
            if (!options.skipHelm && BRANCH_NAME == options.primaryBranch) {
                //TODO
            }

            // Pipeline stops here when not on the primary branch or the service is on restricted list.
            boolean restrictedFromDeployment = (options.chartYamlMap?.name && RESTRICTED_FROM_DEPLOYMENT.contains(options.chartYamlMap.name))
            currentBuild.rawBuild.result = Result.SUCCESS
        } catch (e) {
            currentBuild.rawBuild.result = Result.FAILURE
            ERROR_MESSAGE = e.getMessage()
            ERROR_STACK_TRACE = "${e.getStackTrace()}"
            throw e
        } finally {

            def cleanStage = new CleanStage(pipelineScript: this, pipelineOptions: options, txp: pipeline)
            stage(cleanStage.name) {
                executeStage(cleanStage, exceptions)
            }

            exceptions.each {
                printException(it.stage, it.exception)
            }
        }
    }
}


def dockerBuild(buildArgs, IMAGE_LABEL) {
    sh "docker build --label 'build=${IMAGE_LABEL}' ${buildArgs}"
}


/**
 * Executes a Stage.
 * <p>
 * In case of Exception, adds it to a pipeline Exceptions collection, prints a message
 * and lets the pipeline fail.
 *
 * @param stage stage to execute
 * @param exceptions collection of pipeline stage-exception pairs
 */
void executeStage(Stage stage, List<?> exceptions) {
    try {
        stage.execute()
    } catch (Exception e) {
        exceptions.add([stage: stage, exception: e])
        error("{$stage.name} failed: {$e.message}")
    }
}

/**
 * Prints details of a failed Stage and its Exception.
 *
 * @param stage Stage which failed
 * @param exception Exception behind the failure
 */
void printException(Stage stage, Exception exception) {
    // If a stage calls another stage which fails, the first one
    // throws a FlowInterruptedException while the second one throws an exception
    // which actually caused a failure.
    if (exception.getClass().getName().endsWith("FlowInterruptedException")) {
        return
    }

    println """\
                Exception in Stage $stage.name
                    class: ${stage.class.simpleName}
                    description: $stage.description""".stripIndent()

    StringWriter sw = new StringWriter()
    PrintWriter pw = new PrintWriter(sw)
    exception.printStackTrace(pw)
    println sw.toString()
}
