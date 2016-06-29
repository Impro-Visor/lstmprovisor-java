/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import architecture.NetworkMeatPacker;
import java.io.File;
import io.leadsheet.LeadSheetDataSequence;
import architecture.CompressingAutoEncoder;
import architecture.FullyConnectedLayer;
import architecture.LSTM;
import org.nd4j.linalg.api.ndarray.INDArray;
import architecture.LeadsheetAutoencoderInputManager;
import architecture.Loadable;
import encoding.EncodingParameters;
import encoding.Group;
import filters.GroupedSoftMaxSampler;
import filters.Operations;
import io.leadsheet.LeadSheetIO;
import java.util.Random;
import org.nd4j.linalg.factory.Nd4j;

/**
 *  Class Driver is an implementation test for CompressingAutoEncoder which reads a LeadSheet file and produces an equivalent length LeadSheet file
 * @author Nicholas Weintraut
 */
public class Driver {
    private static final boolean advanceDecoding = true; //should we start decoding as soon as possible?
    
    public static void main(String[] args) {
        
        //here is just silly code for generating name based on an LSTM lol $wag
        LSTM lstm = new LSTM();
        FullyConnectedLayer fullLayer = new FullyConnectedLayer(Operations.None);
        Loadable titleNetLoader = new Loadable(){
            @Override
            public boolean load(INDArray array, String path)
            {
                String car = pathCar(path);
                String cdr = pathCdr(path);
                switch(car)
                {
                    case "full": return fullLayer.load(array, cdr);
                    case "lstm": return lstm.load(array, cdr);
                    default:
                        return false;
                }
            }
        };
        
        String[] notFound1 = (new NetworkMeatPacker()).pack(args[3], titleNetLoader);
        for(String name : notFound1)
            System.out.println(name);
        
        Random rand = new Random();
        
        INDArray charOut = Nd4j.create(new double[27]);
        charOut.putScalar(0, 1.0);
        GroupedSoftMaxSampler sampler = new GroupedSoftMaxSampler(new Group[]{new Group(0, 27, true)});
        String songTitle = "";
        for(int i = 0; i < 8; i++)
        {
            charOut = sampler.filter(fullLayer.forward(lstm.step(charOut)));
            int charIndex = 0;
            for(; charIndex < charOut.length(); charIndex++)
            {
                if(charOut.getDouble(charIndex) == 1.0)
                    break;
            }
            if(charIndex >= 26)
                songTitle += " ";
            else
                songTitle += ((char) ('a' + charIndex));
        }
        //end stupid stuff, songTitle will be used later during writeCall
        LogTimer.initStartTime();
        LogTimer.log("Generated song name: " + songTitle);
        
        System.out.println(Double.valueOf("4.652957618236541748e-02"));
        //check if we have three arguments (first is input file path, second is output folder path)
        if (args.length > 2) {
            
            /*Initialization*/
            LogTimer.initStartTime();   //start our logging timer to keep track of our execution time
            File inputFile = new File(args[0]); //load input file
            LogTimer.log("Reading file...");
            LeadSheetDataSequence inputSequence = LeadSheetIO.readLeadSheet(inputFile);  //read our leadsheet to get a data vessel as retrieved in rbm-provisor
            LogTimer.log("Instantiating autoencoder...");
            int inputSize = 34;
            int outputSize = EncodingParameters.noteEncoder.getNoteLength();
            int featureVectorSize = 100;
            CompressingAutoEncoder autoencoder = new CompressingAutoEncoder(new LeadsheetAutoencoderInputManager(EncodingParameters.noteEncoder.getNoteLength()), inputSize, outputSize, featureVectorSize); //create our network
            
            //"pack" the network from weights and biases file directory
            LogTimer.log("Packing autoencoder from files");
            String[] notFound = (new NetworkMeatPacker()).pack(args[2], autoencoder);
            if(notFound.length > 0)
            {
                System.err.println(notFound.length + " files were not able to be matched to the architecture!");
                for(String fileName : notFound)
                {
                    System.err.println("\t" + fileName);
                }
            }
            LeadSheetDataSequence outputSequence = inputSequence.dup();
            int j = 0;
            /*while(outputSequence.hasMelodyLeft())
            {
                INDArray melodyPop = outputSequence.pollMelody();
                for(int i = 0; i < melodyPop.length(); i++)
                    System.out.print(melodyPop.getDouble(i) + " ");
                System.out.println("<- encoded melody step " + j);
                j++;
            }*/
            outputSequence.clearMelody();
            
            
            
            LogTimer.startLog("Encoding data...");
            INDArray cellState = autoencoder.getDecoderLSTM1().cellState;
            for(int i = 0; i < cellState.length(); i++)
                System.out.print(cellState.getDouble(i));
            System.out.println("<- cellState");
            //TradingTimer.initStart(); //start our trading timer to keep track our our generation versus realtime play
            while(inputSequence.hasNext()) { //iterate through time steps in input data
                INDArray inputVector = inputSequence.retrieve();
                //TradingTimer.waitForNextTimedInput();
                autoencoder.encodeStep(inputVector); //feed the resultant input vector into the network
                if(advanceDecoding) { //if we are using advance decoding (we start decoding as soon as we can)
                    if(autoencoder.canDecode()) { //if queue has enough data to decode from
                        outputSequence.pushStep(null, null, autoencoder.decodeStep()); //take sampled data for a timestep from autoencoder
                        //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are
                    }
                }
            }
            while(autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                    outputSequence.pushStep(null, null, autoencoder.decodeStep()); //take sampled data for a timestep from autoencoder
                    //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are       
            }
            LogTimer.log("Writing file...");
            
            String outputFilename = args[1] + java.io.File.separator + inputFile.getName().replace(".ls", "_Output"); //we'll write our generated file with the same name plus "_Output"
            LeadSheetIO.writeLeadSheet(outputSequence, outputFilename, songTitle);
            System.out.println(outputFilename);
            LogTimer.log("Process finished"); //Done!

        }  
    }
}
