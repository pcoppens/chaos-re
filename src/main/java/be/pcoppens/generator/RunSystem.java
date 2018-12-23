package be.pcoppens.generator;

public class RunSystem implements Runnable {
    boolean isRunning=true;
    private DistribuedSystem ds;

    public RunSystem(DistribuedSystem ds){
        this.ds=ds;
    }
    @Override
    public void run() {
        while(isRunning){
            ds.runSystem();
        }
    }
}
