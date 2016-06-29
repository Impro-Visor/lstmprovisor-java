/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 *
 * @author cssummer16
 */
public class LeadsheetAutoencoderInputManager extends AutoencoderInputManager{
    

    private int noteSize;
    private int inputSize;
    private int featureVectorSize;
    
    public LeadsheetAutoencoderInputManager(int inputSize, int noteSize, int featureVectorSize)
    {
        this.inputSize = inputSize;
        this.noteSize = noteSize;
        this.featureVectorSize = featureVectorSize;
    }
    
    public LeadsheetAutoencoderInputManager(int noteSize)
    {
        this.noteSize = noteSize;
    }
    
    public void setInputSizes(int inputSize, int featureVectorSize)
    {
        this.inputSize = inputSize;
        this.featureVectorSize = featureVectorSize;
    }

    @Override
    public INDArray retrieveEncoderInput() {
        return encoderQueue.poll();
    }

    @Override
    public INDArray retrieveDecoderInput(INDArray neuralQueueOutput, INDArray decoderOutput) {
        //currently we don't do anything with the decoder size
        return Nd4j.concat(0, decoderQueue.peek().get(NDArrayIndex.interval(0, decoderQueue.poll().length() - noteSize)), neuralQueueOutput);
    }

    @Override
    public int getEncoderInputSize() {
        return inputSize;
    }

    @Override
    public int getDecoderInputSize() {
        return featureVectorSize + inputSize;
    }
    
}
