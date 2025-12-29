pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        // App & Registry Config
        APP_NAME       = "devops-taskmaster"
        AWS_ACCOUNT_ID = "123456789012"
        AWS_REGION     = "us-east-1"
        ECR_REPO       = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${APP_NAME}"
        DOCKERHUB_REPO = "jonathan661/${APP_NAME}"
        SCANNER_HOME   = tool 'sonar-scanner'

        // Jenkins Credentials IDs
        SONAR_TOKEN_ID = 'sonar-token'
        DOCKER_CREDS   = 'docker-cred'
        AWS_CREDS      = 'aws-ecr-creds'
        GIT_CREDS      = 'git-cred'
        K8S_CREDS      = 'k8-cred'
    }

    stages {

        stage('Initialize & Cleanup') {
            steps {
                cleanWs()
                git branch: 'develop',
                    credentialsId: "${GIT_CREDS}",
                    url: 'https://github.com/tambe-jonathan/java-devsecops-platform-end-to-end.git'
            }
        }

        stage('Compile & Test') {
            steps {
                sh "mvn -f app/pom.xml clean compile test"
            }
        }

        stage('Security: Trivy FS Scan') {
            steps {
                // Informational scan (does not fail build)
                sh "trivy fs --exit-code 0 --severity HIGH,CRITICAL ."
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh """
                    ${SCANNER_HOME}/bin/sonar-scanner \
                      -Dsonar.projectName=${APP_NAME} \
                      -Dsonar.projectKey=${APP_NAME} \
                      -Dsonar.java.binaries=.
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build & Archive: Nexus') {
            steps {
                withMaven(
                    globalMavenSettingsConfig: 'global-settings',
                    jdk: 'jdk17',
                    maven: 'maven3'
                ) {
                    sh "mvn -f app/pom.xml package deploy -DskipTests"
                }
            }
        }

        stage('Containerize: Docker Build') {
            steps {
                script {
                    sh "docker build -t ${APP_NAME}:${BUILD_NUMBER} -f app/Dockerfile ."
                    sh "docker tag ${APP_NAME}:${BUILD_NUMBER} ${ECR_REPO}:latest"
                    sh "docker tag ${APP_NAME}:${BUILD_NUMBER} ${ECR_REPO}:${BUILD_NUMBER}"
                    sh "docker tag ${APP_NAME}:${BUILD_NUMBER} ${DOCKERHUB_REPO}:latest"
                }
            }
        }

        stage('Security: Trivy Image Scan') {
            steps {
                // Fails build on CRITICAL vulnerabilities
                sh "trivy image --exit-code 1 --severity CRITICAL ${DOCKERHUB_REPO}:latest"
            }
        }

        stage('Publish: ECR & DockerHub') {
            steps {
                script {

                    // Push to AWS ECR
                    withAWS(credentials: "${AWS_CREDS}", region: "${AWS_REGION}") {
                        sh """
                        aws ecr get-login-password --region ${AWS_REGION} |
                        docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                        """
                        sh "docker push ${ECR_REPO}:latest"
                        sh "docker push ${ECR_REPO}:${BUILD_NUMBER}"
                    }

                    // Push to DockerHub
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${DOCKER_CREDS}",
                            usernameVariable: 'DUSER',
                            passwordVariable: 'DPASS'
                        )
                    ]) {
                        sh "echo ${DPASS} | docker login -u ${DUSER} --password-stdin"
                        sh "docker push ${DOCKERHUB_REPO}:latest"
                    }
                }
            }
        }

        stage('Deploy: Kubernetes') {
            steps {
                withKubeConfig(
                    credentialsId: "${K8S_CREDS}",
                    serverUrl: 'https://172.31.15.201:6443'
                ) {
                    // Ensure namespace exists (idempotent)
                    sh "kubectl get ns webapp >/dev/null 2>&1 || kubectl create ns webapp"

                    // Deploy & restart
                    sh "kubectl apply -f deployment.yaml"
                    sh "kubectl rollout restart deployment ${APP_NAME} -n webapp"
                    sh "kubectl get pods -n webapp"
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished. Cleaning up workspace..."
            cleanWs()
        }

        success {
            mail to: 'jonathanta2023@gmail.com',
                 subject: "SUCCESS: ${APP_NAME} Build #${BUILD_NUMBER}",
                 body: """The DevOps Taskmaster has been successfully deployed to Kubernetes.
View Build: ${env.BUILD_URL}"""
            echo "Successfully deployed ${APP_NAME} build #${BUILD_NUMBER}"
        }

        failure {
            mail to: 'jonathanta2023@gmail.com',
                 subject: "FAILURE: ${APP_NAME} Build #${BUILD_NUMBER}",
                 body: """The pipeline failed.
Check SonarQube, Trivy, or Jenkins logs immediately.
View Build: ${env.BUILD_URL}"""
            echo "Pipeline failed. Review SonarQube, Trivy, or Jenkins logs."
        }
    }
}
