#!/bin/bash
# Pass the properties file as an argument to the script. It is assumed to be in the batch directory.

./JarProject.sh

MODEL_CLASSPATH=distrib/ForagingModel.jar:land:lib/commons-math3-3.2.jar:lib/commons-collections-3.2.jar:lib/concurrent.jar:lib/commons-io-2.4.jar:lib/commons-lang3-3.1.jar:lib/commons-exec-1.1.jar:lib/jdom-2.0.4.jar:lib/opencsv-2.3.jar:lib/commons-cli-1.2.jar:lib/mockito-all-1.9.5.jar:lib/commons-configuration-1.9.jar:lib/commons-lang-2.6.jar:lib/commons-logging-1.1.1.jar:lib/slf4j-api-1.7.7.jar:lib/slf4j-log4j12-1.7.7.jar:lib/log4j-1.2.17.jar

echo $(date)

# Execute in batch mode.
java -cp $MODEL_CLASSPATH -Xms512M -Xmx1024M -Djava.library.path=/Library/Frameworks/R.framework/Resources/library/rJava/jri/ ForagingModel.core.Runner -propertiesFile batch/$1 ${2:+$2}

echo $? 
echo $(date)
