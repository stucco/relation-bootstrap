# relation-bootstrap

## High level instructions for training a relation classifier:

We assume that cyber entity extracted versions of the documents have already been produced. 

<br>


1. Place the entity extracted, serialized documents (.ser.gz files) in the directory relation-bootstrap/producedfiles/entityextractedserialized/ .

<br>


2. Run this program:

	gov.ornl.stucco.PrintPreprocessedDocuments 

It takes the output produced by the entity-extractor in the form of serialized documents and produces text versions of them in 
relation-bootstrap/producedfiles/entityextractedtext/ .  The three files are called aliasreplaced, entityreplaced, and original.  Details of these files’ contents can be seen in comments at the top of gov.ornl.stucco.PrintPreprocessedDocuments.java.

<br>


3. Run these programs:

	TrainModel.py original
	TrainModel.py aliasreplaced
	TrainModel.py entityreplaced

This program takes the output from the previous program and trains a word2vec model on it.  It then writes a text file containing the vectors learned for each word to relation-bootstrap/producedfiles/models/wordvectors.original , wordvectors.aliasreplaced, or wordvectors.entityreplaced , depending on which of the three command line arguments it was given.

<br>
