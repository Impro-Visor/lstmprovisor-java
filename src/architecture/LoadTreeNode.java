/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import java.util.ArrayList;
import java.util.List;
import mikera.arrayz.INDArray;

/**
 * Class load tree specifies a tree-structure for loading and checking the load status of a network. 
 * Nodes contain a loadString, whether or not the network piece they correspond to was successful in loading,
 * Possibly a reference to an INDArray to load (this field mostly only set in leaf nodes),
 * And an array of all child nodes.
 * @author Nicholas Weintraut
 */
public class LoadTreeNode {
    private LoadTreeNode[] children;
    private String loadString;
    private boolean successful;
    private INDArray dataPointer;
    private Loadable networkPiece;

    /**
     * Constructor which initializes a node with the given load string and data pointer. Useful for leaf nodes.
     * @param loadString
     * @param dataPointer 
     */
    public LoadTreeNode(String loadString, INDArray dataPointer){
        setLoadString(loadString);
        this.children = null;
        this.successful = false;
        this.dataPointer = dataPointer;
        this.networkPiece = null;
    }
    
    /**
     * Constructor which initializes this node's children with the given array and assigns the children load strings. Useful for loadables which contain no data themselves, but have child loadables
     * @param loadStrings The load strings to assign to the children. Must be the same length as children
     * @param children The child nodes. Must be the same length as load strings.
     */
    public LoadTreeNode(String[] loadStrings, LoadTreeNode[] children) {
        if(children == null)
            throw new RuntimeException("The array of child nodes you passed is null! Use the constructor LoadTreeNode(String... loadStrings) instead if this was intentional.");
        else if(loadStrings == null) {
            throw new RuntimeException("The array of load strings you passed is null!");
        }
        else if(loadStrings.length != children.length) {
            throw new RuntimeException("Your array of child nodes and array of load strings are of different lengths! " + loadStrings.length + " load strings and " + children.length + "child nodes.");
        }
        for(int i = 0; i <  children.length; i++) {
            children[i].setLoadString(loadStrings[i]);
        }
        this.children = children;
        this.loadString = null;
        this.successful = false;
        this.dataPointer = null;
    }
    
    /**
     * Constructor which initializes this node's children as leaf nodes that contain data pointers
     * @param loadStrings The load strings to assign to the leaf nodes
     * @param dataPointers The data references for the leaf nodes to point to
     */
    public LoadTreeNode(String[] loadStrings, INDArray[] dataPointers) {
        if(loadStrings == null)
            throw new RuntimeException("The array of load Strings you passed is null!");
        else if(dataPointers == null) {
            throw new RuntimeException("The array of INDArray references you passed is null!");
        }
        else if(loadStrings.length != dataPointers.length) {
            throw new RuntimeException("Your array of load strings and array of data pointers are of different lengths! " + loadStrings.length + " load strings and " + dataPointers.length + "data pointers.");
        }
        
        for(int i = 0; i <  children.length; i++) {
            children[i].setLoadString(loadStrings[i]);
            children[i].setDataPointer(dataPointers[i]);
        }
        
        this.children = new LoadTreeNode[loadStrings.length];
        for(int i = 0; i < 0; i++) {
            this.children[i] = new LoadTreeNode(loadStrings[i], dataPointers[i]);
        }
        this.loadString = null;
        this.successful = false;
    }
    
    /**
     * @return a list of all required path strings which have not been loaded.
     */
    public List<String> getUnsuccessfullPaths(){
        List<String> unsuccessfulPaths = new ArrayList<>();
        if(children != null) {
            //iterate through all child nodes and add unsuccessful sub-paths to our list
            for(LoadTreeNode child : children) {
                List<String> childUnsuccessfulPaths = child.getUnsuccessfullPaths();
                for(String path : childUnsuccessfulPaths) {
                    //add this node's load string to the front of each sub-path
                    path = this.getLoadString() + path;
                }
                unsuccessfulPaths.addAll(childUnsuccessfulPaths);
            }
        }
        //base case: if this node has a data pointer and has not yet been marked successful, then return its load string as an unsuccessful path
        if(dataPointer != null && !successful)
            unsuccessfulPaths.add(loadString);
        return unsuccessfulPaths;
    }
    
    
    
    /**
     * Sets the INDArray that this node will mutate on a call to load()
     * @param dataPointer The INDArray object this node should load with data
     */
    public void setDataPointer(INDArray dataPointer){
        this.dataPointer = dataPointer;
    }
    
    public void setNetworkPiece(Loadable networkPiece) {
        this.networkPiece = networkPiece;
    }
    
    public Loadable getNetworkPiece() {
        return networkPiece;
    }
    
    public INDArray getDataPointer() {
        return dataPointer;
    }
    
    public void setSuccessful(boolean successful){
        this.successful = successful;
    }
    
    public void setLoadString(String loadString) {
        this.loadString = loadString;
    }
    
    public String getLoadString(){
        return loadString;
    }
    
    public LoadTreeNode[] getChildren(){
        return children;
    }
}
