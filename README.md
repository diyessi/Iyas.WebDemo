# Iyas.WebDemo
Web demo of Iyas CQA

To build, you must first install Iyas.mini
cd Iyas.mini
mvn -Dmaven.test.skip=true install

Then package the demo
cd Iyas.WebDemo
mvn -Dmaven.test.skip=true package

The .war file will be in target/WebDemo.war
