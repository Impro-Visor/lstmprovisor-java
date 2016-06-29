/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters;

import mikera.vectorz.AVector;

/**
 * Enum operations specifies functionality on a number of in-place operations that can be performed on AVectors
 * @author cssummer16
 */
public enum Operations{
        Sigmoid, Tanh, Softmax, None;
        public AVector operate(AVector input)
        {
            switch(this)
            {
                case Sigmoid:   input.multiply(-1);
                                input.exp();
                                input.add(1.0);
                                input.reciprocal();
                                return input;
                case Tanh:      input.tanh();
                                return input;
                case Softmax:   AVector temp = input;
                                input.exp();
                                input.divide(temp.elementSum());
                                return input;
                case None:
                default:
                        return input;
                    
            }
        }
    }
