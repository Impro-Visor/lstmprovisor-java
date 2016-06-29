/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.util.Queue;
import java.util.LinkedList;
/**
 * Class AutoencoderInputManager takes in the initial input for an Autoencoder and handles 
 * chopping up data that should go to either the encoder, decoder, or both 
 * (Usually, the encoder will get all input, and then a portion of the input will be sent to the decoder for reconstruction).
 * @see CompressingAutoencoder
 * @author Nicholas Weintraut
 */
public abstract class AutoencoderInputManager {
    protected Queue<INDArray> encoderQueue;
    protected Queue<INDArray> decoderQueue;
    
    public AutoencoderInputManager()
    {
        encoderQueue = new LinkedList<INDArray>();
        decoderQueue = new LinkedList<INDArray>();
    }
    public void takeInput(INDArray input)
    {
        encoderQueue.offer(input);
        decoderQueue.offer(input.dup());
    }
    
    public abstract int getEncoderInputSize();
    
    
    public abstract int getDecoderInputSize();
            
    
    public abstract INDArray retrieveEncoderInput();
    
    public abstract INDArray retrieveDecoderInput(INDArray neuralQueueOutput, INDArray decoderOutput);
}
