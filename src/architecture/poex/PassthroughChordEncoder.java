/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture.poex;

import encoding.ChordEncoder;
import static encoding.ChordEncoder.CHORD_TYPES;
import static encoding.ChordEncoder.DISTANCES_FROM_C;
import java.util.Map;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

/**
 *
 * @author cssummer16
 */
public class PassthroughChordEncoder extends ChordEncoder {
    @Override
    public AVector encode(String root, String type)
    {
        AVector chordData = CHORD_TYPES.getValue(type);
        if(chordData == null)
            return null;
        else {
            AVector allData = Vector.of(DISTANCES_FROM_C.getValue(root).intValue());
            return allData.join(chordData);
        }
    }
    
    @Override
    public String decode(AVector chordData) {
        double root = chordData.get(0);
        AVector typeData = chordData.subVector(1, chordData.length()-1);
        String type = null;
        double transposition = 0;
        for(Map.Entry<String, AVector> entry : CHORD_TYPES.entrySet()) {
            if(typeData.equals(entry.getValue())){
                type = entry.getKey();
                break;
            }
        }
        if(type == null)
        {
            throw new RuntimeException("Chord not found!");
        }
        if(("NC").equals(type))
            return "NC";
        else
            return DISTANCES_FROM_C.getKey(root) + type;
    }
}
