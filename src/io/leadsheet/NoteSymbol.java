/**
 * This Java Class is part of the RBM-provisor Application
 * which, in turn, is part of the Intelligent Music Software
 * project at Harvey Mudd College, under the directorship of Robert Keller.
 *
 * Copyright (C) 2009 Robert Keller and Harvey Mudd College
 *
 * RBM-provisor is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * RBM-provisor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * merchantability or fitness for a particular purpose.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RBM-provisor; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package io.leadsheet;

import polya.*;
import java.io.Serializable;


/**
 * A NoteSymbol represents a note symbolically, and is typically created
 * from a leadsheet string.  A NoteSymbol can also represent a rest,
 * consistent with a Note doing so.  However,
 * in contrast to class Note, it doesn't say anything about ties and such.
 * A Note can be created from a NoteSymbol using the toNote() method.
 *
 * @see         Leadsheet
 * @author      Robert Keller
 */
public class NoteSymbol implements Constants {

    /**
     * the PitchClass of this note.  A rest is represented by a pitchClass value of null.
     */

    private PitchClass pitchClass;

    /**
     * The octave of this note, where the octave middle-C and just above is 0,
     * the octave above that is 1, below that is -1, etc.
     */

   private int octave;


    /**
     * The duration of the note in slots
     */

    private int duration = 60;

    private String stringRep;

    /**
     * used in lick generation
     */

    private Double probability = 1.0;

    /*
     * Midi	Pitch class	Octave
     *  0       c               -5
     * 12       c               -4
     * 24       c               -3
     * 36       c               -2
     * 48       c               -1
     * 60       c                0
     * 72       c                1  
     * 84       c                2
     * 96       c                3
     * 108      c                4
     * 120      c                5
     * 127      g                5
     */

    /**
     * Creates a NoteSymbol from a leadsheet String.
     * Because the String could be ill-formed, we use a factory rather than a constructor
     * and keep the actual constructor private.
     */

   public static NoteSymbol makeNoteSymbol(String string)
     {
     return makeNoteSymbol(string, 0);
     } 
/*
   public static NoteSymbol makeNoteSymbol(Note note)
     {
     return makeNoteSymbol(note.toLeadsheet());
     } 
//*/
    public static NoteSymbol makeNoteSymbol(String string, int transposition)
     {
     int len = string.length();

     if( len == 0 )
       {
       return null;
       }

    string = string.toLowerCase();

    NoteSymbol noteSymbol = new NoteSymbol();

     // First determine if we have a rest

     char c = string.charAt(0);

     if( c == Key.RESTCHAR )
       {
       noteSymbol.pitchClass = null;
       noteSymbol.duration = Key.getDuration(string.substring(1));
       noteSymbol.stringRep = string;
       return noteSymbol;
       }    

     if( !PitchClass.isValidPitchStart(c) )
       {
       return null;
       }

     int index = 1;

     boolean natural = true;
     boolean sharp = false;

     StringBuffer noteBase = new StringBuffer();

     noteBase.append(c);

     if( index < len )
       {
       char second = string.charAt(1);
       if( second == SHARP || second == FLAT )
	 {
	 index++;
	 noteBase.append(second);
	 natural = false;
	 sharp = (second == SHARP);
	 }
       }

     noteSymbol.pitchClass = PitchClass.getPitchClass(noteBase.toString()).transpose(transposition);

     if( noteSymbol.pitchClass == null )
       {
       return null;
       }

     noteSymbol.octave = 0;

     // Check for any octave shifts specified in the notation

     boolean more = true;
     while( index < len && more )
       {
       switch( string.charAt(index) )
	 {
	 case PLUS:
	     noteSymbol.octave++;
	     index++;
	     break;

	 case MINUS:
	     noteSymbol.octave--;
	     index++;
	     break;

	 default:
	     more = false;
	 }
       }

     String durationString = string.substring(index);

     noteSymbol.duration = Key.getDuration(durationString);

     noteSymbol.establishStringRep(noteSymbol.octave, noteSymbol.duration);

     return noteSymbol;
     }


    public void setProbability(double probability)
    {
        this.probability = probability;
    }
    
    public Double getProbability()
    {
    return probability;
    }

    public NoteSymbol getRestSymbol(int duration)
    {
    return new NoteSymbol(null, 0, duration);
    }

  public boolean enharmonic(NoteSymbol other)
    {
    return getSemitones() == other.getSemitones();
    }

  public boolean enharmonic(PitchClass pc)
    {
    return getSemitones() == pc.getSemitones();
    }

  public int getRise(NoteSymbol other)
    {
    return other.getSemitones() - getSemitones();
    }

