/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoding;
import java.util.Map.Entry;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 * Class ChordEncoder describes encoding and decoding procedures from chord names to bit vectors and vice-versa
 * @author Nicholas Weintraut
 */
public class ChordEncoder {
    
    public ChordEncoder(){}
    
    public INDArray encode(String root, String type)
    {
        INDArray chordData = CHORD_TYPES.getValue(type);
        //System.out.println();
        //for(int i = 0; i < chordData.length(); i++)
        //    System.out.print(chordData.getDouble(i) + " ");
        //System.out.println();
        //System.out.println(CHORD_TYPES.getKey(chordData));
        INDArray transposedData = transposeChordData(CHORD_TYPES.getValue(type), (int) DISTANCES_FROM_C.getValue(root).intValue());
        //System.out.println(decode(transposedData));
        //System.out.println();
        //for(int i = 0; i < transposedData.length(); i++)
        //    System.out.print(transposedData.getDouble(i) + " ");
        //System.out.println();
        return transposedData;
    }
    
    public String decode(INDArray chordData) {
        
        String type = null;
        boolean foundC = false;
        double transposition = 0;
        while(!foundC && transposition < 12) {
            //for(int i = 0; i < chordData.length(); i++)
            //        System.out.print(chordData.getDouble(i) + " ");
            //System.out.println("<- transposition " +  transposition);
            for(Entry<String, INDArray> entry : CHORD_TYPES.entrySet())
            {
                if(chordData.equals(entry.getValue()))
                    type = entry.getKey();
            }
            //type = CHORD_TYPES.getKey(chordData);
            if(type != null) {
                foundC = true;
            }
            else {
                chordData = transposeChordData(chordData, -1);
                transposition++;
            }
        }
        if(transposition == 12)
        {
            throw new RuntimeException("Chord not found!");
        }
        if(("NC").equals(type))
            return "NC";
        else
            return DISTANCES_FROM_C.getKey(transposition) + type;
    }
    
    public INDArray transposeChordData(INDArray chordData, int distance)
    {
        //we check if distance is zero and simply return for simplicity, but also because Nd4j has a bug where passing (length, length) gives an INDArray of size 1
        //also Nd4j concat, when given an INDArray of size 1 and another INDArray, returns a two dimensional array with only two ELEMENTS...no matter the size of the second array
        //yay nd4j $wag
        if(distance == 0)
            return chordData;
        else
        {
            INDArray part1;
            INDArray part2;
            if(distance > 0)
            {
                part1 = chordData.get(NDArrayIndex.interval(chordData.length() - (distance % chordData.length()), chordData.length()));
                part2 = chordData.get(NDArrayIndex.interval(0, chordData.length() - (distance % chordData.length())));
            }
            else
            {
                part1 = chordData.get(NDArrayIndex.interval((-1 * distance) % chordData.length(), chordData.length()));
                part2 = chordData.get(NDArrayIndex.interval(0, (-1 * distance) % chordData.length()));          
            }
            //System.out.println(part1);
            //System.out.println(part2);
            INDArray concatenated = Nd4j.concat(0, part1, part2);
            /*System.out.println();
            for(int i = 0; i < concatenated.length(); i++)
                System.out.print(concatenated.getDouble(i) + " ");
            System.out.println();*/
            return concatenated;
        }
    }
    
