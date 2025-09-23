pipeline {
    agent any

    environment {
        IMAGE_NAME = "registry"
        CONTAINER_NAME = "adminserviceregistry"
        DOCKER_NETWORK = "updated_orgadmin_rmscadminnetwork"
        HOST_PORT = "8761"
        CONTAINER_PORT = "8761"
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
            }
        }

        stage('Clean Old Docker Image') {
            steps {
                echo "Removing old Docker image if it exists..."
                sh "docker rmi -f ${IMAGE_NAME}:latest || true"
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image..."
                sh "docker build -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Stop & Remove Old Container') {
            steps {
                sh """
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true
                """
            }
        }

        stage('Run Docker Container') {
            steps {
                sh """
                    docker run -d --name ${CONTAINER_NAME} \\
                        -p ${HOST_PORT}:${CONTAINER_PORT} \\
                        --network ${DOCKER_NETWORK} \\
                        -e EUREKA_SERVER_ENABLE_SELF_PRESERVATION=false \\
                        -e EUREKA_SERVER_EVICTION_INTERVAL_TIMER_IN_MS=5000 \\
                        -e EUREKA_CLIENT_REGISTER_WITH_EUREKA=false \\
                        -e EUREKA_CLIENT_FETCH_REGISTRY=false \\
                        ${IMAGE_NAME}:latest
                """
            }
        }
    }

    post {
        always {
            echo '✅ Pipeline execution completed.'
        }
    }
}
