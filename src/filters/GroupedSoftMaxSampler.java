/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters;

import filters.Operations;
import encoding.Group;
import java.util.Random;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 *
 * @author cssummer16
 */
public class GroupedSoftMaxSampler implements DataFilter    {
    
    private Group[] groups;
    
    public GroupedSoftMaxSampler(Group[] groups)
    {
        this.groups = groups;
    }
    
    public INDArray filter(INDArray output)
    {
        int groupKernel = 0;
                for(Group group : groups) {
                    if(group.isOneHot()) {
                        INDArray groupData = Operations.Softmax.operate(output.get(NDArrayIndex.interval(group.startIndex, group.endIndex)));
                        
                        int index = 0;
                        double randPoint = (new Random()).nextDouble();
                        while(randPoint > 0.0 && index < groupData.length() - 1) {
                            if(groupData.getDouble(index) < randPoint)
                                randPoint -= groupData.getDouble(index++);
                            else
                                randPoint -= groupData.getDouble(index);
                        }
                        for(int j = 0; j < group.length(); j++) {
                            if(j != index) {
                                output.putScalar(groupKernel + j, 0.0);
                            }
                            else {
                                output.putScalar(groupKernel + j, 1.0);
                            }
                        }
                        
                    }
                    else {
                        INDArray groupData = Operations.Sigmoid.operate(output.get(NDArrayIndex.interval(group.startIndex, group.endIndex)));
                        Random rand = new Random();
                        for(int j = 0; j < group.length(); j++) {
                            double nextDouble = rand.nextDouble();
                            //System.out.println("(" + gOutput[j] + ", " + nextDouble + ")");
                            if(groupData.getDouble(j) >= nextDouble)
                            {
                                output.putScalar(groupKernel + j, 1.0);
                            }
                            else {
                                output.putScalar(groupKernel + j, 0.0);
                            }
                        }
                    }
                    
                    groupKernel += group.length();
                }
                return output;
    }
}