    public final static INDArray NO_CHORD         = Nd4j.create(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    public final static INDArray C_MAJOR          = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray C_MAJOR_7        = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1});
    public final static INDArray C_MINOR_7        = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C_DOM_7          = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C_MINOR_9        = Nd4j.create(new double[]{1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray C_13             = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0});
    public final static INDArray C_MINOR_7_FLAT_5 = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0});
    public final static INDArray C_DOM_7_SHARP_9  = Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C_DOM_7_FLAT_9   = Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C_DIM_7          = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0});
    public final static INDArray C_9              = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C_MAJOR_9        = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1});
    public final static INDArray C_DOM_7_SHARP_11 = Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C_MAJOR_7_SHARP_11= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1});
    public final static INDArray C_6              = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0});
    public final static INDArray C_7_ALT          = Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray NC              = Nd4j.create(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    public final static INDArray C		= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray CM		= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray Cm_sharp_5	= Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray Cm_plus_        = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray Cm		= Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray Cm11_sharp_5	= Nd4j.create(new double[]{1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0});
    public final static INDArray Cm11		= Nd4j.create(new double[]{1, 0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray Cm11b5          = Nd4j.create(new double[]{1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 0});
    public final static INDArray Cm13            = Nd4j.create(new double[]{1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0});
    public final static INDArray Cm6             = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0});
    public final static INDArray Cm69            = Nd4j.create(new double[]{1, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0});
    public final static INDArray Cm7_sharp_5	= Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray Cm7             = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray Cm7b5           = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0});
    public final static INDArray Ch7              = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0});
    public final static INDArray Cm9_sharp_5	= Nd4j.create(new double[]{1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray Cm9             = Nd4j.create(new double[]{1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0});
    public final static INDArray Cm9b5           = Nd4j.create(new double[]{1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0});
    public final static INDArray CmM7            = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1});
    public final static INDArray CmM7b6          = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1});
    public final static INDArray CmM9            = Nd4j.create(new double[]{1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1});
    public final static INDArray Cmadd9          = Nd4j.create(new double[]{1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray Cmb6            = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray Cmb6M7          = Nd4j.create(new double[]{1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1});
    public final static INDArray Cmb6b9          = Nd4j.create(new double[]{1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray CM_sharp_5	= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray C_plus_         = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray Caug            = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray C_plus_7        = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray CM_sharp_5add9	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray CM7_sharp_5	= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1});
    public final static INDArray CM7_plus_	= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1});
    public final static INDArray CM9_sharp_5	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1});
    public final static INDArray C_plus_add9	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0});
    public final static INDArray C7              = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C7_sharp_5	= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C7_plus_        = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray Caug7           = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C7aug           = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C7_sharp_5_sharp_9	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C7alt           = Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C7b13           = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 1, 0});
    public final static INDArray C7b5_sharp_9	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C7b5            = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0});
    public final static INDArray C7b5b13         = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C7b5b9          = Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0});
    public final static INDArray C7b5b9b13	= Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C7b6            = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 1, 0});
    public final static INDArray C7b9_sharp_11	= Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0});
    public final static INDArray C7b9_sharp_11b13	= Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C7b9            = Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C7b9b13_sharp_11	= Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C7b9b13         = Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 0, 1, 1, 0, 1, 0});
    public final static INDArray C7no5           = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0});
    public final static INDArray C7_sharp_11	= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0});
    public final static INDArray C7_sharp_11b13	= Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C7_sharp_5b9_sharp_11	= Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0});
    public final static INDArray C7_sharp_5b9            = Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C7_sharp_9_sharp_11	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0});
    public final static INDArray C7_sharp_9_sharp_11b13	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C7_sharp_9	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C7_sharp_9b13	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0});
    public final static INDArray C9              = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0});
    public final static INDArray C9_sharp_5	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C9_plus_        = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0});
    public final static INDArray C9_sharp_11	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0});
    public final static INDArray C9_sharp_11b13	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C9_sharp_5_sharp_11	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0});
    public final static INDArray C9b13           = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0});
    public final static INDArray C9b5            = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0});
    public final static INDArray C9b5b13         = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0});
    public final static INDArray C9no5           = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0});
    public final static INDArray C13_sharp_11	= Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 1, 0});
    public final static INDArray C13_sharp_9_sharp_11	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0});
    public final static INDArray C13_sharp_9	= Nd4j.create(new double[]{1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0});
    public final static INDArray C13             = Nd4j.create(new double[]{1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0});
    public final static INDArray C13b5           = Nd4j.create(new double[]{1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0});
    public final static INDArray C13b9_sharp_11	= Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0});
    public final static INDArray C13b9           = Nd4j.create(new double[]{1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0});
    public final static INDArray CMsus2          = Nd4j.create(new double[]{1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray CMsus4          = Nd4j.create(new double[]{1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0});
    public final static INDArray Csus2           = Nd4j.create(new double[]{1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0});
    public final static INDArray Csus4           = Nd4j.create(new double[]{1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0});
    public final static INDArray Csusb9          = Nd4j.create(new double[]{1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0});
    public final static INDArray C7b9b13sus4	= Nd4j.create(new double[]{1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0});
    public final static INDArray C7b9sus         = Nd4j.create(new double[]{1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0});
    public final static INDArray C7b9sus4        = Nd4j.create(new double[]{1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C7sus           = Nd4j.create(new double[]{1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C7sus4          = Nd4j.create(new double[]{1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C7sus4b9        = Nd4j.create(new double[]{1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C7sus4b9b13	= Nd4j.create(new double[]{1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0});
    public final static INDArray C7susb9         = Nd4j.create(new double[]{1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C9sus4          = Nd4j.create(new double[]{1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C9sus           = Nd4j.create(new double[]{1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C11             = Nd4j.create(new double[]{1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0});
    public final static INDArray C13sus          = Nd4j.create(new double[]{1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 0});
    public final static INDArray C13sus4         = Nd4j.create(new double[]{1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 1, 0});
    public final static INDArray CBlues          = Nd4j.create(new double[]{1, 0, 0, 1, 0, 1, 1, 1, 0, 0, 1, 0});
    public final static INDArray CBass           = Nd4j.create(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    
    public final static BidirectionalHashMap<String, INDArray> CHORD_TYPES = new BidirectionalHashMap<>();
    static {
        CHORD_TYPES.put("NC", NO_CHORD);
        CHORD_TYPES.put("", C_MAJOR);
        CHORD_TYPES.put("M", CM);
        CHORD_TYPES.put("M7", C_MAJOR_7);
        CHORD_TYPES.put("m7", C_MINOR_7);
        CHORD_TYPES.put("7", C_DOM_7);
        CHORD_TYPES.put("m9", C_MINOR_9);
        CHORD_TYPES.put("13", C_13);
        CHORD_TYPES.put("m7b5", C_MINOR_7_FLAT_5);
        CHORD_TYPES.put("7#9", C_DOM_7_SHARP_9);
        CHORD_TYPES.put("7b9", C_DOM_7_FLAT_9);
        CHORD_TYPES.put("o7", C_DIM_7);
        CHORD_TYPES.put("9", C_9);
        CHORD_TYPES.put("M9", C_MAJOR_9);
        CHORD_TYPES.put("7#11", C_DOM_7_SHARP_11);
        CHORD_TYPES.put("M7#11", C_MAJOR_7_SHARP_11);
        CHORD_TYPES.put("6", C_6);
        CHORD_TYPES.put("7alt", C_7_ALT);
        CHORD_TYPES.put("", C);
        CHORD_TYPES.put("m#5", Cm_sharp_5);
        CHORD_TYPES.put("m+", Cm_plus_);
        CHORD_TYPES.put("m", Cm);
        CHORD_TYPES.put("m11#5", Cm11_sharp_5);
        CHORD_TYPES.put("m11", Cm11);
        CHORD_TYPES.put("m11b5", Cm11b5);
        CHORD_TYPES.put("m13", Cm13);
        CHORD_TYPES.put("m6", Cm6);
        CHORD_TYPES.put("m69", Cm69);
        CHORD_TYPES.put("m7#5", Cm7_sharp_5);
        CHORD_TYPES.put("m7", Cm7);
        CHORD_TYPES.put("m7b5", Cm7b5);
        CHORD_TYPES.put("h7", Ch7);
        CHORD_TYPES.put("m9#5", Cm9_sharp_5);
        CHORD_TYPES.put("m9", Cm9);
        CHORD_TYPES.put("m9b5", Cm9b5);
        CHORD_TYPES.put("mM7", CmM7);
        CHORD_TYPES.put("mM7b6", CmM7b6);
        CHORD_TYPES.put("mM9", CmM9);
        CHORD_TYPES.put("madd9", Cmadd9);
        CHORD_TYPES.put("mb6", Cmb6);
        CHORD_TYPES.put("mb6M7", Cmb6M7);
        CHORD_TYPES.put("mb6b9", Cmb6b9);
        CHORD_TYPES.put("M#5", CM_sharp_5);
        CHORD_TYPES.put("+", C_plus_);
        CHORD_TYPES.put("aug", Caug);
        CHORD_TYPES.put("+7", C_plus_7);
        CHORD_TYPES.put("M#5add9", CM_sharp_5add9);
        CHORD_TYPES.put("M7#5", CM7_sharp_5);
        CHORD_TYPES.put("M7+", CM7_plus_);
        CHORD_TYPES.put("M9#5", CM9_sharp_5);
        CHORD_TYPES.put("+add9", C_plus_add9);
        CHORD_TYPES.put("7", C7);
        CHORD_TYPES.put("7#5", C7_sharp_5);
        CHORD_TYPES.put("7+", C7_plus_);
        CHORD_TYPES.put("aug7", Caug7);
        CHORD_TYPES.put("7aug", C7aug);
        CHORD_TYPES.put("7#5#9", C7_sharp_5_sharp_9);
        CHORD_TYPES.put("7alt", C7alt);
        CHORD_TYPES.put("7b13", C7b13);
        CHORD_TYPES.put("7b5#9", C7b5_sharp_9);
        CHORD_TYPES.put("7b5", C7b5);
        CHORD_TYPES.put("7b5b13", C7b5b13);
        CHORD_TYPES.put("7b5b9", C7b5b9);
        CHORD_TYPES.put("7b5b9b13", C7b5b9b13);
        CHORD_TYPES.put("7b6", C7b6);
        CHORD_TYPES.put("7b9#11", C7b9_sharp_11);
        CHORD_TYPES.put("7b9#11b13", C7b9_sharp_11b13);
        CHORD_TYPES.put("7b9", C7b9);
        CHORD_TYPES.put("7b9b13#11", C7b9b13_sharp_11);
        CHORD_TYPES.put("7b9b13", C7b9b13);
        CHORD_TYPES.put("7no5", C7no5);
        CHORD_TYPES.put("7#11", C7_sharp_11);
        CHORD_TYPES.put("7#11b13", C7_sharp_11b13);
        CHORD_TYPES.put("7#5b9#11", C7_sharp_5b9_sharp_11);
        CHORD_TYPES.put("7#5b9", C7_sharp_5b9);
        CHORD_TYPES.put("7#9#11", C7_sharp_9_sharp_11);
        CHORD_TYPES.put("7#9#11b13", C7_sharp_9_sharp_11b13);
        CHORD_TYPES.put("7#9", C7_sharp_9);
        CHORD_TYPES.put("7#9b13", C7_sharp_9b13);
        CHORD_TYPES.put("9", C9);
        CHORD_TYPES.put("9#5", C9_sharp_5);
        CHORD_TYPES.put("9+", C9_plus_);
        CHORD_TYPES.put("9#11", C9_sharp_11);
        CHORD_TYPES.put("9#11b13", C9_sharp_11b13);
        CHORD_TYPES.put("9#5#11", C9_sharp_5_sharp_11);
        CHORD_TYPES.put("9b13", C9b13);
        CHORD_TYPES.put("9b5", C9b5);
        CHORD_TYPES.put("9b5b13", C9b5b13);
        CHORD_TYPES.put("9no5", C9no5);
        CHORD_TYPES.put("13#11", C13_sharp_11);
        CHORD_TYPES.put("13#9#11", C13_sharp_9_sharp_11);
        CHORD_TYPES.put("13#9", C13_sharp_9);
        CHORD_TYPES.put("13", C13);
        CHORD_TYPES.put("13b5", C13b5);
        CHORD_TYPES.put("13b9#11", C13b9_sharp_11);
        CHORD_TYPES.put("13b9", C13b9);
        CHORD_TYPES.put("Msus2", CMsus2);
        CHORD_TYPES.put("Msus4", CMsus4);
        CHORD_TYPES.put("sus2", Csus2);
        CHORD_TYPES.put("sus4", Csus4);
        CHORD_TYPES.put("susb9", Csusb9);
        CHORD_TYPES.put("7b9b13sus4", C7b9b13sus4);
        CHORD_TYPES.put("7b9sus", C7b9sus);
        CHORD_TYPES.put("7b9sus4", C7b9sus4);
        CHORD_TYPES.put("7sus", C7sus);
        CHORD_TYPES.put("7sus4", C7sus4);
        CHORD_TYPES.put("7sus4b9", C7sus4b9);
        CHORD_TYPES.put("7sus4b9b13", C7sus4b9b13);
        CHORD_TYPES.put("7susb9", C7susb9);
        CHORD_TYPES.put("9sus4", C9sus4);
        CHORD_TYPES.put("9sus", C9sus);
        CHORD_TYPES.put("11", C11);
        CHORD_TYPES.put("13sus", C13sus);
        CHORD_TYPES.put("13sus4", C13sus4);
        CHORD_TYPES.put("Blues", CBlues);
        CHORD_TYPES.put("Bass", CBass);
    }
    
    public final static BidirectionalHashMap<String, Double> DISTANCES_FROM_C = new BidirectionalHashMap<>();
    static {
        DISTANCES_FROM_C.put("C", 0.0);
        DISTANCES_FROM_C.put("C#", 1.0);
        DISTANCES_FROM_C.put("Db", 1.0);
        DISTANCES_FROM_C.put("D", 2.0);
        DISTANCES_FROM_C.put("D#", 3.0);
        DISTANCES_FROM_C.put("Eb", 3.0);
        DISTANCES_FROM_C.put("E", 4.0);
        DISTANCES_FROM_C.put("F", 5.0);
        DISTANCES_FROM_C.put("F#", 6.0);
        DISTANCES_FROM_C.put("Gb", 6.0);
        DISTANCES_FROM_C.put("G", 7.0);
        DISTANCES_FROM_C.put("G#", 8.0);
        DISTANCES_FROM_C.put("Ab", 8.0);
        DISTANCES_FROM_C.put("A", 9.0);
        DISTANCES_FROM_C.put("A#", 10.0);
        DISTANCES_FROM_C.put("Bb", 10.0);
        DISTANCES_FROM_C.put("B", 11.0);
        // no chord ("NC"): no transposition
        DISTANCES_FROM_C.putKeyToValueOnly("NC", 0.0);
    }
}
