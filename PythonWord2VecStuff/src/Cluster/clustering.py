# clustering.py

"""
Description: 
        k-means implementation from 
        https://datasciencelab.wordpress.com/2013/12/12/clustering-with-k-means-in-python/

        gap statistic implementation from 
        https://datasciencelab.wordpress.com/tag/gap-statistic/ 
"""

## ---- modules ---- ##
import numpy as np 
from numpy import linalg 
import random 
import matplotlib.pyplot as plt
import os



#Get the paths to a few different files we will use.  We assume that this program stays somewhere within the git project.
#This is the directory that this file is actually located in
programpath = os.path.dirname(os.path.realpath(__file__))

#This is the directory of the git project which includes both the Java project and this python project.
path_to_git_project = programpath
while os.path.basename(path_to_git_project) != "relation-bootstrap":
    path_to_git_project = os.path.abspath(os.path.join(path_to_git_project, ".."))
    
#This is the directory where all the different files produced by the Java (and this Python) project go.
produced_file_dir = os.path.abspath(os.path.join(path_to_git_project, "ProducedFiles"))

instancesfile = os.path.join(produced_file_dir, 'Testing', 'InstanceFiles', 'RelationInstances.aliasreplaced.c.1')
filetowriteto = "/Users/p5r/Downloads/ccluster"




### ---- data sampling ---- ## 
#def init_board(N):
#    X = np.array([(random.uniform(-1, 1), random.uniform(-1, 1)) for i in range(N)])
#    return X
#
#
#def init_board_gauss(N, k):
#    n = float(N)/k
#    X = []
#    for i in range(k):
#        c = (random.uniform(-1, 1), random.uniform(-1, 1))
#        print "center %s = %s" %(i,c)
#        s = random.uniform(0.05,0.3)
#        x = []
#        while len(x) < n:
#            a, b = np.array([np.random.normal(c[0], s), np.random.normal(c[1], s)])
#            # Continue drawing points from the distribution in the range [-1,1]
#            if abs(a) < 1 and abs(b) < 1:
#                x.append([a,b])
#        X.extend(x)
#    X = np.array(X)[:N]
#    return X


def init_board_relationcontexts():
    X = []
    valuesTotext = {}
    with open(instancesfile) as f:
        for line in f:
            x = []
            splitline = line.split(' ')
            for i in range(1, len(splitline)):
                if splitline[i] == '#':
                    break
                featureandvalue = splitline[i].split(':')
                value = float(featureandvalue[1])
                x.append(value)
            xholder = np.array(x)
            comment = line.split('#', 1)[1].rstrip().split(' ')
            
            firstentity = comment[3]
            secondentity = comment[5]
            firstentityindex = 9999999
            secondentityindex = 0
            for i in range(6, len(comment)):
                if comment[i] == firstentity:
                    firstentityindex = min(firstentityindex, i)
                if comment[i] == secondentity:
                    secondentityindex = max(secondentityindex, i)
            
            #valuesTotext.append([xholder, comment])
            valuesTotext[np.array_str(xholder)] = " ".join(comment[6:firstentityindex]) + "\t" + firstentity + "\t" + " ".join(comment[firstentityindex+1:secondentityindex]) + "\t" + secondentity + "\t" + " ".join(comment[secondentityindex+1:len(comment)])
            
            X.append(xholder)
    X = np.array(X)
    return X, valuesTotext



## ---- k-means implementaton ---- ##  
def cluster_points(X, mu):
    clusters  = {}
    for x in X:
        bestmukey = min([(i[0], np.linalg.norm(x-mu[i[0]])) \
                    for i in enumerate(mu)], key=lambda t:t[1])[0]
        try:
            clusters[bestmukey].append(x)
        except KeyError:
            clusters[bestmukey] = [x]
    return clusters
 

def reevaluate_centers(mu, clusters):
    newmu = []
    keys = sorted(clusters.keys())
    for k in keys:
        newmu.append(np.mean(clusters[k], axis = 0))
    return newmu
 

def has_converged(mu, oldmu):
    return (set([tuple(a) for a in mu]) == set([tuple(a) for a in oldmu]))
 

def find_centers(X, K):
    # Initialize to K random centers
    oldmu = random.sample(X, K)
    mu = random.sample(X, K)
    while not has_converged(mu, oldmu):
        oldmu = mu
        # Assign all points in X to clusters
        clusters = cluster_points(X, mu)
        # Reevaluate centers
        mu = reevaluate_centers(oldmu, clusters)
    return(mu, clusters)



