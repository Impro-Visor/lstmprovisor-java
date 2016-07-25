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
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import nickd4j.ReadWriteUtilities;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import population.QueuePopulation;

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
        String referenceQueuePath = config.getString("reference_queue", "nil");
        String inputCorpusFolder = config.getString("input_corpus_folder");
        String featureMatrixPath = config.getString("feature_matrix_path");
        String pcaFeatureMatrixPath = config.getString("pca_features_path");
        String featurePropertiesPath = config.getString("feature_properties_path");
        String featureProperty = config.getString("feature_property");
        
        boolean shouldGenerateFromPCAFeatures = config.getBoolean("generate_from_pca");
        boolean shouldGenerateQueueFeatureMatrix = config.getBoolean("should_generate_feature_matrix", false);
        boolean shouldWriteQueue = config.getBoolean("should_write_generated_queue");
        boolean frankensteinTest = config.getBoolean("queue_tests_frankenstein");
        boolean interpolateTest = config.getBoolean("queue_tests_interpolation");
        boolean populationTradingTest = config.getBoolean("queue_tests_population_trading", false);
        boolean iterateOverCorpus = config.getBoolean("iterate_over_corpus", false);
        boolean shouldGenerateSongTitle = config.getBoolean("generate_song_title");
        boolean shouldGenerateSong = config.getBoolean("generate_leadsheet");
        boolean shouldGenerateFeatureProperties = config.getBoolean("generate_feature_properties");
        

        LogTimer.initStartTime();   //start our logging timer to keep track of our execution time
        LogTimer.log("Creating name generator...");

        //here is just silly code for generating name based on an LSTM
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

        LogTimer.log("Packing name generator from files...");
        String[] notFound1 = (new NetworkMeatPacker()).pack(nameGeneratorParamsPath, titleNetLoader);
        if (notFound1.length > 0) {
            System.err.println("The following parameter files were not matched to the network:");
        }
        for (String name : notFound1) {
            System.err.println("\t" + name);
        }

        String characterString = " !\"'[],-.01245679:?ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz";

        //Initialization
        LogTimer.log("Creating autoencoder...");
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

        if (shouldGenerateQueueFeatureMatrix) {
            File[] queueFiles;
            if (iterateOverCorpus) {
                queueFiles = new File(queueFolderPath).listFiles();
            } else {
                queueFiles = new File[]{new File(referenceQueuePath)};
            }
            List<AMatrix> queueMatrixes = new ArrayList<>();
            for (File queueFile : queueFiles) {
                if (queueFile.getName().endsWith(".q")) {
                    FragmentedNeuralQueue queue = new FragmentedNeuralQueue();
                    queue.initFromFile(queueFile.getPath());
                    queueMatrixes.add(queue.getFeatureMatrix());
                }
            }
            System.out.println(queueMatrixes.get(0).rowCount());
            AVector[] features = new AVector[queueMatrixes.size() * queueMatrixes.get(0).rowCount()];
            int currFeature = 0;
            for (AMatrix queueMatrix : queueMatrixes) {
                for (INDArray feature : queueMatrix.toSliceArray()) {
                    features[currFeature++] = feature.toVector();
                }
            }
            for (AVector feature : features) {
                System.out.println(feature);
            }
            AMatrix totalFeatureMatrix = Matrix.create(features);

            String writeData = ReadWriteUtilities.getNumpyCSVString(totalFeatureMatrix);
            BufferedWriter writer = new BufferedWriter(new FileWriter(featureMatrixPath));
            writer.write(writeData);
            writer.close();
        }

        File[] songFiles;
        if (iterateOverCorpus) {
            songFiles = new File(inputCorpusFolder).listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        System.out.println(name);
                        System.out.println(name.endsWith(".ls"));
                        return name.endsWith(".ls");
                    }
                });
        } else {
            songFiles = new File[]{new File(inputSongPath)};
        }
        int numFeaturesPerSong = 8;
        AVector featurePropertyValues = Vector.createLength(songFiles.length * numFeaturesPerSong);
        int featureIndex = 0;
        for (File inputFile : songFiles) {
            System.out.println(inputFile.getName());
            (new NetworkMeatPacker()).refresh(autoEncoderParamsPath, autoencoder, "initialstate");
            String songTitle;
            if (shouldGenerateSongTitle) {
                Random rand = new Random();
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

                LogTimer.log("Generated song name: " + songTitle);
            } else {
                songTitle = "The Song We Never Name";
            }
            LogTimer.log("Reading file...");
            LeadSheetDataSequence inputSequence = LeadSheetIO.readLeadSheet(inputFile);  //read our leadsheet to get a data vessel as retrieved in rbm-provisor
            //System.out.println(inputFile.getName());
            if(inputSequence.hasNext())
                System.out.println("the input has some meat!");
            LeadSheetDataSequence outputSequence = inputSequence.copy();

            outputSequence.clearMelody();

            if (interpolateTest) {
                LeadSheetDataSequence additionalOutput = outputSequence.copy();
                for (int i = 0; i < numInterpolationDivisions; i++) {
                    outputSequence.concat(additionalOutput.copy());
                }
            } else if (shouldGenerateFromPCAFeatures) {
                int spacing = 24;
                int numComponents = 100;
                LeadSheetDataSequence newOutputSequence = new LeadSheetDataSequence();
                while (newOutputSequence.maxLength() < spacing * numComponents) {
                    LeadSheetDataSequence currSegment = outputSequence.copy();
                    while (currSegment.maxLength() > 0 && newOutputSequence.maxLength() < spacing * numComponents) {
                        newOutputSequence.pushStep(currSegment.pollBeats(), currSegment.pollChords(), null);
                    }
                }
                outputSequence = newOutputSequence;
            }
            LeadSheetDataSequence decoderInputSequence = outputSequence.copy();
            if (shouldGenerateFromPCAFeatures) {
                FragmentedNeuralQueue pcaQueue = new FragmentedNeuralQueue();
                int spacing = 24;
                AMatrix pcaFeatureMatrix = (AMatrix) ReadWriteUtilities.readNumpyCSVFile(pcaFeatureMatrixPath);
                pcaQueue.initFromFeatureMatrix(pcaFeatureMatrix, 24);
                autoencoder.setQueue(pcaQueue);
                int i = 0;
                while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                    outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder
                    //TradingTimer.logTimestep(); //log our time to TradingTimer so we can know how far ahead of realtime we are    
                    if (++i == spacing) {
                        i = 0;
                        (new NetworkMeatPacker()).refresh(autoEncoderParamsPath, autoencoder, "initialstate");
                    }
                }
                LogTimer.log("Writing file...");

                String outputFilename = outputFolderPath + java.io.File.separator + "pcaFeatureGen_Output.ls"; //we'll write our generated file with the same name plus "_Output"
                LeadSheetIO.writeLeadSheet(outputSequence, outputFilename, "PCA Feature plug-in Etude");
                System.out.println(outputFilename);
            } else if (populationTradingTest) {
                LogTimer.log("Extending outputSequence...");

                LeadSheetDataSequence humanTradingPartsSequence = inputSequence.copy();
                LeadSheetDataSequence newOutputSequence = new LeadSheetDataSequence();
                int tradingPartSize = 192;
                int numTradingParts = 0;
                int lastTradingPartSize = 0;
                while (outputSequence.hasNonMelodyDataLeft()) {
                    LeadSheetDataSequence currExtendedPart = new LeadSheetDataSequence();
                    int i = 0;
                    for (; i < tradingPartSize && outputSequence.hasNonMelodyDataLeft(); i++) {
                        currExtendedPart.pushStep(outputSequence.pollBeats(), outputSequence.pollChords(), null);
                    }
                    lastTradingPartSize = i;
                    numTradingParts++;
                    currExtendedPart.concat(currExtendedPart.copy());
                    newOutputSequence.concat(currExtendedPart);
                }
                outputSequence = newOutputSequence;

                LogTimer.log("Setting up population...");
                QueuePopulation population = new QueuePopulation(100);
                FragmentedNeuralQueue firstRefQueue = new FragmentedNeuralQueue();
                firstRefQueue.initFromFile(referenceQueuePath);

                Random interpRand = new Random();
                double interpRange = config.getDouble("trading_interp_range", 0.2);
                double interpMin = config.getDouble("trading_interp_min", 0.3);
                double populationHerd = config.getDouble("trading_population_herd_magnitude", 0.1);
                double maxMutationStrength = config.getDouble("trading_population_max_mutation", 0.3);
                double crossoverProbability = config.getDouble("trading_population_crossover_prob", 0.2);

                for (int i = 0; i < numTradingParts; i++) {
                    LogTimer.startLog("Generating trading part...");
                    LogTimer.startLog("Encoding input part");
                    int currTradingPartSize = (i == numTradingParts - 1) ? lastTradingPartSize : tradingPartSize;
                    for (int j = 0; j < currTradingPartSize; j++) {
                        autoencoder.encodeStep(inputSequence.retrieve());
                    }
                    LogTimer.endLog();
                    LogTimer.startLog("Modifying queue");
                    FragmentedNeuralQueue generatedQueue = autoencoder.getQueue();
                    FragmentedNeuralQueue modifiedQueue = generatedQueue.copy();

                    if (i > 0) {
                        modifiedQueue.basicInterpolate(population.sample(), (interpRand.nextDouble() * interpRange) + interpMin);
                    }

                    autoencoder.setQueue(modifiedQueue);
                    LogTimer.endLog();
                    LogTimer.startLog("Pushing human part");
                    for (int j = 0; j < currTradingPartSize; j++) {
                        outputSequence.pushStep(null, null, humanTradingPartsSequence.pollMelody());
                    }
                    LogTimer.endLog();
                    LogTimer.startLog("Decoding generated part");
                    for (int j = 0; j < currTradingPartSize; j++) {
                        outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve()));
                    }
                    LogTimer.endLog();
                    LogTimer.startLog("Performing population operations");
                    if (i != numTradingParts - 1) {
                        if (i > 0) {
                            population.herd(generatedQueue, populationHerd);
                        }
                        population.add(generatedQueue);
                        population.evolve(maxMutationStrength, crossoverProbability);
                    }
                    LogTimer.endLog();
                    LogTimer.startLog("Refreshing network");
                    (new NetworkMeatPacker()).refresh(autoEncoderParamsPath, autoencoder, "initialstate");
                    LogTimer.endLog();
                    LogTimer.endLog();
                }
                LogTimer.log("Writing file...");

                String outputFilename = outputFolderPath + java.io.File.separator + inputFile.getName().replace(".ls", "_TradingOutput.ls"); //we'll write our generated file with the same name plus "_Output"
                LeadSheetIO.writeLeadSheet(outputSequence, outputFilename, songTitle);
                System.out.println(outputFilename);

            } else {
                LogTimer.startLog("Encoding data...");
                while (inputSequence.hasNext()) { //iterate through time steps in input data
                    autoencoder.encodeStep(inputSequence.retrieve()); //feed the resultant input vector into the network
                }
                LogTimer.endLog();

                if (shouldWriteQueue) {
                    String queueFilePath = queueFolderPath + java.io.File.separator + inputFile.getName().replace(".ls", ".q");
                    FragmentedNeuralQueue currQueue = autoencoder.getQueue();
                    currQueue.writeToFile(queueFilePath);
                    LogTimer.log("Wrote queue " + inputFile.getName().replace(".ls", ".q") + " to file...");
                }
                if (shouldGenerateSong) {
                    if (interpolateTest) {

                        FragmentedNeuralQueue refQueue = new FragmentedNeuralQueue();
                        refQueue.initFromFile(referenceQueuePath);

                        FragmentedNeuralQueue currQueue = autoencoder.getQueue();

                        autoencoder.setQueue(currQueue.copy());
                        while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                            outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder     
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
                            }
                        }

                    } else if (frankensteinTest) {
                        LogTimer.startLog("Loading queues");
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
                            LogTimer.endLog();

                            LeadSheetDataSequence additionalOutput = outputSequence.copy();
                            for (int i = 1; i < numCombinations; i++) {
                                outputSequence.concat(additionalOutput.copy());
                            }
                            decoderInputSequence = outputSequence.copy();

                            FragmentedNeuralQueue origQueue = autoencoder.getQueue();

                            for (int i = 0; i < numCombinations; i++) {

                                LogTimer.startLog("Performing queue interpolation...");
                                AVector combinationStrengths = Vector.createLength(queuePopulation.size());
                                Random vectorRand = new Random(i);
                                for (int j = 0; j < combinationStrengths.length(); j++) {
                                    combinationStrengths.set(j, vectorRand.nextDouble());
                                }
                                combinationStrengths.divide(combinationStrengths.elementSum());
                                FragmentedNeuralQueue currQueue = origQueue.copy();
                                for (int k = 0; k < combinationStrengths.length(); k++) {
                                    currQueue.basicInterpolate(queuePopulation.get(k), combinationStrengths.get(k) * interpolationMagnitude);
                                }
                                LogTimer.endLog();
                                autoencoder.setQueue(currQueue);
                                LogTimer.startLog("Refreshing autoencoder state...");
                                (new NetworkMeatPacker()).refresh(autoEncoderParamsPath, autoencoder, "initialstate");
                                LogTimer.endLog();
                                LogTimer.startLog("Decoding segment...");
                                while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                                    outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder     
                                }
                                LogTimer.endLog();
                            }
                        }
                    }

                    while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
                        //System.out.println("push from decode");
                        outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder    
                    }

                    if (shouldGenerateFeatureProperties) {
                        
                        LeadSheetDataSequence copySequence = outputSequence.copy();
                        int spacing = 24;
                        System.out.println("Generating feature property values for song");
                        switch (featureProperty) {
                            case "rest":
                                
                                boolean wasLastRest = false;
                                for(int j = 0; j < numFeaturesPerSong; j++)
                                {
                                    //System.out.println(j);
                                    int restStepCount = 0;
                                    for (int i = 0; i < spacing; i++) {
                                        //System.out.println(i);
                                        AVector polledMelodyStep = copySequence.pollMelody();
                                        //System.out.println(polledMelodyStep);
                                        if(polledMelodyStep.get(0) == -1.0) {
                                            wasLastRest = true;
                                            restStepCount++;
                                        }
                                        else if(polledMelodyStep.get(0) == -2.0 && wasLastRest)
                                           restStepCount++;
                                        else
                                            wasLastRest = false;
                                    }
                                    featurePropertyValues.set(featureIndex++, ((double) restStepCount) / ((double) spacing));
                                }
                                break;
                            case "sustain":
                                
                                for(int j = 0 ; j < numFeaturesPerSong; j++)
                                {
                                    int sustainStepCount = 0;
                                    for (int i = 0; i < spacing; i++) {
                                        AVector polledMelodyStep = copySequence.pollMelody();
                                        if(polledMelodyStep.get(0) == -2.0)
                                            sustainStepCount++;
                                    }
                                    featurePropertyValues.set(featureIndex++, ((double) sustainStepCount) / ((double) spacing));
                                }
                                break;
                            case "articulate":

                                
                                for (int j = 0; j < numFeaturesPerSong; j++) {
                                    //System.out.println(j);
                                    int articulateStepCount = 0;
                                    for (int i = 0; i < spacing; i++) {
                                        //System.out.println(i);
                                        AVector polledMelodyStep = copySequence.pollMelody();
                                        //System.out.println(polledMelodyStep);
                                        if (polledMelodyStep.get(0) >= 0.0)
                                            articulateStepCount++;
                                        
                                    }
                                    featurePropertyValues.set(featureIndex++, ((double) articulateStepCount) / ((double) spacing));
                                }
                                break;
                            default:
                                break;
                        }

                    }

                    LogTimer.log("Writing file...");

                    String outputFilename = outputFolderPath + java.io.File.separator + inputFile.getName().replace(".ls", "_Output.ls"); //we'll write our generated file with the same name plus "_Output"
                    LeadSheetIO.writeLeadSheet(outputSequence, outputFilename, songTitle);
                    System.out.println(outputFilename);
                } else {
                    autoencoder.setQueue(new FragmentedNeuralQueue());
                }
            }
        }
        
        if(shouldGenerateFeatureProperties) {
        //write generated feature_properties
        System.out.println(featurePropertyValues.length());
        BufferedWriter writer = new BufferedWriter(new FileWriter(featurePropertiesPath + "_" + featureProperty + ".v"));
        writer.write(ReadWriteUtilities.getNumpyCSVString(featurePropertyValues));
        writer.close();
        }
        LogTimer.log("Process finished"); //Done!

    }
}
