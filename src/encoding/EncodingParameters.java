/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoding;

/**
 *
 * @author cssummer16
 */
public class EncodingParameters {
    
    //must update
    public static final NoteEncoder noteEncoder = new CirclesOfThirdsEncoder();
    public static final ChordEncoder chordEncoder = new ChordEncoder();
}