## ---- gap-statistic implementation ---- ##
def Wk(mu, clusters):
    K = len(mu)
    return sum( [ np.linalg.norm( mu[i] - c)**2/(2*len(c)) for i in range(K) for c in clusters[i] ] )


def bounding_box(X):
    xmin, xmax = min( X, key=lambda a:a[0] )[0], max( X, key=lambda a : a[0] )[0]
    ymin, ymax = min( X, key=lambda a:a[1] )[1], max( X, key=lambda a : a[1] )[1]
    return (xmin,xmax), (ymin,ymax)
 

def gap_statistic(X, kmin, kmax):
    """ 
    Input:     X = np.array with a data point in each row 
            kmin = positive int, smallest k we should try 
            kmax = positive int, max k we should try 
    Output: k = the k used for clustering
            ks = list of k's tried 
            Wks = list of sum(log(cluster variances)) (one for each k in ks)
            Wkbs = list of expected sum(log(cluster variances)) for data sampled from a uniform distribution 
            sk = standard deviation of the Wkbs's. 
    Description : runs gap statistis. Gap is the Wkbs - Wks (clusters should be farther apart in a uniform than in our data)
                    We look for the first k such that gap[k] > gap[k+1] - sd[k+1]
    """ 
    (xmin,xmax), (ymin,ymax) = bounding_box(X)
    #ks = range(kmin, kmax+1) ## ks is the range of the number of clusters to try
    ks = range(kmin, kmax+1, 5) ## ks is the range of the number of clusters to try
    Wks = np.zeros(len(ks)) ## to be populated log variance of each cluster of real data
    Wkbs = np.zeros(len(ks)) ## to be populated average log variance of each cluster of sampled data
    sk = np.zeros(len(ks)) ## to be populated standard deviation of Wkbs 
    for indk, k in enumerate(ks):
        mu, clusters = find_centers(X,k) ## mu's rows are the cluster centers

        Wks[indk] = np.log(Wk(mu, clusters))
        
        ## Create B reference datasets
        B = 10
        BWkbs = np.zeros(B)
        for i in range(B):
            Xb = []
            for n in range(len(X)):
                Xb.append([random.uniform(xmin,xmax),
                          random.uniform(ymin,ymax)])
            Xb = np.array(Xb)
            mu, clusters = find_centers(Xb,k)
            BWkbs[i] = np.log(Wk(mu, clusters))
        
        Wkbs[indk] = sum(BWkbs)/B
        sk[indk] = np.sqrt(sum((BWkbs-Wkbs[indk])**2)/B)

    sk = sk * np.sqrt( 1 + 1/B )


    plt.figure(1)

    gaps = Wkbs - Wks    
    plt.subplot(211)
    plt.ylabel("Gaps E(log(Wb_k)) - W_k")
    plt.xlabel("k")
    plt.plot(ks, gaps, 'bo')


    check = [gaps[i] - gaps[i+1] + sk[i+1] for i in xrange(len(ks)-1)]     ## we care about the first k where gap[k]>gap[k+1] - sd[k+1]
    plt.subplot(212)
    plt.plot(ks[:-1], check, 'go')
    plt.ylabel("Gap[k] - Gap[k+1] - sd[k+1]")
    plt.xlabel("k")

    plt.show()

    for i in xrange(len(check)): 
        if check[i]>0: 
            break
    k  = ks[i] ## this is the k we want

    return (k, ks, Wks, Wkbs, sk)  


## ---- test it ---- ##
if __name__ == '__main__':
    #X = init_board_gauss(300, 3) ## get data 
    X, valuesTotext = init_board_relationcontexts() ## get data
    
    
    
    (k, ks, Wks, Wkbs, sk)  = gap_statistic(X,10,50)
    mu, clusters = find_centers(X, k) ## cluster 
    
    
    
    f = open(filetowriteto,'w')
    print k
    print ""
    for i in xrange(len(clusters)):
        for x in clusters[i]:
            valuesstring = np.array_str(x)
            print valuesTotext[valuesstring]
            f.write(valuesTotext[valuesstring] + "\n")
        print ""
        f.write("\n");
    f.close() # you can omit in most cases as the destructor will call it
    
    
    
    #c = {}
    #for i in xrange(len(clusters)): 
    #    c[i] = np.array(clusters[i])
    #plt.plot(c[0][:,0], c[0][:,1], 'bo')
    #
    #if k == 2: 
    #    plt.plot(c[1][:,0], c[1][:,1], 'ro')
    #
    #if k == 3: 
    #    plt.plot(c[2][:,0], c[2][:,1], "go" )
    #plt.show()

