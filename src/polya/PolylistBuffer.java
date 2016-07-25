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

/*
 * Provides a convenient buffer for gestating Polylists.
 * PolylistBuffer is to Polylist as StringBuffer is to String.
 * Objects are added front to back to a Polylist.
 * 
 * PolylistBuffer differs from cons'ing in that the former is
 * a mutable object to which Objects are added, whereas cons'ing
 * is a method that returns a new Polylist from an existing one.
 */

package polya;

/**
 *
 * @author keller
 */

public class PolylistBuffer
{
Polylist contents = Polylist.nil;


public PolylistBuffer()
  {
  }

/** 
 * Append an Object to the Polylist.
 */

public void append(Object ob)
  {
  contents = contents.cons(ob);
  }


/** 
 * Return the Polylist constructed thus far.
 */

public Polylist toPolylist()
  {
  return contents.reverse();
  }


}
