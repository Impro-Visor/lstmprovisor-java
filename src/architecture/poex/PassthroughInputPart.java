/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture.poex;

import mikera.vectorz.AVector;

/**
 *
 * @author cssummer16
 */
public class PassthroughInputPart extends RelativeInputPart {
    private AVector next_result;
    private int valid_ct;
    
    public PassthroughInputPart() {
        this.next_result = null;
        this.valid_ct = 0;
    }
    
    public void provide(AVector next_result, int valid_for) {
        this.next_result = next_result;
        this.valid_ct = valid_for;
    }
    public void provide(AVector next_result) {
        this.provide(next_result,1);
    }

    @Override
    public AVector generate(int relativePosition, int chordRoot, AVector chordTypeData) {
        if(this.valid_ct > 0) {
            this.valid_ct--;
            return this.next_result;
        } else {
            throw new RuntimeException("generate called without providing a result to return!");
        }
    }
    
}
