/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;


import architecture.ConnectomeLoader;
import java.io.File;
import io.leadsheet.LeadsheetDataSequence;
import architecture.FragmentedNeuralQueue;
import architecture.Loadable;
import architecture.NameGenerator;
import architecture.poex.ProductCompressingAutoencoder;
import io.leadsheet.LeadsheetIO;
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
 * Class Driver is an implementation test of ProductCompressingAutoencoder which supports a number of tests.
 * The path to a properties file which will supply parameter values for the tests should be passed in as argument 0 to main.
 * @author Nicholas Weintraut
 */
public class Driver {
    
    
    /**
     * The path to a properties file which will supply parameter values for the tests should be passed in as argument 0 to main. 
     * The test that will be run is determined by the value of 'test_type' in the properties file, and each of the tests have their own properties:
     *      'encode+decode' - Encode and decode the given leadsheet with the autoencoder, writing the result to a leadsheet file.
     *              Params: 
     *                  * autoencoder_connectome={the path to the connectome which the autoencoder will be loaded with}
     *                  * name_generator_connectome={the path to the connectome which the name generator will be loaded with}
     *                  * input_leadsheet={the path to the leadsheet file which will be encoded and decoded}
     *                  * output_folder={the path to the output folder which the result leadsheet file will be written in}
     * 
     *      'encode+write_queue' - Encode the given leadsheet with the autoencoder, then write the encoded feature queue to a queue file.
     *              Params:
     *                  * autoencoder_connectome={the path to the connectome which the autoencoder will be loaded with}
     *                  * input_leadsheet={the path to the leadsheet file which will be encoded}
     *                  * queue_folder={the path to the output folder which the result queue file will be written in}
     * 
     *      'encode+write_queue+decode' - Encode the given leadsheet with the autoencoder, write the encoded feature queue to a queue file, and then write the result leadsheet to a leadsheet file.
     *                  * autoencoder_connectome={the path to the connectome which the autoencoder will be loaded with}
     *                  * name_generator_connectome={the path to the connectome which the name generator will be loaded with}
     *                  * input_leadsheet={the path to the leadsheet file which will be encoded and decoded}
     *                  * queue_folder={the path to the output folder which the result queue file will be written in}
     *                  * output_folder={the path to the output folder which the result leadsheet file will be written in}
     *      'create_feature_property_vector' - Given a corpus folder of leadsheets, construct a vector consisting of property analysis values for each feature in the corpus data
     *                  * input_corpus_folder={the path to the corpus folder containing all leadsheets to analyze}
     *                  * feature_size={the size (in time steps) of each feature}
     *                  * feature_properties_path={the path to write the generated vector file to (the file will be a csv file containing all the values in left-to-right order}
     *                  * feature_property={the type of feature property to analyze - current options are 'rest', 'sustain', articulate' (these return ratios of time steps with the given property to the total time steps in the feature).
     *      'compile_feature_queue_matrix' - Given a corpus folder of feature queues, construct a matrix of all feature vectors and write it as a csv file
     *                  * queue_folder={the path to the folder containing all queue files to compile}
     *                  * feature_matrix_path={the path to write the result csv file to}
     *      'generate_from_feature_queue_matrix' - Given a matrix of feature vectors, load the autoencoder with a queue of those features and decode from it, writing the result leadsheet to a file
     *                  * autoencoder_connectome={the path to the connectome which the autoencoder will be loaded with}
     *                  * reference_leadsheet={the path to the leadsheet we will take the chord sequence from (and loop it to match the length of the feature queue)}
     *                  * feature_queue_matrix_path={the path to the feature queue matrix file we will decode from}
     *                  * output_file_path={the path to the file we will write our result leadsheet to}
     *                  * (optional) song_title={the song title to write in the leadsheet file - by default this is "Generation from Feature Matrix {path of the feature matrix}"}
     *                  * feature_size={the size (in time steps) of features}
     *      'population_trade' - Given a leadsheet file, split it into sections of a specified size, and between sections, generate a response that plays off of a population of previously encoded feature queues
     *                  * autoencoder_connectome={the path to the connectome which the autoencoder will be loaded with}
     *                  * input_leadsheet={the path to the leadsheet file which will be encoded and traded with}     
     *                  * output_folder={the path to the output folder which the result leadsheet file will be written in}
     *                  * trading_part_size={the size (in time steps) of each trading part. The input leadsheet will be split into sections of this size, and trading responses will be generated in between.}
     *                  * interpolation_variance={a random value between zero and this will be added to the interpolation_min at each trading section to calculate the interpolation of the recently encoded queue towards the queue population before decoding the trading response}
     *                  * interpolation_min={the minimum ratio of interpolation at each trading section}
     *                  * herding_strength={the maximum strength of the herding operation at each section (all queues in the population are interpolated a random amount towards the most recent queue)}
     *                  * mutation_strength={the maximum strength of mutation at each section (each element of the feature vectors of all queues in the population are mutated at a random strength}
     *                  * crossover_strength{the maximum strength of crossover at each section (there is a chance for every queue that the queue will swap a random feature of itself with the corresponding feature of another random queue)}
     *      'interpolation' - Given a leadsheet file and a reference queue file, encode the leadsheet file with the autoencoder, and generate from the encoded queue for a number of divisions of a full interpolation towards the target queue
     *                  * autoencoder_connectome={the path to the connectome which the autoencoder will be loaded with}
     *                  * input_leadsheet={the path to the leadsheet file which will be encoded and interpolated}
     *                  * target_queue={the path to the queue to interpolate towards at each interpolation value};
     *                  * output_folder={the path to the output folder which the result leadsheet file will be written in}
     *                  * num_interpolation_divisions={the number of divisions of the interpolation strength from 0.0 to 1.0 (the length of the result leadsheet will be equal to the length of the original times 1 + number of divisions, as the first section of the result leadsheet is for interpolation 0.0)}
     *      'frankenstein' - Given a primary queue, a reference leadsheet for chords, and a corpus of queue files, construct the result leadsheet from a series of randomly weighted interpolations of the primary queue towards the set of selected queues.
     *                  * autoencoder_connectome={the path to the connectome which the autoencoder will be loaded with}
     *                  * primary_queue_path={the path to the queue which will serve as the base for all of the queue combinations (which are the result of sequential interpolations instead of a weighted sum)}
     *                  * reference_leadsheet={the path to the leadsheet we will take the chord sequence from (and loop it to match the desired length of our output}
     *                  * queue_folder={the path to the folder containing all queue files we can select from}
     *                  * output_file_path={the path to the file we will write our result leadsheet to}
     *                  * num_reference_queues={the number of reference queues we will pick at random from the queue folder to sample from)
     *                  * num_combinations={the number of queue combinations to sample and create the result leadsheet from}
     *                  * interpolation_strength={the total magnitude of all interpolation operations for each combination}
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder
                = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters().properties()
                        .setFileName(args[0])
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
                        .setIncludesAllowed(false));
        Configuration config = builder.getConfiguration();

        LogTimer.initStartTime();   //start our logging timer to keep track of our execution time

        //switch statement to run the appropriate test
        switch (config.getString("test_type")) {
            case "encode+decode": {
                //load parameter values from config file
                String autoencoderConnectomePath = config.getString("autoencoder_connectome");
                String nameGeneratorConnectomePath = config.getString("name_generator_connectome");
                String inputLeadsheetPath = config.getString("input_leadsheet");
                String outputFolderPath = config.getString("output_folder");

                //initialize networks
                NameGenerator nameGenerator = initializeNameGenerator(nameGeneratorConnectomePath);
                ProductCompressingAutoencoder autoencoder = initializeAutoencoder(autoencoderConnectomePath, false);

                //initialize input sequences and output sequence
                LeadsheetDataSequence inputSequence = leadsheetToSequence(inputLeadsheetPath);
                LeadsheetDataSequence outputSequence = inputSequence.copy();
                outputSequence.clearMelody();
                LeadsheetDataSequence decoderInputSequence = outputSequence.copy();

                //encode and decode
                encodeFromSequence(autoencoder, inputSequence);
                decodeToSequence(autoencoder, outputSequence, decoderInputSequence);

                //generate song title
                String songTitle = nameGenerator.generateName();

                //write output to specified directory with same file name + _aeOutput suffix
                writeLeadsheetFile(outputSequence, outputFolderPath, new File(inputLeadsheetPath).getName(), "_aeOutput", songTitle);
            }
            break;

            case "encode+write_queue": {
                //load parameter values from config file
                String autoencoderConnectomePath = config.getString("autoencoder_connectome");
                String inputLeadsheetPath = config.getString("input_leadsheet");
                String queueFolderPath = config.getString("queue_folder");

                //initialize network
                ProductCompressingAutoencoder autoencoder = initializeAutoencoder(autoencoderConnectomePath, false);

                //initialize input sequence
                LeadsheetDataSequence inputSequence = leadsheetToSequence(inputLeadsheetPath);

                //encode
                encodeFromSequence(autoencoder, inputSequence);
                //write to a queue file in the specified queue folder (the write method will handle removing/adding extensions
                writeQueueFile(autoencoder, queueFolderPath, new File(inputLeadsheetPath).getName());
            }
            break;
            case "encode+write_queue+decode": {
                //load parameter values from config file
                String autoencoderConnectomePath = config.getString("autoencoder_connectome");
                String nameGeneratorConnectomePath = config.getString("name_generator_connectome");
                String inputLeadsheetPath = config.getString("input_leadsheet");
                String queueFolderPath = config.getString("queue_folder");
                String outputFolderPath = config.getString("output_folder");

                //initialize networks
                NameGenerator nameGenerator = initializeNameGenerator(nameGeneratorConnectomePath);
                ProductCompressingAutoencoder autoencoder = initializeAutoencoder(autoencoderConnectomePath, false);

                //initialize input sequences and output sequence
                LeadsheetDataSequence inputSequence = leadsheetToSequence(inputLeadsheetPath);
                LeadsheetDataSequence outputSequence = inputSequence.copy();
                outputSequence.clearMelody();
                LeadsheetDataSequence decoderInputSequence = outputSequence.copy();

                //encode
                encodeFromSequence(autoencoder, inputSequence);
                //write to a queue file in the specified queue folder (the write method will handle removing/adding extensions
                writeQueueFile(autoencoder, queueFolderPath, new File(inputLeadsheetPath).getName());
                //decode
                decodeToSequence(autoencoder, outputSequence, decoderInputSequence);

                //generate song title
                String songTitle = nameGenerator.generateName();

                //write output to specified directory with same file name + _aeOutput suffix
                writeLeadsheetFile(outputSequence, outputFolderPath, new File(inputLeadsheetPath).getName(), "_aeOutput", songTitle);
            }
            break;
            case "create_feature_property_vector": {
                //load parameter values from config file
                String inputCorpusFolder = config.getString("input_corpus_folder");
                int featureSize = config.getInt("feature_size");
                String featurePropertiesPath = config.getString("feature_properties_path");
                String featureProperty = config.getString("feature_property");

                //compile array of valid leadsheet files
                File[] songFiles = new File(inputCorpusFolder).listFiles((File dir, String name) -> name.endsWith(".ls"));

                //construct feature property vector from analyzed feature property values of all songs
                AVector featurePropertyValues = Vector.createLength(0);
                int featureIndex = 0;
                for (File inputFile : songFiles) {
                    LeadsheetDataSequence melodySequence = leadsheetToSequence(inputFile.getPath());
                    featurePropertyValues.join(melodyFeatureAnalysis(melodySequence, featureProperty, featureSize));
                }

                //write generated feature_properties
                BufferedWriter writer = new BufferedWriter(new FileWriter(featurePropertiesPath + "_" + featureProperty + ".v"));
                writer.write(ReadWriteUtilities.getNumpyCSVString(featurePropertyValues));
                writer.close();
            }
            break;
            case "compile_feature_queue_matrix": {
                //load parameter values from config file
                String queueFolderPath = config.getString("queue_folder");
                String featureMatrixPath = config.getString("feature_matrix_path");

                //generate feature matrix from all feature queues in specified queue folder
                File[] queueFiles = new File(queueFolderPath).listFiles((File dir, String name) -> name.endsWith(".q"));
                AMatrix totalFeatureMatrix = generateFeatureQueueMatrix(queueFiles);
                String writeData = ReadWriteUtilities.getNumpyCSVString(totalFeatureMatrix);
                BufferedWriter writer = new BufferedWriter(new FileWriter(featureMatrixPath));
                writer.write(writeData);
                writer.close();
            }
            break;
            case "generate_from_feature_queue_matrix": {
                //load parameter values from config file
                String autoencoderConnectomePath = config.getString("autoencoder_connectome");
                String referenceLeadsheetPath = config.getString("reference_leadsheet");
                String featureQueueMatrixPath = config.getString("feature_queue_matrix_path");
                String outputFilePath = config.getString("output_file_path");
                String songTitle = config.getString("song_title", "Generation from Feature Matrix " + featureQueueMatrixPath);
                int featureSize = config.getInt("feature_size");

                //initialize network
                ProductCompressingAutoencoder autoencoder = initializeAutoencoder(autoencoderConnectomePath, false);

                //initialize chord sequence
                LeadsheetDataSequence chordSequence = leadsheetToSequence(referenceLeadsheetPath);
                chordSequence.clearMelody();

                //call generation method
                generateFromFeatureMatrix(autoencoder, autoencoderConnectomePath, chordSequence, featureQueueMatrixPath, featureSize, outputFilePath, songTitle);
            }
            break;
            case "population_trade": {
                //load parameter values from config file
                String autoencoderConnectomePath = config.getString("autoencoder_connectome");
                String inputLeadsheetPath = config.getString("input_leadsheet");
                String outputFolderPath = config.getString("output_folder");
                int tradingPartSize = config.getInt("trading_part_size");
                double interpVariance = config.getDouble("interpolation_variance");
                double interpMin = config.getDouble("interpolation_min");
                double herdingStrength = config.getDouble("herding_strength");
                double mutationStrength = config.getDouble("mutation_strength");
                double crossoverStrength = config.getDouble("crossover_strength");
                
                //initialize network
                ProductCompressingAutoencoder autoencoder = initializeAutoencoder(autoencoderConnectomePath, true);
                
                //perform population trading test
                populationTradingTest(autoencoder, autoencoderConnectomePath, new File(inputLeadsheetPath), new File(outputFolderPath), tradingPartSize, interpVariance, interpMin, herdingStrength, mutationStrength, crossoverStrength);
            } 
                break;
            case "interpolation": {
                //load parameter values from config file
                String autoencoderConnectomePath = config.getString("autoencoder_connectome");
                String inputLeadsheetPath = config.getString("input_leadsheet");
                String targetQueuePath = config.getString("target_queue");
                String outputFolderPath = config.getString("output_folder");
                int numInterpolationDivisions = config.getInt("num_interpolation_divisions");

                //initialize network
                ProductCompressingAutoencoder autoencoder = initializeAutoencoder(autoencoderConnectomePath, false);

                //perform the interpolation test
                interpolateTest(autoencoder, autoencoderConnectomePath, new File(inputLeadsheetPath), new File(targetQueuePath), new File(outputFolderPath), numInterpolationDivisions);
            }
            break;
            case "frankenstein": {
                //load parameter values from config file
                String autoencoderConnectomePath = config.getString("autoencoder_connectome");
                String primaryQueuePath = config.getString("primary_queue_path");
                String referenceLeadsheetPath = config.getString("reference_leadsheet");
                String queueFolderPath = config.getString("queue_folder");
                String outputFilePath = config.getString("output_file_path");
                int numReferenceQueues = config.getInt("num_reference_queues");
                int numCombinations = config.getInt("num_combinations");
                double interpolationMagnitude = config.getDouble("interpolation_strength");

                //initialize network
                ProductCompressingAutoencoder autoencoder = initializeAutoencoder(autoencoderConnectomePath, false);

                //initialize chord sequence
                LeadsheetDataSequence chordSequence = leadsheetToSequence(referenceLeadsheetPath);
                chordSequence.clearMelody();

                //perform frankenstein test
                frankensteinTest(autoencoder, autoencoderConnectomePath, primaryQueuePath, new File(queueFolderPath), outputFilePath, chordSequence, numReferenceQueues, numCombinations, interpolationMagnitude);
            }
            break;
            default:
                throw new RuntimeException("Unrecognized test type");
        }
        LogTimer.log("Process finished"); //Done!
    }

    /**
     * Helper network to refresh all "initialstate" values in a network
     * @param network The network whose initial state we will reset
     * @param connectomePath The path to the connectome we will get out initialstate values from 
     */
    public static void refreshNetworkState(Loadable network, String connectomePath) {
        (new ConnectomeLoader()).refresh(connectomePath, network, "initialstate");
    }

