package mp;

import java.io.IOException;
import java.sql.Time;
import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimerTask;

public class Replica_env extends Replica{
    public int W_value;
    public int R_value;

    private final int write_mode = 0;
    private final int read_mode = 1;
    private final int get_write_req_mode = 2;
    private final int get_read_req_mode = 3;

    private volatile int count_write;
    private volatile int count_read;
    private Character write_key; // The key used in the write operation
    private int write_value;    // The value used in the write operation
    private Character read_key;  // The key used in the read operation
    private Pair read_pair;      // The most recent pair received when waiting for acknowledges

    private volatile int max_waiting_time;

    class Pair{
        Integer value;
        Timestamp timestamp;
        Pair(Integer value, Timestamp timestamp){
            this.value = value;
            this.timestamp = timestamp;
        }
    }
//    class TimerTaskTest extends TimerTask{
//        @Override
//
//    }

    private HashMap<Character, Pair> map = new HashMap<>();
    public  BasicMulticast bMulti;

    // 0 for write, 1 for read, 2 for getWriteRequest, 3 for getReadRequest
    public Replica_env(BasicMulticast bMulti){
        this.bMulti = bMulti;
        count_read = 0;
        count_write = 0;
        max_waiting_time = bMulti.u.hostInfo.maxDelay * 3;
        startListen();
    }

    class TimerHelper {
        Timer timer;

        public TimerHelper(int delay) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Waited too much time, operation aborted");
                    count_read = R_value;
                    count_read = W_value;
                    timer.cancel();
                }
            }, delay);
        }

        public void destroyTimer(){
            this.timer.cancel();
        }
    }

    @Override
    public  void write(Character key, Integer value) {
        write_key = key;
        write_value = value;
        count_write = 0;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        map.put(key,new Pair(value, timestamp));
        String message = write_mode +  "||" + timestamp + "||" + key +"||" + value;
        TimerHelper t = null;
        try {
            bMulti.multicast(message);
             t = new TimerHelper(max_waiting_time);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(count_write < W_value) { }
        t.destroyTimer();
        count_write = 0;
        return;
    }
    @Override
    public  Integer read(Character key){
        read_key = key;
        read_pair = new Pair( null, new Timestamp(0));
        count_read = 0;
        String message = read_mode + "||" + key;
        TimerHelper t = null;
        try {
            bMulti.multicast(message);
            t = new TimerHelper(max_waiting_time);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(count_read < R_value){ }
        t.destroyTimer();
        count_read = 0;
        return read_pair.value;
    }
    @Override
    public  void getWriteRequest(String message){
        int firstSplit = Utility.nthIndexOf(message, "||", 1);
        int secondSplit = Utility.nthIndexOf(message, "||", 2);
        int thirdSplit = Utility.nthIndexOf(message, "||", 3);
        int fourthSplit = Utility.nthIndexOf(message, "||", 4);

        int senderId = Integer.parseInt(message.substring(0, firstSplit));
        Timestamp currrentTimestamp = Timestamp.valueOf(message.substring(secondSplit + 2, thirdSplit));
        Character key = message.charAt(fourthSplit - 1);
        int value = Integer.parseInt(message.substring(fourthSplit + 2));

        if(!map.containsKey(key)){
            map.put(key, new Pair(value, currrentTimestamp));
        }
        else if(currrentTimestamp.after(map.getOrDefault(key, null).timestamp)){
            map.put(key, new Pair(value, currrentTimestamp));
        }
        try{
            this.bMulti.u.unicast_send(senderId, get_write_req_mode + "||" + key.toString() + "||"+ value);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }
    @Override
    public  void getReadRequest(String message){
        int firstSplit = Utility.nthIndexOf(message, "||", 1);
        int secondSplit = Utility.nthIndexOf(message, "||", 2);
        int senderId = Integer.parseInt(message.substring(0, firstSplit));
        Character key = message.charAt(secondSplit + 2);

        Pair pair = map.getOrDefault(key, null);

        try{
            if(pair != null){
                bMulti.u.unicast_send(senderId, get_read_req_mode  + "||" + pair.timestamp.toString() + "||" + key + "||" + pair.value);
            } else {
                bMulti.u.unicast_send(senderId, get_read_req_mode  + "||" + new Timestamp(0).toString() + "||" + key + "||" + null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }


    public void startListen() {
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                }catch (IOException e){
                    e.printStackTrace();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        new Thread(listener).start();
    }

    private void listen()throws IOException, InterruptedException{
        String message;
        // format of message:
        // write: id||write_mode||timestamp||key||value
        // read: id||read_mode||key
        //get write request: id||get_write_req_mode||key||value
        //get read request: id||get_read_req_mode||timestamp||key||value
        while(true){
            if((message = bMulti.deliver()) != null){
//                System.out.println(message);
//                System.out.println("W: "+count_write);
//                System.out.println("R: "+count_read);
                int firstSplit = Utility.nthIndexOf(message, "||", 1);
                int secondSplit = Utility.nthIndexOf(message,"||",2);
                int mode = Integer.parseInt(message.substring(firstSplit + 2,secondSplit));
//                System.out.println("mode: "+ mode);
                if(mode == write_mode){
                    getWriteRequest(message);
                }
                else if(mode == read_mode){
                    getReadRequest(message);
                }
                else if(mode == get_write_req_mode){
                    int thirdSplit = Utility.nthIndexOf(message, "||", 3);
                    Character key = message.charAt(thirdSplit - 1);
                    int value = Integer.parseInt(message.substring(thirdSplit + 2));
                    if(key == write_key && value == write_value){
                        count_write++;
                    }
                }
                else if(mode == get_read_req_mode){
                    int thirdSplit = Utility.nthIndexOf(message, "||", 3);
                    int fourthSplit = Utility.nthIndexOf(message, "||", 4);
                    Timestamp timestamp = Timestamp.valueOf(message.substring(secondSplit + 2, thirdSplit));
                    Character key = message.charAt(fourthSplit - 1);
                    Integer value = message.substring(fourthSplit + 2).equals("null") ? null : Integer.parseInt(message.substring(fourthSplit + 2));
                    if(key == read_key) {
                        count_read++;
                        if (timestamp.after(read_pair.timestamp)) {
                            read_pair.timestamp = timestamp;
                            read_pair.value = value;
                        }
                    }
                }
            }
            Thread.sleep(100);
        }
    }

    @Override
    public void dump() {
        Set<Map.Entry<Character, Pair>> s = map.entrySet();
        Iterator<Map.Entry<Character, Pair>> it = s.iterator();
        while( it.hasNext() ) {
            Map.Entry<Character, Pair> e = it.next();
            System.out.println("   " + e.getKey() + " " + e.getValue().value);
        }
    }
}