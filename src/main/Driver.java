/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import architecture.NetworkMeatPacker;
import java.io.File;
import io.leadsheet.LeadSheetDataSequence;
import architecture.FragmentedNeuralQueue;
import architecture.FullyConnectedLayer;
import architecture.LSTM;
import architecture.Loadable;
import architecture.poex.ProductCompressingAutoEncoder;
import encoding.EncodingParameters;
import encoding.Group;
import filters.GroupedSoftMaxSampler;
import filters.Operations;
import io.leadsheet.LeadSheetIO;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Class Driver is an implementation test for CompressingAutoEncoder which reads
 * a LeadSheet file and produces an equivalent length LeadSheet file
 *
 * @author Nicholas Weintraut
 */
public class Driver {

    private static final boolean advanceDecoding = false; //should we start decoding as soon as possible?
    

    public static void main(String[] args) throws FileNotFoundException, IOException, ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder
                = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters().properties()
                        .setFileName(args[0])
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
                        .setIncludesAllowed(false));
        Configuration config = builder.getConfiguration();

        String inputSongPath = config.getString("input_song");
        String outputFolderPath = config.getString("output_folder");
        String autoEncoderParamsPath = config.getString("auto_encoder_params");
        String nameGeneratorParamsPath = config.getString("name_generator_params");
        String queueFolderPath = config.getString("queue_folder");
        String referenceQueuePath = config.getString("reference_queue");
        boolean shouldWriteQueue = config.getBoolean("should_write_generated_queue");
        boolean frankensteinTest = config.getBoolean("queue_tests_frankenstein");
        boolean interpolateTest = config.getBoolean("queue_tests_interpolation");

        String songTitle;
        //here is just silly code for generating name based on an LSTM lol $wag
        LSTM lstm = new LSTM();
        FullyConnectedLayer fullLayer = new FullyConnectedLayer(Operations.None);
        Loadable titleNetLoader = new Loadable() {
            @Override
            public boolean load(INDArray array, String path) {
                String car = pathCar(path);
                String cdr = pathCdr(path);
                switch (car) {
                    case "full":
                        return fullLayer.load(array, cdr);
                    case "lstm":
                        return lstm.load(array, cdr);
                    default:
                        return false;
                }
            }
        };

        String[] notFound1 = (new NetworkMeatPacker()).pack(nameGeneratorParamsPath, titleNetLoader);
        if (notFound1.length > 0) {
            System.err.println("The following parameter files were not matched to the network:");
        }
        for (String name : notFound1) {
            System.err.println("\t" + name);
        }
        Random rand = new Random();
        String characterString = " !\"'[],-.01245679:?ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz";
        AVector charOut = Vector.createLength(characterString.length());
        GroupedSoftMaxSampler sampler = new GroupedSoftMaxSampler(new Group[]{new Group(0, characterString.length(), true)});
        songTitle = "";
        for (int i = 0; i < 50; i++) {
            charOut = fullLayer.forward(lstm.step(charOut));
            charOut = sampler.filter(charOut);
            int charIndex = 0;
            for (; charIndex < charOut.length(); charIndex++) {
                if (charOut.get(charIndex) == 1.0) {
                    break;
                }
            }
            songTitle += characterString.substring(charIndex, charIndex + 1);
        }
        songTitle = songTitle.trim();
        //end stupid stuff, songTitle will be used later during writeCall
        LogTimer.initStartTime();
        LogTimer.log("Generated song name: " + songTitle);

        //Initialization
        LogTimer.initStartTime();   //start our logging timer to keep track of our execution time
        File inputFile = new File(inputSongPath); //load input file
        LogTimer.log("Reading file...");
        LeadSheetDataSequence inputSequence = LeadSheetIO.readLeadSheet(inputFile);  //read our leadsheet to get a data vessel as retrieved in rbm-provisor
        LogTimer.log("Instantiating autoencoder...");
        int inputSize = 34;
        int outputSize = EncodingParameters.noteEncoder.getNoteLength();
        int featureVectorSize = 100;
        ProductCompressingAutoEncoder autoencoder = new ProductCompressingAutoEncoder(24, 9, inputSize, outputSize, featureVectorSize, 48, 84 + 1, false); //create our network

        int numInterpolationDivisions = 5;

        //"pack" the network from weights and biases file directory
        LogTimer.log("Packing autoencoder from files");
        String[] notFound = (new NetworkMeatPacker()).pack(autoEncoderParamsPath, autoencoder);
        if (notFound.length > 0) {
            System.err.println(notFound.length + " files were not able to be matched to the architecture!");
            for (String fileName : notFound) {
                System.err.println("\t" + fileName);
            }
        }
        LeadSheetDataSequence outputSequence = inputSequence.copy();

        outputSequence.clearMelody();
        if (interpolateTest) {
            LeadSheetDataSequence additionalOutput = outputSequence.copy();
            for (int i = 0; i < numInterpolationDivisions; i++) {
                outputSequence.concat(additionalOutput.copy());
            }
        }
        LeadSheetDataSequence decoderInputSequence = outputSequence.copy();

        LogTimer.startLog("Encoding data...");
        //TradingTimer.initStart(); //start our trading timer to keep track our our generation versus realtime play
        while (inputSequence.hasNext()) { //iterate through time steps in input data
            //TradingTimer.waitForNextTimedInput();
            autoencoder.encodeStep(inputSequence.retrieve()); //feed the resultant input vector into the network
            if (advanceDecoding) { //if we are using advance decoding (we start decoding as soon as we can)
                if (autoencoder.canDecode()) { //if queue has enough data to decode from
                    outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder
                    //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are
                }
            }
        }

        if (shouldWriteQueue) {
            String queueFilePath = queueFolderPath + java.io.File.separator + inputFile.getName().replace(".ls", ".q");
            autoencoder.hotSwapQueue(queueFilePath, queueFilePath);
        }
        if (interpolateTest) {

            FragmentedNeuralQueue refQueue = new FragmentedNeuralQueue();
            refQueue.initFromFile(referenceQueuePath);

            FragmentedNeuralQueue currQueue = autoencoder.getQueue();
            //currQueue.writeToFile(queueFilePath);

            autoencoder.setQueue(currQueue.copy());
            while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder
                //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are       
            }

            for (int i = 1; i <= numInterpolationDivisions; i++) {
                System.out.println("Starting interpolation " + ((1.0 / numInterpolationDivisions) * (i)));
                (new NetworkMeatPacker()).refresh(autoEncoderParamsPath, autoencoder, "initialstate");
                FragmentedNeuralQueue currCopy = currQueue.copy();
                currCopy.basicInterpolate(refQueue, (1.0 / numInterpolationDivisions) * (i));
                autoencoder.setQueue(currCopy);
                int timeStep = 0;
                while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                    System.out.println("interpolation " + i + " step " + ++timeStep);
                    outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder
                    //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are       
                }
            }

        }
        if (frankensteinTest) {
            File queueFolder = new File(queueFolderPath);
            int numComponents = config.getInt("frankenstein_num_components", 5);
            int numCombinations = config.getInt("frankenstein_num_combinations", 6);
            double interpolationMagnitude = config.getDouble("frankenstein_magnitude", 2.0);
            if (queueFolder.isDirectory()) {
                File[] queueFiles = queueFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.contains(".q");
                    }
                });

                List<File> fileList = new ArrayList<>();
                for (File file : queueFiles) {
                    fileList.add(file);
                }
                Collections.shuffle(fileList);
                int numSelectedFiles = (numComponents > queueFiles.length) ? queueFiles.length : numComponents;

                for (int i = 0; i < queueFiles.length - numSelectedFiles; i++) {
                    fileList.remove(fileList.size() - 1);
                }
                List<FragmentedNeuralQueue> queuePopulation = new ArrayList<>(fileList.size());
                songTitle += " - a mix of ";
                for (File file : fileList) {
                    FragmentedNeuralQueue newQueue = new FragmentedNeuralQueue();
                    newQueue.initFromFile(file.getPath());
                    queuePopulation.add(newQueue);
                    songTitle += file.getName().replaceAll(".ls", "") + ", ";
                }

                LeadSheetDataSequence additionalOutput = outputSequence.copy();
                for (int i = 1; i < numCombinations; i++) {
                    outputSequence.concat(additionalOutput.copy());
                }
                decoderInputSequence = outputSequence.copy();

                FragmentedNeuralQueue origQueue = autoencoder.getQueue();

                for (int i = 0; i < numCombinations; i++) {
                    AVector combinationStrengths = Vector.createLength(queuePopulation.size());
                    Random vectorRand = new Random(i);
                    for (int j = 0; j < combinationStrengths.length(); j++) {
                        combinationStrengths.set(j, vectorRand.nextDouble());
                    }

                    (new NetworkMeatPacker()).refresh(autoEncoderParamsPath, autoencoder, "initialstate");
                    combinationStrengths.divide(combinationStrengths.elementSum());
                    FragmentedNeuralQueue currQueue = origQueue.copy();
                    for (int k = 0; k < combinationStrengths.length(); k++) {
                        currQueue.basicInterpolate(queuePopulation.get(k), combinationStrengths.get(k) * interpolationMagnitude);
                    }
                    autoencoder.setQueue(currQueue);
                    while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                        outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder
                        //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are       
                    }
                }

            }
        }

        while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
            outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder
            //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are       
        }
        LogTimer.log("Writing file...");

        String outputFilename = outputFolderPath + java.io.File.separator + inputFile.getName().replace(".ls", "_Output"); //we'll write our generated file with the same name plus "_Output"
        LeadSheetIO.writeLeadSheet(outputSequence, outputFilename, songTitle);
        System.out.println(outputFilename);
        LogTimer.log("Process finished"); //Done!

    }
}
