# installation of minikube
```
  curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
  sudo install minikube-linux-amd64 /usr/local/bin/minikube
  minikube start
  sudo snap install --channel stable kubectl --classic
  snap install kubectx --classic
```
---

# Setup K8-Cluster using kubeadm [K8s Version ---> 1.28.1]

## Pre-requisite
 create three virtual machine on AWS of type t2 medium
 name the one virtual machine as **master node**
 name the two others as **workers nodes**
 switch to root user before doing the following installation
```
sudo su -
```

1. **Update System Packages [On Master & Worker Node]**

```
sudo apt-get update
```
2. **Install Docker[On Master & Worker Node]**

```
sudo apt install docker.io -y
sudo chmod 666 /var/run/docker.sock
```

3. **Install Required Dependencies for Kubernetes[On Master & Worker Node]**

```
sudo apt-get install -y apt-transport-https ca-certificates curl gpg
sudo mkdir -p -m 755 /etc/apt/keyrings   
```
4. **Add Kubernetes Repository and GPG Key[On Master & Worker Node]**

```
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list
```
5. **Update Package List[On Master & Worker Node]**

```
sudo apt update
```
6. **Install Kubernetes Components[On Master & Worker Node]**

```
sudo apt install -y kubeadm=1.28.1-1.1 kubelet=1.28.1-1.1 kubectl=1.28.1-1.1
```
7. **Initialize Kubernetes Master Node [On MasterNode]**

```
sudo kubeadm init --pod-network-cidr=10.244.0.0/16
```

8. ** Configure Kubernetes Cluster [On MasterNode]**

```
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```
9. **Deploy Networking Solution (Calico) [On MasterNode]**

```
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml
```

10. **Deploy Ingress Controller (NGINX) [On MasterNode]**

```
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.49.0/deploy/static/provider/baremetal/deploy.yaml
```

11. **Test that the cluster is working**

```
kubectl get nodes
```
