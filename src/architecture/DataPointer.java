/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mikera.arrayz.INDArray;

/**
 *
 * @author Nick
 */
public class DataPointer {
    Consumer<INDArray> setter;
    //Supplier<INDArray> getter;
    public DataPointer(Consumer<INDArray> setter){
        this.setter = setter;
    }
    
    public void set(INDArray data){
        setter.accept(data);
    }
}
