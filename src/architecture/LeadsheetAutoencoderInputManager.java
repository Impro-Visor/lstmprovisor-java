/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import mikera.vectorz.AVector;

/**
 *
 * @author cssummer16
 */
public class LeadsheetAutoencoderInputManager extends AutoencoderInputManager{
    

    private int noteSize;
    private int inputSize;
    private int featureVectorSize;
    private boolean hideOutput;
    
    public LeadsheetAutoencoderInputManager(int inputSize, int noteSize, int featureVectorSize)
    {
        this.inputSize = inputSize;
        this.noteSize = noteSize;
        this.featureVectorSize = featureVectorSize;
        hideOutput = true;
    }
    
    public LeadsheetAutoencoderInputManager(int noteSize, boolean hideOutput)
    {
        this.noteSize = noteSize;
        this.hideOutput = hideOutput;
    }
    
    public void setHideOutput(boolean hideOutput)
    {
        this.hideOutput = hideOutput;
    }
    
    public void setInputSizes(int inputSize, int featureVectorSize)
    {
        this.inputSize = inputSize;
        this.featureVectorSize = featureVectorSize;
    }

    @Override
    public AVector retrieveEncoderInput() {
        return encoderQueue.poll();
    }

    @Override
    public AVector retrieveDecoderInput(AVector neuralQueueOutput, AVector decoderOutput) {
        //currently we don't do anything with the decoder size
        AVector decInput = decoderQueue.peek().subVector(0, decoderQueue.poll().length() - noteSize).join(neuralQueueOutput);
        if(!hideOutput)
            decInput = decInput.join(decoderOutput);
        return decInput;
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
