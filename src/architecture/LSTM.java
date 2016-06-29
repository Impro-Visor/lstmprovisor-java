/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
//import org.nd4j.linalg.api.shape.

/**
 * Class LSTMNode implements an LSTM neural network node as described by Hochreiter and Schmidhuber (and explaiend by colah's blog).
 * The LSTMNode is currently operated via the push method, which pushes data through the recurrent node and retrieves the result vector.
 * @author Nicholas Weintraut
 */
public class LSTM implements Loadable{  
    
    
    //the state of the cell after last operation, will be fed back into node.
    public INDArray cellState;
    //the result Ht of the node
    public INDArray result;
    //The weights of the nodes activations, stored as a 3 dimensional array comprised of the 2 dimensional weight matrices for each activation
    INDArray[] weights;
    INDArray activationWeights;
    INDArray inputWeights;
    INDArray forgetWeights;
    INDArray outputWeights;
    
    //The default bias values which the weighted activations will operate on, stored as a 2 dimensional array comprised of bias vectors for each activation
    INDArray[] biases;
    INDArray activationBiases;
    INDArray inputBiases;
    INDArray forgetBiases;
    INDArray outputBiases;
    
    INDArray[] sigmoidLayers;
    INDArray[] sigmoidMult1;
    INDArray tanhLayer;
    
    
    public LSTM(int inputSize, int outputSize)
    {
        //biases should have columns for 4 activations and rows for each output     
        //weights should have entries for 4 activations, columns for each input, and rows for each output

        activationWeights = Nd4j.rand(new int[]{outputSize, inputSize + outputSize}).mul(2.0).sub(1.0);
        inputWeights = Nd4j.rand(new int[]{outputSize, inputSize + outputSize}).mul(2.0).sub(1.0);
        forgetWeights = Nd4j.rand(new int[]{outputSize, inputSize + outputSize}).mul(2.0).sub(1.0);
        outputWeights = Nd4j.rand(new int[]{outputSize, inputSize + outputSize}).mul(2.0).sub(1.0);
        this.weights = new INDArray[]{inputWeights, forgetWeights, outputWeights, activationWeights};
        
        activationBiases = Nd4j.rand(new int[]{outputSize}).mul(2.0).sub(1.0);
        inputBiases = Nd4j.rand(new int[]{outputSize}).mul(2.0).sub(1.0);
        forgetBiases = Nd4j.rand(new int[]{outputSize}).mul(2.0).sub(1.0);
        outputBiases = Nd4j.rand(new int[]{outputSize}).mul(2.0).sub(1.0);
        this.biases = new INDArray[]{inputBiases, forgetBiases, outputBiases, activationBiases};
        
        //initialize cellState and result to be of output size, and initialize result to all zeros
        this.cellState = Nd4j.create(outputSize);
        this.result = Nd4j.zeros(outputSize);
        this.sigmoidLayers = new INDArray[3];
        this.sigmoidMult1 = new INDArray[3];
    }
    
    public LSTM()
    {
        weights = new INDArray[4];
        biases = new INDArray[4];
        sigmoidLayers = new INDArray[3];
        sigmoidMult1 = new INDArray[3];
    }
    
    public void initWeights()
    {
        weights[0] = forgetWeights;
        weights[1] = inputWeights;
        weights[2] = outputWeights;
        weights[3] = activationWeights;
    }
    
    public void initBiases()
    {
        biases[0] = forgetBiases;
        biases[1] = inputBiases;
        biases[2] = outputBiases;
        biases[3] = activationBiases;
    }
    
    /**
     * Pushes an input vector through the LSTM and gets an output. Cell state and recurrent result value are updated.
     * @param input The input vector to push through
     * @return The result vector
     */
    public INDArray step(INDArray input)
    {
        
        System.out.println(input);
        //concatenate result vector onto the end of input vector, dimension zero as it is 1-dimensional INDArray
        System.out.println(result);
        input = Nd4j.concat(0, input, result);
        System.out.println(input);
        input = input.transposei();
        //System.out.println(input.rows());
        /* There are 4 layers in LSTM, in order of sig(0) sig(1) sig(2) tanh(3) */
        //For each sigmoid layer, multiply its weight matrix by the input vector, add its bias vector, and perform sigmoid operation on resultant vector
        for(int i = 0; i <  3; i++)
        {
            System.out.println(weights[i].columns());
            System.out.println(input.rows());
            System.out.println(input.columns());
            //System.out.println(weights[i].columns());
            //The "columns" of the weights 3d matrix should represent the two-dimensional matrices for each of the activations.
            if(sigmoidMult1[i] == null)
                sigmoidMult1[i] = weights[i].mmul(input);
            else
                sigmoidMult1[i] = weights[i].mmul(input);            
           if(sigmoidLayers[i] == null)
                sigmoidLayers[i] = Transforms.sigmoid(sigmoidMult1[i].transpose().add(biases[i]));

        }
        //Calculate tanh layer in same fashion as sigmoid layer, but with tanh activation function
        if(tanhLayer == null)
            tanhLayer = weights[3].mmul(input);
        else
            tanhLayer = weights[3].mmul(input);
        tanhLayer = Transforms.tanh(tanhLayer.transpose().add(biases[3]));
        
        //do the first element-wise multiplication: sigmoid layer 1 and the current cell state
        INDArray multOp1 = sigmoidLayers[0].mul(cellState);
        
        //do the second element-wise multiplication: sigmoid layer 2 and the tanh layer
        INDArray multOp2 = sigmoidLayers[1].mul(tanhLayer);

        cellState = multOp1.add(multOp2);
        
        INDArray tanhOp = Transforms.tanh(cellState);
        result = sigmoidLayers[2].mul(tanhOp);
        
        return result.dup();
    }

    @Override
    public boolean load(INDArray data, String loadPath) {
        boolean found = true;
        switch(loadPath){
            case "activate_b":  activationBiases = data;
                                break;
            case "activate_w":  activationWeights = data;
                                break;
            case "forget_b":    forgetBiases = data;
                                break;
            case "forget_w":    forgetWeights = data;
                                break;
            case "input_b":     inputBiases = data;                          
                                break;
            case "input_w":     
                                inputWeights = data;
                                break;
            case "out_b":
                                
                                outputBiases = data;
                                break;
            case "out_w":       
                                outputWeights = data;
                                break;
            case "initialstate":    cellState = data.get(NDArrayIndex.interval(0, data.length()/2));
                                    //System.out.println(cellState);
                                    result = data.get(NDArrayIndex.interval(data.length()/2, data.length()));
                                    break;
            default: found = false;
                    break;
        }
        initWeights();
        initBiases();
        return found;
    }
    
}
