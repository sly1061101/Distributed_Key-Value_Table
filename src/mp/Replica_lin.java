package mp;

import java.io.IOException;
import java.util.HashMap;

public class Replica_lin extends Replica {
    public HashMap<Character, Integer> map;
    public TotalMulticast tm;
    public volatile boolean isWaiting;

    public Replica_lin(TotalMulticast tm) {
        map = new HashMap<>();
        this.tm = tm;
        isWaiting = false;
        startListen();
    }

    @Override
    public void write(Character key, Integer value) {
        //format: "writeReq||key||value"
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
        //format: "readReq||key"
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
    //format of parameter: "key||value"
    public  void getWriteRequest(String message) {
        char key = message.charAt(0);
        int value = Integer.parseInt(message.substring(Utility.nthIndexOf(message, "||", 1) + 2));
        map.put(key, value);
        return ;
    }

    @Override
    public  void getReadRequest(String message) {
        //do nothing
        return;
    }

    private void startListen() {
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                listen();
            }
        };

        new Thread(listener).start();
    }

    private void listen() {
        String message;
        //format of message:
        // "sender id||writeReq||key||value"
        // "sender id||readReq||key"
        while(true) {
            if ((message = tm.deliver()) != null) {
                int senderId = Integer.parseInt(message.substring(0, Utility.nthIndexOf(message, "||", 1)));
                String command = message.substring(Utility.nthIndexOf(message, "||", 1) + 2, Utility.nthIndexOf(message, "||", 2));
                if (command.equals("writeReq")) {
                    getWriteRequest(message.substring(Utility.nthIndexOf(message, "||", 2) + 2));
//                    System.out.println("sender ID: " +  senderId + " my ID: " + tm.u.ID);
                    if (senderId == tm.u.ID) {
                        isWaiting = false;
//                        System.out.println("I have set the flag!!!");
                    }
                } else if (command.equals("readReq")) {
                    getReadRequest(message.substring(Utility.nthIndexOf(message, "||", 2) + 2));
                    if (senderId == tm.u.ID)
                        isWaiting = false;
                }
            }
        }
    }
}