/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;
import org.nd4j.linalg.api.ndarray.INDArray;
import lstm.LSTM;
import lstm.FragmentedNeuralQueue;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 *
 * @author cssummer16
 */
public class CompressingAutoencoder {
    private int inputSize;
    private int featureVectorSize;
    private int outputSize;
    private LSTM encoder;
    private FragmentedNeuralQueue queue;
    private LSTM decoder;
    
    
    public CompressingAutoencoder(int inputSize, int featureVectorSize, int outputSize)
    {
        this.inputSize = inputSize;
        this.featureVectorSize = featureVectorSize;
        this.outputSize = outputSize;
        encoder = new LSTM(inputSize, featureVectorSize + 1);
        queue = new FragmentedNeuralQueue(featureVectorSize);
        decoder = new LSTM(featureVectorSize, outputSize);
    }
    public CompressingAutoencoder(int inputSize, int featureVectorSize, int outputSize, INDArray encoderWeights, INDArray encoderBiases, INDArray decoderWeights, INDArray decoderBiases)
    {
        this.inputSize = inputSize;
        this.featureVectorSize = featureVectorSize;
        this.outputSize = outputSize;
        encoder = new LSTM(inputSize, featureVectorSize + 1, encoderWeights, encoderBiases);
        queue = new FragmentedNeuralQueue(featureVectorSize);
        decoder = new LSTM(featureVectorSize, outputSize, decoderWeights, decoderBiases);
    }
    
    public void encodeStep(INDArray input)
    {
        if(input.length() == inputSize)
        {
            INDArray encoding = encoder.step(input);
            queue.enqueueStep(encoding.get(NDArrayIndex.interval(0,featureVectorSize)), encoding.getDouble(featureVectorSize));
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
        return decoder.step(queue.dequeueStep());
    }
    /*
    public static void main(String[] args)
    {
        int steps = 192;
        int inputSize = 32;
        //There is an error with Nd4j furing multiplication of weights and input when the input size on an LSTM node is greater than the output size
        //geez
        int featureVectorSize = 500;
        int outputSize = 32;
        
    
        System.out.println("STARTED");
        long startTime = System.nanoTime();
        for(int i = 0; i < inputSequence.rows(); i++)
        {
            INDArray lstmOut1 = encoder.step(inputSequence.slice(0));
            //System.out.println("whyyyyyyy");
            INDArray lstm1Vector = Nd4j.create(lstmOut1.length() - 1);
            for(int j = 0; j < lstm1Vector.length(); j++)
            {
                lstm1Vector.putScalar(j, lstmOut1.getDouble(j));
            }
            //System.out.println("here we go queue!!!");
            fragQueue.enqueueStep(lstm1Vector, lstmOut1.getDouble(lstmOut1.length() - 1));
            //System.out.println("we queue'd!");
        }
        
        for(int i = 0; i < inputSequence.rows(); i++)
        {
            //System.out.println("AAAHHHHHH");
            
            //System.out.println(decoder.step(fragQueue.peek()));
            fragQueue.dequeueStep();
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        float milliDuration = duration / 1000000.0f;
        System.out.println("The operation took " + milliDuration + " milliseconds");
    }*/
}
