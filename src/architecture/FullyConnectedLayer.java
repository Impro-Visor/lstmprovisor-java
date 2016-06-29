/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import filters.Operations;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Class FullyConnectedLayer is an implementation of a simple neural network layer which multiplies inputs by a weight matrix and adds biases,
 * then performs an operation on the resultant vector such as sigmoid or tanh.
 * @author Nicholas Weintraut
 */
public class FullyConnectedLayer implements Loadable {
    
    private INDArray weights;
    private INDArray biases;
    private Operations type;
    
    private INDArray multResult;
    
    
    public FullyConnectedLayer (int inputSize, int outputSize, Operations type)
    {
        this.weights = Nd4j.rand(new int[]{outputSize, inputSize}).mul(2.0).sub(1.0);
        this.biases = Nd4j.rand(new int[]{outputSize}).mul(2.0).sub(1.0);
        this.type = type;
    }
    
    public FullyConnectedLayer (Operations type)
    {
        this.type = type;
    }
    
    public INDArray forward (INDArray input)
    {
            if(multResult == null)
                multResult = type.operate(weights.mmul(input.transpose()));
            return type.operate(weights.mmul(input.transpose(), multResult));
    }

    @Override
    public boolean load(INDArray data, String loadPath) {
        switch(loadPath)
        {
            case "b":   this.biases = data;
                        return true;
            case "w":   this.weights = data;
                        return true;
            default:    return false;
        }
    }
}
