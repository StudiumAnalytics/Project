#include <iostream>
#include <string.h>
#include <fstream>
#include <math.h>
#include <vector>
using namespace std;
void printSeqs(vector<vector<float> > seqs, fstream &out);
float getLabelForUpperBound(vector<float> row);
vector<vector<float> > getBaseSequences();
vector<vector<float> > getAllSequences(vector<vector<float> > baseSequences);


int main(){
    fstream out;
    out.open("UBTrainingData.txt",ios::out);
    vector<vector<float> > sequences = getAllSequences(getBaseSequences());
    printSeqs(sequences,out);
    out.close();
    return 0;
}

void printSeqs(vector<vector<float> > seqs, fstream &out){
    for(int i = 0; i< seqs.size();i++){
        float label = getLabelForUpperBound(seqs[i]);
        out<<label<<",";
        for(int j = 0; j< seqs[j].size();j++){
            out<<seqs[i][j];
            if(j< seqs[i].size()-1) out<<",";
            else out<<endl;
        }
    }
}

vector<vector<float> > getAllSequences(vector<vector<float> > baseSequences){
    for(int i = 0; i< 5; i++){
        vector<float> curSeq = baseSequences[i];
        for(int j = 2; j< 21; j++){ // 19 sequenze
            vector<float> newSeq;
            for(int k = 0; k< curSeq.size(); k++){
                newSeq.push_back(curSeq[k] * j);
            }
            baseSequences.push_back(newSeq);
        }
    }
    return baseSequences;
}

vector<vector<float> > getBaseSequences(){
    vector<vector<float> > sequences;
    
    vector<float> stableSeq(10,0.5);
    
    float toUpSeqArray[10] = {0.5,1,1.5,2,2.5,3,3.5,4,4.5,5}; //sale
    vector<float> toUpSeq(&toUpSeqArray[0], &toUpSeqArray[0]+10);
    
    float toDownSeqArray[10] = {5,4.5,4,3.5,3,2.5,2,1.5,1,0.5}; //scende
    vector<float> toDownSeq(&toDownSeqArray[0], &toDownSeqArray[0]+10);
    
    float downUpSeqArray[10] = {2.5,2,1.5,1,0.5,0.5,1,1.5,2,2.5}; //scende-sale
    vector<float> downUpSeq(&downUpSeqArray[0], &downUpSeqArray[0]+10);
    
    float upDownSeqArray[10] = {0.5,1,1.5,2,2.5,2.5,2,1.5,1,0.5}; //sale-scende
    
    vector<float> upDownSeq(&upDownSeqArray[0], &upDownSeqArray[0]+10);
    
    sequences.push_back(stableSeq);
    sequences.push_back(toUpSeq);
    sequences.push_back(toDownSeq);
    sequences.push_back(downUpSeq);
    sequences.push_back(upDownSeq);
    
    return sequences;
    
}
    
float getLabelForUpperBound(vector<float> row){
    float curWheight = 10;
    float wheightsSum = 10;
    float valuesSum = 0;
    for(int i = 0 ; i< row.size(); i++){
        valuesSum += row[i] * curWheight;
        
        curWheight = curWheight/1.5;
        wheightsSum += curWheight;
            
    }
    float label = valuesSum / wheightsSum;
    return label;
        
}
