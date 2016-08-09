#Instructions for setting up the project

The following instructions are necessary whether you want to train a new model for predicting relationships, or whether you just want to make new predictions using models that have already been trained:


---

<br>

Navigate to the directory you wish to work in and run the command:

	git clone https://github.com/stucco/relation-bootstrap

In order to retrieve the project.


---

<br>

Navigate to /relation-bootstrap/Relation-Bootstrap and run the commands:

	sudo mvn package
	sudo mvn dependency:copy-dependencies

In order to build the project and save the dependencies in a convenient location for inclusion in the classpath when running java programs.  (I expected they would be unnecessary or would somehow get included in the classpath automatically since we used Maven to build the project, but this doesn't seem to be the case)


---

<br>
<br>
<br>

# Instructions for training a relation classifier:

(These steps are illustrated in TrainingDiagram.png and DiagramsKey)

---

<br>

#### Place the entity extracted, serialized documents (.ser.gz files) you want to use for training in the directory *relation-bootstrap/DataFiles/Training/EntityExtractedSerialized/* .  (We assume that cyber entity extracted versions of the training documents have already been produced)


---

<br>

#### Navigate to relation-bootstrap/Relation-Bootstap/target and run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/PrintPreprocessedDocuments training

This program takes the output produced by the entity-extractor in the form of serialized documents and produces text versions of them in 
*relation-bootstrap/ProducedFiles/Training/EntityExtractedText/* .  The four files are called *aliasreplaced*, *entityreplaced*, *original*, and *unlemmatized*.  Details of these files’ contents can be seen in comments at the top of *gov.ornl.stucco.relationprediction.PrintPreprocessedDocuments.java*.


---

<br>

###Training a word2vec model

The following steps between these lines is optional depending on whether you also want to train a new word2vec model.  If you do not, and there is already a file in *relation-bootstrap/ProducedFiles/Models/* for the preprocessedtype you plan to use for training, you can skip the following few steps for producing a word2vec model:

<br>

#### Select a directory where you have lots of space (~60GB).  We will refer to this directory as "the directory"

<br>

#### Download an English Wikipedia dump into the directory.  The one we originally used can be found at https://dumps.wikimedia.org/enwiki/20160601/ in the enwiki-20160601-pages-articles-multistream.xml.bz2 file.

<br>

#### Extract the file's contents with a command like:
	
	bzip2 -d enwiki-20160601-pages-articles-multistream.xml.bz2

<br>

#### Download the WikiExtractor.py program from http://medialab.di.unipi.it/wiki/Wikipedia_Extractor into the directory.

<br>

#### Extract the article text from the XML files using a command like:

	python WikiExtractor.py -b 1000M -o extracted enwiki-20160601-pages-articles-multistream.xml

<br>

#### Navigate to relation-bootstrap/Relation-Bootstap/target and run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/WriteLemmatizedWikipediaArticlesFile directory/extracted/AA/

The one command line argument here should be the directory where you put the text files in the previous command.  If you ran the previous command with the "-o extracted" option just as given, directory/extracted/AA should be the location of these text files.  This program may take about a day to run.

<br>

#### Navigate to relation-bootstrap/PythonWord2VecStuff/src/Word2Vec and run this program:

	python ./TrainModel.py preprocessedtype

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the output from the previous program and trains a word2vec model on it.  It then writes a text file called *wordvectors.original*, *wordvectors.aliasreplaced*, or *wordvectors.entityreplaced* into the *relation-bootstrap/ProducedFiles/Models/* directory, depending on which type of preprocessed document was named in the first command line argument.  This file that gets written contains the vectors learned for each word given the training data.

Note that this is a very time-expensive program to run.  It may take a week to complete.


---

<br>

#### Run this program:
	
	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/FindAndOrderAllInstances preprocessedtype training

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the output of the previous two programs to write a file that lists all the instances we want to use for training, heuristically identifying them as positive or negative in the process.  The reason we want to do this is that it yields an order over all instances, and we can ensure all future programs handle instances in the same order.  This is useful because without it, we would have to hold more instances in memory in later programs that deal with multiple versions of the same files, as there could be alignment issues.


---

<br>

#### Run this program:

	java  -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/WriteRelationInstanceFiles preprocessedtype featuretypecode training

	preprocessedtype = original | entityreplaced | aliasreplaced
	featuretypecode = Any implemented feature type code listed in FeatureMap.java.
	
This program takes the output of the previous three programs to write a lot of data files for relationship SVM classifiers.  preprocessedtype tells the program which output files written by the previous two programs to use.  featuretype tells the program which one type of feature to use to represent the instances.  You can see a list of available feature type codes near the top of FeatureMap.java.  If you intend to run an experiment using several feature types, you must run this program once for each of the feature types you want to use.


---

<br>

#### Run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar" gov.ornl.stucco.relationprediction/RunRelationSVMs preprocessedtype featuretypecodes

	preprocessedtype = original | entityreplaced | aliasreplaced
	featuretypecodes = a list of unseparated feature type codes from FeatureMap.java.

This program takes the instance data written by the previous program, trains a SVM on it, and applies the SVM to a test set.  Also, for each possible combination of parameter settings, it writes an SVM model file to the disk so that this model can later be used to make predictions.  It will not run properly unless WriteRelationInstanceFiles has been run using the same preprocessedtype parameter.
featuretypecodes tells the program which sets of features to use to represent an instance.  The codes are each a single character in length, and featuretypecodes is a lost of these characters without any separator between them. 
 
Note that this is a very time-expensive program to run.  It may take a week to complete.


---

<br>

#### Run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/CalculateResults preprocessedtype featuretypecodes

	preprocessedtype = original | entityreplaced | aliasreplaced
	featuretypecodes = a list of unseparated feature type codes from FeatureMap.java.

This program does a grid search over the predictions made by the previous program in order to select the best parameter set via 4-fold cross-validation on the training set.  It then calculates results on the test set.  It performs these steps 5 times, once for each test fold.  It then micro averages and prints the results (f-score, precision, and recall) across all 5 test folds.  It additionally prints the set of these parameters that performed best across the 5 folds.  

In addition to printing these results to the screen, it saves them in a file located in the ".../relation-bootstrap/ProducedFiles/Training/ExperimentalResultsFiles" directory (which can be obtained programmatically via the method ProducedFileGetter.getResultsFile).  The parameters that get saved in this file are used when making predictions for new documents (described below).


---

<br>
<br>
<br>



#Instructions for classifying relationship candidates appearing in a set of entity-extracted documents (ser.gz files)

(These steps are illustrated in PredictingNewDataDiagram.png and DiagramsKey)


---

<br>

#### Place the entity extracted, serialized documents (.ser.gz files) for which you want to make predictions in the directory *relation-bootstrap/DataFiles/Testing/EntityExtractedSerialized/* .  (We assume that cyber entity extracted versions of the training documents have already been produced)


---

<br>

#### Navigate to relation-bootstrap/Relation-Bootstap/target and run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/PrintPreprocessedDocuments

This program takes the output produced by the entity-extractor in the form of serialized documents and produces text versions of them in 
*relation-bootstrap/ProducedFiles/Testing/EntityExtractedText/* .  The four files are called *aliasreplaced*, *entityreplaced*, *original*, and *unlemmatized*.  Details of these files’ contents can be seen in comments at the top of *gov.ornl.stucco.relationprediction.PrintPreprocessedDocuments.java*.


---

#### Run this program:
	
	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/FindAndOrderAllInstances preprocessedtype

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the output of the previous two programs to write a file that lists all the instances we want to use for testing, heuristically identifying them as positive or negative in the process when possible, and simply labeling them "0" otherwise.  The reason we want to do this is that it yields an order over all instances, and we can ensure all future programs handle instances in the same order.  This is useful because without it, we would have to hold more instances in memory in later programs that deal with multiple versions of the same files, as there could be alignment issues.


---

<br>

#### Run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/WriteRelationInstanceFiles preprocessedtype featuretypecode

	preprocessedtype = original | entityreplaced | aliasreplaced
	featuretypecode = Any implemented feature type code listed in FeatureMap.java.
	
This program takes the output of the previous programs to write a lot of data files for relationship SVM classifiers.  preprocessedtype tells the program which output files written by the previous two programs to use.  featuretype tells the program which one type of feature to use to represent the instances.  You can see a list of available feature type codes near the top of FeatureMap.java.  If you intend to run an experiment using several feature types, you must run this program once for each of the feature types you want to use.


---

<br>

#### Run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar" gov.ornl.stucco.relationprediction/PredictUsingExistingSVMModel preprocessedtype featuretypecodes

	preprocessedtype = original | entityreplaced | aliasreplaced
	featuretypecodes = a list of unseparated feature type codes from FeatureMap.java.

This program takes the instance data written by the previous program and applies an SVM model to it that was written during training.  It chooses which parameters to use based on the output of CalculateResults, which gets run during the training phase.  This program will not run properly unless WriteRelationInstanceFiles has been run using the same preprocessedtype parameter.  And of course CalculateResults needs to be run with the same preprocessedtype parameter during training as well.
 

The resulting predictions can be found in ...relation-bootstrap/ProducedFiles/Testing/PredictionsFiles.  This file can be gotten programatically via the method gov.ornl.stucco.relationprediction/ProducedFileGetter.getPredictionsFile.  Each line of predictions is of the form:

	HeuristicLabel/Predictedlabel # InstanceID OriginalEntity1Text ModifiedEntity1Text OriginalEntity2Text ModifiedEntity2Text SourceText

The HeuristicLabel is assigned by referring to a big dump of NVD xml files and a dump of Freebase aliases.  

A PredictedLabel of 1 indicates that the learner predicted that this is a positive instance of the class named in the file, and a PredictedLabel of -1 indicates that the learner predicted that this is a negative instance of the class named in the file.  The class name is the last number in the filename.  See the constants at the top of gov.ornl.stucco.relationprediction/GenericCyberEntityTextRelationship.java for information about what class number corresponds to what relationship.  If the class number in the file name is negative, that just means the entities appeared in the text in the reverse order compared to the order they are listed in the constant names.  So for example, the file corresponding to predictions of instances of Software Vendors and the Software Products they sell ends in a 1 if the Vendor appears before the Product in the text.  But If the Vendor appears after the Product, the filename ends in -1 because the constant corresponding to this relationship in GenericCyberEntityRelationship is called RT_SWVENDOR_SWPRODUCT = 1.

The # is only here to separate the labels from the text that follows.

See gov.ornl.stucco.relationprediction/InstanceID for the meaning of the InstanceID in prediction lines.

OriginalEntity1Text is the text corresponding to the entity appearing first in the relationship.  It is unmodified from the source text from which it came, except that it has been concatenated into one token by replacing its spaces with '_'s, it is in square brackets ("[]"), and it has had its entity type prepended to it.  See the static blocks at the top of gov.ornl.stucco.relationprediction/CyberEntityText for a list of entity type texts that can appear here.

ModifiedEntity1Text is the same as OriginalEntity1Text, only in this version, the entity's original text has been replaced by whatever the preprocessedtype we used dictates.  So it might be an alias of the entity if preprocessedtype=aliasreplaced, or it might be just an entity type if preprocessedtype=entityreplaced, or it may be unchanged if preprocessedtype=original.

OriginalEntity2Text and ModifiedEntity2Text are exactly the same as OriginalEntity1Text and ModifiedEntity1Text except that they refer to the second entity appearing in the text in the relationship rather than to the first.

Finally, SourceText is the original text from which the relationship candidate was extracted, buth with its entities replaced with their OriginalEntityText versions.


Add "allpositive" (without quotes) to the list of command line parameters if you instead want to look at baseline results wherein the positive class is predicted for every instance.  


---

<br>
<br>
<br>

# Miscellaneous Notes:

There are instructions on how to get around one of the collector issues in the collectorfiles directory.


