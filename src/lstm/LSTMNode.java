/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.api.ndarray.*;
//import org.nd4j.linalg.api.shape.

/**
 *
 * @author cssummer16
 */
public class LSTMNode {  
    
    INDArray recurrence;
    INDArray result;
    double[] weights;
    
    public LSTMNode()
    {
        
    }
    
    public INDArray push(INDArray input)
    {
        //INDArray sig = Transforms.sigmoid(, true);
        //INDArray tanh = Transforms.tanh(input, false);
        //INDArray mult = sig.dup().mul(recurrence);
        return null;
    }
    
}
