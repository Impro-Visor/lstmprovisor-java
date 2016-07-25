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
import polya.Polylist.*;

/**
 * The Key class deals with keys, their creation from strings, transposition, etc.
 * Keys are immutable objects and will be created only at initialization.
 *
 * @see         Note
 * @see         Part
 * @author      Robert Keller
 */
public class Key
        implements Constants
  {
  /**
   * The key is uniquely represented by its index.
   */
  private int index;

  /**
   * Each key has a unique name as well.
   */
  private String name;

  /**
   * The position of c (or b#) in this key
   */
  private int cPosition;

  public static int default_numerator = 8;	// eighth note default

  public static int gbkey = 0,  dbkey = 1,  abkey = 2,  ebkey = 3,  bbkey = 4,  
                    fkey = 5,  ckey = 6,  gkey = 7,  dkey = 8,  akey = 9,  
                    ekey = 10,  bkey = 11,  fskey = 12,  cskey = 13;

  private static int CINDEX = 6;

  private static int CINDEX_OFFSET = 2;	// Difference between two tables

  /**
   * table of all Keys organized as a line of fifths
   */
  public static Key key[] = {new Key(0, "gb", 6),
                               new Key(1, "db", 11),
                               new Key(2, "ab", 4),
                               new Key(3, "eb", 9),
                               new Key(4, "bb", 2),
                               new Key(5, "f", 7),
                               new Key(6, "c", 0),
                               new Key(7, "g", 5),
                               new Key(8, "d", 10),
                               new Key(9, "a", 3),
                               new Key(10, "e", 8),
                               new Key(11, "b", 1),
                               new Key(12, "f#", 6),
                               new Key(13, "c#", 11)
  };

  public static Key Ckey = key[CINDEX];

  /**
   * names for PitchClass indices
   */
  static public final int fb = 0,  cb = 1,  gb = 2,  db = 3,  ab = 4,  eb = 5,  
          bb = 6,  f = 7,  c = 8,  g = 9,  d = 10,  a = 11,  e = 12,  b = 13,  
          fs = 14,  cs = 15,  gs = 16,  ds = 17,  as = 18,  es = 19,  bs = 20;


  /* scales corresponding to keys */
  public static int cycleIndices[][] = {
    {gb, db, ab, eb, bb, f, c, g, d, a, fb, cb},
    {db, ab, eb, bb, f, c, g, d, a, fb, cb, gb},
    {ab, eb, bb, f, c, g, d, a, fb, cb, gb, db},
    {eb, bb, f, c, g, d, a, fb, cb, gb, db, ab},
    {bb, f, c, g, d, a, fb, cb, gb, db, ab, eb},
    {f, c, g, d, a, e, cb, gb, db, ab, eb, bb},
    {c, g, d, a, e, b, gb, db, ab, eb, bb, f},
    {g, d, a, e, b, fs, cs, gs, ds, bb, es, c},
    {d, a, e, b, fs, cs, gs, ds, bb, es, bs, g},
    {a, e, b, fs, cs, gs, ds, as, es, bs, g, d},
    {e, b, fs, cs, gs, ds, as, es, bs, g, d, a},
    {b, fs, cs, gs, ds, as, es, bs, g, d, a, e},
    {fs, cs, gs, ds, as, es, bs, g, d, a, e, b},
    {cs, gs, ds, as, es, bs, g, d, a, e, b, fs}};

  public static int chromaticIndices[][] = {
    {c, db, d, eb, fb, f, gb, g, ab, a, bb, cb}, /* fb */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, cb}, /* cb */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, cb}, /* gb */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, cb}, /* db */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, b}, /* ab */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, b}, /* eb */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, b}, /* bb */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, b}, /* f  */
    {c, db, d, eb, e, f, gb, g, ab, a, bb, b}, /* c  */
    {c, db, d, eb, e, f, fs, g, ab, a, bb, b}, /* g  */
    {c, cs, d, eb, e, f, fs, g, ab, a, bb, b}, /* d  */
    {c, cs, d, ds, e, f, fs, g, gs, a, bb, b}, /* a  */
    {c, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* e  */
    {c, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* b  */
    {bs, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* f# */
    {bs, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* c# */
    {bs, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* g# */
    {bs, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* d# */
    {bs, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* a# */
    {bs, cs, d, ds, e, f, fs, g, gs, a, as, b}, /* e# */
    {bs, cs, d, ds, e, f, fs, g, gs, a, as, b} /* b# */};

  /**
   * table used to adjust input notes from staves, depending on key signature
   */
  public static int adjustPitchInKey[][] =
          {
    /*  c      d       e   f      g      a      b */
    {-1, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1}, /* gb -6 */
    {0, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1}, /* db -5 */
    {0, 0, -1, 0, -1, 0, 0, 0, 0, -1, 0, -1}, /* ab -4 */
    {0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, -1}, /* eb -3 */
    {0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1}, /* bb -2 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1}, /* f  -1 */
    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, /* c   0 */
    {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, /* g   1 */
    {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, /* d   2 */
    {1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0}, /* a   3 */
    {1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0}, /* e   4 */
    {1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0}, /* b   5 */
    {1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 0}, /* f#  6 */
    {1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1} /* c#  7*/ 
    /*  c      d       e   f      g      a      b */};

  /** transpositions of one key to another */
  public static int transpositions[][] =
          {
    {gbkey, gkey, abkey, akey, bbkey, bkey, ckey, dbkey, dkey, ebkey, ekey, fkey},
    {dbkey, dkey, ebkey, ekey, fkey, gbkey, gkey, abkey, akey, bbkey, bkey, ckey},
    {abkey, akey, bbkey, bkey, ckey, dbkey, dkey, ebkey, ekey, fkey, gbkey, gkey},
    {ebkey, ekey, fkey, gbkey, gkey, abkey, akey, bbkey, bkey, ckey, dbkey, dkey},
    {bbkey, bkey, ckey, dbkey, dkey, ebkey, ekey, fkey, gbkey, gkey, abkey, akey},
    {fkey, gbkey, gkey, abkey, akey, bbkey, bkey, ckey, dbkey, dkey, ebkey, ekey},
    {ckey, dbkey, dkey, ebkey, ekey, fkey, gbkey, gkey, abkey, akey, bbkey, bkey},
    {gkey, abkey, akey, bbkey, bkey, ckey, cskey, dkey, ebkey, ekey, fkey, fskey},
    {dkey, ebkey, ekey, fkey, fskey, gkey, abkey, akey, bbkey, bkey, ckey, cskey},
    {akey, bbkey, bkey, ckey, cskey, dkey, ebkey, ekey, fkey, fskey, gkey, abkey},
    {ekey, fkey, fskey, gkey, abkey, akey, bbkey, bkey, ckey, cskey, dkey, ebkey},
    {bkey, ckey, cskey, dkey, ebkey, ekey, fkey, fskey, gkey, abkey, akey, bbkey},
    {fskey, gkey, abkey, akey, bbkey, bkey, ckey, cskey, dkey, ebkey, ekey, fkey},
    {cskey, dkey, ebkey, ekey, fkey, fskey, gkey, abkey, akey, bbkey, bkey, ckey}};

  static public int OCTAVE = 12;

  static private int BOTTOM_KEY = 0;

  static private int TOP_KEY = 13;

  static private int KEY_SIZE = 14;

  static private int BOTTOM_PITCH = 0;

  static private int TOP_PITCH = 20;

  static private int PITCH_NAME_SIZE = 21;

  /**
   * the symbol used to designate a flat pitch
   */
  public static final char FLAT = 'b';

  /**
   * the symbol used to designate a sharp pitch
   */
  public static final char SHARP = '#';

  /**
   * the symbol used to designate an octave up in the lead sheet notation
   */
  public static final char PLUS = '+';

  /**
   * the character that represents the rest
   */
  public static final char RESTCHAR = 'r';

  /**
   * the symbol used to designate an octave down in the lead sheet notation
   */
  public static final char MINUS = '-';

  /**
   * the symbol used for slash chords in the lead sheet notation
   */
  public static final char SLASH = '/';

  /**
   * the symbol used to increase duration by one-half in the lead sheet notation
   */
  public static final char DOT = '.';

  /**
   * the difference between semitone indices in the pitches table
   */
  public static final int SEMITONEOFFSET = 7;

  /**
   * Construct the static keys.
   */
  private Key(int index, String name, int cPosition)
    {
    this.index = index;
    this.name = name;
    this.cPosition = cPosition;
    }

  /**
   * Get a key given its name
   */
  public static Key getKey(String name)
    {
    String lcName = name.toLowerCase();

    for( int i = BOTTOM_KEY; i <= TOP_KEY; i++ )
      {
      if( lcName.equals(key[i].name) )
        {
        return key[i];
        }
      }
    return null;	// no such key
    }

  /**
   * Get a key given its number of sharps (flats if negative);
   */
  public static Key getKey(int sharps)
    {
    int index = sharps + CINDEX;

    if( index < BOTTOM_KEY || index > TOP_KEY )
      {
      return null;	// no such key
      }

    return key[index];
    }

  /**
   * Render chromatic pitch as it would be in this key.
   */
  public PitchClass renderInKey(String name)
    {
    int position = PitchClass.getPitchClass(name).getIndex();

    if( position < 0 )
      {
      return null;
      }

    int pitchIndex = (key[index].cPosition + position) % OCTAVE;

    return PitchClass.getPitchClass(chromaticIndices[index][pitchIndex]);
    }

  /**
   * Defines how an offset of some number of semitones from the tonic of the key 
   * maps into pitch names.
   */
  public PitchClass rep(int offset)
    {
    if( offset < 0 )
      {
      offset += (-offset / OCTAVE + 1) * OCTAVE;
      }

    assert (offset >= 0);

    offset = offset % OCTAVE;

    return PitchClass.getPitchClass(chromaticIndices[index][offset]);
    }

  /**
   * Get the index of this key. (might not be needed)
   */
  public int getIndex()
    {
    return index;
    }

  /**
   * Transpose this key to another, by the given number of semitones.
   */
  public Key transpose(int semitones)
    {
    if( semitones < 0 )
      {
      semitones = OCTAVE - (-semitones) % OCTAVE;
      }

    assert (semitones >= 0);

    semitones = semitones % OCTAVE;

    int newIndex = transpositions[index][semitones];

    assert (newIndex >= 0 && newIndex < KEY_SIZE);

    return key[newIndex];
    }

  /**
   * Transpose a PitchClass in this key.
   */
  public static PitchClass transpose(PitchClass pc, int semitones)
    {
    return pc.transpose(semitones);
    }

  public static String getKeyName(int index)
    {
    assert (index >= BOTTOM_KEY);
    assert (index <= TOP_PITCH);

    return key[index].name;
    }

  public String toString()
    {
    return name;
    }

  /**
   * Get the delta in the line of fifths, corresponding to a key
   * represented by a number of sharps (or negative for flats)
   * and a number of semitones transposition.
   */
  public static int getKeyDelta(int sharps, int rise)
    {
    int newSharps = sharps + rise * SEMITONEOFFSET;

    while( newSharps < MIN_KEY )
      {
      newSharps += OCTAVE;
      }

    while( newSharps > MAX_KEY )
      {
      newSharps -= OCTAVE;
      }

    return newSharps;
    }
/*
  public static Polylist transposeChordList(Polylist chordSeq, int rise)
    {
    return transposeChordList(chordSeq, rise, Ckey);
    }


  public static Polylist transposeChordList(Polylist chordSeq, int rise,
                                              Key key)
    {
    if( rise == 0 || chordSeq.isEmpty() )
      {
      return chordSeq;
      }

    // make a new list of the transposed chords in the sequence
    Polylist newChords = Polylist.nil;
    while( chordSeq.nonEmpty() )
      {
      String item = (String)chordSeq.first();

      // For now, we are skipping bar-line info
      if( !Advisor.licksIgnore.member(item) )
        {
        newChords = newChords.cons(transposeChord(item, rise, key));
        }
      chordSeq = chordSeq.rest();
      }

    return newChords.reverse();
    }//*/

  public static String transposeChord(String chord, int rise, Key key)
    {
    if( rise == 0 || chord.equals(NOCHORD) )
      {
      return chord;
      }

    Polylist exploded = explodeChord(chord);

    String root = (String)exploded.first();
    String body = (String)exploded.second();
    String afterSlash = (String)exploded.third();

    PitchClass pc = PitchClass.getPitchClass(root);

    assert (pc != null);

    PitchClass newRoot = pc.transpose(rise);

    if( afterSlash.equals("") )
      {
      return newRoot.getChordBase() + body;
      }

    // Deal with slash-chord

    PitchClass bass = PitchClass.getPitchClass((String)exploded.fourth());
    ;
    assert (bass != null);

    String newBass = bass.transpose(rise).getChordBase();

    return newRoot.getChordBase() + body + "/" + newBass;
    }


// Change the root of the chord to C
  public static String makeCroot(String chord)
    {
    return makeRoot(CROOT, chord);
    }


// Change the root of the chord to specified note
  public static String makeRoot(String root, String chord)
    {
    Polylist exploded = explodeChord(chord);

    String body = (String)exploded.second();
    String afterSlash = (String)exploded.third();

    if( afterSlash.equals("") )
      {
      return root + body;
      }

    String origRoot = (String)exploded.first();

    int rise = PitchClass.findRise(root.toLowerCase(), origRoot);

    // Deal with slash-chord

    PitchClass bass = PitchClass.getPitchClass((String)exploded.fourth());
    assert (bass != null);

    String newBass = bass.transpose(rise).getChordBase();

    return root + body + "/" + newBass;
    }

  static String getRoot(String chord)
    {
    return (String)explodeChord(chord).first();
    }

  static boolean sameRoot(String chord1, String chord2)
    {
    return getRoot(chord1).equals(getRoot(chord2));
    }

  static boolean isValidStem(String stem)
    {
    switch( stem.charAt(0) )
      {
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
      case 'G':
        return true;
      default:
        return false;
      }
    }

  static boolean hasValidStem(String chord)
    {
    return explodeChord(chord) != null;
    }

  /**
   * Explode a chord from the leadsheet notation into four parts:
   * the root, the type of chord, the string after a slash, if any,
   * and the bass note.
   * If there is no slash, the third component is the null string, and
   * the bass note is the same as the root.
   *
   *
   * If the chord doen't make sense, then null is returned.
   *
   * @param chord the string naming the chord.
   */
  static Polylist explodeChord(String chord)
    {
    if( chord.equals("") )
      {
      return null;	// Error indicator
      }

    StringBuffer buffer1 = new StringBuffer();

    buffer1.append(chord.charAt(0));

    if( !isValidStem(buffer1.toString()) )
      {
      return null;
      }

    int len = chord.length();

    int index = 1;

    if( index < len )
      {
      char c = chord.charAt(1);
      if( c == SHARP || c == FLAT )
        {
        buffer1.append(c);
        index++;
        }
      }

    String root = buffer1.toString();

    if( !PitchClass.isValidPitch(root.toLowerCase()) )
      {
      return null;	// root is not known
      }

    // Get the type of the chord.

    StringBuffer buffer2 = new StringBuffer();

    while( index < len && chord.charAt(index) != SLASH )
      {
      buffer2.append(chord.charAt(index));
      index++;
      }

    String body = buffer2.toString();

    String bass = root.toLowerCase();	// default
    String afterSlash = "";		// default

    StringBuffer buffer3 = new StringBuffer();

    if( index < len )
      {
      // We have a slash chord.
      index++;	// skip the slash

      while( index < len )
        {
        buffer3.append(chord.charAt(index));
        index++;
        }

      afterSlash = buffer3.toString();

      if( !PitchClass.isValidPitch(afterSlash.toLowerCase()) )
        {
        return null;
        }
      bass = afterSlash.toLowerCase();
      }

    return Polylist.list(root, body, afterSlash, bass);
    }

  /**
   * Look up a string in a table.
   * Eventually uses of this could be replaced with a map of some kind.
   */
  static int lookup(String arg, String[] table)
    {
    for( int i = 0; i < table.length; i++ )
      {
      if( table[i].equals(arg) )
        {
        return i;
        }
      }
    return -1;	// Not Found
    }


/**
 * Return list of any invalid notes in the argument list.
@param L
@return
 */

public static Polylist invalidNotes(Polylist L)
  {
    if( L.isEmpty() )
      {
        return Polylist.nil;
      }

    Object first = L.first();
    
    if( first instanceof String && NoteSymbol.isValidNote((String)first) )
      {
        return invalidNotes(L.rest());
      }
    else if( first instanceof Polylist
        && ((Polylist)first).length() == 2
        && ((Polylist)first).first() instanceof String
        && NoteSymbol.isValidNote((String)((Polylist)first).first())
        && ((Polylist)first).second() instanceof Number )
      {
        return invalidNotes(L.rest());
      }
    else
    {
       return invalidNotes(L.rest()).cons(first);

    }
  }

  /**
   * Get midi pitch from a leadsheet melody note.
   * Duration is ignored.  -1 is returned if the note is a rest.
   */
  public static int pitchFromLeadsheet(String string)
    {
    return pitchFromLeadsheet(string, 0);
    }

  public static int pitchFromLeadsheet(String string, int rise)
    {
    Note note = noteFromLeadsheet(string, rise, BEAT);
    if( note == null )
      {
      return -1;
      }
    return note.getPitch();
    }

  /**
   * Transform a leadsheet melody note or rest given as a string to 
   * a Note, e.g. for insertion into a Score.  Note that octave
   * shifts and durations are accepted in leadsheet melody notation.
   */
  public static Note noteFromLeadsheet(String string, int rise,
                                         int slotsPerBeat)
    {
    return noteFromLeadsheet(string, rise, BEAT, Ckey);
    }

  public static Note noteFromLeadsheet(String string, int rise,
                                         int slotsPerBeat, Key key)
    {
    return NoteSymbol.makeNoteSymbol(string, rise).toNote();
    }


  /* This overlaps noteFromLeadsheet and should be refactored. */
  public static int durationFromLeadsheet(String string)
    {
    int len = string.length();

    if( len == 0 )
      {
      return 0;
      }

    char c = string.charAt(0);

    if( c == RESTCHAR )
      {
      int duration = getDuration(string.substring(1));
      return duration;
      }

    if( !PitchClass.isValidPitchStart(c) )
      {
      return 0;
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

    // Check for any octave shifts specified in the notation

    boolean more = true;
    while( index < len && more )
      {
      switch( string.charAt(index) )
        {
        case PLUS:
          index++;
          break;

        case MINUS:
          index++;
          break;

        default:
          more = false;
        }
      }

    return getDuration(string.substring(index));
    }

  /**
   * Use this to get duration from duration string such as
   * "4+8" as in leadsheet notation, except that a null 
   * string returns 0 rather than the default value.
   */

  public static int getDuration0(String item)
  {
    if( item.trim().equals("") )
      {
      return 0;
      }
    else 
      {
      return getDuration(item);
      }
  }
  
  /**
   * This method provides part of the functionality of noteFromLeadhsheet,
   * namely getting the duration part of a note or rest.
   */
  public static int getDuration(String item)
    {
    int len = item.length();
    int index = 0;
    if( len == 0 ||
            !Character.isDigit(item.charAt(index)) )
      {
      return DEFAULT_DURATION;
      }

    int duration = 0;
    boolean firsttime = true;

    // Example of input is 2.+8/3+32 meaning the value of a dotted halfnote
    // eighth note triplet, and 32nd note.
    // Note that there is no + to start with.

    while( index < len && ((item.charAt(index) == PLUS) || firsttime) )
      {
      int numerator = 0;
      int denominator = 1;
      int this_duration;

      if( firsttime )
        {
        firsttime = false; // no leading +
        }
      else
        {
        index++;		  // skip infix +'s
        }

      boolean hasDigit = false;

      // Accumulate digits part
      StringBuffer dur = new StringBuffer();
      while( index < len && Character.isDigit(item.charAt(index)) )
        {
        hasDigit = true;
        dur.append(item.charAt(index));
        index++;
        }

      if( hasDigit )
        {
        numerator = new Integer(dur.toString()).intValue();
        }
      else
        {
        numerator = default_numerator;
        }

      int slots = WHOLE;  // 1 whole note = 4 quarter  notes

      // Check for tuplet
      if( index < len && item.charAt(index) == SLASH )
        {
        index++;
        if( index >= len || !Character.isDigit(item.charAt(index)) )
          {
          return DEFAULT_DURATION;
          }

        StringBuffer tuplet = new StringBuffer();
        while( index < len && Character.isDigit(item.charAt(index)) )
          {
          tuplet.append((Character)item.charAt(index));
          index++;
          }

        denominator = new Integer(tuplet.toString()).intValue();
        }

      if( denominator > 1 )
        {
        slots *= (denominator - 1); // was 2
        }

      /* suppress warning -- RK
      if( slots % (numerator*denominator) != 0 )
      {
      ErrorLog.log(ErrorLog.WARNING, "Tuplet value is not exact: " + item
      + ", doing the best we can");
      }
       */

      this_duration = slots / (numerator * denominator);

      // Handle dotted notes, which add to individual duration.

      while( index < len && item.charAt(index) == DOT )
        {
        this_duration = (3 * this_duration) / 2;
        index++;
        }

      duration += this_duration;
      }

    if( index < len )
      {
      //ErrorLog.log(ErrorLog.WARNING,
      //        "Ignoring garbage after end of note duration: " + item);
      }

    if( duration <= 0 )
      {
      duration = DEFAULT_DURATION;
      }

    return duration;
    }

  /**
   * Return a profile of a list of note Strings.
   */
  public static String profileNoteStringList(Polylist L,
                                               boolean includeTrailer)
    {
    char UP = '/';
    char DOWN = '\\';
    char NEUTRAL = ' ';
    String LEADER = " ";
    String GAP = " ";
    String TRAILER = "-note:";

    Polylist R = Polylist.nil;
    StringBuffer buffer = new StringBuffer();
    int noteCount = 0;

    int previousPitch = -1;
    char previousSymbol = NEUTRAL;

    buffer.append(LEADER);

    // get the first pitch in the list
    while( L.nonEmpty() )
      {
      Object ob = L.first();
      if( ob instanceof NoteSymbol || ob instanceof String )
        {
        noteCount++;
        int pitch =
                (ob instanceof String) ? pitchFromLeadsheet((String)ob) : ((NoteSymbol)ob).getMIDI();
        noteCount++;
        if( pitch >= 0 )
          {
          previousPitch = pitch;
          L = L.rest();
          break;
          }
        }
      L = L.rest();
      }

    while( L.nonEmpty() )
      {
      Object ob = L.first();
      if( ob instanceof String || ob instanceof NoteSymbol )
        {
        noteCount++;
        int pitch =
                (ob instanceof String) ? pitchFromLeadsheet((String)ob) : ((NoteSymbol)ob).getMIDI();
        if( pitch >= 0 )
          {
          if( pitch > previousPitch )
            {
            if( previousSymbol != UP )
              {
              buffer.append(UP);
              previousSymbol = UP;
              }
            }
          else if( pitch < previousPitch )
            {
            if( previousSymbol != DOWN )
              {
              buffer.append(DOWN);
              previousSymbol = DOWN;
              }
            }
          previousPitch = pitch;
          }
        }
      L = L.rest();
      }

    if( includeTrailer )
      {
      buffer.append(GAP + noteCount + TRAILER);
      }

    return buffer.toString();
    }

  /**
   * Transpose list of note Strings in leadsheet form by a certain rise
   * returning a list of note Strings.
   */
  public static Polylist transposeNoteStringList(Polylist L,
                                                   int rise,
                                                   Key key)
    {
    Polylist R = Polylist.nil;

    while( L.nonEmpty() )
      {
      if( !(L.first() instanceof String) )
        {
        //ErrorLog.log(ErrorLog.WARNING,
        //        "Unexpected item " + L.first() + " preceding: " + L.rest());
        }
      else
        {
        R = R.cons(transposeNoteString((String)L.first(), rise, key));
        }
      L = L.rest();
      }

    return R.reverse();
    }

  /**
   * Get duration of a list of note strings.  Count rests,
   * except for trailing rests
   * returning an integer duration
   */
  public static int getDuration(Polylist L)
    {
    Polylist R = L.reverse();

    // ignore any rests at end (beginning of reverse)

    while( R.nonEmpty() )
      {
      String note = (String)R.first();
      if( !isRest(note) )
        {
        break;
        }
      R = R.rest();
      }

    int totalDuration = 0;

    while( R.nonEmpty() )
      {
      totalDuration += getDuration((String)R.first());
      R = R.rest();
      }

    return totalDuration;
    }

  static boolean isRest(String noteString)
    {
    assert (noteString != "");

    char c = noteString.charAt(0);

    assert (Character.isLowerCase(c));

    if( c == RESTCHAR )
      {
      return true;
      }

    return false;
    }

  /**
   * Transpose a note String in leadsheet form by a certain rise
   * returning a String.  Returns null if the note String is not well-formed.
   */
  public static String transposeNoteString(String noteString, int rise,
                                             Key key)
    {
    assert (noteString != "");

    char c = noteString.charAt(0);

    assert (Character.isLowerCase(c));

    if( c == RESTCHAR )
      {
      return noteString;		// rests are unchanged
      }

    Polylist item = Polylist.explode(noteString).rest();

    boolean natural = true;
    boolean sharp = false;

    StringBuffer noteBase = new StringBuffer();
    noteBase.append(c);

    if( item.nonEmpty() )
      {
      Character second = (Character)item.first();
      if( second.equals(SHARP) || second.equals(FLAT) )
        {
        item = item.rest();
        noteBase.append(second);
        }
      }

    PitchClass pc = PitchClass.getPitchClass(noteBase.toString());

    PitchClass newPC = pc.transpose(rise);

    // Get octave info from original note.

    int octavesUp = 0;

    while( item.nonEmpty() )
      {
      Character x = (Character)item.first();
      if( x.equals(PLUS) )
        {
        octavesUp++;
        item = item.rest();
        }
      else if( x.equals(MINUS) )
        {
        octavesUp--;
        item = item.rest();
        }
      else
        {
        break;
        }
      }

    String duration = item.implode();

    // Adjust for the possibility that transposition will change octaves.

    if( (rise > 0) && (pc.getSemitones() > newPC.getSemitones()) )
      {
      octavesUp++;
      }
    else if( (rise < 0) && (pc.getSemitones() < newPC.getSemitones()) )
      {
      octavesUp--;
      }

    // Creat the new octave string.

    StringBuffer octaveString = new StringBuffer();
    while( octavesUp > 0 )
      {
      octaveString.append(PLUS);
      octavesUp--;
      }
    while( octavesUp < 0 )
      {
      octaveString.append(MINUS);
      octavesUp++;
      }

    String result = newPC.toString() + octaveString.toString() + duration;

    return result;
    }

  /**
   * Transpose list of note Strings in leadsheet form by a certain rise
   * returning a list of numbers representing the notes independent of key.
   */
  public static Polylist transposeNoteStringListToNumbers(Polylist L,
                                                            int rise,
                                                            Key key)
    {
    Polylist R = Polylist.nil;

    while( L.nonEmpty() )
      {
      R = R.cons(transposeNoteStringToNumbers((String)L.first(), rise, key));
      L = L.rest();
      }

    return R.reverse();
    }

  /**
   * Transpose a note String in leadsheet form by a certain rise
   * returning a String representing a number, such as "3", "b5", "#2", etc.
   */
  public static String transposeNoteStringToNumbers(String noteString,
                                                      int rise,
                                                      Key key)
    {
    assert (noteString != "");

    char c = noteString.charAt(0);

    assert (Character.isLowerCase(c));

    if( c == RESTCHAR )
      {
      return noteString;		// rests are unchanged
      }

    Polylist item = Polylist.explode(noteString).rest();

    StringBuffer noteBase = new StringBuffer();
    noteBase.append(c);

    if( item.nonEmpty() )
      {
      Character second = (Character)item.first();
      if( second.equals(SHARP) || second.equals(FLAT) )
        {
        item = item.rest();
        noteBase.append(second);
        }
      }

    PitchClass pc = PitchClass.getPitchClass(noteBase.toString());

    PitchClass newPC = pc.transpose(rise);

    String newNote = PitchClass.numbers[newPC.getIndex()];

    // Get octave info from original note.

    int octavesUp = 0;

    while( item.nonEmpty() )
      {
      Character x = (Character)item.first();
      if( x.equals(PLUS) )
        {
        octavesUp++;
        item = item.rest();
        }
      else if( x.equals(MINUS) )
        {
        octavesUp--;
        item = item.rest();
        }
      else
        {
        break;
        }
      }

    // Creat the new octave string.

    StringBuffer octaveString = new StringBuffer();
    while( octavesUp > 0 )
      {
      octaveString.append(PLUS);
      octavesUp--;
      }
    while( octavesUp < 0 )
      {
      octaveString.append(MINUS);
      octavesUp++;
      }

    // Leave off duration for now, to keep it from being too busy.

    return newNote + octaveString.toString();
    }


  /*
   * transposeOne transposes one pitch class
   */
  public static String transposeOne(String from, String to, String p, Key key)
    {
    if( p.charAt(0) == 'r' )
      {
      return p; // rest
      }

    int rise = PitchClass.findRise(from, to);

    PitchClass pc = PitchClass.getPitchClass(p);

    PitchClass newPC = pc.transpose(rise);

    return newPC.toString();
    }


  /*
   * transposeKey transposes a key by the difference between to and from pitches
   */
  public Key transposeKey(String from, String to)
    {
    int rise = PitchClass.findRise(from, to);

    return transpose(rise);
    }


  /*
   * transposeList is a list version of transposePitch.
   * It transposes a whole list of pitches by the interval between
   * the from and to strings.
   */
  public static Polylist transposeList(String from, String to, Polylist L,
                                         Key key)
    {
    if( L.isEmpty() )
      {
      return Polylist.nil;
      }

    from = from.toLowerCase();
    to = to.toLowerCase();

    if( from == to )
      {
      return L;	// No actual transposition needed.
      }

    PitchClass fromPC = PitchClass.getPitchClass(from);
    assert (fromPC != null);

    PitchClass toPC = PitchClass.getPitchClass(to);
    assert (toPC != null);

    int rise = toPC.getSemitones() - fromPC.getSemitones();

    Polylist R = Polylist.nil;
    while( L.nonEmpty() )
      {
      assert (L.first() instanceof String);
      String p = (String)L.first();

      if( p.charAt(0) == 'r' )
        {
        R = R.cons(p);
        }
      else
        {
        PitchClass pc = PitchClass.getPitchClass(p);

        PitchClass newPC = pc.transpose(rise);

        R = R.cons(newPC.toString());
        }
      L = L.rest();
      }

    return R.reverse();
    }

  /**
   * enharmonic determines whether the pitches represented by
   * two strings representing pitch are enharmonically equivalent
   */
  public static boolean enharmonic(String x, String y)
    {
    return PitchClass.findRise(x, y) == 0;
    }

  /**
   * enMember determines whether the first pitch is enharmonically
   * equivalent to some member of a list
   */
  public static boolean enhMember(String x, Polylist L)
    {
    while( L.nonEmpty() )
      {
      if( enharmonic(x, (String)L.first()) )
        {
        return true;
        }
      L = L.rest();
      }
    return false;
    }

  /**
   * Auxiliary unit test method for Key.
   */
  static boolean test(String name)
    {
    Key key = getKey(name);
    assert (key != null);
    if( key.toString().equals(name.toLowerCase()) )
      {
      System.out.println("Key test passed for " + name);
      return true;
      }
    else
      {
      System.out.println("Key test failed for " + name);
      return false;
      }
    }

  /**
   * Make a note from a pitch class name specified as one of the Strings
   * in the pitches table.  A pitch class represents many
   * individual pitches.  The specific pitch is found by
   * using the midiBase argument as C that beings the octave in
   * which the desired note occurs.
   *
   * If there is a problem with the String, null is returned.
   */
  public static Note makeNote(String pitchClassName, int midiBase,
                                int duration)
    {
    return makeNoteAbove(pitchClassName, midiBase, 0, duration);
    }

  /**
   * Make a note from a pitch class specified as one of the Strings
   * in the pitches table, above a minimum MIDI value.
   *  A pitch class represents many
   * individual pitches.  The specific pitch is found by
   * using the midiBase argument as C that beings the octave in
   * which the desired note occurs.  However, this octave can be
   * modified by specifying a non-zero minimum.  This is used in
   * arpeggios, for example, to guarantee the notes keep ascending,
   * by specifying the previous midi value.
   *
   * If there is a problem with the String, null is returned.
   */
  public static Note makeNoteAbove(String pitchClassName, int midiBase,
                                     int minimum, int duration)
    {
    PitchClass pc = PitchClass.getPitchClass(pitchClassName);

    int midi = midiBase + pc.getSemitones();

    while( midi < minimum )
      {
      midi += OCTAVE;
      }

    boolean natural = pc.getNatural();

    boolean sharp = !natural && pc.getSharp();

    return new Note(midi, natural, sharp, duration);
    }

  /**
   * Unit test method for Key.
   */
  public static void main(String arg[])
    {
    if( test("gb") && test("db") && test("ab") && test("eb") && test("bb") && test("f") && test("c") && test("g") && test("d") && test("a") && test("e") && test("b") && test("f#") && test("c#") && test("Gb") && test("Db") && test("Ab") && test("Eb") && test("Bb") && test("F") && test("C") && test("G") && test("D") && test("A") && test("E") && test("B") && test("F#") && test("C#") )
      {
      System.out.println("All tests passed.");
      }
    else
      {
      System.out.println("Some test failed.");
      }

    System.out.println("Major scales:");

    for( int i = 0; i < 14; i++ )
      {
      System.out.print("key " + key[i].toString() + ": ");
      System.out.print(key[i].rep(0) + " ");
      System.out.print(key[i].rep(2) + " ");
      System.out.print(key[i].rep(4) + " ");
      System.out.print(key[i].rep(5) + " ");
      System.out.print(key[i].rep(7) + " ");
      System.out.print(key[i].rep(9) + " ");
      System.out.print(key[i].rep(11) + " ");
      System.out.println();
      System.out.println();
      }

    System.out.println("Chromatic scales:");

    for( int i = 0; i < 14; i++ )
      {
      System.out.print("key " + key[i].toString() + ": ");
      for( int j = -12; j <= 12; j++ )
        {
        System.out.print(key[i].rep(j) + " ");
        }
      System.out.println();
      System.out.println();
      }

    for( int i = 0; i < 14; i++ )
      {
      for( int j = 0; j <= 12; j++ )
        {
        System.out.println("key " + key[i].toString() + " transposed " + j + " = " + key[i].transpose(j));
        }
      System.out.println();
      }

    // render each pitch in each key

    for( int i = 0; i < 14; i++ )
      {
      System.out.print("key " + key[i].toString() + ": ");
      for( int j = BOTTOM_PITCH; j <= TOP_PITCH; j++ )
        {
        System.out.print(key[i].renderInKey(PitchClass.getPitchClass(j).toString()) + " ");
        }
      System.out.println();
      }
    }

  }

