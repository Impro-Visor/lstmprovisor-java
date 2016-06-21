/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

/**
 *
 * @author cssummer16
 */
public enum OpType{
        Sigmoid, Tanh, Softmax, None;
        public INDArray operate(INDArray input)
        {
            switch(this)
            {
                case Sigmoid: return Transforms.sigmoid(input);
                case Tanh: return Transforms.tanh(input);
                case Softmax: INDArray output = Nd4j.create(input.shape());
                        for(int i = 0; i < input.length(); i++)
                        {
                            double sum = 0.0;
                            for(int k = 0; k < input.length(); k++)
                            {
                                sum += Math.exp(input.getDouble(k));
                            }
                            
                            output.putScalar(i, Math.exp(input.getDouble(i)) / sum);
                        }
                        return output;
                case None:
                default:
                        return input;
                    
            }
        }
    }
