/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.Stack;

/**
 *
 * @author cssummer16
 */
public class LogTimer {
    
    public static Stack<Long> startLogTimes = new Stack<Long>();
    
    public static Long startTime;
    
    public static void initStartTime()
    {
        startTime = System.nanoTime();
    }
    
    public static void startLog(String message)
    {
        startLogTimes.push(System.nanoTime() - startTime);
        System.out.print(((startLogTimes.peek()) / 1000000000.00) + "seconds: " + message);
    }
    
    public static void endLog()
    {
        System.out.println("took " + ((System.nanoTime() - startTime - startLogTimes.pop()) / 1000000000.00) + " seconds: ");
    }
    
    public static void log(String message)
    {
        System.out.println(((System.nanoTime() - startTime) / 1000000000.00) + "seconds: " + message);
    }
    
}
