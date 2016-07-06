/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture.poex;

import architecture.FragmentedNeuralQueue;
import architecture.Loadable;
import filters.Operations;
import io.leadsheet.LeadSheetDataSequence;
import java.util.Random;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import nickd4j.NNUtilities;

/**
 *
 * @author cssummer16
 */
public class ProductCompressingAutoEncoder implements Loadable {
    private int inputSize;
    private int featureVectorSize;
    private int outputSize;
    private int low_bound;
    private int high_bound;
    private Expert[] encoder_experts;
    private Expert[] decoder_experts;
    private RelativeInputPart[][] encoder_inputs;
    private RelativeInputPart[][] decoder_inputs;
    private RelativeNoteEncoding[] encoder_expert_encodings;
    private RelativeNoteEncoding[] decoder_expert_encodings;
    private FragmentedNeuralQueue queue;
    
    private PassthroughInputPart beat_part;
    private PassthroughInputPart feature_part;
    private PassthroughInputPart[] cur_output_parts;
    private PassthroughInputPart[] last_output_parts;
    
    private LeadSheetDataSequence inputSequence;
    private LeadSheetDataSequence outputSequence;
    
    private int num_experts;
    
    private Random rand;
    
    public ProductCompressingAutoEncoder(LeadSheetDataSequence inputSequence, int inputSize, int outputSize, int beatVectorSize, int featureVectorSize, int lowbound, int highbound){
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.featureVectorSize = featureVectorSize;
        this.low_bound = lowbound;
        this.high_bound = highbound;
        this.rand = new Random();
        this.num_experts = 2;
        
        this.queue = new FragmentedNeuralQueue();
        
        this.inputSequence = inputSequence;
        this.outputSequence = inputSequence.copy();
        this.outputSequence.clearMelody();
        
        this.encoder_experts = new Expert[2];
        this.encoder_experts[0] = new Expert(Operations.None);
        this.encoder_experts[1] = new Expert(Operations.None);
        this.decoder_experts = new Expert[2];
        this.decoder_experts[0] = new Expert(Operations.None);
        this.decoder_experts[1] = new Expert(Operations.None);
        
        this.beat_part = new PassthroughInputPart(beatVectorSize);
        this.feature_part = new PassthroughInputPart(this.featureVectorSize);
        this.cur_output_parts = new PassthroughInputPart[2];
        this.cur_output_parts[0] = new PassthroughInputPart(this.outputSize);
        this.cur_output_parts[1] = new PassthroughInputPart(this.outputSize);
        this.last_output_parts = new PassthroughInputPart[2];
        this.last_output_parts[0] = new PassthroughInputPart(this.outputSize);
        this.last_output_parts[1] = new PassthroughInputPart(this.outputSize);
        
        this.encoder_inputs = new RelativeInputPart[2][4];
        this.encoder_inputs[0][0] = this.beat_part;
        this.encoder_inputs[0][1] = new PositionInputPart(lowbound, highbound, 2);
        this.encoder_inputs[0][2] = new ChordInputPart();
        this.encoder_inputs[0][3] = this.cur_output_parts[0];
        this.encoder_inputs[1][0] = this.beat_part;
        this.encoder_inputs[1][1] = new PositionInputPart(lowbound, highbound, 2);
        this.encoder_inputs[1][2] = new ChordInputPart();
        this.encoder_inputs[1][3] = this.cur_output_parts[1];
        
        this.decoder_inputs = new RelativeInputPart[2][5];
        this.decoder_inputs[0][0] = this.beat_part;
        this.decoder_inputs[0][1] = new PositionInputPart(lowbound, highbound, 2);
        this.decoder_inputs[0][2] = new ChordInputPart();
        this.decoder_inputs[0][3] = this.feature_part;
        this.decoder_inputs[0][4] = this.last_output_parts[0];
        this.decoder_inputs[1][0] = this.beat_part;
        this.decoder_inputs[1][1] = new PositionInputPart(lowbound, highbound, 2);
        this.decoder_inputs[1][2] = new ChordInputPart();
        this.decoder_inputs[1][3] = this.feature_part;
        this.decoder_inputs[1][4] = this.last_output_parts[1];
        
        this.encoder_expert_encodings = new RelativeNoteEncoding[2];
        this.encoder_expert_encodings[0] = new IntervalRelativeNoteEncoding(lowbound, highbound);
        this.encoder_expert_encodings[1] = new ChordRelativeNoteEncoding();
        this.decoder_expert_encodings = new RelativeNoteEncoding[2];
        this.decoder_expert_encodings[0] = new IntervalRelativeNoteEncoding(lowbound, highbound);
        this.decoder_expert_encodings[1] = new ChordRelativeNoteEncoding();
        
        for(int i=0; i<this.num_experts; i++) {
            this.encoder_expert_encodings[i].reset();
            this.last_output_parts[i].provide(this.decoder_expert_encodings[i].reset());
        }
    }
    
    
    public boolean hasDataStepsLeft() {
        return !queue.isEmpty();
    }
    
