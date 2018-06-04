#!/bin/bash

# JAVA 8
sudo apt-get update -y
sudo apt-get upgrade -y
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update -y
sudo apt-get install oracle-java8-installer
sudo apt install oracle-java8-set-default
java -version
#javac -version

# MAVEN
cd /opt/
sudo wget http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
sudo tar -xvzf apache-maven-3.3.9-bin.tar.gz
sudo mv apache-maven-3.3.9 maven 
sudo nano /etc/profile.d/mavenenv.sh
# export M2_HOME=/opt/maven
# export PATH=${M2_HOME}/bin:${PATH}
sudo chmod +x /etc/profile.d/mavenenv.sh
sudo source /etc/profile.d/mavenenv.sh
mvn --version
cd ~

# Scala
 sudo apt-get remove scala-library scala
 #sudo wget www.scala-lang.org/files/archive/scala-2.11.8.deb
 #sudo dpkg -i scala-2.11.8.deb
 sudo wget www.scala-lang.org/files/archive/scala-2.10.6.deb
 sudo apt-get -f install
 sudo dpkg -i scala-2.10.6.deb
 
# SBT - Maybe unnecessary
 echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
 sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
 sudo apt-get update
 sudo apt-get install sbt
 
# Thrift 0.7.0 - NOT WORKING
sudo apt-get install ant
sudo apt-get install libboost-dev libboost-test-dev libboost-program-options-dev libboost-filesystem-dev libboost-thread-dev libevent-dev automake libtool flex bison pkg-config g++ libssl-dev
sudo apt-get install libssl-dev libboost-dev flex bison g++
wget https://archive.apache.org/dist/thrift/0.7.0/thrift-0.7.0.tar.gz
tar -zxvf thrift-0.7.0.tar.gz
cd thrift-0.7.0
chmod u+x configure install-sh
./configure --prefix=${HOME}/project --exec-prefix=${HOME}/project --with-python=no --with-erlang=no --with-java=no --with-php=no --with-csharp=no --with-ruby=no
sudo make
sudo make install
cd ..

# protobuf 2.5.0
wget clone https://github.com/google/protobuf/releases/download/v2.5.0/protobuf-2.5.0.tar.gz
tar -zxvf protobuf-2.5.0.tar.gz
sudo apt-get update
sudo apt-get install build-essential 
cd protobuf-2.5.0
./configure
sudo make
sudo make check
sudo make install
protoc --version
cd ..

# Import parquet-format, parquet-mr, apache 
cd 182-231/parquet-format
mvn clean install -DskipTests
cd ..
cd 182-231/parquet-mr
mvn clean install -DskipTests
cd ..
cd spark
./build/mvn -DskipTests clean package

# shared folder command in VirtualBOX, adding user to vboxsf group
# sudo usermod -a -G vboxsf user #<-change to user name