/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
//import org.nd4j.linalg.api.shape.

/**
 * Class LSTMNode implements an LSTM neural network node as described by Hochreiter and Schmidhuber (and explaiend by colah's blog).
 * The LSTMNode is currently operated via the push method, which pushes data through the recurrent node and retrieves the result vector.
 * @author Nicholas Weintraut
 */
public class LSTMNode {  
    
    //The default bias values which the weighted activations will operate on, stored as a 2 dimensional array comprised of bias vectors for each activation
    INDArray biases;
    //the state of the cell after last operation, will be fed back into node.
    INDArray cellState;
    //the result Ht of the node
    INDArray result;
    //The weights of the nodes activations, stored as a 3 dimensional array comprised of the 2 dimensional weight matrices for each activation
    INDArray weights;
    
    public LSTMNode(int inputSize, int outputSize, INDArray weights, INDArray biases)
    {
        //biases should have columns for 4 activations and rows for each output
        Nd4j.create(new int[]{4, outputSize}).checkDimensions(biases);
        this.biases = biases;
        //weights should have entries for 4 activations, columns for each input, and rows for each output
        Nd4j.create(new int[]{4, inputSize, outputSize}).checkDimensions(weights);
        this.weights = weights;
        //initialize cellState and result to be of output size, and initialize result to all zeros
        this.cellState = Nd4j.create(outputSize);
        this.result = Nd4j.zeros(outputSize);
    }
    
    /**
     * Pushes an input vector through the LSTM and gets an output. Cell state and recurrent result value are updated.
     * @param input The input vector to push through
     * @return The result vector
     */
    public INDArray step(INDArray input)
    {
        input = input.transpose();
        //System.out.println(input.rows());
        //concatenate result vector onto the end of input vector, dimension zero as it is 1-dimensional INDArray
        //input = Nd4j.concat(1, input, result);
        System.out.println(input.rows());
        
        /* There are 4 layers in LSTM, in order of sig(0) sig(1) sig(2) tanh(3) */
        //For each sigmoid layer, multiply its weight matrix by the input vector, add its bias vector, and perform sigmoid operation on resultant vector
        INDArray[] sigmoidLayers = new INDArray[3];
        for(int i = 0; i <  3; i++)
        {
            //The "columns" of the weights 3d matrix should represent the two-dimensional matrices for each of the activations.
            //System.out.println("We are about to sigmoid!");
            //System.out.println("\t We are gonna multiplying!");
            INDArray mult1 = weights.slice(0).mmul(input);
            
            //System.out.println("\t We multiplied!");
           //System.out.println("\t We are gonna add biases and sigmoid, then finish!");
            sigmoidLayers[i] = Transforms.sigmoid(mult1.transpose().add(biases.slice(0)));
            //System.out.println("We sigmoided!");
        }
        //Calculate tanh layer in same fashion as sigmoid layer, but with tanh activation function
        INDArray tanhLayer = Transforms.tanh(weights.slice(0).mmul(input).transpose().add(biases.slice(0)));
        
        //do the first element-wise multiplication: sigmoid layer 1 and the current cell state
        INDArray multOp1 = sigmoidLayers[0].mul(cellState);
        
        //do the second element-wise multiplication: sigmoid layer 2 and the tanh layer
        INDArray multOp2 = sigmoidLayers[1].mul(tanhLayer);
        //
        cellState = multOp1.add(multOp2);
        INDArray tanhOp = Transforms.tanh(cellState);
        result = sigmoidLayers[2].mul(tanhOp);

        return result.dup();
    }
    
}