    public void encodeStep() {
        AVector beat = this.inputSequence.pollBeats();
        AVector raw_chord = this.inputSequence.pollChords();
        AVector raw_melody = this.inputSequence.pollMelody();
        int chord_root = (int) raw_chord.get(0);
        AVector chord_type = raw_chord.subVector(1, 12);
        int midinote = (int) raw_melody.get(0);
        System.out.println("Beat is " + beat);
        this.beat_part.provide(beat,this.num_experts);
        
        AVector accum_activations = null;
        for(int i=0; i<this.num_experts; i++) {
            RelativeNoteEncoding enc = this.encoder_expert_encodings[i];
            AVector encval = enc.encode(midinote, chord_root);
            int relpos = enc.get_relative_position(chord_root);
            this.cur_output_parts[i].provide(encval);
            
            AVector full_encoder_input = RelativeInputPart.combine(this.encoder_inputs[i], relpos, chord_root, chord_type);
            AVector activations = this.encoder_experts[i].process(full_encoder_input);
            
            if(accum_activations == null)
                accum_activations = activations;
            else
                accum_activations.add(activations);
        }
        
        Operations.Sigmoid.operate(accum_activations);
        AVector outputVector = accum_activations.subVector(1, featureVectorSize);
        this.queue.enqueueStep(outputVector, accum_activations.get(0));
    }
    
    
    public boolean canDecode() {
        return queue.hasFullBuffer();
    }
    
    public void perturbQueue(){
        // don't do anything (for now?)
    }
    
    public AVector decodeStep() {
        AVector beat = this.outputSequence.pollBeats();
        AVector raw_chord = this.outputSequence.pollChords();
        int chord_root = (int) raw_chord.get(0);
        AVector chord_type = raw_chord.subVector(1, 12);
        this.beat_part.provide(beat,this.num_experts);
        this.feature_part.provide(this.queue.dequeueStep(), this.num_experts);
        
        AVector accum_probabilities = null;
        for(int i=0; i<this.num_experts; i++) {
            RelativeNoteEncoding enc = this.decoder_expert_encodings[i];
            int relpos = enc.get_relative_position(chord_root);
            
            AVector full_decoder_input = RelativeInputPart.combine(this.decoder_inputs[i], relpos, chord_root, chord_type);
            AVector activations = this.decoder_experts[i].process(full_decoder_input);
            AVector probabilities = enc.getProbabilities(activations, chord_root, this.low_bound, this.high_bound);
            
            if(accum_probabilities == null)
                accum_probabilities = probabilities.mutable();
            else
                accum_probabilities.multiply(probabilities);
        }
        
        accum_probabilities.normalise();
        int sampled = NNUtilities.sample(this.rand, accum_probabilities);
        int midival;
        if(sampled == 0)
            midival = -1;
        else if(sampled == 1)
            midival = -2;
        else
            midival = this.low_bound + (sampled-2);
        
        for(int i=0; i<this.num_experts; i++) {
            RelativeNoteEncoding enc = this.decoder_expert_encodings[i];
            AVector prev_output = enc.encode(midival, chord_root);
            this.last_output_parts[i].provide(prev_output);
        }
        return Vector.of(midival);
    }
    
    @Override
    public boolean load(INDArray data, String loadPath) {
        // Expected format: (enc|dec)_#_<expert params>
        // i.e. enc_1_full_w
        String car = pathCar(loadPath);
        String cdr = pathCdr(loadPath);
        String car2 = pathCar(cdr);
        String cdr2 = pathCdr(cdr);
        Expert[] exps;
        switch (car) {
            case "enc":
                exps = this.encoder_experts;
                break;
            case "dec":
                exps = this.decoder_experts;
                break;
            default:
                return false;
        }
        int expert_idx = Integer.parseInt(car2);
        return exps[expert_idx].load(data, cdr2);
    }
}