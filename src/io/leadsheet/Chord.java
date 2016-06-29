/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.leadsheet;

/**
 *
 * @author cssummer16
 */
public class Chord {
    private int duration;
    private String root;
    private String type;
    
    public Chord(int duration, String root, String type) {
        this.duration = duration;
        this.root = root;
        this.type = type;
    }
    
    public int getDuration()
    {
        return duration;
    }
    
    public void setDuration(int duration)
    {
        this.duration = duration;
    }
    
    public String getRoot()
    {
        return root;
    }
    
    public String getType()
    {
        return type;
    }
}
