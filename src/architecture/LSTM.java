/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import filters.Operations;
import java.util.Arrays;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.arrayz.INDArray;
//import org.nd4j.linalg.api.shape.

/**
 * Class LSTMNode implements an LSTM neural network node as described by Hochreiter and Schmidhuber (and explaiend by colah's blog).
 * The LSTMNode is currently operated via the push method, which pushes data through the recurrent node and retrieves the result vector.
 * @author Nicholas Weintraut
 */
public class LSTM implements Loadable{  
    
    
    //the state of the cell after last operation, will be fed back into node.
    public AVector cellState;
    //the result Ht of the node
    public AVector result;
    //The weights of the nodes activations, stored as a 3 dimensional array comprised of the 2 dimensional weight matrices for each activation
    AMatrix[] weights;
    AMatrix activationWeights;
    AMatrix inputWeights;
    AMatrix forgetWeights;
    AMatrix outputWeights;
    
    //The default bias values which the weighted activations will operate on, stored as a 2 dimensional array comprised of bias vectors for each activation
    AVector[] biases;
    AVector activationBiases;
    AVector inputBiases;
    AVector forgetBiases;
    AVector outputBiases;
    
    AVector[] sigmoidLayers;
    AVector[] sigmoidMult1;
    AVector tanhLayer;
    
    public LSTM()
    {
        weights = new AMatrix[4];
        biases = new AVector[4];
        sigmoidLayers = new AVector[3];
        sigmoidMult1 = new AVector[3];
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
    public AVector step(AVector input)
    {
        return this.step(input, false);
    }
    
    public AVector step(AVector input, boolean printInternals)
    {

        //concatenate result vector onto the end of input vector, dimension zero as it is 1-dimensional AVector
        //System.out.println(input);
        //System.out.println(result);
        input = input.join(result);
        if(input.length() != weights[0].getShape(1)){
            throw new RuntimeException("Input was the wrong shape! Input ( + last out) was "
                                        + input.length()
                                        + "but weights were "
                                        + weights[0].getShape(1));
        }
        //System.out.println(input);
        //System.out.println(input.rows());
        /* There are 4 layers in LSTM, in order of sig(0) sig(1) sig(2) tanh(3) */
        //For each sigmoid layer, multiply its weight matrix by the input vector, add its bias vector, and perform sigmoid operation on resultant vector
        for(int i = 0; i <  3; i++)
        {
            //System.out.println(weights[i].columns());
            //The "columns" of the weights 3d matrix should represent the two-dimensional matrices for each of the activations.
            sigmoidMult1[i] = weights[i].innerProduct(input);
            //System.out.println(sigmoidMult1[i]);
            sigmoidMult1[i].add(biases[i]);      
            //System.out.println(sigmoidMult1[i]);
            sigmoidLayers[i] = Operations.Sigmoid.operate(sigmoidMult1[i]);
            //System.out.println(sigmoidLayers[i]);
            if(printInternals)
            {
                System.out.println( "sigmoidLayers[" + i + "]: " + sigmoidLayers[i]);
                System.out.println("sigmoidLayers[" + i + "] shape: " + Arrays.toString(sigmoidLayers[i].getShape()));
            }
        }
        //Calculate tanh layer in same fashion as sigmoid layer, but with tanh activation function
        tanhLayer = weights[3].innerProduct(input);
        tanhLayer.add(biases[3]);
        tanhLayer = Operations.Tanh.operate(tanhLayer);
        if(printInternals)
            System.out.println( "tanhLayer: " + tanhLayer);
        
        //do the first element-wise multiplication: sigmoid layer 1 and the current cell state
        sigmoidLayers[0].multiply(cellState);
        //System.out.println("multiplication of forget and cell state: " + sigmoidLayers[0]);
        //do the second element-wise multiplication: sigmoid layer 2 and the tanh layer
        sigmoidLayers[1].multiply(tanhLayer);
        //System.out.println("multiplication of input and activation layer: " + sigmoidLayers[1]);
        sigmoidLayers[0].add(sigmoidLayers[1]);
        //System.out.println("addition of " + sigmoidLayers[1]);
        cellState = sigmoidLayers[0];
        if(printInternals)
            System.out.println( "next cellState: " + cellState);
        sigmoidLayers[0] = null;
        sigmoidLayers[1] = null;
        
        AVector tanhOp = Operations.Tanh.operate(cellState.copy());
        sigmoidLayers[2].multiply(tanhOp);
        result = sigmoidLayers[2];
        sigmoidLayers[2] = null;
        
        return result.copy();
    }
    
    public String toString()
    {
        return "CellState: " + cellState.toStringFull() + 
                "\n LastResult: " +result.toStringFull() +
                "\n Activation biases: " + activationBiases.toStringFull() +
                "\n Activation weight shape: " + Arrays.toString(activationWeights.getShape()) +
                "\n Forget biases: " + forgetBiases.toStringFull() +
                "\n Forget weight shape: " + Arrays.toString(forgetWeights.getShape()) + 
                "\n Input biases: " + inputBiases.toStringFull() + 
                "\n Input weight shape: " + Arrays.toString(inputWeights.getShape()) + 
                "\n Output biases: " + outputBiases.toStringFull() +
                "\n Output weight shape: " + Arrays.toString(outputWeights.getShape());
        
    }

    @Override
    public boolean load(INDArray data, String loadPath) {
        boolean found = true;
        switch(loadPath){
            case "activate_b":  activationBiases = (AVector) data;
                                break;
            case "activate_w":  activationWeights = (AMatrix) data;
                                break;
            case "forget_b":    forgetBiases = (AVector) data;
                                break;
            case "forget_w":    forgetWeights = (AMatrix) data;
                                break;
            case "input_b":     inputBiases = (AVector) data;                          
                                break;
            case "input_w":     
                                inputWeights = (AMatrix) data;
                                break;
            case "out_b":
                                
                                outputBiases = (AVector) data;
                                break;
            case "out_w":       
                                outputWeights = (AMatrix) data;
                                break;
            case "initialstate":    AVector dataCast = (AVector) data;
                                    cellState = dataCast.subVector(0, (dataCast.length()/2)).dense();
                                    //System.out.println(cellState);
                                    result = dataCast.subVector(dataCast.length()/2, dataCast.length()/2).dense();
                                    break;
            default: found = false;
                    break;
        }
        initWeights();
        initBiases();
        return found;
    }
    
}
