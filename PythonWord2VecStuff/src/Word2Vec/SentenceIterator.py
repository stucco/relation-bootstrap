#This class just iterates over the lines in a file (particularly, we send it the training data file with one line per sentence) 
#so that we do not have to store all the sentences in memory.


import zipfile, os


class SentenceIterator(object):
    def __init__(self, entityextractedfilename, wikipedialemmatizeddir, repeatrelevanttimes):
        self.entityextractedfilename = entityextractedfilename
        self.wikipedialemmatizeddir = wikipedialemmatizeddir
        self.repeatrelevanttimes = repeatrelevanttimes
 
    def __iter__(self):
        
        for i in xrange(1, self.repeatrelevanttimes):
            zipthing = zipfile.ZipFile(self.entityextractedfilename, 'r')
            zipinternalfilename = zipthing.namelist()[0]    #The zip file contains only one file.
            with zipthing.open(zipinternalfilename, 'r') as f:
                for line in f:
                    yield line.split()
                
                
        for g in os.listdir(self.wikipedialemmatizeddir):
            if g.endsWith(".zip"):
                zipthing = zipfile.ZipFile(g, 'r')
                zipinternalfilename = zipthing.namelist()[0]    #Each zip file contains only one file.
                with zipthing.open(zipinternalfilename, 'r') as f:
                    for line in f:
                        yield line.split()
        
        
        
        
        
        
        