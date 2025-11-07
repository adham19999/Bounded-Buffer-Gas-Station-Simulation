import java.util.Scanner;

class Colors{

    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
}

class Semaphore {
    private int value = 0;
    public Semaphore(int initial) {
        value = initial;
    }

    public synchronized void waitSem() throws InterruptedException {
        value--;
        while(value < 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                
            }
        }
        
    }
    public synchronized void signalSem() {
        value++;
        notify();
    }

    public synchronized int getValue() {
        return value;
    }
}

class SharedBuffer {
    private int size;
    private Car store[];
    private int inptr = 0;
    private int outptr = 0;

    //use semaphore to control access
    private Semaphore spaces;    // counts empty slots
    private Semaphore elements;  // counts filled slots
    private Semaphore mutex;

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

        store[inptr] = car;
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
    public boolean isFull() {
        return spaces.getValue() == 0;
    }
}

class Car extends Thread {
    private final String name;
    private final SharedBuffer buffer;
    private final Semaphore serviceBays;
    public Car(String name, SharedBuffer buffer, Semaphore serviceBays) {
        this.name = name;
        this.buffer = buffer;
        this.serviceBays = serviceBays;
    }

    @Override
    public void run() {
        try {
            System.out.println(Colors.YELLOW + name + Colors.BLUE + " arrived" + Colors.RESET);

            // Check if waiting area is full before enqueueing
            synchronized (buffer) {  
                if (buffer.isFull()) {
                    System.out.println(Colors.RED + name + Colors.CYAN + " cannot enter, waiting area is FULL, waiting outside..." + Colors.RESET);
                }
            }

            // This line will block if no space is available
            buffer.enqueue(this);

            System.out.println(Colors.YELLOW + name + Colors.GREEN + " entered waiting area" + Colors.RESET);

            // If all pumps busy (no available bays)
            if (serviceBays.getValue() == 0) {
                System.out.println(Colors.YELLOW + name + Colors.CYAN + " is waiting for a free pump" + Colors.RESET);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(Colors.YELLOW + name + " was interrupted");
        }
    }
  
    @Override
    public String toString() {
        return name;
    }
}

class Pump extends Thread {
    private SharedBuffer b;
    private int pumpID;

    public Pump(SharedBuffer b, int pumpID) {
        this.b = b;
        this.pumpID = pumpID;
    }   

    @Override
    public void run() {
        
            try {
                while (true) {
                    Car car = b.dequeue();
                    if (car == null) break; // stop signal

                
                    System.out.println(Colors.PURPLE + "Pump " + pumpID + ": " + Colors.YELLOW + car + Colors.BLUE + " Occupied" + Colors.RESET);
                    System.out.println(Colors.PURPLE + "Pump " + pumpID + ": " + Colors.YELLOW + car + Colors.GREEN + " begins service at Bay " + pumpID + Colors.RESET);

                    Thread.sleep(700); // simulate service

                    System.out.println(Colors.PURPLE + "Pump " + pumpID + ": " + Colors.YELLOW + car + Colors.GREEN + " finishes service" + Colors.RESET);
                    System.out.println(Colors.PURPLE + "Pump " + pumpID + ": Bay " + pumpID + Colors.GREEN + " is now free" + Colors.RESET);

            
            }
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        
        System.out.println("Pump " + pumpID + " finished servicing " + " cars");
    }


}

class ServiceStation {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter waiting area capacity (1-10): ");
        int waitingArea = sc.nextInt();
        if (waitingArea < 1 || waitingArea > 10) waitingArea = 5;

        System.out.print("Enter number of service bays (pumps): ");
        int numPumps = sc.nextInt();

        System.out.print("Enter cars arriving (space-separated, e.g., C1 C2 C3 C4 C5): ");
        sc.nextLine(); 
        String[] carNames = sc.nextLine().trim().split("\\s+");

        SharedBuffer buffer = new SharedBuffer(waitingArea);
        Semaphore serviceBays = new Semaphore(numPumps);

        // Start pumps (consumers)
        Pump[] pumps = new Pump[numPumps];
        for (int i = 0; i < numPumps; i++) {
            pumps[i] = new Pump(buffer, i + 1);
            pumps[i].start();
        }

        // Start cars (producers)
        Car[] cars = new Car[carNames.length];
        for (int i = 0; i < carNames.length; i++) {
            cars[i] = new Car(carNames[i], buffer, serviceBays); // pass serviceBays
            cars[i].start();
            try {
                Thread.sleep(200); // delay between arrivals
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Wait for all cars to finish (produced)
        for (Car car : cars) {
            try {
                car.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

      
        // Send one (null) for each pump to stop them
        for (int i = 0; i < numPumps; i++) {
            try {
                buffer.enqueue(null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Wait for all pumps to finish
        for (Pump pump : pumps) {
            try {
                pump.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("All cars processed; simulation ends.");
        sc.close();
    }
}