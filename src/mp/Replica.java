package mp;

import java.util.HashMap;

public abstract class Replica{
    public HashMap<Character, Integer> map = new HashMap<>();

    public abstract void write();
    public abstract void read();
    public abstract void getWriteRequest();
    public abstract void getReadRequest();
}