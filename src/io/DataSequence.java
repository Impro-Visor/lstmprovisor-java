/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Interface DataSequence describes commands to retrieve sequential INDArray data
 * @author Nicholas Weintraut
 */
public interface DataSequence {
    public INDArray retrieve();
    public boolean hasNext();
    public int entrySize();
    public<T extends DataSequence> T dup();
}
