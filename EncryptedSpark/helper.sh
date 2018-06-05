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
sudo source /etc/profile.d/mavenenv.sh //remove sudo
mvn --version
cd ~

# Scala
 sudo apt-get remove scala-library scala
 #sudo wget www.scala-lang.org/files/archive/scala-2.11.8.deb
 #sudo dpkg -i scala-2.11.8.deb
 sudo wget www.scala-lang.org/files/archive/scala-2.10.6.deb
 sudo apt-get -f install //had to do it twice? might need to do sbt first
 sudo dpkg -i scala-2.10.6.deb //or it was here? just try to install sbt first, at least up to the keyserver one
 
# SBT - Maybe unnecessary
 echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
 sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
 sudo apt-get update
 sudo apt-get install sbt
 
# Thrift 0.7.0
sudo apt-get install ant
sudo apt-get install libboost-dev libboost-test-dev libboost-program-options-dev libboost-filesystem-dev libboost-thread-dev libevent-dev automake libtool flex bison pkg-config g++ libssl-dev
sudo apt-get install libssl-dev libboost-dev flex bison g++
wget https://archive.apache.org/dist/thrift/0.7.0/thrift-0.7.0.tar.gz | tar zx
#tar -zxvf thrift-0.7.0.tar.gz
cd thrift-0.7.0
#chmod u+x configure install-sh
 sudo chmod 777 configure
#./configure --prefix=${HOME}/project --exec-prefix=${HOME}/project --with-python=no --with-erlang=no --with-java=no --with-php=no --with-csharp=no --with-ruby=no
 ./configure
sudo apt-get install python-dev
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
sudo ldconfig
protoc --version
cd ..

#alternative protobuf
#sudo apt-get install build-essential
#mkdir /tmp/protobuf_install
#cd /tmp/protobuf_install
#wget http://protobuf.googlecode.com/files/protobuf-2.5.0.tar.gz 
#tar xzvf protobuf-2.5.0.tar.gz
#cd  protobuf-2.5.0
#./configure
#make
#make check
#sudo make install
#sudo ldconfig
#protoc --version 

# under 182-231\parquet-format\src\main\java\org\apache\parquet\format there is a file called Util.java
# comment the new Exception("ERROR").printStackTrace(); lines

# Import parquet-format, parquet-mr, apache 
 tar -zxvf parq.tgz
 tar -zxvf spar-parq-encr.tgz
cd 182-231/parquet-format
mvn clean install -DskipTests
cd ..
cd parquet-mr
mvn clean install -DskipTests
cd ..
cd ..
cd spark
./build/mvn -DskipTests clean package

# shared folder command in VirtualBOX, adding user to vboxsf group
# sudo usermod -a -G vboxsf user #<-change to user name
#alternatively:
#sudo adduser <user_name> vboxsf
#then restart / re log