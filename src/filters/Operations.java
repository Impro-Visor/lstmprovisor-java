/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

/**
 * Enum operations specifies functionality on a number of in-place operations that can be performed on INDArrays
 * @author cssummer16
 */
public enum Operations{
        Sigmoid, Tanh, Softmax, None;
        public INDArray operate(INDArray input)
        {
            switch(this)
            {
                case Sigmoid: return Transforms.sigmoid(input, false);
                case Tanh: return Transforms.tanh(input, false);
                case Softmax: return Transforms.exp(input, false).div(input.sum(1));
                case None:
                default:
                        return input;
                    
            }
        }
    }
