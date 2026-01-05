# java-devsecops-platform-end-to-end
Enterprise-grade DevSecOps Lifecycle for a Java Microservice. Features a hardened CI/CD pipeline (Jenkins, SonarQube, Trivy), Artifact Management (Artifactory, ECR), and a high-availability deployment to Kubeadm/AWS EKS Cluster with GitOps (ArgoCD), Observability (Prometheus/Grafana), and K8s Security Hardening.
# Enterprise DevSecOps Platform: taskmaster-v2
[![Build Status](https://img.shields.io/badge/Jenkins-Pipeline-blue?style=for-the-badge&logo=jenkins)](https://jenkins.io)
[![Security: Trivy](https://img.shields.io/badge/Security-Trivy_Enabled-green?style=for-the-badge&logo=trivy)](https://aquasecurity.github.io/trivy/)
[![Code Quality: SonarQube](https://img.shields.io/badge/Quality-SonarQube_Passed-brightgreen?style=for-the-badge&logo=sonarqube)](https://sonarqube.org)

An automated, high-availability CI/CD ecosystem designed for a Java-based microservice architecture. This platform implements a "Shift-Left" security strategy, ensuring code quality, vulnerability management, and immutable deployment patterns.

---

## 🏗 System Architecture & Workflow
The platform orchestrates a multi-stage lifecycle across distributed infrastructure (AWS ECR, Nexus, DockerHub, and Kubernetes).

1.  **Continuous Integration:** Maven-based lifecycle with automated Unit Testing.
2.  **Statutory Security (Shift-Left):** Dual-layer scanning (Filesystem + Image) via Trivy.
3.  **Static Analysis:** Deep-code inspection using SonarQube Quality Gates.
4.  **Artifact Governance:** Parallel publishing to Nexus Repository Manager.
5.  **Immutable Image Distribution:** Multi-registry synchronization (AWS ECR & DockerHub).
6.  **Continuous Deployment:** Automated Rolling Update on MicroK8s (AWS-backed).



---

## 🛠 Tech Stack
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

## 🏗️ Prerequisites & Infrastructure

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

## 🔐 Jenkins Credential Configuration

Configure the following IDs exactly as listed in **Manage Jenkins > Credentials** to ensure pipeline compatibility:

| Credential ID | Type | Description |
| :--- | :--- | :--- |
| `github-creds` | Username/Password | GitHub credentials or Personal Access Token (PAT). |
| `docker-cred` | Username/Password | Docker Hub credentials (**User: jonathan661**). |
| `sonar-token` | Secret Text | Auth token generated in SonarQube settings. |
| `k8-cred` | Secret File | A clean `kubeconfig` file (see setup below). |

---

## 🛠️ Critical Configurations

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

## 🔐 Enterprise Prerequisites & Configuration
To ensure pipeline stability, the following configurations are strictly required:

### 1. Global Tool Configurations (Jenkins)
Ensure the following tools are defined in **Manage Jenkins > Global Tool Configuration**:
* **JDK:** Name: `jdk17`
* **Maven:** Name: `maven3`
* **Sonar Scanner:** Name: `sonar-scanner`

### 2. Required Jenkins Credentials
| ID | Type | Role |
| :--- | :--- | :--- |
| `github-creds` | Username/Password | Git checkout authentication |
| `aws-ecr-creds` | AWS Credentials | ECR Image push/pull |
| `dockerhub-creds` | Username/Password | DockerHub Image synchronization |
| `k8-cred` | **Secret File** | Clean YAML `kubeconfig` with TLS-bypass |
| `global-settings` | Managed File | Maven `settings.xml` for Nexus authentication |

---

## 🚀 Execution Proofs

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

## 📋 Troubleshooting & Maintenance

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
