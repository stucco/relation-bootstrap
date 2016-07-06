In order to run PrintPreprocessedDocuments, you need to put some subset of the NVD XML 2.0 Schema xml files from this site https://nvd.nist.gov/download.cfm in the directory containing the file you're reading.  The files must be uncompressed.  The more of these xml files you use, the better our results should be (because we extract the relationships used to construct our training data from these).  I used all of the xml files available for my experiments.  This set includes:

nvdcve-2.0-2002.xml
nvdcve-2.0-2003.xml
nvdcve-2.0-2004.xml
nvdcve-2.0-2005.xml
nvdcve-2.0-2006.xml
nvdcve-2.0-2007.xml
nvdcve-2.0-2008.xml
nvdcve-2.0-2009.xml
nvdcve-2.0-2010.xml
nvdcve-2.0-2011.xml
nvdcve-2.0-2012.xml
nvdcve-2.0-2013.xml
nvdcve-2.0-2014.xml
nvdcve-2.0-2015.xml
nvdcve-2.0-2016.xml


I do not include these in the git project because they are all pretty large and I wasn't sure if it would be unreasonable to store large data files in the repository.

