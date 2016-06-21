/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author cssummer16
 */
public class FullyConnectedLayer {
    
    private INDArray weights;
    private INDArray biases;
    private OpType type;
    
    private INDArray multResult;
    
    
    public FullyConnectedLayer (int inputSize, int outputSize, OpType type)
    {
        this.weights = Nd4j.rand(new int[]{outputSize, inputSize}).mul(2.0).sub(1.0);
        this.biases = Nd4j.rand(new int[]{outputSize}).mul(2.0).sub(1.0);
        this.type = type;
    }
    
    public FullyConnectedLayer (int inputSize, int outputSize, OpType type, INDArray weights, INDArray biases)
    {
        this.weights = weights;
        this.biases = biases;
        this.type = type;
    }
    
    public INDArray forward (INDArray input)
    {
            if(multResult == null)
                multResult = type.operate(weights.mmul(input.transposei()));
            return type.operate(weights.mmul(input.transposei(), multResult));
        
    }
}
