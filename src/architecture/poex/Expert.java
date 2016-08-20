/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture.poex;

import architecture.FullyConnectedLayer;
import architecture.LSTM;
import architecture.LoadTreeNode;
import architecture.Loadable;
import filters.Operations;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;

/**
 *
 * @author cssummer16
 */
public class Expert implements Loadable {
    private LoadTreeNode loadNode;
    private LSTM lstm1;
    private LSTM lstm2;
    private FullyConnectedLayer fullLayer;

    public Expert(Operations outputOperation) {
        this.lstm1 = new LSTM();
        this.lstm2 = new LSTM();
        this.fullLayer = new FullyConnectedLayer(outputOperation);
    }
    
    public AVector process(AVector input) {
        AVector val1 = lstm1.step(input);
        AVector val2 = lstm2.step(val1);
        AVector val3 = fullLayer.forward(val2);
        return val3;
    }

    @Override
    public LoadTreeNode constructLoadTree() {
        String[] loadStrings = new String[]{"full","lstm1","lstm2"};
        LoadTreeNode[] childNodes = new LoadTreeNode[]{fullLayer.constructLoadTree(), lstm1.constructLoadTree(), lstm2.constructLoadTree()};
        LoadTreeNode loadNode = new LoadTreeNode(loadStrings, childNodes);
        assignToNode(loadNode);
        return loadNode;
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
}
