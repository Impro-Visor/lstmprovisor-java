/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author cssummer16
 */
public interface Loadable {

    public static String SEPARATOR = "_";
    
    public boolean load(INDArray data, String loadPath);
    
    public default String pathCar(String loadPath)
    {
        return loadPath.replaceFirst(SEPARATOR + ".*", "");
    }
    
    public default String pathCdr(String loadPath)
    {
        return loadPath.replaceFirst("[^" + SEPARATOR + "]*" + SEPARATOR, "");
    }
}
