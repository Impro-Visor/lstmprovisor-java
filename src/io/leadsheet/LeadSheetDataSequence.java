/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.leadsheet;

import encoding.EncodingParameters;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.util.Queue;
import org.nd4j.linalg.factory.Nd4j;
import io.DataSequence;
import java.util.LinkedList;
/**
 *
 * @author cssummer16
 */
public class LeadSheetDataSequence implements DataSequence{
    private Queue<INDArray> beats;
    private Queue<INDArray> chords;
    private Queue<INDArray> melody;
    
    private int entrySize;
    
    public LeadSheetDataSequence()
    {
        beats = new LinkedList<>();
        chords = new LinkedList<>();
        melody = new LinkedList<>();
        entrySize = 0;
    }
    
    /**
     * Copies this LeadSheetDataSequence by copying input LeadSheetDataSequence's beat, chord, and melody queues (It does duplicate INDArray objects!)
     * @return A duplicate of this LeadSheetDataSequence 
     */
    @Override
    public LeadSheetDataSequence dup() {
        LeadSheetDataSequence duplicate = new LeadSheetDataSequence();
        beats.stream().forEach((beat) -> {duplicate.beats.offer(beat.dup());});
        chords.stream().forEach((chord) -> {duplicate.chords.offer(chord.dup());});
        melody.stream().forEach((noteStep) -> {duplicate.melody.offer(noteStep.dup());});
        duplicate.entrySize = entrySize;
        return duplicate;
    }
    
    public INDArray pollMelody() {
        return melody.poll();
    }
    
    public boolean hasMelodyLeft(){
        return !melody.isEmpty();
    }
    
    public boolean hasChordsLeft(){
        return !chords.isEmpty();
    }
    
    public void clearMelody() {
        melody = new LinkedList<>();
    }
    
    public INDArray pollChords() {
        return chords.poll();
    }
    
    public INDArray pollBeats() {
        return beats.poll();
    }
    
    public void pushStep(INDArray beat, INDArray chord, INDArray note) {
        if(beat != null) {
            this.beats.offer(beat);
        }
        if(chord != null) {
            this.chords.offer(chord);
        }
        if(note != null) {
            this.melody.offer(note);
        }
    }
    
    @Override
    public int entrySize() {
        if(entrySize == 0)
            entrySize = ((!beats.isEmpty()) ? beats.peek().length() : 0) +
                    ((!chords.isEmpty()) ? chords.peek().length() : 0) +
                    ((!melody.isEmpty()) ? melody.peek().length() : 0);
        return entrySize;
    }
    
    public boolean isBalanced() {
        return beats.size() == chords.size()  && beats.size() == melody.size();
    }
    
    @Override
    public INDArray retrieve() {
        return Nd4j.concat(0, beats.poll(), chords.poll(), melody.poll());
    }
    
    @Override
    public boolean hasNext() {
        return !melody.isEmpty() && !beats.isEmpty() && !chords.isEmpty();
    }
}
