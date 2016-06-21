/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;
import org.nd4j.linalg.api.ndarray.INDArray;
import lstm.LSTM;
import lstm.FullyConnectedLayer;
import lstm.FragmentedNeuralQueue;
import lstm.OpType;
import lstm.Sampler;
import lstm.NoteSoftMaxOneHotSampler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import lstm.OutputFilter;
import lstm.SwitchPairingsFilter;
import main.LogTimer;

/**
 *
 * @author cssummer16
 */
public class CompressingAutoencoder {
    private int inputSize;
    private int chopSize;
    private int featureVectorSize;
    private int outputSize;
    private INDArray currInput;
    private INDArray currOutput;
    private LSTM encoder1;
    private LSTM encoder2;
    private FullyConnectedLayer fullLayer1;
    private FragmentedNeuralQueue queue;
    private LSTM decoder1;
    private LSTM decoder2;
    private FullyConnectedLayer fullLayer2;
    private Sampler finalSampler;
    private OutputFilter outputFilter;
    
    
    public CompressingAutoencoder(int inputSize, int chopSize, int encoderBridge, int encoderSize, int decoderBridge, int decoderSize, int featureVectorSize, int outputSize)
    {
        this.inputSize = inputSize;
        this.chopSize = chopSize;
        this.featureVectorSize = featureVectorSize;
        this.outputSize = outputSize;
        encoder1 = new LSTM(inputSize, encoderBridge);
        encoder2 = new LSTM(encoderBridge, encoderSize);
        fullLayer1 = new FullyConnectedLayer(encoderSize, featureVectorSize + 1, OpType.Sigmoid);
        queue = new FragmentedNeuralQueue(featureVectorSize);
        decoder1 = new LSTM(featureVectorSize + (inputSize - chopSize) + outputSize, decoderBridge);
        decoder2 = new LSTM(decoderBridge, decoderSize);
        //op type is none because we will feed its result through a one hot softmax sampler
        fullLayer2 = new FullyConnectedLayer(decoderSize, outputSize, OpType.None);
        finalSampler = new NoteSoftMaxOneHotSampler();
        outputFilter = new SwitchPairingsFilter();
        
        currInput = Nd4j.zeros(inputSize);
        currOutput = Nd4j.zeros(outputSize);
    }
    public CompressingAutoencoder(int inputSize, int lstmSize, int featureVectorSize, int outputSize, INDArray encoder1Weights, INDArray encoder2Weights, INDArray encoder1Biases, INDArray fullLayer1Weights, INDArray fullLayer1Biases, INDArray encoder2Biases, INDArray decoder1Weights, INDArray decoder2Weights, INDArray decoder1Biases, INDArray decoder2Biases, INDArray fullLayer2Weights, INDArray fullLayer2Biases)
    {
        this.inputSize = inputSize;
        this.featureVectorSize = featureVectorSize;
        this.outputSize = outputSize;
        encoder1 = new LSTM(inputSize, lstmSize, encoder1Weights, encoder1Biases);
        encoder2 = new LSTM(lstmSize, lstmSize, encoder2Weights, encoder2Biases);
        fullLayer1 = new FullyConnectedLayer(lstmSize, featureVectorSize + 1, OpType.Softmax, fullLayer2Weights, fullLayer2Biases);
        queue = new FragmentedNeuralQueue(featureVectorSize);
        decoder1 = new LSTM(featureVectorSize + (inputSize - chopSize) + outputSize, lstmSize, decoder1Weights, decoder1Biases);
        decoder2 = new LSTM(lstmSize, lstmSize, decoder2Weights, decoder2Biases);
        
        fullLayer2 = new FullyConnectedLayer(lstmSize, outputSize, OpType.None, fullLayer2Weights, fullLayer2Biases);
        finalSampler = new NoteSoftMaxOneHotSampler();
        outputFilter = new SwitchPairingsFilter();
    }
    
    public void encodeStep(INDArray input)
    {
        
        if(input.length() == inputSize)
        {
            currInput = input;
            //long startTime = System.nanoTime();
            INDArray encoding1 = encoder1.step(input);
            INDArray encoding2 = encoder2.step(encoding1);
            INDArray vectorEncoding = fullLayer1.forward(encoding2);
            //long startQueueTime = System.nanoTime();
            queue.enqueueStep(vectorEncoding.get(NDArrayIndex.interval(1,featureVectorSize + 1)), vectorEncoding.getDouble(0));
            //long endTime = System.nanoTime();
            //System.out.println("LSTMs took " + (startQueueTime - startTime) / 1000000000.00 + " seconds, while enqueue took " + (endTime - startQueueTime) / 1000000000.00 + " seconds");
        }
        else
        {
            throw new RuntimeException("Your input had a greater size than this network is configured for!");
        }
    }
    
    public boolean canDecode()
    {
        return queue.hasFullBuffer();
    }
    
    public INDArray decodeStep()
    {
        //LogTimer.startLog("Decoding1...");
        INDArray decoding1 = decoder1.step(Nd4j.concat(0, currInput.get(NDArrayIndex.interval(0, inputSize - chopSize)), queue.dequeueStep(), currOutput));
        //LogTimer.endLog();
        //LogTimer.startLog("Decoding2...");
        INDArray decoding2 = decoder2.step(decoding1);
        //LogTimer.endLog();
        //LogTimer.startLog("FullLayer...");
        INDArray decoding3 = fullLayer2.forward(decoding2);
        //LogTimer.endLog();
        //use sampler, and then apply cleaning filter
        currOutput = outputFilter.filter(finalSampler.sample(decoding3));
        return currOutput;
    }
}
