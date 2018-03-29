package mp;

public abstract class Replica {

    public abstract void write(Character key, Integer value);
    public abstract Integer read(Character key);
    public abstract void getWriteRequest(String message);
    public abstract void getReadRequest(String message);
    public abstract void dump();
}