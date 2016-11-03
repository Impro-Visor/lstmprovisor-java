/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import filters.Operations;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;

/**
 * Class FullyConnectedLayer is an implementation of a simple neural network layer which multiplies inputs by a weight matrix and adds biases,
 * then performs an operation on the resultant vector such as sigmoid or tanh.
 * @author Nicholas Weintraut
 */
public class FullyConnectedLayer implements Loadable {
    
    private LoadTreeNode loadNode;
    private AMatrix weights;
    private AVector biases;
    private Operations type;
    
    private AVector multResult;
    
    public FullyConnectedLayer (Operations type)
    {
        this.type = type;
        initDummyData();
    }
    
    public AVector forward (AVector input)
    {
            
            multResult = weights.innerProduct(input);
            multResult.add(biases);
            return type.operate(multResult);
    }
    
    @Override
    public LoadTreeNode constructLoadTree() {
        String[] loadStrings = new String[]{"b","w"};
        DataPointer[] dataPointers = new DataPointer[]{ new DataPointer(data -> biases = data.asVector()), 
                                                        new DataPointer(data -> weights = (AMatrix) data)};
        LoadTreeNode primaryNode = new LoadTreeNode(loadStrings, dataPointers);
        assignToNode(primaryNode);
        return primaryNode;
    }

    @Override
    public LoadTreeNode getCurrentLoadTree() {
        return loadNode;
    }

    @Override
    public void assignToNode(LoadTreeNode node) {
        this.loadNode = node;
        this.loadNode.setNetworkPiece(this);
    }

    private void initDummyData() {
        weights = Matrix.create(1,1);
        biases = Vector.create(new double[1]);
    }
}
