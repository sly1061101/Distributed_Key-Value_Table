package mp;

import java.io.IOException;
import java.util.HashMap;

public abstract class Replica {
    public HashMap<Character, Integer> map = new HashMap<>();

    public abstract void write(Character key, Integer value);
    public abstract Integer read(Character key);
    public abstract void getWriteRequest(String message);
    public abstract void getReadRequest(String message);
}