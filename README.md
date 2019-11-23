# RanFog
A java program to implement Random Forest in a general framework

## Introduction
This manual describes how to use the program RanFoG, which is focused, but not restricted to, on the analysis of genomic data using random forest. Two versions are available: RanFoG_class.jar for classification problems (discrete phenotypes) and RanFoG_reg.jar for regression problems (continuous phenotypes). The user may choose the version that best adapts to her/his problem. The code is written in Java SE 7 [1], which is an object oriented multiplatform operative system, with GNU GPL license and an extense class library. The program is compiled to run in all kind of platforms (windows, linux, mac, ..) that have previously installed the java virtual machine. Please, make sure your computer can run java code, otherwise the user should have to install the latest java virtual machine available at http://www.java.com/download/. Java was chosen due to its exibility at creating and managing list and its multiplatform
characteristics.

## Purpose.
This manual does not aim to thoroughly describe Random Forest theory or methodological details behind RanFoG code,but to be a self-explanatory guide to implement RanFoG in user's own data. Random Forest theory and methods implemented in Ran-FoG can be found in [2] and [3]. The user is encouraged to consult them for details. This is a developing software, therefore any feedback on possible bugs, problems,
running errors or suggestions are welcome and encouraged.

## Part I - How to use it
### Way of execution
RanFoG must be run in a command line mode. The execution is simple, just type in the command line the following order depending on your problem category:

for classification problems:

java -jar RanFoG_class.jar t file1 file2 s

for regression problems:

java -jar RanFoG_reg.jar t file1 file2 s

t, file1, file2 and s are the four arguments that must be passed to the
program:

t is the number of trees to grow in the forest,
file1 is the training file,
file2 is the testing file, and
s is the number of covariates or SNPs that are going to be analyzed.

All arguments must be passed to the program and the order must be kept. Note that in this case the file RanFoG_class.jar or RanFoG_class.jar must be in the same folder as the training and testing files. The 'java -jar RanFog_x.jar' command will implement the neccesary classes and methods of the java virtual machine in your computer to run the compressed java code in the RanFoG program.

### Preparing files
The program needs two input files: a training set and a testing set. Inferences will be done using the training set, whereas the testing set will be used to test the predictive ability of Random Forest under the given scenario. Both files must have the same format, with p+2 columns separated by spaces. First column is the response variable (linear phenotype or disease status). Second column is the ID of the individual. Then, p columns with the genotype code of each marker, coded from 0 to the number of possible genotypes (maximum value=2). In case no predictions are neccesary, the user must still provide a testing set. Just copy a few lines of the training file to create a testing file, and use it as if it were a real testing le. Then, discard the 'Trees.test' and 'Predictions.txt' files.

### Example of regression problems:

File1

-0.333 1 2 2 1 1 0 1 1 1 0 2 1 2 1 0 1 1 0 2 1 0

-1.112 2 0 0 2 1 1 1 1 2 0 0 1 1 0 1 0 1 0 0 1 0

+1.960 3 1 2 2 1 0 0 2 2 0 1 1 1 0 1 1 2 2 2 1 0

+0.444 4 1 1 1 2 2 1 0 0 1 1 0 1 1 1 0 0 0 2 0 2

-0.451 5 1 2 0 1 2 1 2 0 0 2 1 2 2 1 1 0 0 1 1 0

File2
+0.343 2001 0 2 1 0 0 1 1 1 1 2 2 2 2 1 1 1 1 2 1 2
-0.617 2002 1 2 1 0 0 0 2 2 2 0 1 0 2 1 2 1 2 2 1 2
+0.437 2003 0 1 2 2 0 1 1 2 1 0 0 1 0 0 2 1 1 1 1 2
+0.293 2004 1 1 2 0 1 1 0 1 2 0 1 0 2 0 2 2 0 1 1 2
+2.131 2005 0 2 0 1 0 2 0 2 0 2 2 1 2 1 0 1 0 1 1 0

Execution order in this case would be:

java -jar RanFoG_reg.jar 500 file1 file2 20

Here, a regression Random Forest of 500 trees will be implemented using file1 as training set, file2 as testing set, with the first 20 SNPs or covariates. This is a screen capture image of how to run RanFog_reg.jar and the first lines of the execution:

At each iteration the program prompts the iteration number, the mean squared error in the testing set, the mean squared error in the out of bag samples (which provides an estimate for the generalization error), and the number of records in the out of bag samples.

Example of classification problems:

