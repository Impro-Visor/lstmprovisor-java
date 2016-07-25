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

/**
 * Unit can be thought of as anything that has a rhythm value, whether it
 * be a chord, a note, or a rest.
 * Since the code in Part for calculating rhythm value is not trivial, it makes
 * sense to share the code for everything that is put in a slot and has a 
 * rhythm value.  Notes and chords store pitch(es) differently,
 * play back differently, and are drawn differently, so this interface
 * is very useful.
 */

public interface Unit {

    /**
     * the default rhythm value for a Unit
     */
    public static final int DEFAULT_RHYTHM_VALUE = Constants.BEAT;

    /**
     * the default dynamic volume for a Unit
     */
    public static final int DEFAULT_DYNAMIC = 85;

    /**
     * Sets the Unit's rhythm value.
     * @param rhythmValue       an integer representing the Unit's rhythm value
     */
    public void setRhythmValue(int rhythmValue);

    /**
     * Returns the Unit's rhythm value.
     * @return int      an integer representing the Unit's rhythm value
     */
    public int getRhythmValue();
    
    public boolean toggleEnharmonic();

    /**
     * Returns a copy of the Unit.
     * @return Unit     a copy of the Unit
     */
    public Unit copy();

    /**
     * Returns a String representation of the Unit
     * @return String   a String representation of the Unit
     */
    public String toString();

    public String toLeadsheet();
      
}
