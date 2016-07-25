/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import mikera.vectorz.AVector;

/**
 * Class DataStep is a wrapper for components of vector data retrievable by name. Used for ease of reading specific data from DataSequence instances
 * @author Nicholas Weintraut
 * @see DataSequence
 */
public class DataStep {
    
    //Component vectors have names used for lookup
    private String[] names;
    private AVector[] components;
    
    public DataStep() {
    }
    
    /**
     * Add a number of names (order must match order of respective components)
     * @param names The array of component names
     */
    public void addNames(String...names)
    {
        this.names = names;
    }
    
    /**
     * Add a number of names (order must match order of respective components)
     * @param components The array of component vectors
     */
    public void addComponents(AVector... components)
    {
        this.components = components;
    }
    
    /**
     * Optional method to check if names and components arrays are instantiated and have the same length;
     * throws DataStepException if either array is null or they are not the same length.
     */
    public void authenticate() throws DataStepException
    {
        if(names == null)
            throw new DataStepException("Names of components have not been initialized.", this);
        else if(components == null)
            throw new DataStepException("Components have not been initialized.", this);
        else if(names.length > components.length)
            throw new DataStepException("There are more names than components.", this);
        else if(names.length < components.length)
            throw new DataStepException("There are more components than names", this);
    }
    
    /**
     * Fast get operation for intended use case (Very small number of components, low storage needed, quick retrieval without a lot of checks)
     * @param componentName The name of the component to retrieve
     * @return The AVector component data that corresponds to the given name
     */
    public AVector get(String componentName)
    {
        for(int i = 0; i < names.length; i++)
        {
            if(names[i].equals(componentName))
                return components[i];
        }
        throw new DataStepException("There is no component with name " + componentName, this);
    }
}
