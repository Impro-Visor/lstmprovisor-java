/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author cssummer16
 */
public interface OutputFilter {
    public INDArray filter(INDArray input);
}
