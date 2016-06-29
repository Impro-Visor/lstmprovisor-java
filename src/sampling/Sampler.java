/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sampling;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Interface sampler represents functionality to take in an INDArray of probability distributions and sample an output vector from it
 * @author Nicholas Weintraut
 */
public interface Sampler {
    public INDArray sample(INDArray input);
}
