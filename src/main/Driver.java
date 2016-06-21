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
        
        /*chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return (f.getName().endsWith(".ls"));
            }

            public String getDescription() {
                return "Leadsheet files (.ls)";
            }
        });
        int status = chooser.showDialog(null, "Choose an input file!");
        */
        if (/*status == JFileChooser.APPROVE_OPTION*/args.length > 1) {
            LogTimer.initStartTime();
            //now we'll load file
            //System.out.println("Loading file!");
            File inputFile = new File(args[0]);
            
            LogTimer.log("Reading file...");
            DataVessel inputContents = LeadSheetHandler.parseLeadSheetLick(/*chooser.getSelectedFile()*/inputFile);
            LogTimer.log("Instantiating autoencoder...");
            int inputSize = inputContents.getNumCols() + inputContents.getNumChordColumns();
            int outputSize = inputContents.getNumCols();
            CompressingAutoencoder autoencoder = new CompressingAutoencoder(inputSize, outputSize, 200, 150, 200, 150, 100, outputSize);
            /* We'll now generate input vectors from the DataVessel and feed them into the network*/
            //get melody data
            int[] melody = inputContents.getMelody();
            //get chord data
            int[] chords = inputContents.getChords();
            
            LogTimer.startLog("Encoding data...");
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
            LogTimer.endLog();
            LogTimer.startLog("Decoding data...");
            int[] newMelody = new int[melody.length];
            //reset melodyKernel for writing new melody vector
            melodyKernel = 0;
            //System.out.println("We're gonna decode!");
            //generate final melody activations and write them to newMelody array
            for(int i = 0; i < inputContents.getNumRows(); i++)
            {
                /* Autoencoder output has already been encoded according to note encoding data */
                INDArray outputVector = autoencoder.decodeStep();
                
                //write final vectors into new melody array
                for(int j = 0; j < outputSize; j++) {
                    newMelody[melodyKernel+j] = outputVector.getInt(j);
                }
                melodyKernel += outputSize;
            }
            LogTimer.endLog();
            //System.out.println("About to prompt user!");
            DataVessel outputVessel = new DataVessel(newMelody, chords, newMelody.length / outputSize, outputSize);
            LogTimer.log("Writing file...");
            //outputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //int outputStatus = outputChooser.showDialog(null, "Choose an output directory!");
            if (/*outputStatus == JFileChooser.APPROVE_OPTION*/true) {
                String outputFilename = args[1] + java.io.File.separator + inputFile.getName().replace(".ls", "_Output");
                LeadSheetHandler.writeLeadSheet(outputVessel, leadsheet.Constants.BEAT / leadsheet.Constants.RESOLUTION_SCALAR, outputFilename);
                System.out.println(outputFilename);
            }
            LogTimer.log("Process finished");
        }  
    }
}
