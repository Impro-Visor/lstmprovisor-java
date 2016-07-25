/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoding;

// groups are used for bundling start and end indices
import java.io.Serializable;
import java.util.HashMap;

public class Group implements Serializable {

    public final int startIndex;    // inclusive
    public final int endIndex;      // exclusive
    public final boolean oneHot;
    public HashMap<Integer, Group[]> offSwitches; //a group can specify indexes, that if 1, turn off all of another group's indexes

    public Group(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.oneHot = true;
    }

    public Group(int startindex, int endIndex, boolean oneHot) {
        this.startIndex = startindex;
        this.endIndex = endIndex;
        this.oneHot = oneHot;
    }

    public boolean isOneHot() {
        return oneHot;
    }

    public int length() {
        return endIndex - startIndex;
    }

    @Override
    public String toString() {
        return "start index: " + startIndex + " end index: " + endIndex;
    }
}
