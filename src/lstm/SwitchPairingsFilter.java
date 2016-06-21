/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lstm;

import org.nd4j.linalg.api.ndarray.INDArray;
import encoding.NoteEncodings;
import encoding.Group;
import rbm.Params;

/**
 *  This is a filter to clean unnecessary bits out of output data based on the current note encoding's switch pairings
 * @author cssummer16
 */
public class SwitchPairingsFilter implements OutputFilter {
    
    public INDArray filter(INDArray input)
    {
        NoteEncodings.SwitchPairing[] pairings = Params.noteEncoding.getSwitchPairings();
        for(int i = 0; i < pairings.length; i++)
        {
            if(input.getDouble(pairings[i].getIndex()) == 0.0)
            {
                Group switchGroup = Params.noteEncoding.getGroups()[pairings[i].getGroupIndex()];
                for(int j = switchGroup.startIndex; j < switchGroup.endIndex; j++)
                {
                    input.putScalar(j, 0.0);
                }
            }
        }
        return input;
    }
    
}
