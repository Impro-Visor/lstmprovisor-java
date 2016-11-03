/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import encoding.Group;
import filters.GroupedSoftMaxSampler;
import filters.Operations;
import java.util.Random;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

/**
 *
 * @author nick
 */
public class NameGenerator implements Loadable {
    private static String characterString = " !\"'[],-.01245679:?ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstuvwxyz";
    
    private LSTM lstm;
    private FullyConnectedLayer fullLayer;

    private LoadTreeNode loadNode;

    public NameGenerator() {
        lstm = new LSTM();
        fullLayer = new FullyConnectedLayer(Operations.None);
        
    }
    
    @Override
    public LoadTreeNode constructLoadTree() {
        String[] loadStrings = {"full", "lstm"};
        LoadTreeNode[] childNodes = {fullLayer.constructLoadTree(), lstm.constructLoadTree()};
        LoadTreeNode primaryNode = new LoadTreeNode(loadStrings, childNodes);
        assignToNode(primaryNode);
        return primaryNode;
    }

    @Override
    public LoadTreeNode getCurrentLoadTree() {
        return loadNode;
    }

    @Override
    public void assignToNode(LoadTreeNode node) {
        node.setNetworkPiece(this);
        this.loadNode = node;
    }
    
    public String generateName() {
        Random rand = new Random();
        AVector charOut = Vector.createLength(characterString.length());
        GroupedSoftMaxSampler sampler = new GroupedSoftMaxSampler(new Group[]{new Group(0, characterString.length(), true)});
        String songTitle = "";
        for (int i = 0; i < 50; i++) {
            charOut = fullLayer.forward(lstm.step(charOut));
            charOut = sampler.filter(charOut);
            int charIndex = 0;
            for (; charIndex < charOut.length(); charIndex++) {
                if (charOut.get(charIndex) == 1.0) {
                    break;
                }
            }
            songTitle += characterString.substring(charIndex, charIndex + 1);
        }
        return songTitle.trim();
    }

    @Override
    public void postLoad() {
        
    }
    
    

    
}
