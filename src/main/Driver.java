/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import javax.swing.JFileChooser;
import leadsheet.LeadSheetHandler;
import rbm.DataVessel;
import architecture.CompressingAutoencoder;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import rbm.Params;
import encoding.Group;
import java.util.Random;

/**
 *
 * @author cssummer16
 */
public class Driver {
    private static JFileChooser chooser = new JFileChooser();
    private static JFileChooser outputChooser = new JFileChooser();
    public static void main(String[] args)
    {
        
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return (f.getName().endsWith(".ls"));
            }

            public String getDescription() {
                return "Leadsheet files (.ls)";
            }
        });
        int status = chooser.showDialog(null, "Choose an input file!");
        if (status == JFileChooser.APPROVE_OPTION) {
            long startTime = System.nanoTime();
            //now we'll load file
            //System.out.println("Loading file!");
            DataVessel inputContents = LeadSheetHandler.parseLeadSheetLick(chooser.getSelectedFile());
            int inputSize = inputContents.getNumCols() + inputContents.getNumChordColumns();
            int outputSize = inputContents.getNumCols();
            CompressingAutoencoder autoencoder = new CompressingAutoencoder(inputSize, 200, outputSize);
            /* We'll now generate input vectors from the DataVessel and feed them into the network*/
            //get melody data
            int[] melody = inputContents.getMelody();
            //get chord data
            int[] chords = inputContents.getChords();
            

            //these kernel variables will keep track of our position in each of the arrays
            int melodyKernel = 0;
            int chordKernel = 0;
            //System.out.println("Starting encoding!");
            //for each row of input (getNumRows retrieves the number of melody bit vectors, we are assuming it matches with number of columns)
            for(int i = 0; i < inputContents.getNumRows(); i++)
            {
                //create an inputVector
                INDArray inputVector = Nd4j.create(inputSize);
                for(int j = 0; j < inputContents.getNumCols(); j++)
                {
                    inputVector.putScalar(j, melody[j + melodyKernel]);
                }
                for(int j = 0; j < inputContents.getNumChordColumns(); j++)
                {
                    inputVector.putScalar(j + inputContents.getNumCols(), chords[j+chordKernel]);
                }
                melodyKernel += inputContents.getNumCols();
                chordKernel += inputContents.getNumChordColumns();
                //System.out.println("encoding step " + i + "!");
                //feed the vector into the network
                autoencoder.encodeStep(inputVector);
            }
            
            int[] newMelody = new int[melody.length];
            //reset melodyKernel for writing new melody vector
            melodyKernel = 0;
            //System.out.println("We're gonna decode!");
            //generate final melody activations and write them to newMelody array
            for(int i = 0; i < inputContents.getNumRows(); i++)
            {
                /* Apply group en-codings for one-hot and normal groups */
                INDArray outputVector = autoencoder.decodeStep();
                int groupKernel = 0;
                for(Group group : Params.noteEncoding.getGroups()) {
                    if(group.isOneHot()) {
                        double[] gOutput = new double[outputVector.length()];
                        for(int j = 0;  j < group.length(); j++) {
                            gOutput[j] = outputVector.getDouble(j + group.startIndex);
                        }
                        double sum = 0.0;
                        for(int j = 0; j < gOutput.length; j++) {
                            sum += gOutput[j];
                        }
                        for(int j = 0; j < gOutput.length; j++) {
                            gOutput[j] = gOutput[j] / sum;
                        }
                        int index = 0;
                        double randPoint = (new Random()).nextDouble();
                        while(randPoint > 0.0 && index < gOutput.length - 1) {
                            if(gOutput[index] < randPoint)
                                randPoint -= gOutput[index++];
                            else
                                randPoint -= gOutput[index];
                        }
                        for(int j = 0; j < group.length(); j++) {
                            if(j != index) {
                                outputVector.putScalar(groupKernel + j, 0.0);
                            }
                            else {
                                outputVector.putScalar(groupKernel + j, 1.0);
                            }
                        }
                    }
                    else {
                        double[] gOutput = new double[outputVector.length()];
                        for(int j = 0;  j < group.length(); j++) {
                            gOutput[j] = outputVector.getDouble(j + group.startIndex);
                        }
                        Random rand = new Random();
                        for(int j = 0; j < group.length(); j++) {
                            double nextDouble = rand.nextDouble();
                            //System.out.println("(" + gOutput[j] + ", " + nextDouble + ")");
                            if(gOutput[j] >= nextDouble)
                            {
                                outputVector.putScalar(groupKernel + j, 1.0);
                            }
                            else {
                                outputVector.putScalar(groupKernel + j, 0.0);
                            }
                        }
                    }
                    
                    groupKernel += group.length();
                }
                //System.out.print("[");
                //for(int k = 0; k < outputVector.length(); k++)
                //{
                //    System.out.print(outputVector.getInt(k) + " ");
                //}
               // System.out.println("]");
                
                //write final vectors into new melody array
                for(int j = 0; j < outputSize; j++) {
                    newMelody[melodyKernel+j] = outputVector.getInt(j);
                }
                melodyKernel += outputSize;
            }
            //System.out.println("About to prompt user!");
            DataVessel outputVessel = new DataVessel(newMelody, chords, newMelody.length / outputSize, outputSize);
            long endTime = System.nanoTime();
            System.out.println("Reading, encoding, decoding and writing took " + (endTime - startTime) / 1000000000.0 + " seconds.");
            outputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int outputStatus = outputChooser.showDialog(null, "Choose an output directory!");
            if (outputStatus == JFileChooser.APPROVE_OPTION) {
                String filename = outputChooser.getSelectedFile().getPath() + java.io.File.separator + chooser.getSelectedFile().getName().replace(".ls", "_Output");
                LeadSheetHandler.writeLeadSheet(outputVessel, leadsheet.Constants.BEAT / leadsheet.Constants.RESOLUTION_SCALAR, filename);
                System.out.println(filename);
            }
        }  
    }  
}
