/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import java.util.ArrayList;

/**
 *
 * @author Den
 */
public class CheckThread extends Thread {
//    public Boolean pause = false;
    public Integer multipliedPeriod = 0, basePeriod = 0;
    /* child threads */
    public ArrayList<CheckThread> childThreads = new ArrayList<>();
    
//    public void pauseThread() {
//        pause = true;
//        childThreads.forEach((thread)-> {
//            thread.pauseThread();
//        });
//    }
//    
//    public void resumeThread() {
//        pause = false;
//        childThreads.forEach((thread)-> {
//            thread.resumeThread();
//        });
//    }
    
    public void setMultiplyIndex(Integer multiplier) {
        this.multipliedPeriod = this.basePeriod * multiplier;
        childThreads.forEach((thread)-> {
            thread.setMultiplyIndex(multiplier);
        });
    }
    public Integer getMultipliedPeriod() {
        return this.multipliedPeriod;
    }
    public void setBasePeriod(Integer basePeriod) {
        this.basePeriod = basePeriod;
    }
    public Integer getBasePeriod() {
        return this.basePeriod;
    }
}