#This class just iterates over the lines in a file (particularly, we send it the training data file with one line per sentence) 
#so that we do not have to store all the sentences in memory.
class SentenceIterator(object):
    def __init__(self, filename):
        self.filename = filename;
 
    def __iter__(self):
        for line in open(self.filename):
             yield line.split()
                
                