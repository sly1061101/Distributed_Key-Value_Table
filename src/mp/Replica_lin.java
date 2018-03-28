package mp;

import java.io.IOException;

public class Replica_lin extends Replica {
    public TotalMulticast tm;
    public boolean isWaiting;

    public Replica_lin(TotalMulticast tm) {
        this.tm = tm;
        isWaiting = false;
    }

    @Override
    public void write(Character key, Integer value) {
        String message = "writeReq" + "||" + key + "||" + value;
        isWaiting = true;
        try {
            tm.multicast(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (isWaiting) {}
        return ;
    }

    @Override
    public  Integer read(Character key) {
        String message = "readReq" + "||" + key;
        isWaiting = true;
        try {
            tm.multicast(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (isWaiting) {}
        return map.getOrDefault(key, null);
    }

    @Override
    public  void getWriteRequest(String message) {

        return;
    }

    @Override
    public  void getReadRequest(String message) {
        //do nothing
        return;
    }
}