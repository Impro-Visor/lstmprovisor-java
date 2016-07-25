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


package encoding;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Class BidirectionalHashMap is simply a two way generic HashMap.
 * Code was created by re-implementing Peter Swire's BidirectionalHashMap class as a generic collection
 * @author Nicholas Weintraut
 */

public class BidirectionalHashMap <K, V> {
    private HashMap<K, V> valueMap;
    private HashMap<V, K> keyMap;


    // constructor
    public BidirectionalHashMap() {
        this.valueMap = new HashMap<>();
        this.keyMap = new HashMap<>();
    }

    public void putKeyToValueOnly(K key, V value)
    {
        valueMap.put(key, value);
    }
    
    public void put(K key, V value){
        valueMap.put(key, value);
        keyMap.put(value, key);
    }
    
    public Set<Entry<K, V>> entrySet()
    {
        return valueMap.entrySet();
    }
    
    public K getKey(V value) {
        return keyMap.get(value);
    }
    
    public V getValue(K key) {
        return valueMap.get(key);
    }
}
