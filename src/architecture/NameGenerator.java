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
import mikera.arrayz.INDArray;
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
    
    public boolean load(INDArray data, String loadString) {
        String pathCdr = pathCdr(loadString);
        String pathCar = pathCar(loadString);
        switch(pathCar) {
            case "lstm": return lstm.load(data, pathCdr);
            case "full": return fullLayer.load(data, pathCdr);
            default:
                return false;
        }
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
    
    

    
}