Disease statuts must be coded as 0=non-aected or 1=aected. Predictions from RanFoG will indicate the genetic probability of the animal to suffer the disease.

File1
outcome ID s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16 s17 s18 s19 s20
1 1 0 0 1 0 1 0 0 0 0 1 1 0 0 0 0 0 1 0 0 0
1 2 0 1 0 0 1 0 0 0 1 1 1 0 0 0 0 0 1 1 0 0
0 3 0 0 0 1 0 0 1 0 0 0 0 1 0 0 1 0 1 1 1 1
0 4 0 0 0 0 1 0 0 1 0 1 1 1 0 0 0 0 0 0 1 0
0 5 0 1 0 1 1 1 1 1 0 0 1 1 1 0 0 1 0 0 1 1

File2
outcome ID s1 s2 s3 s4 s5 s6 s7 s8 s9 s10 s11 s12 s13 s14 s15 s16 s17 s18 s19 s20
1 11 0 1 1 1 1 0 1 0 0 1 0 1 1 1 0 0 0 0 1 0
0 12 0 0 1 0 0 0 1 0 1 1 0 0 1 1 1 1 0 1 0 0
0 13 1 0 0 0 1 0 0 1 0 1 1 0 0 0 1 0 0 0 1 1
1 14 1 1 0 1 0 1 1 0 1 1 1 0 0 1 0 0 1 0 0 0
0 15 0 1 0 1 0 0 0 0 0 0 0 1 0 0 1 1 0 0 1 0

Execution order in this case would be:

java -jar RanFoG_class.jar 500 file1 file2 20

Here, a classification Random Forest of 500 trees will be implemented using file1 as training set, file2 as testing set, with the first 20 SNPs or covariates. RanFoG will prompt the following message if it completes all the interation without errors:

### Output files
RanFoG creates six output les which are organized in columns separated by spaces. Four of them are referred to inferences made on the training file: 'Variable_Importance.txt', 'TimesSelected.txt', 'Trees.txt' and 'EGBV.txt'.

-Variable_Importance.txt . This file is organized in columns ordered by covariate in the input file. First column is the order of the covariate in the input file, and second column is the variable importance. The higher the value in the second column, the more important the variable is. To obtain the relative variable importance, these values have to be divided by the maximum variable importance among all covariates. Then, values will range between 0 and 1 and the variableimportance is called 'relative' because it is expressed with respect to the most important covariate. Therefore, after this transformation, the most important covariate will have value equal 1, whereas the
rest of them will have a relative importance value with respect to 1. To know details on the calculation of variable importance, please
refer to [3].

-TimesSelected.txt . This file is organized in columns ordered by covariate in the input file. First column is the order of the covariate in the input file, and second column is the number of times a covariate is selected to split a node. This file may provide an insight of the importance of the covariates, however, to know their real importance, the user must use the 'Variable_Importance.txt ' file.

-Trees.txt . This file is also organized in columns ordered by tree constructed. The first column is the missclasication rate in the training file using the bootstrapped sample, whereas the second column is the missclassification rate in the respective out-of-bag sample. Please, refer to [3] for details on how these sample sets are constructed.

-EGBV.txt . This file contains two columns. The first one is the corresponding ID of individuals in the training set. The second column is the estimated value in regression problems or the predicted probability of that individual of being susceptible to the analyzed event in
classification problems. 

The files 'Predictions.txt' and 'Trees.test' are generated from predictions in the testing file.
-Trees.test . This is a single column file containing the misclassification rate of individuals in the testing file ordered by tree. Therefore, first row is the missclassification rate after the first tree is constructed, and the last row is the missclassification rate after the whole forest was grown. Please, refer to [3] for details on how predictions are calculated.

-Predictions.txt . This file contains two columns. The first one is the corresponding ID of individuals in the testing set. The second column is the predicted value in regression problems or the predicted probability of that individual of being susceptible to the analyzed event in classification events.

## Bibliography
[1] Horstmann C. Java Concepts. John Wiley and Sons, Inc, 2008.

[2] Breiman L. Random forest. Machine Learning, 45(1):5-32, 2001.

[3] Gonzalez-Recio O. and S. Forni. Analyses of discrete traits in a genomic
selection context using bayesian regressions and machine learning. in prepa-
ration, pages 01, 2010.

[4] Breiman L. Bagging predictors. Machine Learning, 24:123-140, 1996.

[5] Tibshirani R., 1996. Bias, variance, and prediction error for classication
rules. Technical Report, Statistics Department, University of Toronto.
