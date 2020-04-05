## Overview

This project includes everything necessary to recreate the simulations from the papers:

Bracis, C., Gurarie, E., Rutter, J. D., & Goodwin, R. A. (2018). Remembering the good and the bad: memory-based mediation of the food–safety trade-off in dynamic landscapes. Theoretical Ecology, 1-15.

Bracis, C., Gurarie, E., Van Moorter, B., & Goodwin, R. A. (2015). Memory effects on movement behavior in animal foraging. PloS one, 10(8), e0136057.

Included are the model source code, the landscape files, and the properties files specifying model parameters.


## Directory structure

The model is written in Java and uses a standard directory structure (bin, distrib, lib, src, test). The parameters of the model are set via a .properties file, examples of which are in the batch directory. Landscape files are in the land directory. The project tests can be run with TestNG.


## Landscape files

Generated landscape files across a range of patch concentration (mean) and patch size (scale). Parameters used to generate the landscape files and descriptive statistics given in the file landParametersExp.csv. Note that only a subset of landscapes were used in the final paper.


## Running the model

The model can be run through the development environment (e.g., Eclipse) or from the command line (BatchRunner.sh).

### To run from the command line (using a jar file)
Steps: 
1. Install Java 1.6 or higher on the machine
2. Ensure you have the following directory structure
	BatchRunner.*
	batch
		properties files
	distrib
		ForagingModel.jar
	land
		landscape csv files
	lib
		library jar files
3. If project has not been built previously, build project and run JarProject.sh (this will create ForagingModel.jar)
4. Run BatchRunner.sh with the properties file name as an argument

To additionally be able to visualize the simulation:
5. Install R and the rJava package
6. On windows, you will likely have to modify the RJAVA variable in your version of BatchRunner.sh to point to the directory with REngine.jar (where R package installed)


### To set up development environment - Eclipse

Steps:
1. Install eclipse (standard Java version)
2. Install TestNG plugin
3. Install R and rJava package
4. Edit eclipse run configurations to set R_HOME to your R path (e.g., /Library/Frameworks/R.framework/Resources). Note this also need to be set on the command line when launching via shell script.
5. Edit run configurations in eclipse
	add VM argument for rJava (e.g., -Djava.library.path=/Library/Frameworks/R.framework/Resources/library/rJava/jri/)
	add environment variable R_HOME for R (e.g., /Library/Frameworks/R.framework/Resources)
	
#### Troubleshooting tips:
on macOS, may need to run the following:
- xcode-select --install (after xcode install or upgrade to get command line tools working)
- sudo R CMD javareconf (after java install, see https://github.com/rstudio/rstudio/issues/2254)
- system.file("jri",package="rJava") (to get location of jri to add as VM argument)

### To set up development environment - IntelliJ IDEA

Steps:
1. Install IntelliJ IDEA and JDK 1.8+
2. In IDEA, choose File | New > "Project from Existing Sources..." and pick the project directory
3. From the Import Project dialog, select "Import project from external model" and select Gradle
4. Bring up the IDEA Preferences; under "Build, Execution, Deployment" | "Build Tools" | "Gradle", make the following changes:
- Check "Automatically import this project on changes in build script files"
- Set Build and run using: IntelliJ IDEA
- Set Run tests using: IntelliJ IDEA

You can right-click the test directory and select "Run 'All Tests'" to build all and run all tests.


### To build the jar for distribution

This project uses gradle. A gradle wrapper has been included, so all gradle commands should be issued as 'gradlew'.

- `gradlew build` - Builds and runs all tests

- `gradlew assemble` - Builds but does not test

- `gradlew clean` - Removes all build artifacts

Note: Jars to distribute are in the `build/libs` directory. The 'full' jar includes all dependent libraries.
