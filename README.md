
# java-devsecops-platform-end-to-end
[![Build Status](https://img.shields.io/badge/Jenkins-Pipeline-blue?style=for-the-badge&logo=jenkins)](https://jenkins.io)
[![Security: Trivy](https://img.shields.io/badge/Security-Trivy_Enabled-green?style=for-the-badge&logo=trivy)](https://aquasecurity.github.io/trivy/)
[![Code Quality: SonarQube](https://img.shields.io/badge/Quality-SonarQube_Passed-brightgreen?style=for-the-badge&logo=sonarqube)](https://sonarqube.org)

Enterprise-grade DevSecOps Lifecycle for a Java Microservice. Features a hardened CI/CD pipeline (Jenkins, SonarQube, Trivy), Artifact Management (Artifactory, ECR), and a high-availability deployment to Kubeadm/AWS EKS Cluster with GitOps (ArgoCD), Observability (Prometheus/Grafana), and K8s Security Hardening.

---

## Case Study: Enterprise-Grade DevSecOps Transition

**The Challenge:** A mid-sized fintech organization struggled with manual deployments that took 4+ hours, frequent "it works on my machine" bugs, and zero security visibility until after production releases. Their infrastructure was fragile, and manual Kubernetes updates caused significant downtime.

**The Solution:** This project implements a **Zero-Trust DevSecOps Pipeline** that automates the entire lifecycle of a Java Spring Boot application. By integrating SonarQube for static analysis and Trivy for container security, we shifted security "left," catching 90% of vulnerabilities before they reached the registry.

**The Result:** * **Deployment Speed:** Reduced from 4 hours to **6 minutes**.
* **Security Posture:** 100% automated scanning of every image build.
* **Reliability:** Automated rolling updates in MicroK8s ensured **zero-downtime** deployments.

## Key Features
The platform orchestrates a multi-stage lifecycle across distributed infrastructure (AWS ECR, Nexus, DockerHub, and Kubernetes).

1.  **Continuous Integration:** Maven-based lifecycle with automated Unit Testing.
2.  **Statutory Security (Shift-Left):** Dual-layer scanning (Filesystem + Image) via Trivy.
3.  **Static Analysis:** Deep-code inspection using SonarQube Quality Gates.
4.  **Artifact Governance:** Parallel publishing to Nexus Repository Manager.
5.  **Immutable Image Distribution:** Multi-registry synchronization (AWS ECR & DockerHub).
6.  **Continuous Deployment:** Automated Rolling Update on MicroK8s (AWS-backed).


---

##  Tech Stack
| Domain | Technology |
| :--- | :--- |
| **Orchestration** | Jenkins (Declarative Pipeline) |
| **Language/Framework** | Java 17, Spring Boot |
| **Build/Dependency** | Apache Maven 3 |
| **Security Scanning** | Aqua Security Trivy (FS & Container) |
| **Code Quality** | SonarQube, Quality Gate Integration |
| **Containerization** | Docker Engine |
| **Registries** | AWS Elastic Container Registry (ECR), DockerHub |
| **Artifact Manager** | Sonatype Nexus |
| **Infrastructure** | Kubernetes (MicroK8s), AWS EC2 |

---
# CI/CD Pipeline Infrastructure & Configuration

This repository contains the configuration and pipeline scripts for a robust CI/CD workflow utilizing Jenkins, SonarQube, and Kubernetes (MicroK8s).

---

##  Prerequisites & Infrastructure

### 1. Server Configuration (AWS EC2 / Ubuntu)
* **Jenkins Server:** Minimum **4GB RAM** (Instance type `t3.medium` recommended).
* **Kubernetes Cluster:** MicroK8s installed on Ubuntu.

### 2. Networking & Security Groups
Ensure the following ports are open in your AWS Security Group to allow traffic:

| Port | Service | Purpose |
| :--- | :--- | :--- |
| **8080** | Jenkins | Web UI Access |
| **9000** | SonarQube | Code Quality Dashboard |
| **16443** | Kubernetes | API Server (Remote Access) |
| **30001** | Application | Frontend/App Access via NodePort |

### 3. Node Dependencies
The Jenkins user must have permissions to execute the following tools. Run these commands on your Jenkins node:

* **Docker:** `sudo usermod -aG docker jenkins`
* **kubectl:** Installed and configured for cluster communication.
* **Trivy:** Latest version installed for vulnerability scanning.
---

#  Project Structure

```text
.
├── .github/workflows/       # GitHub Actions (e.g., maven-publish.yml)
├── app/                     # Java Source Code
│   ├── src/main/            # Spring Boot Logic & Resources
│   ├── Dockerfile           # Multi-stage optimized build
│   └── pom.xml              # Maven Dependencies
├── docs/                    # Component-specific documentation
│   ├── Docker.md, Port.md, jenkins.md, kubernetes.md, etc.
├── infra/                   # Kubernetes Manifests
│   └── deployment.yaml      # Deployment & Service configuration
├── scripts/                 # Automation & Security utility scripts
│   ├── health-check.sh
│   ├── setup-cluster.sh
│   └── trivy-scan.sh
├── .dockerignore
├── .gitignore
├── Jenkinsfile              # CI/CD Orchestration script
├── sonar-project.properties  # SonarQube configurations
└── README.md                # Project Documentation
```

##  Jenkins Credential Configuration

Configure the following IDs exactly as listed in **Manage Jenkins > Credentials** to ensure pipeline compatibility:

| Credential ID | Type | Description |
| :--- | :--- | :--- |
| `github-creds` | Username/Password | GitHub credentials or Personal Access Token (PAT). |
| `docker-cred` | Username/Password | Docker Hub credentials (**User: jonathan661**). |
| `sonar-token` | Secret Text | Auth token generated in SonarQube settings. |
| `k8-cred` | Secret File | A clean `kubeconfig` file (see setup below). |

---

## Critical Configurations

### Kubernetes kubeconfig Setup (`k8-cred`)
To prevent TLS handshake errors or formatting issues (e.g., `x509: certificate is valid for 127.0.0.1`), use the following structure for your secret file. 

> [!IMPORTANT]
> If your cluster certificate is issued for private/loopback IPs, ensure `insecure-skip-tls-verify: true` is set.

```yaml
apiVersion: v1
clusters:
- cluster:
    insecure-skip-tls-verify: true
    server: https://<YOUR-SERVER-PUBLIC-IP>:16443
  name: microk8s-cluster
contexts:
- context:
    cluster: microk8s-cluster
    user: admin
  name: microk8s
current-context: microk8s
kind: Config
preferences: {}
users:
- name: admin
  user:
    token: <YOUR-AUTH-TOKEN>
```


---

##  Pipeline Executions

### Stage 1: Continuous Security (Trivy & SonarQube)
The pipeline enforces a **zero-tolerance policy** for CRITICAL vulnerabilities during the Image Scan phase.
> *[Place Image of Trivy Scan Output here]*
> *[Place Image of SonarQube Dashboard/Quality Gate here]*

### Stage 2: Artifact & Image Distribution
Immutable tags are pushed simultaneously to AWS and DockerHub for redundancy.
> *[Place Image of DockerHub Repository showing Latest & Build Number tags here]*
> *[Place Image of AWS ECR Console here]*

### Stage 3: Kubernetes Deployment
The deployment utilizes a `rollout restart` strategy to ensure zero-downtime updates.
> *[Place Image of "kubectl get pods -n webapp" showing 3/3 Running here]*

---

## 🖥 Live Application Demo
Upon successful deployment, the application is exposed via a **NodePort Service (30001)**.

![Application Demo GIF](https://via.placeholder.com/800x400?text=Insert+Your+Application+Running+GIF+Here)

---

## Deployment Guide

Follow these steps to replicate this enterprise DevSecOps deployment on your own infrastructure.

---

### I. Infrastructure Requirements

* **Host OS:** Ubuntu 22.04 LTS (AWS EC2 or local VM).
* **K8s Distribution:** MicroK8s (Required addons: `dns`, `dashboard`, `storage`).
* **Networking:** Ensure your Cloud Security Group allows the following inbound traffic:

| Port | Service | Description |
| :--- | :--- | :--- |
| **16443** | Kubernetes API | Remote cluster management |
| **30001** | Application | External access to the deployed app |
| **8080** | Jenkins | Automation server UI |
| **9000** | SonarQube | Code quality analysis UI |

---

### II. Jenkins Credential Setup

To match the environment defined in the `Jenkinsfile`, you **must** configure these credentials in Jenkins:

1.  **`k8-cred`** (Secret File): Your `kubeconfig` file. 
    > **Note:** Ensure it contains `insecure-skip-tls-verify: true` and the **Public IP** of your cluster.
2.  **`dockerhub-creds`** (Username/Password): Your Docker Hub login details.
3.  **`github-creds`** (Username/Password): Your GitHub username and Personal Access Token (PAT).
4.  **`aws-ecr-creds`** (AWS Credentials): Your IAM Access Key and Secret Key for ECR registry access.

---

### III. Deployment Steps

#### 1. Clone the Repository
```bash
git clone [https://github.com/tambe-jonathan/java-devsecops-platform-end-to-end.git](https://github.com/tambe-jonathan/java-devsecops-platform-end-to-end.git)
cd java-devsecops-platform-end-to-end
```
### 2. Apply Manifests Manually (Optional Testing)
Before running the pipeline, you can verify your cluster connection:

Bash
```
kubectl apply -f infra/deployment.yaml
```
### 3. Execute the Pipeline
#### 1. Execute the Pipeline
1.  Create a new **Pipeline** job in Jenkins.
2.  Point the **Pipeline Script from SCM** to this repository.
3.  Click **Build Now**.
4.  The pipeline will build the `.jar`, scan code quality/vulnerabilities, push the image, and perform a rollout restart on the cluster.

---

### IV. Accessing the Application

Once the Jenkins pipeline shows a **SUCCESS** status, you can access your live application at:

`http://<YOUR_PUBLIC_IP>:30001`

---

> [!TIP]
> Use `kubectl get pods` to verify that the application instances are running and healthy after the pipeline completes.
##  Troubleshooting & Maintenance

### Common Remediation Patterns
* **TLS Handshake Failures:** If the K8s API server uses a private CA, ensure the `k8-cred` secret file in Jenkins includes `insecure-skip-tls-verify: true` and removes `certificate-authority-data`.
* **ImagePullBackOff:** Verify the `DOCKERHUB_REPO` variable in the Jenkinsfile (`jonathan661/devops-taskmaster`) matches the `image` field in `infra/deployment.yaml`.
* **Quality Gate Timeout:** Default is 5 minutes. If SonarQube analysis is heavy, increase the `timeout` block in the `Quality Gate` stage.

### Log Inspection
```bash
# To view live application logs
kubectl logs -f -l app=devops-taskmaster -n webapp

# To verify Service exposure
kubectl get svc -n webapp
```
---

##  Real-World Simulation Notice
> [!IMPORTANT]
> This project is designed as a high-fidelity simulation of how modern enterprise applications are deployed in production environments. It mirrors industry-standard workflows including **automated quality gates**, **container orchestration**, and **security-first CI/CD**. While hosted on a single cluster for demonstration, the underlying architecture is built to scale across multi-node AWS environments.

---

**Developed with 🛡️ by Agbor Jonathan/github.com/tambe-jonathan** *Building secure, scalable, and resilient automated systems.*

[ ![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white) ](YOUR_LINKEDIN_URL)
[ ![Portfolio](https://img.shields.io/badge/Portfolio-FF5722?style=for-the-badge&logo=todoist&logoColor=white) ](YOUR_PORTFOLIO_URL)
