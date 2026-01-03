pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        // Application
        APP_NAME = "devops-taskmaster"

        // AWS / ECR
        AWS_ACCOUNT_ID = "891377318635"
        AWS_REGION     = "us-east-1"
        ECR_REPO       = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${APP_NAME}"

        // DockerHub
        DOCKERHUB_REPO = "jonathan661/${APP_NAME}"

        // SonarQube
        SCANNER_HOME = tool 'sonar-scanner'

        // Jenkins Credentials
        AWS_CREDS    = "aws-ecr-creds"
        DOCKER_CREDS = "dockerhub-creds"
        GIT_CREDS    = "git-cred"
        K8S_CREDS   = "k8-cred"
    }

    stages {

        stage('Checkout') {
            steps {
                cleanWs()
                git branch: 'develop',
                    credentialsId: "${GIT_CREDS}",
                    url: 'https://github.com/tambe-jonathan/java-devsecops-platform-end-to-end.git'
            }
        }

        stage('Compile & Test') {
            steps {
                sh 'mvn -f app/pom.xml clean compile test'
            }
        }

        stage('Security: Trivy FS Scan') {
            steps {
                sh 'trivy fs --exit-code 0 --severity HIGH,CRITICAL .'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh """
                      ${SCANNER_HOME}/bin/sonar-scanner \
                      -Dsonar.projectName=devops-taskmaster \
                      -Dsonar.projectKey=devops-taskmaster \
                      -Dsonar.sources=app/src/main/java \
                      -Dsonar.java.binaries=app/target/classes'
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

        stage('Build & Publish Artifact (Nexus)') {
            steps {
                withMaven(
                    globalMavenSettingsConfig: 'global-settings',
                    jdk: 'jdk17',
                    maven: 'maven3'
                ) {
                    sh 'mvn -f app/pom.xml package deploy -DskipTests'
                }
            }
        }

        stage('Docker Build & Tag') {
            steps {
                sh """
                  docker build -t ${APP_NAME}:${BUILD_NUMBER} -f app/Dockerfile .
                  docker tag ${APP_NAME}:${BUILD_NUMBER} ${ECR_REPO}:latest
                  docker tag ${APP_NAME}:${BUILD_NUMBER} ${ECR_REPO}:${BUILD_NUMBER}
                  docker tag ${APP_NAME}:${BUILD_NUMBER} ${DOCKERHUB_REPO}:latest
                """
            }
        }

        stage('Security: Trivy Image Scan') {
            steps {
                sh "trivy image --exit-code 1 --severity CRITICAL ${DOCKERHUB_REPO}:latest"
            }
        }

        stage('Push Images (ECR & DockerHub)') {
            steps {
                script {

                    // Push to AWS ECR
                    withAWS(credentials: "${AWS_CREDS}", region: "${AWS_REGION}") {
                        sh """
                          aws ecr get-login-password --region ${AWS_REGION} |
                          docker login --username AWS --password-stdin \
                          ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

                          docker push ${ECR_REPO}:latest
                          docker push ${ECR_REPO}:${BUILD_NUMBER}
                        """
                    }

                    // Push to DockerHub
                    withCredentials([
                        usernamePassword(
                            credentialsId: "${DOCKER_CREDS}",
                            usernameVariable: 'DUSER',
                            passwordVariable: 'DPASS'
                        )
                    ]) {
                        sh """
                          echo \$DPASS | docker login -u \$DUSER --password-stdin
                          docker push ${DOCKERHUB_REPO}:latest
                        """
                    }
                }
            }
        }

        stage('Deploy: Kubernetes') {
            steps {
                withCredentials([file(credentialsId: "${K8S_CREDS}", variable: 'KUBECONFIG')]) {
                    sh '''
                      export KUBECONFIG=$KUBECONFIG

                      kubectl get nodes
                      kubectl get ns webapp >/dev/null 2>&1 || kubectl create ns webapp

                      kubectl apply -f deployment.yaml
                      kubectl rollout restart deployment devops-taskmaster -n webapp
                      kubectl get pods -n webapp
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }

        success {
            mail to: 'jonathanta2023@gmail.com',
                 subject: "SUCCESS: ${APP_NAME} Build #${BUILD_NUMBER}",
                 body: """SUCCESS 🚀

Application: ${APP_NAME}
Build Number: ${BUILD_NUMBER}

The application has been successfully:
- Built
- Scanned (SonarQube & Trivy)
- Published (Nexus, ECR, DockerHub)
- Deployed to Kubernetes

Build URL:
${env.BUILD_URL}
"""
        }

        failure {
            mail to: 'jonathanta2023@gmail.com',
                 subject: "FAILURE: ${APP_NAME} Build #${BUILD_NUMBER}",
                 body: """FAILURE ❌

Application: ${APP_NAME}
Build Number: ${BUILD_NUMBER}

The pipeline failed.
Check the Jenkins console output immediately.

Common failure points:
- SonarQube Quality Gate
- Trivy CRITICAL vulnerabilities
- ECR/DockerHub authentication
- Kubernetes image pull

Build URL:
${env.BUILD_URL}
"""
        }
    }
}
