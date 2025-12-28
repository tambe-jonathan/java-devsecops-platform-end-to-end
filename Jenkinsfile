pipeline {
    agent any

    environment {
        // Registry & Project Config
        AWS_ACCOUNT_ID = "123456789012" // Replace with yours
        AWS_REGION     = "us-east-1"
        ECR_REPO       = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/devops-taskmaster"
        DOCKERHUB_REPO = "your-dockerhub-user/devops-taskmaster"
        
        // Tool IDs from Jenkins Credentials Manager
        SONAR_TOKEN_ID = 'sonar-token'
        DOCKER_CREDS   = 'docker-hub-creds'
        AWS_CREDS      = 'aws-ecr-creds'
    }

    stages {
        stage('Initialize & Cleanup') {
            steps {
                echo "Cleaning workspace for a fresh build..."
                cleanWs()
            }
        }

        stage('Quality Gate: SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube-Server') {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=devops-taskmaster'
                }
            }
        }

        stage('Artifact: Build & Artifactory') {
            steps {
                sh 'mvn clean package -DskipTests'
                // In a real corp env, you'd use the 'rtUpload' command for JFrog here
                echo "Pushing .jar to Artifactory..."
            }
        }

        stage('Security: Trivy Filesystem Scan') {
            steps {
                // Scan the source code before even building the image
                sh 'trivy fs --exit-code 0 --severity HIGH,CRITICAL .'
            }
        }

        stage('Containerize: Docker Build') {
            steps {
                sh 'docker build -t devops-taskmaster:${BUILD_NUMBER} .'
                sh 'docker tag devops-taskmaster:${BUILD_NUMBER} ${ECR_REPO}:latest'
                sh 'docker tag devops-taskmaster:${BUILD_NUMBER} ${DOCKERHUB_REPO}:latest'
            }
        }

        stage('Security: Trivy Image Scan') {
            steps {
                // This stage fails the build if the image is unsafe
                sh "trivy image --exit-code 1 --severity CRITICAL ${ECR_REPO}:latest"
            }
        }

        stage('Publish: ECR & DockerHub') {
            steps {
                // Login and Push to ECR
                withAWS(credentials: "${AWS_CREDS}", region: "${AWS_REGION}") {
                    sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}"
                    sh "docker push ${ECR_REPO}:latest"
                }
                
                // Login and Push to DockerHub
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDS}", usernameVariable: 'DUSER', passwordVariable: 'DPASS')]) {
                    sh "echo ${DPASS} | docker login -u ${DUSER} --password-stdin"
                    sh "docker push ${DOCKERHUB_REPO}:latest"
                }
            }
        }

        stage('Deploy: Kubeadm Cluster') {
            steps {
                // We assume the Jenkins user has .kube/config set up as discussed
                sh 'kubectl apply -f k8s-deployment.yaml'
                sh 'kubectl rollout restart deployment devops-demo-app'
            }
        }
    }

    post {
        success {
            echo "Successfully deployed build #${BUILD_NUMBER}"
        }
        failure {
            echo "Deployment failed. Check SonarQube or Trivy logs."
        }
    }
}
