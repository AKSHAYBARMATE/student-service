pipeline {
    agent any

    environment {
        COMPOSE_FILE = "docker-compose.uploading.yml"
        REGISTRY_CONTAINER_NAME = "adminserviceregistry"
        TARGET_SERVICE = "uploadingservice"
        TARGET_CONTAINER_NAME = "configurationservice" // container_name from your compose file
        TARGET_IMAGE_NAME = "configuration:latest" // image name from your compose file
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Version') {
            steps {
                sh 'docker --version'
                sh 'docker compose version'
            }
        }

        stage('Check Registry and Run Service') {
            steps {
                script {
                    def isRegistryRunning = sh(
                        script: "docker ps -q -f name=${REGISTRY_CONTAINER_NAME}",
                        returnStdout: true
                    ).trim()

                    if (isRegistryRunning) {
                        echo "${REGISTRY_CONTAINER_NAME} is running. Proceeding to build and start ${TARGET_SERVICE}..."

                        // Remove existing container
                        sh "docker rm -f ${TARGET_CONTAINER_NAME} || true"

                        // Remove existing image
                        sh "docker rmi -f ${TARGET_IMAGE_NAME} || true"

                        // Build and run the service
                        sh "docker compose -f ${COMPOSE_FILE} build ${TARGET_SERVICE}"
                        sh "docker compose -f ${COMPOSE_FILE} up -d ${TARGET_SERVICE}"
                    } else {
                        error "${REGISTRY_CONTAINER_NAME} is not running. Aborting deployment of ${TARGET_SERVICE}."
                    }
                }
            }
        }
    }

    post {
        always {
            echo '✅ Pipeline execution completed.'
        }
    }
}
