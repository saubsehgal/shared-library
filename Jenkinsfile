
pipeline {
    agent {
        docker {
            image ${AGENT_IMAGE}
            label ${AGENT_LABEL}
            args '--entrypoint=""'
        }
    }
    options {
        timestamps()
    }
    stages {

        stage('Checkout Repo') {
            steps {
                checkout scm
            }
        }

        stage('Run tests') {
            steps {
                sh './gradlew test'
            }
        }

        stage('SonarQube analysis') {

            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './gradlew sonarqube'
                }
            }

        }

    }
    post {
        always {
            script {
                currentBuild.result = currentBuild.result ?: 'SUCCESS'
                archiveArtifacts(artifacts: "build/reports/tests/test/**", fingerprint: false)
                notifyBitbucket()
            }
        }
    }
}
