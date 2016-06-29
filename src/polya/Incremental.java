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

package polya;

public class Incremental extends Polylist
  {
  Object value;

  public Object first()
    {
    ensureGrown();
    return ((Polylist)value).first();
    }

  public Polylist rest()
    {
    ensureGrown();
    return ((Polylist)value).rest();
    }

  public boolean isEmpty()
    {
    ensureGrown();
    return ((Polylist)value).isEmpty();
    }

  public boolean nonEmpty()
    {
    ensureGrown();
    return ((Polylist)value).nonEmpty();
    }

  public String toString()
    {
    if( value instanceof Growable )
      return "...";
    else
      return ((Polylist)value).toString();
    }

  public Incremental(Growable growable)
    {
    value = growable;
    }

  public void ensureGrown()
    {
    while( value instanceof Growable )
      {
      value = ((Growable)value).grow();
      }
    }

  // use with caution!

  public boolean grown()
    {
    return !(value instanceof Growable);
    }

  public Polylist getList()
    {
    ensureGrown();
    return (Polylist)value;
    }
  }
