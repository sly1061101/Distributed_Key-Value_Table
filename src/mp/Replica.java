package mp;

import java.io.IOException;
import java.util.HashMap;

public abstract class Replica{
    public HashMap<Character, Integer> map = new HashMap<>();

    public abstract void write(Character key, Integer value)throws InterruptedException, IOException;
    public abstract void read(Character key)throws InterruptedException, IOException;
    public abstract void getWriteRequest(String message);
    public abstract void getReadRequest(String message);
}