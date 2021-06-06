package com.comp.jenkins.shared.stages

/**
 * Stage for preparing, building and packaging charts. Runs terraform for feature branches.
 */
class HelmBuildStage extends Stage {

    @Override
    String getName() {
        return "Helm Build"
    }

    @Override
    String getDescription() {
        return "Prepares, builds and packages charts. Runs terraform for feature branches."
    }

    @Override
    protected void executeCoreLogic() {
        //TODO
    }
}
