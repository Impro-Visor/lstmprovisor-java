/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import mikera.vectorz.*;
import mikera.matrixx.*;

/**
 *
 * @author cssummer16
 */
public class VectorzTest {
    public static void main(String[] args)
    {
        
        Matrix m1 = Matrixx.create(new double[][]{new double[]{1.0, 2.0}, new double[]{0.3, 1.0}});
        AVector v = Vector.of(0.1, 0.9);

        System.out.println("m: \n" + m1 + "\n");
        System.out.println("v: \n" + v + "\n");
        
        //use inner product for any matrix multiplication, no need to transpose vectors :)
        Vector v1 = m1.innerProduct(v);
        System.out.println("(m * v): \n" + v1 + "\n");
        
        //some operations, such as tanh, are in-place. There is no sigmoid or softmax, but they can be implemented
        v1.tanh();
        System.out.println("(m * v).tanh(): \n" + v1 + "\n");
        
        //lets try making softmax
        Vector v2 = Vector.of(1.0, 3.0, 4.0, 2.0, 1.0);
        Vector softMaxxedV2 = v2.clone();
        softMaxxedV2.exp();
        softMaxxedV2.divide(v2.elementSum());
        System.out.println("Soft-maxed (m * v): \n" + softMaxxedV2);
        System.out.println("Soft-maxed (m * v) sum: \n" + softMaxxedV2.elementSum());
        
        //v2.divide(0);
        
        v = v.getTransposeCopy();
        System.out.println("v transposed: \n" + v);
        
        Matrix m2 = Matrixx.create(new double[][]{new double[]{3.0, 4.0, 1.0, 2.0}, new double[]{2.5, 2.5, 5.0, 1.3}});
        Matrix m3 = Matrixx.create(new double[][]{new double[]{1.0, 2.0}, new double[]{1.0, 2.0}, new double[]{3.0, 4.0}, new double[]{4.0, 5.0}});
        Matrix m4 = m2.innerProduct(m3);
        System.out.println("mmul1 result: \n" + m4 + "\n");
        Matrix m5 = m3.innerProduct(m2);
        System.out.println("mmul2 result: \n" + m5 + "\n");
    }
}
