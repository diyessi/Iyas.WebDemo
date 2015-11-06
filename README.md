# Iyas.WebDemo
Web demo of Iyas CQA

To build, you must first install Iyas.mini
cd Iyas.mini
mvn -Dmaven.test.skip=true install

Then package the demo
cd Iyas.WebDemo
mvn -Dmaven.test.skip=true package

The .war file will be in target/WebDemo.war

You will need to create a .ivy2 directory for the tomcat7 user.
```
sudo bash
cd ~tomcat7
mkdir .ivy2
chown tomcat7.tomcat7 .ivy2
```