  public static boolean equalNoteSequences(Polylist x, Polylist y)
    {
    while( x.nonEmpty() && y.nonEmpty() )
      {
      if( !((NoteSymbol)x.first()).equalNotes((NoteSymbol)y.first()) )
        {
        return false;
        }
      x = x.rest();
      y = y.rest();
      }

    return x.isEmpty() && y.isEmpty();
    }
 
  public static boolean isomorphicNoteSequences(Polylist x, Polylist y)
    {
    if( x.isEmpty() || y.isEmpty() )
      {
      return x.isEmpty() && y.isEmpty();
      }
    
    NoteSymbol x0 = (NoteSymbol)x.first();
    NoteSymbol y0 = (NoteSymbol)y.first();

    x = x.rest();
    y = y.rest();

    int rise = x0.getRise(y0);

    x = transposeNoteSymbolList(x, rise);

    return equalNoteSequences(x, y);
    }

  public boolean equalNotes(NoteSymbol other)
    {
    return duration == other.duration 
        && octave == other.octave
        && enharmonic(other);
    }

  public boolean equals(Object other)
     {
     if( other instanceof NoteSymbol )
       {
       return equalNotes((NoteSymbol)other);
       }
     return false;
     }

  public int getSemitonesAbove(NoteSymbol other)
    {
    int distance = other.getSemitones() - this.getSemitones();
    while( distance <= 0 )
      {
      distance += OCTAVE;
      }
    return distance;
    }


/**
 * Check to see whether the list argument contains a member that
 * is enharmonically equivalent to this member.
 */

  public boolean enhMember(Polylist noteSymbols)
    {
    while( noteSymbols.nonEmpty() )
      {
      NoteSymbol ns = (NoteSymbol)noteSymbols.first();
      if( enharmonic(ns) )
        {
        return true;
        }
      noteSymbols = noteSymbols.rest();
      }
    return false;
    }

  public Polylist enhDrop(Polylist noteSymbols)
    {
    return enhDrop(noteSymbols, pitchClass);
    }

  public static Polylist enhDrop(Polylist noteSymbols, PitchClass toDrop)
    {
    Polylist result = Polylist.nil;

    while( noteSymbols.nonEmpty() )
      {
      NoteSymbol first = (NoteSymbol)noteSymbols.first();
      if( !first.enharmonic(toDrop) )
        {
        result = result.cons(first);
        }
      noteSymbols = noteSymbols.rest();
      }
    return result.reverse();
    }
  
  public static NoteSymbol getHighest(Polylist notes) {
      NoteSymbol highest = (NoteSymbol)notes.first();
      notes = notes.rest();
      while(notes.nonEmpty()) {
          NoteSymbol current = (NoteSymbol)notes.first();
          notes = notes.rest();
          if(current.getMIDI() > highest.getMIDI())
              highest = current;
      }
      return highest;
  }
  
  public static NoteSymbol getLowest(Polylist notes) {
      NoteSymbol lowest = (NoteSymbol)notes.first();
      notes = notes.rest();
      while(notes.nonEmpty()) {
          NoteSymbol current = (NoteSymbol)notes.first();
          notes = notes.rest();
          if(current.getMIDI() < lowest.getMIDI())
              lowest = current;
      }
      return lowest;
  }
  
 /**
  * Create a closed voicing ascending from the first note in the list.
  */

  public static Polylist closedVoicing(Polylist noteSymbols)
    {
    if( noteSymbols.isEmpty() )
      {
      return Polylist.nil;
      }

    Polylist result = Polylist.nil;

    // below, applying the constructor adjusts the octave
    NoteSymbol previous = new NoteSymbol((NoteSymbol)noteSymbols.first());
    noteSymbols = previous.enhDrop(noteSymbols);
    result = result.cons(previous);

    while( noteSymbols.nonEmpty() )
      {
      NoteSymbol next = new NoteSymbol(previous.findNextHigher(noteSymbols));
      if( !next.enharmonic(previous) )
        {
        result = result.cons(next);
        }
      noteSymbols = next.enhDrop(noteSymbols);
      }
    return result.reverse();
    }

  public NoteSymbol findNextHigher(Polylist noteSymbols)
    {
    // below, applying the constructor adjusts the octave
    NoteSymbol nextHigher = new NoteSymbol((NoteSymbol)noteSymbols.first());
    noteSymbols = noteSymbols.rest();
    int distance = getSemitonesAbove(nextHigher);

    while( noteSymbols.nonEmpty() )
      {
    // below, applying the constructor adjusts the octave
      NoteSymbol next = new NoteSymbol((NoteSymbol)noteSymbols.first());
      int d = getSemitonesAbove(next);
      if( d < distance )
        {
        nextHigher = next;
        distance = d;
        }
      noteSymbols = noteSymbols.rest();
      }
    return nextHigher;
    }  

