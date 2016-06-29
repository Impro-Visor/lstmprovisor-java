/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nickd4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author cssummer16
 */
public class ReadUtilities {
    public static INDArray readNumpyCSV(String filePath)
    {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            
            ArrayList<String[]> contents = new ArrayList<>();
            boolean reading = true;
            while(reading)
            {
                String line = reader.readLine();
                if(line != null)
                    contents.add(line.split(","));
                else
                    reading = false;
            }
            //will get an error if you don't have any data, so don't do that lol
            int[] shape = new int[]{contents.size(), contents.get(0).length};
            INDArray readArray = Nd4j.create(shape);
            for(int rowIndex = 0; rowIndex < contents.size(); rowIndex++){
                String[] line = contents.get(rowIndex);
                for(int colIndex = 0; colIndex < line.length; colIndex++){
                    readArray.putScalar(new int[]{rowIndex, colIndex}, Double.valueOf(line[colIndex]));
                }
            }
            
            return readArray;
            
            
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
