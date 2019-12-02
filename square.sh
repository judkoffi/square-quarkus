#!bin/bash 
echo "============== Install docker ==============" 
apt-get update
apt-get install apt-transport-https ca-certificates curl gnupg2 software-properties-common
curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"
apt-get update
apt-get install docker.io
systemctl status docker
usermod -aG docker ${USER}
echo "============================================="
echo "============== Install mvn ==============" 
apt-get install maven
echo "============================================="
echo "============== Install JDK ==============" 
apt-get install openjdk-11-jdk
echo "============================================="
echo "============== Set JDK HOME ==============" 
export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")
echo "============================================="
echo "============== Install git ==============" 
apt-get install git
echo "============================================="
echo "============================================="
echo "============== pull depo ================"
git clone https://gitlab.com/esipe-info2019/dacosta-koffi.git
echo "============================================="
echo "=================== cd dacosta-koffi/ =============="
cd dacosta-koffi/
echo "============================================="