    /**
     * Given an autoencoder, write the contents of the autoencoder's queue to a queue file
     * @param autoencoder The autoencoder whose queue we will write to a file
     * @param queueFolderPath The folder to write the queue file in
     * @param fileName The name of the queue file
     */
    public static void writeQueueFile(ProductCompressingAutoencoder autoencoder, String queueFolderPath, String fileName) {
        fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".q";
        String queueFilePath = queueFolderPath + java.io.File.separator + fileName;
        FragmentedNeuralQueue currQueue = autoencoder.getQueue();
        currQueue.writeToFile(queueFilePath);
        LogTimer.log("Wrote queue " + fileName + " to file...");
    }

    /**
     * Given a primary queue, a reference leadsheet for chords, and a corpus of queue files, construct the result leadsheet from a series of randomly weighted interpolations of the primary queue towards the set of selected queues.
     * @param autoencoder
     * @param autoencoderConnectomePath
     * @param primaryQueuePath
     * @param queueFolder
     * @param outputFilePath
     * @param chordSequence
     * @param numReferenceQueues
     * @param numCombinations
     * @param interpolationMagnitude 
     */
    public static void frankensteinTest(ProductCompressingAutoencoder autoencoder, String autoencoderConnectomePath, String primaryQueuePath, File queueFolder, String outputFilePath, LeadsheetDataSequence chordSequence, int numReferenceQueues, int numCombinations, double interpolationMagnitude) {
        LogTimer.startLog("Loading queues");

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
            int numSelectedFiles = (numReferenceQueues > queueFiles.length) ? queueFiles.length : numReferenceQueues;

            for (int i = 0; i < queueFiles.length - numSelectedFiles; i++) {
                fileList.remove(fileList.size() - 1);
            }
            List<FragmentedNeuralQueue> queuePopulation = new ArrayList<>(fileList.size());
            String songTitle = "A mix of ";
            for (File file : fileList) {
                FragmentedNeuralQueue newQueue = new FragmentedNeuralQueue();
                newQueue.initFromFile(file.getPath());
                queuePopulation.add(newQueue);
                songTitle += file.getName().replaceAll(".q", "") + ", ";
            }
            LogTimer.endLog();

            LeadsheetDataSequence outputSequence = chordSequence.copy();
            LeadsheetDataSequence additionalOutput = chordSequence.copy();
            for (int i = 1; i < numCombinations; i++) {
                outputSequence.concat(additionalOutput.copy());
            }
            LeadsheetDataSequence decoderInputSequence = outputSequence.copy();

            FragmentedNeuralQueue primaryQueue = new FragmentedNeuralQueue();
            primaryQueue.initFromFile(primaryQueuePath);

            for (int i = 0; i < numCombinations; i++) {

                LogTimer.startLog("Performing queue interpolation...");
                AVector combinationStrengths = Vector.createLength(queuePopulation.size());
                Random vectorRand = new Random(i);
                for (int j = 0; j < combinationStrengths.length(); j++) {
                    combinationStrengths.set(j, vectorRand.nextDouble());
                }
                combinationStrengths.divide(combinationStrengths.elementSum());
                FragmentedNeuralQueue currQueue = primaryQueue.copy();
                for (int k = 0; k < combinationStrengths.length(); k++) {
                    currQueue.basicInterpolate(queuePopulation.get(k), combinationStrengths.get(k) * interpolationMagnitude);
                }
                LogTimer.endLog();
                autoencoder.setQueue(currQueue);
                refreshNetworkState(autoencoder, autoencoderConnectomePath);
                decodeToSequence(autoencoder, outputSequence, decoderInputSequence);
            }
            
            writeLeadsheetFile(outputSequence, outputFilePath, "_randomInterpSamples", songTitle);
            
        } else {
            throw new RuntimeException("Given queue folder is not a directory!");
        }
    }
    
    /**
     * Given a leadsheet file and a reference queue file, encode the leadsheet file with the autoencoder, and generate from the encoded queue for a number of divisions of a full interpolation towards the target queue
     * @param autoencoder
     * @param autoencoderConnectomePath
     * @param inputFile
     * @param referenceQueueFile
     * @param outputFolder
     * @param numDivisions 
     */
    public static void interpolateTest(ProductCompressingAutoencoder autoencoder, String autoencoderConnectomePath, File inputFile, File referenceQueueFile, File outputFolder, int numDivisions) {
        LeadsheetDataSequence inputSequence = leadsheetToSequence(inputFile.getPath());
        LeadsheetDataSequence outputSequence = inputSequence.copy();
        outputSequence.clearMelody();
        LeadsheetDataSequence additionalOutput = outputSequence.copy();
        for (int i = 0; i < numDivisions; i++) {
            outputSequence.concat(additionalOutput.copy());
        }
        LeadsheetDataSequence decoderInputSequence = outputSequence.copy();
        
        encodeFromSequence(autoencoder, inputSequence);
        
        FragmentedNeuralQueue refQueue = new FragmentedNeuralQueue();
        refQueue.initFromFile(referenceQueueFile.getPath());

        FragmentedNeuralQueue currQueue = autoencoder.getQueue();

        autoencoder.setQueue(currQueue.copy());
        decodeToSequence(autoencoder, outputSequence, decoderInputSequence);

        for (int i = 1; i <= numDivisions; i++) {
            LogTimer.log("Starting interpolation " + ((1.0 / numDivisions) * (i)));
            refreshNetworkState(autoencoder, autoencoderConnectomePath);
            FragmentedNeuralQueue currCopy = currQueue.copy();
            currCopy.basicInterpolate(refQueue, (1.0 / numDivisions) * (i));
            autoencoder.setQueue(currCopy);
            decodeToSequence(autoencoder, outputSequence, decoderInputSequence);
        }
        String fileName = inputFile.getName().replace(".ls", "") + " to " + referenceQueueFile.getName().replace(".q","");
        writeLeadsheetFile(outputSequence, outputFolder.getPath(), fileName, "_Interpolation", fileName);
    }
    
    /**
     * Given a leadsheet file and a reference queue file, encode the leadsheet file with the autoencoder, and generate from the encoded queue for a number of divisions of a full interpolation towards the target queue
     * @param autoencoder
     * @param autoencoderConnectomePath
     * @param inputFile
     * @param outputFolder
     * @param tradingPartSize
     * @param interpRange
     * @param interpMin
     * @param herdStrength
     * @param mutationStrength
     * @param crossoverStrength 
     */
    public static void populationTradingTest(ProductCompressingAutoencoder autoencoder, String autoencoderConnectomePath, File inputFile, File outputFolder, int tradingPartSize, double interpRange, double interpMin, double herdStrength, double mutationStrength, double crossoverStrength) {
                
                LeadsheetDataSequence inputSequence = leadsheetToSequence(inputFile.getPath());
                LeadsheetDataSequence outputSequence = inputSequence.copy();
                outputSequence.clearMelody();
                LeadsheetDataSequence decoderInputSequence = outputSequence.copy();
                
                LogTimer.log("Extending outputSequence...");
                LeadsheetDataSequence humanTradingPartsSequence = inputSequence.copy();
                LeadsheetDataSequence newOutputSequence = new LeadsheetDataSequence();
                int numTradingParts = 0;
                int lastTradingPartSize = 0;
                while (outputSequence.hasNonMelodyDataLeft()) {
                    LeadsheetDataSequence currExtendedPart = new LeadsheetDataSequence();
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

                Random interpRand = new Random();

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
                            population.herd(generatedQueue, herdStrength);
                        }
                        population.add(generatedQueue);
                        population.evolve(mutationStrength, crossoverStrength);
                    }
                    LogTimer.endLog();
                    refreshNetworkState(autoencoder, autoencoderConnectomePath);
                    LogTimer.endLog();
                }
                String fileName = inputFile.getName().replace(".ls","");
                writeLeadsheetFile(outputSequence, outputFolder.getPath(), fileName, "_TradingOutput", "Trading on " + fileName);
    }
    
    /**
     * Given a leadsheet file, construct a LeadsheetDataSequence containing melody, chord, and beat data
     * @param leadsheetFilePath The leadsheet file to read
     * @return The constructed LeadSheetDataSequence
     */
    public static LeadsheetDataSequence leadsheetToSequence(String leadsheetFilePath) {
        LogTimer.log("Reading file...");
        LeadsheetDataSequence sequence = LeadsheetIO.readLeadsheet(leadsheetFilePath);  //read our leadsheet to get a data vessel as retrieved in rbm-provisor
        if (!sequence.hasNext()) {
            throw new RuntimeException("The sequence read from the file contains insufficient data. Check that the file has melody and chord data");
        }
        return sequence;
    }
    
    /**
     * Create a name generator network, and load it with the given connectome, printing any error messages
     * @param nameGeneratorConnectomePath The path of the connectome to load into the network
     * @return The created name generator network
     */
    public static NameGenerator initializeNameGenerator(String nameGeneratorConnectomePath) {
        NameGenerator nameGenerator = new NameGenerator();
        LogTimer.log("Packing name generator from files...");
        String[][] unmatchedPathsNameGenerator = (new ConnectomeLoader()).load(nameGeneratorConnectomePath, nameGenerator);

        String[] notFoundNameGen = unmatchedPathsNameGenerator[0];
        if (notFoundNameGen.length > 0) {
            System.err.println(notFoundNameGen.length + " files were not able to be matched to the name generator network!");
            for (String fileName : notFoundNameGen) {
                System.err.println("\t" + fileName);
            }
        }
        
        String[] missingNameGen = unmatchedPathsNameGenerator[1];
        if (missingNameGen.length > 0) {
            System.err.println(missingNameGen.length + " files needed for loading the name generator were missing from the connectome file!");
            for (String fileName : missingNameGen) {
                System.err.println("\t" + fileName);
            }
        }
        
        return nameGenerator;
    }
    
    /**
     * Create a product compressing autoencoder network and load it with the given connectome, printing any error messages
     * @param autoencoderConnectomePath The path of the connectome to load into the network
     * @return The created product compressing autoencoder network
     */
    public static ProductCompressingAutoencoder initializeAutoencoder(String autoencoderConnectomePath, boolean variational){
        //Initialization
        LogTimer.log("Initializing autoencoder...");
        ProductCompressingAutoencoder autoencoder = new ProductCompressingAutoencoder(24, 9, 48, 84 + 1, variational); //create our network

        //load the network from connectome file or directory
        LogTimer.startLog("Loading autoencoder from files");
        String[][] autoencoderUnmatchedPaths = (new ConnectomeLoader()).load(autoencoderConnectomePath, autoencoder);

        String[] autoencoderUnrecognizedPaths = autoencoderUnmatchedPaths[0];
        if (autoencoderUnrecognizedPaths.length > 0) {
            System.err.println(autoencoderUnrecognizedPaths.length + " files were not recognized by the product compressing autoencoder architecture!");
            for (String fileName : autoencoderUnrecognizedPaths) {
                System.err.println("\t" + fileName);
            }
        }
        
        String[] autoencoderMissingPaths = autoencoderUnmatchedPaths[1];
        if (autoencoderMissingPaths.length > 0) {
            System.err.println(autoencoderMissingPaths.length + " files for loading the product compressing autoencoder network were missing from the connectome file!");
            for (String fileName : autoencoderMissingPaths) {
                System.err.println("\t" + fileName);
            }
        }
        
        LogTimer.endLog();
        
        return autoencoder;
    }
    
    /**
     * Given an array of queue files, construct a matrix of all feature vectors in the queue files
     * @param queueFiles An array of queue files to compile into a matrix
     * @return The compiled matrix of feature vectors, where each row is a feature vector, and rows are ordered with the vectors of each queue contiguous, and those sets of vectors in the same order as the queues in the array
     */
    public static AMatrix generateFeatureQueueMatrix(File[] queueFiles) {
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
            return Matrix.create(features);
    }
    
    /**
     * Given a matrix of feature vectors, load the feature vectors into a queue with the given spacing, and generate a leadsheet with the given chord sequence
     * @param autoencoder
     * @param autoencoderConnectomePath
     * @param chordSequence
     * @param featureMatrixPath
     * @param featureSize
     * @param outputFilePath
     * @param songTitle 
     */
    public static void generateFromFeatureMatrix(ProductCompressingAutoencoder autoencoder, String autoencoderConnectomePath, LeadsheetDataSequence chordSequence, String featureMatrixPath, int featureSize, String outputFilePath, String songTitle) {
        AMatrix featureMatrix = (AMatrix) ReadWriteUtilities.readNumpyCSVFile(featureMatrixPath);
        int numComponents = featureMatrix.columnCount();
        LeadsheetDataSequence outputSequence = new LeadsheetDataSequence();
        while (outputSequence.maxLength() < featureSize * numComponents) {
            LeadsheetDataSequence currSegment = chordSequence.copy();
            while (currSegment.maxLength() > 0 && outputSequence.maxLength() < featureSize * numComponents) {
                outputSequence.pushStep(currSegment.pollBeats(), currSegment.pollChords(), null);
            }
        }

        LeadsheetDataSequence decoderInputSequence = outputSequence.copy();
        FragmentedNeuralQueue pcaQueue = new FragmentedNeuralQueue();
        pcaQueue.initFromFeatureMatrix(featureMatrix, featureSize);
        autoencoder.setQueue(pcaQueue);
        int i = 0;
        while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!{
            outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder
            if (++i == featureSize) {
                i = 0;
                refreshNetworkState(autoencoder, autoencoderConnectomePath);
            }
        }
        writeLeadsheetFile(outputSequence, outputFilePath, "_featureMatrixGen", songTitle);
    }
    
    
    
    /**
     * Writes the data contained in the output sequence to a file
     * @param outputSequence Contains all melody and chord data which will be written to the leadsheet
     * @param outputFolderPath The path of the folder we'll write out file in
     * @param fileName The name of the file. The suffix and a .ls extension will be appended to the end of the file name or overwrite the existing extension if there is one
     * @param suffix A small keyword or id to add to the end of the file name, such as "_Output" or "_TradingOutput"
     * @param songTitle The name of the song to write in the leadsheet! :)
     */
    public static void writeLeadsheetFile(LeadsheetDataSequence outputSequence, String outputFolderPath, String fileName, String suffix, String songTitle){
        LogTimer.startLog("Writing file...");
        String outputFilename = outputFolderPath + java.io.File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + suffix + ".ls"; //we'll write our generated file with the same name plus "_Output"
        LeadsheetIO.writeLeadsheet(outputSequence, outputFilename, songTitle);
        System.out.println(outputFilename);
        LogTimer.endLog();
    }
    
    
    /**
     * Writes the data contained in the output sequence to a file
     * @param outputSequence Contains all melody and chord data which will be written to the leadsheet
     * @param filePath The path and name of the new file. The suffix and a .ls extension will be appended to the end of the file name or overwrite the existing extension if there is one
     * @param suffix A small keyword or id to add to the end of the file name, such as "_Output" or "_TradingOutput"
     * @param songTitle The name of the song to write in the leadsheet! :)
     */
    public static void writeLeadsheetFile(LeadsheetDataSequence outputSequence, String filePath, String suffix, String songTitle){
        LogTimer.startLog("Writing file...");
        String outputFilename = filePath.substring(0, filePath.lastIndexOf(".")) + suffix + ".ls"; //we'll write our generated file with the same name plus "_Output"
        LeadsheetIO.writeLeadsheet(outputSequence, outputFilename, songTitle);
        System.out.println(outputFilename);
        LogTimer.endLog();
    }
    
    
    /**
     * Encode the input sequence into a feature representation in the autoencoder's queue
     * @param autoencoder The autoencoder to encode with
     * @param inputSequence This sequence should contain the input melody, chord data, and beat data
     */
    public static void encodeFromSequence(ProductCompressingAutoencoder autoencoder, LeadsheetDataSequence inputSequence) {
        LogTimer.startLog("Encoding data...");
        while (inputSequence.hasNext()) { //iterate through time steps in input data
            autoencoder.encodeStep(inputSequence.retrieve()); //feed the resultant input vector into the network
        }
        LogTimer.endLog();
    }

    /**
     * Decodes melody data from the current contents of the autoencoder queue and the decoder input sequence, and pushes it into the outputSequence
     * @param autoencoder The autoencoder to decode with, whose queue data we'll decode from
     * @param outputSequence The LeadsheetDataSequence which we will push our melody data into
     * @param decoderInputSequence This should contain the additional data the decoder of the network needs to decode melody data (for LeadsheetDataSequence this is the chord and beat data)
     */
    public static void decodeToSequence(ProductCompressingAutoencoder autoencoder, LeadsheetDataSequence outputSequence, LeadsheetDataSequence decoderInputSequence) {
        LogTimer.startLog("Decoding...");
        while (autoencoder.hasDataStepsLeft()) { //we are done encoding all time steps, so just finish decoding!
            outputSequence.pushStep(null, null, autoencoder.decodeStep(decoderInputSequence.retrieve())); //take sampled data for a timestep from autoencoder     
        }
        LogTimer.endLog();
    }

    /**
     * Loads the queue contents of an autoencoder from a feature matrix file
     *
     * @param autoencoder The autoencoder network we're going to load our
     * created queue into
     * @param featureMatrixPath A feature matrix file is a csv file where each
     * row is a feature vector
     * @param featureSize The distance we will space these features in the
     * queue. This should be the same featureSize you have specified for the
     * queue
     */
    public static void loadQueueFromFeatureMatrixFile(ProductCompressingAutoencoder autoencoder, String featureMatrixPath, int featureSize) {
        FragmentedNeuralQueue pcaQueue = new FragmentedNeuralQueue();
        int spacing = featureSize;
        AMatrix pcaFeatureMatrix = (AMatrix) ReadWriteUtilities.readNumpyCSVFile(featureMatrixPath);
        pcaQueue.initFromFeatureMatrix(pcaFeatureMatrix, featureSize);
        autoencoder.setQueue(pcaQueue);
    }

    /**
     * Analyze the melody contained in a LeadsheetDataSequence, and return a
     * vector of the property values
     *
     * @param sequence The sequence whose melody data we'll analyze
     * @param featureProperty Elements of the returned vector will be between
     * zero and one, and represent the ratio of the timeSteps with the given
     * feature property to the total timeSteps in a feature: "rest" -> there is
     * rest on this timeStep, "sustain" -> this timeStep is sustaining a note,
     * "articulation" -> this timeStep is articulating a new note
     * @param featureSize This is the size of each feature in timeSteps
     * @return An AVector with size equal to sequence melody length divided by
     * featureSize, wherein each element of the vector is the property-graded
     * value of the feature
     */
    public static AVector melodyFeatureAnalysis(LeadsheetDataSequence sequence, String featureProperty, int featureSize) {

        LogTimer.startLog("Analyzing generatedFeatures");
        int numFeatures = sequence.melodySize() / featureSize; //if the melody part isn't divisible by the fixed feature size, round down.
        AVector featurePropertyValues = Vector.createLength(numFeatures);
        int featureIndex = 0;
        LeadsheetDataSequence copySequence = sequence.copy(); //create a copy of our sequence to perform analysis on
        int spacing = featureSize;
        switch (featureProperty) /* Could refactor this to make calls to an interface for testing if we design more feature properties */ {
            case "rest":
                boolean wasLastRest = false;
                for (int j = 0; j < numFeatures; j++) {
                    int restStepCount = 0;
                    for (int i = 0; i < spacing; i++) {
                        AVector polledMelodyStep = copySequence.pollMelody();
                        if (polledMelodyStep.get(0) == -1.0) {
                            wasLastRest = true;
                            restStepCount++;
                        } else if (polledMelodyStep.get(0) == -2.0 && wasLastRest) {
                            restStepCount++;
                        } else {
                            wasLastRest = false;
                        }
                    }
                    featurePropertyValues.set(featureIndex++, ((double) restStepCount) / ((double) spacing));
                }
                break;
            case "sustain":
                for (int j = 0; j < numFeatures; j++) {
                    int sustainStepCount = 0;
                    for (int i = 0; i < spacing; i++) {
                        AVector polledMelodyStep = copySequence.pollMelody();
                        if (polledMelodyStep.get(0) == -2.0) {
                            sustainStepCount++;
                        }
                    }
                    featurePropertyValues.set(featureIndex++, ((double) sustainStepCount) / ((double) spacing));
                }
                break;
            case "articulate":
                for (int j = 0; j < numFeatures; j++) {
                    int articulateStepCount = 0;
                    for (int i = 0; i < spacing; i++) {
                        AVector polledMelodyStep = copySequence.pollMelody();
                        if (polledMelodyStep.get(0) >= 0.0) {
                            articulateStepCount++;
                        }
                    }
                    featurePropertyValues.set(featureIndex++, ((double) articulateStepCount) / ((double) spacing));
                }
                break;
            default:
                break;
        }
        LogTimer.endLog();
        return featurePropertyValues;
    }

}
