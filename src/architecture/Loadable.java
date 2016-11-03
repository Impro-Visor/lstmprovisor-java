/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import java.util.LinkedList;
import mikera.arrayz.INDArray;

/**
 *
 * @author cssummer16
 */
public interface Loadable {

    public static String SEPARATOR = "_";

    public LoadTreeNode constructLoadTree();

    public LoadTreeNode getCurrentLoadTree();

    public void assignToNode(LoadTreeNode node);

    public default void postLoad() {
        if(getCurrentLoadTree().getChildren() != null) {
            LinkedList<LoadTreeNode> nodeQueue = new LinkedList<LoadTreeNode>();
            for(LoadTreeNode child : getCurrentLoadTree().getChildren()){
                nodeQueue.offer(child);
            }
            while (!nodeQueue.isEmpty()) {
                LoadTreeNode curr = nodeQueue.poll();
                if (curr.getNetworkPiece() != null) {
                    curr.getNetworkPiece().postLoad();
                }
                LoadTreeNode[] children = curr.getChildren();
                if (children != null) {
                    for (LoadTreeNode child : children) {
                        nodeQueue.offer(child);
                    }
                }
            }
        }

    }
    
    public default String pathCar(String loadPath) {
        return loadPath.replaceFirst(SEPARATOR + ".*", "");
    }

    public default void authenticatePathFormatting(String loadPath) {
        if (loadPath.contains(SEPARATOR + SEPARATOR) || loadPath.indexOf(SEPARATOR) == 0) {
            throw new RuntimeException("The load path " + loadPath + " was formatted incorrectly!");
        }
    }

    public default boolean load(INDArray data, String loadPath) {
        //this method will throw a RuntimeExceeption if the loadPath is formatted incorrectly (with two contiguous sepparator characters OR starting with a separator character)
        authenticatePathFormatting(loadPath);

        /*we are going to keep going down the chain of child nodes until one of the following is true:
            1. we reached the end of the path
            2. a path element doesn't exist
            3. we reached a new loadable (we'll call load() on it and return the result)
         */
        LoadTreeNode currNode = getCurrentLoadTree();
        boolean foundLoadable = false;
        String searchPath = loadPath;
        while (searchPath.length() > 0) {
            /*  If our load path isn't empty yet, we assume that we are going to try to load one of the children. 
                If no child whose load string matches the next element of the path is found, then this node either has a singular data pointer (return true), or the path has hanging elements
             */
            
            LoadTreeNode[] currChildren = currNode.getChildren();
            if (currChildren == null) {
               
                if (pathCdr(searchPath).length() == 0 && currNode.getLoadString().equals(pathCar(searchPath))) {
                    if (currNode.getDataPointer() != null) {
                        /* If this case triggers, then that means:
                            -This is a loadable network piece whose node possesses a data pointer in addition to child nodes
                            -A load path was supplied which points only to this loadable, and no further. */
                        currNode.setData(data);
                        currNode.setSuccessful(true);
                        
                        return true;
                    } else {
                        //this is reached iff the node the path is pointing to doesn't possess a data pointer to load to
                        return false;
                    }
                }
                //this is reached iff there are still elements left in the search path, but the current node has no child nodes and doesn't match the car of searchpath
                return false;
            } else {
                String pathCar = pathCar(searchPath); //the next element of the path
                String pathCdr = pathCdr(searchPath); //the remainder of the path to search for after locating this element 
                boolean foundMatchingChild = false;
                
                //search the children for a node whose load string matches the current element of the path
                int i = 0;
                for (; i < currChildren.length; i++) {
                    
                    if (currChildren[i].getLoadString().equals(pathCar)) {
                        foundMatchingChild = true;
                        break;
                    }
                }
                if (foundMatchingChild) {
                    
                    /* If the found child has a network piece associated, 
                       then we can call load() on it to give it control of how it loads from the remaining path */
                    if (currChildren[i].getNetworkPiece() != null) {
                        return currChildren[i].getNetworkPiece().load(data, pathCdr);
                    } else {
                        /* If the found child node has no associated network piece, we'll set this child node as active 
                           and continue searching for the load path destination. 
                           This will be the case when reaching leaf nodes or with helper nodes in the middle of a load tree
                           (such as in ProductCompressingAutoencoder's encoder/decoder nodes) */
                        currNode = currChildren[i];
                        searchPath = pathCdr;
                    }
                } else {
                    
                    //this is reached iff none of the current node's children match the next element of the path
                    return false;
                }
            }
        }
        
        if (currNode.getDataPointer() != null) {
            
            /* If this case triggers, then that means:
               -This is a loadable network piece whose node possesses a data pointer in addition to child nodes
               -A load path was supplied which points only to this loadable, and no further. */
            currNode.setData(data);
            currNode.setSuccessful(true);
            return true;
        } else {
            //this is reached iff the node the path is pointing to doesn't possess a data pointer to load to
            return false;
        }
    }
    
    public default String pathCdr(String loadPath)
    {
        int counter = 0;
        for( int i=0; i<loadPath.length(); i++ ) {
            if( loadPath.charAt(i) == '_' ) {
               counter++;
            } 
        }
        if(counter == 0)
            return "";
        else
            return loadPath.replaceFirst("[^" + SEPARATOR + "]*" + SEPARATOR, "");
    }
}
