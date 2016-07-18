# Instructions for training a relation classifier:



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

The following step is optional depending on whether you also want to train a new word2vec model.  If you do not, and there is already a file in *relation-bootstrap/ProducedFiles/Models/* for the preprocessedtype you plan to use for training, you can skip this step:

#### Navigate to relation-bootstrap/PythonWord2VecStuff/src/Word2Vec and run this program:

	python ./TrainModel.py preprocessedtype

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the output from the previous program and trains a word2vec model on it.  It then writes a text file called *wordvectors.original* , *wordvectors.aliasreplaced*, or *wordvectors.entityreplaced* into the *relation-bootstrap/ProducedFiles/Models/* directory, depending on which type of preprocessed document was named in the first command line argument.  This file that gets written contains the vectors learned for each word given the training data.

Note that this is a very time-expensive program to run.  It may take a week to complete.


---

<br>

#### Run this program:

	WriteRelationInstanceFiles preprocessedtype training

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the output of the previous two programs to write a lot of data files for relationship SVM classifiers.  preprocessedtype tells the program which output files written by the previous two programs to use.


---

<br>

#### Run this program:

	RunRelationSVMs preprocessedtype training

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the instance data written by the previous program, trains a SVM on it, and applies the SVM to a test set.  Also, for each possible combination of parameter settings, it writes an SVM model file to the disk so that this model can later be used to make predictions.  It will not run properly unless WriteRelationInstanceFiles has been run using the same preprocessedtype parameter.
 
Note that this is a very time-expensive program to run.  It may take a week to complete.


---

<br>

#### Run this program:

	CalculateResults preprocessedtype

	preprocessedtype = original | entityreplaced | aliasreplaced

This program does a grid search over the predictions made by the previous program in order to select the best parameter set via 4-fold cross-validation on the training set.  It then calculates results on the test set.  It performs these steps 5 times, once for each test fold.  It then micro averages and prints the results (f-score, precision, and recall) across all 5 test folds.  It additionally prints the set of these parameters that performed best across the 5 folds.  

