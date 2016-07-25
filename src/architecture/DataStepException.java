/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

/**
 * DataStepException marks that there is an error with the initialization or operation of a DataStep instance
 * @author cssummer16
 */
public class DataStepException extends RuntimeException {
    private DataStep dataStep = new DataStep();
    public DataStepException(String message, DataStep dataStep)
    {
        super(message);
        this.dataStep = dataStep;
    }
}
