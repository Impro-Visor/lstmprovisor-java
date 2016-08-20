/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture.poex;

import main.*;
import architecture.ConnectomeLoader;
import java.io.File;
import io.leadsheet.LeadsheetDataSequence;
import architecture.CompressingAutoEncoder;
import architecture.FullyConnectedLayer;
import architecture.LSTM;
import architecture.LeadsheetAutoencoderInputManager;
import architecture.Loadable;
import architecture.NameGenerator;
import architecture.poex.ProductCompressingAutoencoder;
import encoding.EncodingParameters;
import encoding.Group;
import filters.GroupedSoftMaxSampler;
import filters.Operations;
import io.leadsheet.LeadsheetIO;
import java.util.Random;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

/**
 *  Class Driver is an implementation test for CompressingAutoEncoder which reads a LeadSheet file and produces an equivalent length LeadSheet file
 * @author Nicholas Weintraut
 */
public class GenerativeProductDriver {
    private static final boolean advanceDecoding = false; //should we start decoding as soon as possible?
    
    public static void main(String[] args) {
        //here is just silly code for generating name based on an LSTM lol $wag
        NameGenerator titleNet = new NameGenerator();
        String[] unmatchedPathsNameGenerator = (new ConnectomeLoader()).load(args[3], titleNet);

        String[] notFoundNameGen = unmatchedPathsNameGenerator;
        if (notFoundNameGen.length > 0) {
            System.err.println(notFoundNameGen.length + " files were not able to be matched to the name generator network!");
            for (String fileName : notFoundNameGen) {
                System.err.println("\t" + fileName);
            }
        }
        
        String songTitle = titleNet.generateName();
        //end stupid stuff, songTitle will be used later during writeCall
        LogTimer.initStartTime();
        LogTimer.log("Generated song name: " + songTitle);
        
        
        //check if we have three arguments (first is input file path, second is output folder path)
       
        if (args.length > 2) {
            
            //Initialization
            LogTimer.initStartTime();   //start our logging timer to keep track of our execution time
            File inputFile = new File(args[0]); //load input file
            LogTimer.log("Reading file...");
            LeadsheetDataSequence inputSequence = LeadsheetIO.readLeadsheet(inputFile);  //read our leadsheet to get a data vessel as retrieved in rbm-provisor
            LogTimer.log("Instantiating generator...");
            int inputSize = 34;
            int outputSize = EncodingParameters.noteEncoder.getNoteLength();
            int featureVectorSize = 100;
            GenerativeProductModel genmodel = new GenerativeProductModel(inputSequence, outputSize, 9, featureVectorSize, 48, 84+1); //create our network
            
            //"pack" the network from weights and biases file directory
            LogTimer.log("Packing melody generator from files");
            String[] unmatchedPathsMelodyGenerator = (new ConnectomeLoader()).load(args[2], genmodel);
            
            String[] notFoundMelodyGen = unmatchedPathsMelodyGenerator;
            if(notFoundMelodyGen.length > 0)
            {
                System.err.println(notFoundMelodyGen.length + " files were not able to be matched to the melody generator network!");
                for(String fileName : notFoundMelodyGen)
                {
                    System.err.println("\t" + fileName);
                }
            }
            
            
            LeadsheetDataSequence outputSequence = inputSequence.copy();
            outputSequence.clearMelody();
            
            LogTimer.startLog("Generating...");
            while(inputSequence.hasNext()) { //iterate through time steps in input data
                outputSequence.pushStep(null, null, genmodel.step());
            }
            LogTimer.log("Writing file...");
            
            String outputFilename = args[1] + java.io.File.separator + inputFile.getName().replace(".ls", "_Output_") + songTitle; //we'll write our generated file with the same name plus "_Output"
            LeadsheetIO.writeLeadsheet(outputSequence, outputFilename, songTitle);
            System.out.println(outputFilename);
            LogTimer.log("Process finished"); //Done!

        }  
    }
}
