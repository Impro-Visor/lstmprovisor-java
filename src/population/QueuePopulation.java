/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package population;

import architecture.FragmentedNeuralQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

/**
 * The queue population will herd amongst the grasses of its ancestors,
 * rise from earthly dew and up the mountains,
 * through the chasms serenaded by rivers,
 * around the rings of latitude with the attitude
 * to evolve, prosper, vibrantly explode into jazz! Razzle dazzle dazzle razzle!
 * @author cssummer16
 */
public class QueuePopulation {
    
    List<FragmentedNeuralQueue> queues;
    
    public QueuePopulation()
    {
        queues = new ArrayList<>();
    }
    
    public void evolve(double maxMutationStrength, double crossoverProb)
    {
        Random mutationRand = new Random();
        Random crossoverRand = new Random();
        for(FragmentedNeuralQueue queue : queues) {
            queue.addNoise(mutationRand.nextDouble() * maxMutationStrength);
        }
        for(FragmentedNeuralQueue queue : queues) {
            if(crossoverRand.nextDouble() <= crossoverProb)
            {
                List<FragmentedNeuralQueue> selectableQueues = new ArrayList<>(queues);
                selectableQueues.remove(queue);
                queue.crossover(selectableQueues.get(crossoverRand.nextInt(selectableQueues.size())), 1);
            }
        }
    }
    
    public void herd(FragmentedNeuralQueue targetQueue, double maxMagnitude)
    {
        Random rand = new Random();
        for(FragmentedNeuralQueue queue : queues)
        {
            queue.basicInterpolate(targetQueue, maxMagnitude * rand.nextDouble());
        }
    }
    
    public void add(FragmentedNeuralQueue newQueue)
    {
        queues.add(newQueue);
    }
    
    public FragmentedNeuralQueue sample()
    {
        Random rand = new Random();
        AVector strengths = Vector.createLength(queues.size());
        for(int i = 0; i <  strengths.length(); i++)
        {
            strengths.set(i, rand.nextDouble());
        }
        strengths.divide(strengths.elementSum());
        FragmentedNeuralQueue sampledQueue = queues.get(0).copy();
        sampledQueue.weightedAverageFeatures(queues, strengths);
        return sampledQueue;
    }
}
