# Iyas.WebDemo
Web demo of Iyas CQA

To build, you must first install Iyas.mini:
```
cd Iyas.mini
mvn -Dmaven.test.skip=true install
```
Next put the models in Iyas.WebDemo as shown here:
```
src/main/resources/
src/main/resources/stoplist-en.txt
src/main/resources/data
src/main/resources/data/SemEval2015-Task3-English-data
src/main/resources/data/SemEval2015-Task3-English-data/datasets
src/main/resources/data/SemEval2015-Task3-English-data/datasets/emnlp15
src/main/resources/data/SemEval2015-Task3-English-data/datasets/emnlp15/CQA-QL-train.xml.klp.model
src/main/resources/qatarliving
src/main/resources/qatarliving/questionInfo.map
src/main/resources/qatarliving/urlMapping.map
```

Then package the demo:
```
cd Iyas.WebDemo
mvn -Dmaven.test.skip=true package
```
The .war file will be in target/WebDemo.war

You will need to create a .ivy2 directory for the tomcat7 user.
```
sudo bash
cd ~tomcat7
mkdir .ivy2
chown tomcat7.tomcat7 .ivy2
```
