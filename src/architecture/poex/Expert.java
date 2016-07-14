/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture.poex;

import architecture.FullyConnectedLayer;
import architecture.LSTM;
import architecture.Loadable;
import filters.Operations;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;

/**
 *
 * @author cssummer16
 */
public class Expert implements Loadable {
    private LSTM lstm1;
    private LSTM lstm2;
    private FullyConnectedLayer fullLayer;
    private boolean shouldPrintInternals;

    public Expert(Operations outputOperation) {
        this.lstm1 = new LSTM();
        this.lstm2 = new LSTM();
        this.fullLayer = new FullyConnectedLayer(outputOperation);
        shouldPrintInternals = false;
    }
    public void setPrintInternals(boolean shouldPrintInternals)
    {
        this.shouldPrintInternals = shouldPrintInternals;
    }
    
    public AVector process(AVector input) {
        //System.out.println("Processing input " + input);
        if(shouldPrintInternals)
            System.out.println(lstm1);
        AVector val1 = lstm1.step(input, shouldPrintInternals);
        
        if(shouldPrintInternals) {
            System.out.println(val1);
            System.out.println(lstm2);
        }
        AVector val2 = lstm2.step(val1, shouldPrintInternals);
        if(shouldPrintInternals)
            System.out.println(val2);
        AVector val3 = fullLayer.forward(val2);
        if(shouldPrintInternals)
            System.out.println(val3);
        
        return val3;
    }
    
    @Override
    public boolean load(INDArray data, String loadPath) {
        String car = pathCar(loadPath);
        String cdr = pathCdr(loadPath);
        switch(car)
        {
            case "full": return fullLayer.load(data, cdr);
            case "lstm1": return lstm1.load(data, cdr);
            case "lstm2": return lstm2.load(data, cdr);
            default: return false;
        }
    }
}
