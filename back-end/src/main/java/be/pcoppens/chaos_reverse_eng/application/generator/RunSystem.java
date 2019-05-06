package be.pcoppens.chaos_reverse_eng.application.generator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Threat for RunSystem.
 */
class RunSystem implements Runnable {
    boolean isRunning=true;
    private DistribuedSystem ds;
    private OutputStream out;

    public RunSystem(DistribuedSystem ds){
        this.ds=ds;
    }

    public RunSystem(DistribuedSystem ds, OutputStream out){
        this.ds=ds;
        this.out=out;
    }

    @Override
    public void run() {
        if(out!=null)
            while (isRunning) {
                try {
                    ds.runSystem(out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        else
            while(isRunning)
                ds.runSystem();
    }
}
