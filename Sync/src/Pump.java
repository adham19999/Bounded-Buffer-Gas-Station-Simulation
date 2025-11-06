public class Pump extends Thread {
    private SharedBuffer b;
    private int TotalProcesses;
    private int pumpID;
    private Semaphore pumbS;

    public Pump(SharedBuffer b, int TotalProcesses, int pumpID, Semaphore pumbS) {
        this.b = b;
        this.TotalProcesses = TotalProcesses;
        this.pumpID = pumpID;
        this.pumbS = pumbS;
    }

    @Override
    public void run() {
        int processeed = 0;
        while(!Thread.currentThread().isInterrupted() && (TotalProcesses <= 0 || processeed < TotalProcesses)) {
            try{
                Car car = b.dequeue();
                pumbS.waitSem();
                System.out.println("Pump " + pumpID + " starts servicing Car " + car);
                Thread.sleep(200);
                System.out.println("Pump " + pumpID + " finishes servicing Car " + car);
                pumbS.signalSem();
                processeed++;
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Pump " + pumpID + " finished servicing " + processeed + " cars");
    }


}
