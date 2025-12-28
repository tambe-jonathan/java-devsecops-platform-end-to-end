```
#!/bin/bash

# Update package manager repositories
sudo apt-get update

# Install necessary dependencies
sudo apt-get install -y ca-certificates curl

# Create directory for Docker GPG key
sudo install -m 0755 -d /etc/apt/keyrings

# Download Docker's GPG key
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc

# Ensure proper permissions for the key
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add Docker repository to Apt sources
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
$(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Update package manager repositories
sudo apt-get update

sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin 

```
---
Save this script in a file, for example, ** install_docker.sh,** and make it executable using:
```
chmod +x install_docker.sh
```
Then, you can run the script using:
```
./install_docker.sh
```

Run the command to install jenkins as a docker container
```
docker run -d -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -v $(which docker):/usr/bin/docker jenkins/jenkins:lts
```

## to access jenkins on the browser, we open port 8080 on the VM
 ### to see ip address of VM do
 
```
curl ifconfig.io
```

on the browser run (insert the copied ip from the ifconfig command above)


> ip_address of VM copied above:8080 

to check initial password we do


> docker logs "containerID"

copy initial paswword and enter it on the password required window,
select sugested plugins,
enter admin credentials. On the jenkins Dashboard, click "Manage Jenkins" then click "Plugins". Under plugins click "Available plugins", select all required plugins and click "Install
