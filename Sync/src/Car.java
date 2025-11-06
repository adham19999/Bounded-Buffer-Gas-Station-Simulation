public class Car extends Thread {
    private final int id;
    private final SharedBuffer buffer;
    public Car(int id, SharedBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }
    @Override
    public void run() {
        System.out.println("Car " + id + " arrives");
        try {
            buffer.enqueue(this);
            System.out.println("Car " + id + " enters queue");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Car " + id + " was interrupted");
        }
    }
    public int getIdValue() {
        return id;
    }

    @Override
    public String toString() {
        return "Car-" + id;
    }
}