public class SharedBuffer {
    private int size = 10;
    private Car store[] = new Car[size];
    private int inptr = 0;
    private int outptr = 0;

    //use semaphore to control access
    private Semaphore spaces;    // counts empty slots
    private Semaphore elements;  // counts filled slots
    private Semaphore mutex;

    Car car;

    public SharedBuffer(int size) {
        this.size = size;
        store = new Car[size];
        spaces = new Semaphore(size);  // initially all slots are empty
        elements = new Semaphore(0);   // no cars yet
        mutex = new Semaphore(1);
    }

    //using mutual exclusion to allow only one thread at a time
    public void enqueue(Car car) throws InterruptedException {
        spaces.waitSem();
        mutex.waitSem();

        store[inptr]= car;
        inptr = (inptr + 1) % size;

        mutex.signalSem();
        elements.signalSem();
    }

    public Car dequeue() throws InterruptedException {
        Car el;
        elements.waitSem();
        mutex.waitSem();

        el = store[outptr];
        outptr = (outptr + 1) % size;

        mutex.signalSem();
        spaces.signalSem();
        return el;
    }
}
