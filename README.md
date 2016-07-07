# Instructions for training a relation classifier:

We assume that cyber entity extracted versions of the documents have already been produced. 

---

<br>


#### Place the entity extracted, serialized documents (.ser.gz files) in the directory *relation-bootstrap/producedfiles/entityextractedserialized/* .

---

<br>


####Place decompressed xml files in 

---

<br>


#### Run this program:

	PrintPreprocessedDocuments

This program takes the output produced by the entity-extractor in the form of serialized documents and produces text versions of them in 
*relation-bootstrap/producedfiles/entityextractedtext/* .  The three files are called *aliasreplaced*, *entityreplaced*, and *original*.  Details of these files’ contents can be seen in comments at the top of *gov.ornl.stucco.PrintPreprocessedDocuments.java*.

---

<br>


#### Run this program:

	TrainModel.py preprocessedtype

	preprocessedtype = original | entityreplaced | aliasreplaced

This program takes the output from the previous program and trains a word2vec model on it.  It then writes a text file called *wordvectors.original* , *wordvectors.aliasreplaced*, or *wordvectors.entityreplaced* into the *relation-bootstrap/producedfiles/models/* directory, depending on which type of preprocessed document was named in the first command line argument.  This file that gets written contains the vectors learned for each word given the training data.

---

<br>


#### Run this program:

	WriteRelationInstanceFiles preprocessedtype contexts

	preprocessedtype = original | entityreplaced | aliasreplaced
	contexts = 000 | 001 | 010 | 011 | 100 | 101 | 110 | 111

This program takes the output of the previous two programs to write a lot of data files for relationship SVM classifiers.  preprocessedtype tells the program which output files written by the previous two programs to use, and contexts tells it which feature representation to use.  More specifically, this argument tells us whether or not we want to use the context preceding the first entity (first digit), whether or not we want to use the context between entities (second digit), and whether or not we want to use the context after the second entity (third digit).  A 1 in one of these spots indicates that we do want to use the corresponding context, and a zero indicates that we don't.

---

<br>


#### Run this program:

	RunRelationSVMs preprocessedtype contexts kerneltype

	preprocessedtype = original | entityreplaced | aliasreplaced
	contexts = 000 | 001 | 010 | 011 | 100 | 101 | 110 | 111
	kerneltype = RBF | Linear

This program takes the instance data written by the previous program, trains a SVM on it, and applies the SVM to a test set.  So it will not run properly unless WriteRelationInstanceFiles has been run using the same preprocessedtype and contexts parameters.  kerneltype refers to the type of kernel used in the SVM.


