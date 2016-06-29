/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoding;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *  Abstract class NoteEncoding allows for strategy implementation of the encoding process, 
 *  providing an interface for encoding and decoding notes between midiValues and bit vectors
 * @author Nicholas Weintraut
 */
public interface NoteEncoder {
    
    public INDArray encode(int midiValue);
    
    public int getSustainKey();
    
    public boolean hasSustain(INDArray input);
    
    public int decode(INDArray input);
    
    public default int getNoteLength(){
        int sum = 0;
        for(Group group : getGroups())
            sum += group.length();
        return sum;
    }
    
    public Group[] getGroups();
    
    public INDArray clean(INDArray input);
    
}
