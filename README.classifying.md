#Instructions for classifying relationship candidates appearing in a set of entity-extracted documents (ser.gz files)


<br>

#### Place the entity extracted, serialized documents (.ser.gz files) for which you want to make predictions in the directory *relation-bootstrap/DataFiles/Testing/EntityExtractedSerialized/* .  (We assume that cyber entity extracted versions of the training documents have already been produced)


---

<br>

#### Navigate to relation-bootstrap/Relation-Bootstap/target and run this program:

	java -cp "./Relation-Bootstrap-0.0.1-SNAPSHOT.jar:./dependency/*" gov.ornl.stucco.relationprediction/PrintPreprocessedDocuments

This program takes the output produced by the entity-extractor in the form of serialized documents and produces text versions of them in 
*relation-bootstrap/ProducedFiles/Testing/EntityExtractedText/* .  The four files are called *aliasreplaced*, *entityreplaced*, *original*, and *unlemmatized*.  Details of these files’ contents can be seen in comments at the top of *gov.ornl.stucco.relationprediction.PrintPreprocessedDocuments.java*.


---

<br>

#### Run this program:

	WriteRelationInstanceFiles preprocessedtype

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the output of the previous two programs to write a lot of data files for relationship SVM classifiers.  preprocessedtype tells the program which output files written by the previous two programs to use.


---

<br>

#### Run this program:

	PredictUsingExistingSVMModel preprocessedtype

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the instance data written by the previous program and applies an SVM model to it that was written during training.  It chooses which parameters to use based on the output of CalculateResults, which gets run during the training phase.  This program will not run properly unless WriteRelationInstanceFiles has been run using the same preprocessedtype parameter.  And of course CalculateResults needs to be run with the same preprocessedtype parameter during training as well.
 

