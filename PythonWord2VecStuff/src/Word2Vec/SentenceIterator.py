#This class just iterates over the lines in a file (particularly, we send it the training data file with one line per sentence) 
#so that we do not have to store all the sentences in memory.
import zipfile, os


#Set this to false once we are done just checking if the program works.
checkingifworking = False


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
                    if not ( line.startswith("###") and line.endswith("###") ):
                        yield line.split()
                
            
         
                
        for dirpath, subdirs, files in os.walk(self.wikipedialemmatizeddir):
            for x in files:
                if x.endswith(".zip"):
                    filepath = os.path.join(dirpath, x)
                    zipthing = zipfile.ZipFile(filepath, 'r')
                    zipinternalfilename = zipthing.namelist()[0]    #Each zip file contains only one file.
                    with zipthing.open(zipinternalfilename, 'r') as f:
                        for line in f:
                            yield line.split()
            if checkingifworking:
                break
        
        
        
        
        