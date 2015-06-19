# coding: utf-8

import pymysql.cursors
import re
import pandas as pd
import numpy as np
from sklearn import svm
import nltk.stem.porter as stem
import scipy as sp
import nltk as nltk
from nltk.corpus import wordnet as wn
from SimplifyTag import *
import time


##################################
# This part is processing the file
# and setting the model to learn
##################################

#Reads the corpus from the file
print "Reading corpus..."
df = pd.read_csv("corpusEqual.txt", sep='\t', header=None, encoding='utf-8')
df.columns = ['Score', 'Comment']
#List of negative terms
listNeg = [
    'arent','cant','cannot','dont','isnt','never','not','wont','wouldnt','aint'
]
#Dictionary used to translate the tags
POStranslate = {
    'ADJ':'a', 'ADV':'r','N':'n','V':'v','VD':'v','VG':'v','VN':'v'
}

#Tokenization of the comments
def tokenize(comment):
    #Split into tokens    
    tokens = comment.lower().replace('[^a-z\s]+', '').split(' ')
    #Applies negation tagging
    neg = False
    for i in range(0, len(tokens)):
        if(neg):
            tokens[i] = tokens[i]+"_N"
        if(tokens[i] in listNeg):
            neg = not neg
    #Filter the tokens using nltk
    '''(filteredTokens = []
    for pair in nltk.pos_tag(tokens):
        #Process the tag to know if noun, verb or adj
        tag = simplify_tag(pair[1])
        if(not tag in POStranslate.keys()):
            continue
        tag = POStranslate[tag]
        if(not tag in ['n', 'v', 'a']):
            continue
        #Add the word to the new list
        filteredTokens.append(pair[0])
    return filteredTokens'''
    return tokens
    
print "Tokenizing comments..."
df['Tokens'] = df['Comment'].apply(tokenize)

#Creates the set of all the words used
print "Creating word set"
wordSet = set()
stemmer = stem.PorterStemmer()
for index, row in df.iterrows():
    for word in row['Tokens']:
        wordSet.add(stemmer.stem(word))
    
# Creates a dict to keep word => index
wordDic = {}
i = 0
for word in wordSet:
    wordDic[word] = i
    i = i + 1

# Sparse matrix
print "Creating sparse matrix"
A_width = len(wordSet) #Each word is one column
A_height = len(df.index) #Each critic is one row
A = sp.sparse.dok_matrix((A_height, A_width))
# We fill the sparse matrix with values from the dataframe
for index, row in df.iterrows():
    for word in row['Tokens']:
        # Skipped words include words less than 4 characters
        if(not word in wordSet):
            continue
        A[index, wordDic[word]] = A[index, wordDic[word]]+1

#The SVM model
print "Learning model"
clf_svm = svm.LinearSVC()
clf_svm.fit(A, df['Score'])


##################################
# This part connects to the db and
# retrieves new tweets to learn
##################################

# Connect to the database
print "Connecting database..."
connection = pymysql.connect(host='localhost',
     user='root',
     passwd='topaze',
     db='movies',
     charset='utf8mb4',
     autocommit=True,
     cursorclass=pymysql.cursors.DictCursor)


# This function retrieves new tweets (id + text)
def getNewTweets():
    newTweets = []    
    try:
        with connection.cursor() as cursor:
            # Read a single record
            sql = "SELECT id, text FROM tweets WHERE score=-1"
            cursor.execute(sql, ())
            result = cursor.fetchmany(size=100)
            for row in result:
                newTweets.append(row)
    except:
        print "Error while retrieving new tweets"
    return newTweets
    
# Uses the classifier to guess the score of the tweet
def getScore(tweet):
    tokens = tokenize(tweet['text'])
    # One row sparse matrix to learn
    B_width = len(wordSet) #Each word is one column
    B_height = 1 #Only one row, for the tweet to be classified
    B = sp.sparse.dok_matrix((B_height, B_width))
    #We fill the sparse matrix with values from the dataframe
    for word in tokens:
        # Skipped words include word less than 4 characters but also words
        # that are found in the tweet but not in corpus
        word = stemmer.stem(word)
        if(not word in wordSet):
            continue
        B[0, wordDic[word]] = B[0, wordDic[word]]+1
    return clf_svm.predict(B)[0]
    
def updateScore(tweet_id, score):
    try:
        with connection.cursor() as cursor:
            # Read a single record
            sql = "UPDATE tweets SET score="+str(score)+" WHERE id="+str(tweet_id)
            cursor.execute(sql)
    except:
        print "Error while updating tweet n°", tweet_id

print "-----Work started----"
while(True):
    tweets = getNewTweets()
    for tweet in tweets:
        updateScore(tweet['id'], getScore(tweet))
    if(len(tweets)==0):
        #print "Wait 5s"
        time.sleep(5)
    else:
        print "Scored", len(tweets), "tweets"
    
#Close the connection
connection.close()
