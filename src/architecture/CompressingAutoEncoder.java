/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import filters.Operations;
import filters.GroupedSoftMaxSampler;
import filters.NoteEncodingCleanFilter;
import java.util.Queue;
import filters.DataFilter;
import encoding.EncodingParameters;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

/**
 * Class CompressingAutoencoder describes a neural network architecture which
 * bridges an LSTM encoder and decoder with a fragmented neural queue.
 *
 * @author Nicholas Weintraut
 */
public class CompressingAutoEncoder implements Loadable {

    private LoadTreeNode loadNode;
    private int inputSize;
    private int featureVectorSize;
    private int outputSize;
    private AVector currOutput;
    private LSTM encoder1;
    private LSTM encoder2;
    private FullyConnectedLayer fullLayer1;
    private FragmentedNeuralQueue queue;
    private LSTM decoder1;
    private LSTM decoder2;
    private FullyConnectedLayer fullLayer2;
    private DataFilter finalSampler;
    private DataFilter outputCleaner;
    private AutoencoderInputManager inputManager;

    /**
     * Initializes an instance of CompressingAutoEncoder without initializing
     * component weights and biases. Weights and biases should be loaded using
     * AutoEncoderMeatPacker
     *
     * @see AutoEncoderMeatPacker
     * @param inputManager The input manager for the CompressingAutoEncoder
     */
    public CompressingAutoEncoder(AutoencoderInputManager inputManager, int inputSize, int outputSize, int featureVectorSize) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.featureVectorSize = featureVectorSize;
        this.inputManager = inputManager;
        encoder1 = new LSTM();
        encoder2 = new LSTM();
        fullLayer1 = new FullyConnectedLayer(Operations.Sigmoid);
        queue = new FragmentedNeuralQueue();
        decoder1 = new LSTM();
        decoder2 = new LSTM();
        //op type is none because we will feed its result through a one hot softmax sampler
        fullLayer2 = new FullyConnectedLayer(Operations.None);
        finalSampler = new GroupedSoftMaxSampler(EncodingParameters.noteEncoder.getGroups());
        outputCleaner = new NoteEncodingCleanFilter();
    }

    public boolean hasDataStepsLeft() {
        return !queue.isEmpty();
    }
    private int timeStep = 0;

    public void encodeStep(AVector input) {
        if (input.length() == inputSize) {
            inputManager.takeInput(input);
            AVector managerInput = inputManager.retrieveEncoderInput();
            AVector encoding1 = encoder1.step(managerInput);
            AVector encoding2 = encoder2.step(encoding1);
            AVector vectorEncoding = fullLayer1.forward(encoding2);
            AVector outputVector = vectorEncoding.subVector(1, featureVectorSize);
            queue.enqueueStep(outputVector, vectorEncoding.get(0));
        } else {
            throw new RuntimeException("Your input had a different size than this network is configured for!");
        }
    }

    public boolean canDecode() {
        return queue.hasFullBuffer();
    }

    public void printFeatureGroups() {
        queue.printFeatureGroups();
    }

    public AVector decodeStep() {
        //current output at very beginning should be a rest
        AVector decoding1 = decoder1.step(inputManager.retrieveDecoderInput(queue.dequeueStep(), currOutput));
        AVector decoding2 = decoder2.step(decoding1);
        AVector decoding3 = fullLayer2.forward(decoding2);
        //use sampler, and then apply cleaning filter
        currOutput = outputCleaner.filter(finalSampler.filter(decoding3));
        return currOutput;
    }

    public LSTM getEncoderLSTM1() {
        return encoder1;
    }

    public LSTM getEncoderLSTM2() {
        return encoder2;
    }

    public FullyConnectedLayer getEncoderFullLayer() {
        return fullLayer1;
    }

    public LSTM getDecoderLSTM1() {
        return decoder1;
    }

    public LSTM getDecoderLSTM2() {
        return decoder2;
    }

    public FullyConnectedLayer getDecoderFullLayer() {
        return fullLayer2;
    }

    public void setCurrOutput(int outputSize) {
        currOutput = Vector.createLength(outputSize);
    }

    @Override
    public boolean load(INDArray data, String loadPath) {
        String car = pathCar(loadPath);
        String cdr = pathCdr(loadPath);
        if (car.equals("enc")) {
            car = pathCar(cdr);
            cdr = pathCdr(cdr);
            switch (car) {
                case "full":
                    return fullLayer1.load(data, cdr);
                case "lstm1":
                    return encoder1.load(data, cdr);
                case "lstm2":
                    return encoder2.load(data, cdr);
                default:
                    return false;
            }
        } else if (car.equals("dec")) {
            car = pathCar(cdr);
            cdr = pathCdr(cdr);
            switch (car) {
                case "full":
                    return fullLayer2.load(data, cdr);
                case "lstm1":
                    return decoder1.load(data, cdr);
                case "lstm2":
                    return decoder2.load(data, cdr);
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public LoadTreeNode constructLoadTree() {
        String[] encoderLoadStrings = new String[]{"full","lstm1","lstm2"};
        LoadTreeNode[] encoderLoadNodes = new LoadTreeNode[]{fullLayer1.constructLoadTree(), encoder1.constructLoadTree(), encoder2.constructLoadTree()};
        LoadTreeNode encoderNode = new LoadTreeNode(encoderLoadStrings, encoderLoadNodes);
        
        String[] decoderLoadStrings = new String[]{"full","lstm1","lstm2"};
        LoadTreeNode[] decoderLoadNodes = new LoadTreeNode[]{fullLayer2.constructLoadTree(), decoder1.constructLoadTree(), decoder2.constructLoadTree()};
        LoadTreeNode decoderNode = new LoadTreeNode(decoderLoadStrings, decoderLoadNodes);
        
        String[] primaryLoadStrings = new String[]{"enc","dec"};
        LoadTreeNode[] primaryChildNodes = new LoadTreeNode[]{encoderNode, decoderNode};
        LoadTreeNode primaryNode = new LoadTreeNode(primaryLoadStrings, primaryChildNodes);
        assignToNode(primaryNode);
        return primaryNode;
    }

    @Override
    public LoadTreeNode getCurrentLoadTree() {
        return loadNode;
    }

    @Override
    public void assignToNode(LoadTreeNode node) {
        node.setNetworkPiece(this);
        this.loadNode = node;
    }
}
