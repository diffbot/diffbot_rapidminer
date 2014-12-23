#!/bin/bash
#Sets up the project to work with using the RAPIDMINER_HOME environment variable (pointing to an existing Rapidminer Studio 6.1 installation).
#It also needs the RapidMiner 5.3.15 sources for development. The suggested branch is: https://github.com/aborg0/rapidminer/compare/extension_java7
#It expects the 5.3.15 sources to be found in the folder pointed by the RM_SOURCES environment variable.
#You might want to start it with RM_SOURCES=$HOME/git/rapidminer RAPIDMINER_HOME=$HOME/rapidminer-studio ./setup.sh command.

#Custom Rapidminer folder
#For RapidMiner 6.2.0, you might change this to RM_62 to avoid confusion
RM61=RM_61

PWD=`pwd`
cd ..
mkdir $RM61
cd $RM61
mkdir lib
ln -s $RM_SOURCES/ant ant
ln -s $RAPIDMINER_HOME/lib/*.jar lib/
#For RapidMiner 6.2:
#ln -s $RAPIDMINER_HOME/lib/rapidminer-studio-core-6.2.0.jar lib/rapidminer.jar
#For RapidMiner 6.1:
ln -s $RAPIDMINER_HOME/lib/rapidminer-studio-core-6.1.0.jar lib/rapidminer.jar
ln -s $RAPIDMINER_HOME/lib/jdbc lib/jdbc
mkdir lib/freehep
ln -s $RAPIDMINER_HOME/lib/freehep*.jar lib/freehep/
mkdir lib/plugins
ln -s $RAPIDMINER_HOME/lib/plugins/*.jar lib/plugins
mkdir text_lib
#or alternatively you can download it from rapidminer.com
ln -s $HOME/.RapidMiner/managed/rmx_text*.jar text_lib
ln -s $HOME/.RapidMiner/managed/rmx_text*.jar lib/plugins
cp $HOME/.RapidMiner/managed/extensions.xml text_lib/extensions.xml
ln -s $RM_SOURCES/build* .

echo "You can import $RM61 to eclipse as a Java project (with all jars as a depencency), but it is not required."
echo "If you decide to add as a Java project, create a run config with com.rapidminer.launcher.GUILauncher"

cd $PWD
