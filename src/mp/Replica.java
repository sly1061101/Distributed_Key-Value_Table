package mp;

import java.util.HashMap;

public abstract class Replica{
    public HashMap<Character, Integer> map = new HashMap<>();
    public int W_value;
    public int R_value;
    public abstract void write(Character key, Integer value);
    public abstract void read(Character key);
    public abstract void getWriteRequest(String message);
    public abstract void getReadRequest(String message);
}