  public Note toNote()
    {
    if( pitchClass == null )
      {
      return Note.makeRest(duration);
      }
    else
      {
      return PitchClass.makeNote(pitchClass, getMIDIoctave(), duration);
      }
    }

  public NoteSymbol transpose(int rise)
    {
    if( isRest() || rise == 0 )
      {
      return this;	// no transposition for rest
      }

    int newOctave = octave;
    int newSemitones = pitchClass.getSemitones() + rise;
    while( newSemitones >= 12 )
      {
      newOctave++;
      newSemitones -= 12;
      }
    while( newSemitones < 0 )
      {
      newOctave--;
      newSemitones += 12;
      }

    NoteSymbol newNoteSymbol = new NoteSymbol(pitchClass.transpose(rise), newOctave, duration);
    newNoteSymbol.setProbability(getProbability());

//System.out.println("transposing NoteSymbol " + this + " by " + rise + " to " + newNoteSymbol);

    return newNoteSymbol;
    }

   /** constructor */

  private NoteSymbol()
    {
    }


   /** constructor */

   public NoteSymbol(PitchClass pitchClass)
     {
     this(pitchClass, 0, BEAT/2);
     } 


   /** constructor */

   public NoteSymbol(NoteSymbol that)
     {
     this(that.getPitchClass());
     } 


   /** constructor */

   public NoteSymbol(PitchClass pitchClass, int octave, int duration)
    {
    this.pitchClass = pitchClass;
    this.duration = duration;

    establishStringRep(octave, duration);
    }

   public String toPitchString() {
       return toPitchStringBuffer(octave).toString();
   }

   public StringBuffer toPitchStringBuffer(int octave) {
       StringBuffer buffer0 = new StringBuffer();
       if( pitchClass == null ) {
           buffer0.append(REST_STRING);
       }
       else {
           buffer0.append(pitchClass);
           this.octave = octave;
           while( octave >= 1 )
           {
               buffer0.append(PLUS);
               octave--;
           }
           while( octave < 0 )
           {
               buffer0.append(MINUS);
               octave++;
           }
       }

       return buffer0;
   }

  protected void establishStringRep(int octave, int duration)
    {
    StringBuffer buffer0 = toPitchStringBuffer(octave);

    StringBuffer buffer = new StringBuffer();
    int value = Note.getDurationString(buffer, duration);
     if( /*value == 0 && */ buffer.length() > 1 )
       {
       buffer0.append(buffer.substring(1));	// discard initial +
       }
/* suppress warning -- RK
    else
       {
       ErrorLog.log(ErrorLog.SEVERE, "There is a problem rendering a note of duration " + duration);
       }
*/

    stringRep = buffer0.toString();
    }

  public static Note toNote(String string)
    {
    return makeNoteSymbol(string).toNote();
    }

   /** 
    * Return an indication of whether or not this is a rest
    */

   public boolean isRest()
     {
     return pitchClass == null;
     }


    /**
     * Return the number of semitones of the pitch class of this note
     */

   public int getSemitones()
     {
     if( isRest() )
       {
       return -1;
       }
     return pitchClass.getSemitones();
     }
   
     /**
     * Indicate whether or not this note is higher than the argument note
     */

   public boolean higher(NoteSymbol that)
     {
     return this.getMIDI() > that.getMIDI();
     }
   
   /**
     * Return the midi pitch number for this NoteSymbol
     */

   public int getMIDI()
     {
     if( isRest() )
       {
       return -1;
       }
     return pitchClass.getSemitones() + getMIDIoctave();
     }

    /**
     * Return the octave component.
     */

   public int getOctave()
     {
     return octave;
     }

    /**
     * Return the midi pitch number for this NoteSymbol
     */

   public int getMIDIoctave()
     {
     return CMIDI + 12*octave;
     }

    /**
     * Return the duration in slots for this NoteSymbol
     */

   public int getDuration()
     {
     return duration;
     }

    /**
     * Return the PitchClass for this NoteSymbol
     */

   public PitchClass getPitchClass()
     {
     return pitchClass;
     }

    /**
     * Return the PitchClass name for this NoteSymbol
     */

   public String getPitchString()
     {
     return pitchClass.toString();
     }

    /**
     * Return the leadsheet notation string for this NoteSymbol.
     */

   public String toString()
     {
     return stringRep;
     }

/**
 * Get duration of a list of NoteSymbols
 * except for trailing rests
 * returning an integer duration
 */

public static int getDuration(Polylist L)
  {
  Polylist R = L.reverse();

  // ignore any rests at end (beginning of reverse)

  while( R.nonEmpty() )
    {
    NoteSymbol noteSymbol = (NoteSymbol)R.first();
    if( !noteSymbol.isRest() )
      {
      break;
      }
    R = R.rest();
    }

  int totalDuration = 0;

  while( R.nonEmpty() )
    {
    assert(R.first() instanceof NoteSymbol);
    totalDuration += ((NoteSymbol)R.first()).getDuration();
    R = R.rest();
    }

  return totalDuration;
  }

