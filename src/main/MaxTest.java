/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
/**
 *
 * @author cssummer16
 */
public class MaxTest {
    
    public static void main(String[] args)
    {
        INDArray test = Nd4j.create(new int[]{1, 2, 3, 4}, new int[]{4});
        System.out.println(test.max(0));
        System.out.println(test.get(NDArrayIndex.interval(0, 2)).max(0));
    }
    
}