    public static Polylist makePitchStringList(Polylist noteSymbolList) {
        Polylist stringList = Polylist.nil;
        while(noteSymbolList.nonEmpty()) {
            stringList = stringList.cons(((NoteSymbol)noteSymbolList.first()).toPitchString());
            noteSymbolList = noteSymbolList.rest();
        }
        return stringList.reverse();
    }

public static Polylist makeNoteSymbolList(Polylist stringList)
  {
  return makeNoteSymbolList(stringList, 0);
  }

/**
 * Make a NoteSymbol list from a list of Strings.
 * As of 6/11/09, a pair (String Probability) can also be used,
 * e.g. to represent the user-declared probability of a color tone.
 @param stringList
 @param rise
 @return
 */

public static Polylist makeNoteSymbolList(Polylist stringList, int rise)
  {
//System.out.print("makeNoteSymbolList " + stringList);
    Polylist R = Polylist.nil;
    while( stringList.nonEmpty() )
      {
        Object ob = stringList.first();
        if( ob instanceof Polylist )
          {
            Polylist L = (Polylist) ob;
            if( L.length() == 2 && L.first() instanceof String && L.second() instanceof Number )
              {
                String string = (String) L.first();
                Double prob = (Double) L.second();
                NoteSymbol ns = makeNoteSymbol(string).transpose(rise);
                ns.setProbability(prob);
                R = R.cons(ns);
              }
          }
        else
          {
            String string = (String) stringList.first();
            NoteSymbol ns = makeNoteSymbol(string).transpose(rise);
            R = R.cons(ns);
          }
        stringList = stringList.rest();
      }
//System.out.println(", rise = " + rise + " to " + R.reverse());
    return R.reverse();
  }

public static Polylist transposeNoteSymbolList(Polylist noteSymbolList, int rise)
  {
//System.out.print("transposeNoteSymbolList " + noteSymbolList);
  Polylist R = Polylist.nil;
  while( noteSymbolList.nonEmpty() )
    {
    NoteSymbol ns = (NoteSymbol)noteSymbolList.first();

    NoteSymbol noteSymbol = ns.transpose(rise);
    R = R.cons(noteSymbol);
    noteSymbolList = noteSymbolList.rest();
    }
//System.out.println(", rise = " + rise + " to " + R.reverse());
  return R.reverse();
  }

static Polylist noteSymbolListToStringList(Polylist noteSymbolList, int rise)
  {
  Polylist R = Polylist.nil;
  while( noteSymbolList.nonEmpty() )
    {
    NoteSymbol noteSymbol = (NoteSymbol)noteSymbolList.first();
    String string = noteSymbol.transpose(rise).toString();
    R = R.cons(string);
    noteSymbolList = noteSymbolList.rest();
    }
  return R.reverse();
  }

  /**
   * Determines if a string is a valid note in leadsheet notation.
   */

  public static boolean isValidNote(String string)
    {
    return makeNoteSymbol(string, 0) != null;
    }



private static Accidental S = Accidental.SHARP;
private static Accidental F = Accidental.FLAT;
private static Accidental N = Accidental.NATURAL;

public static Accidental accidentalByKey[][] = {
                                         { N, F,  N,  F,  F,  N, F,  N, F,  N, F, F}, /* gb */
                                         { N, F,  N,  F,  F,  N, F,  N, F,  N, F, F}, /* db */
                                         { N, F,  N,  F,  F,  N, F,  N, F,  N, F, F}, /* ab */
                                         { N, F,  N,  F,  F,  N, F,  N, F,  N, F, F}, /* eb */
                                         { N, F,  N,  F,  F,  N, F,  N, F,  N, F, F}, /* bb */
                                         { N, F,  N,  F,  N,  N, F,  N, F,  N, F, F}, /* f  */
                                         { N, F,  N,  F,  N,  N, F,  N, F,  N, F, N}, /* c  */
                                         { N, S,  N,  S,  N,  S, S,  N, S,  N, S, N}, /* g  */
                                         { S, S,  N,  S,  N,  S, S,  N, S,  N, S, N}, /* d  */
                                         { S, S,  N,  S,  N,  S, S,  N, S,  N, S, N}, /* a  */
                                         { S, S,  N,  S,  N,  S, S,  N, S,  N, S, N}, /* e  */
                                         { S, S,  N,  S,  N,  S, S,  N, S,  N, S, N}, /* b  */
                                         { S, S,  N,  S,  N,  S, S,  N, S,  N, S, N}, /* f# */
                                         { S, S,  N,  S,  N,  S, S,  N, S,  N, S, N}  /* c# */
                                         };



}
