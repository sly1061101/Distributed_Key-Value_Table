package mp;

import java.io.IOException;
import java.sql.Time;
import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Replica_env extends Replica{
    class Pair{
        Integer value;
        Timestamp timestamp;
        Pair(Integer value, Timestamp timestamp){
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    public int W_value;
    public int R_value;

    private final int write_mode = 0;
    private final int read_mode = 1;
    private final int get_write_req_mode = 2;
    private final int get_read_req_mode = 3;

    private int count_write;
    private int count_read;
    private Character write_key; // The key used in the write operation
    private int write_value;    // The value used in the write operation
    private Character read_key;  // The key used in the read operation
    private Pair read_pair;      // The most recent pair received when waiting for acknowledges

    private HashMap<Character, Pair> map = new HashMap<>();
    public  BasicMulticast bMulti;

    // 0 for write, 1 for read, 2 for getWriteRequest, 3 for getReadRequest
    public Replica_env(BasicMulticast bMulti){
        this.bMulti = bMulti;
        count_read = 0;
        count_write = 0;
        startListen();
    }

    @Override
    public  void write(Character key, Integer value) {
        write_key = key;
        write_value = value;
        count_write = 0;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String message = write_mode +  "||" + timestamp + "||" + key +"||" + value;
        try {
            bMulti.multicast(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(count_write < W_value){}
        count_write = 0;
        return;
    }
    @Override
    public  Integer read(Character key){
        read_key = key;
        read_pair = new Pair( 0, new Timestamp(System.currentTimeMillis()))
        ;
        count_read = 0;
        String message = read_mode + "||" + key;
        try {
            bMulti.multicast(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(count_read < R_value){}
        count_read = 0;
        // to be modified: avoid NOP
        return map.getOrDefault(key, null).value;
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

        if(currrentTimestamp.after(map.getOrDefault(key, null).timestamp)){
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
        int senderId = Integer.parseInt(message.substring(firstSplit + 2, secondSplit));
        Character key = message.charAt(secondSplit + 2);

        Pair pair = map.getOrDefault(key, null);

        try{
            if(pair != null){
                bMulti.u.unicast_send(senderId, get_read_req_mode  + pair.timestamp.toString() + "||" + key + "||" + pair.value);
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
                    listen();
            }
        };

    }

    private void listen(){
        String message;
        // format of message:
        // write: id||write_mode||timestamp||key||value
        // read: id||read_mode||key
        //get write request: id||get_write_req_mode||key||value
        //get read request: id||get_read_req_mode||timestamp||key||value
        while(true){
            if((message = bMulti.deliver()) != null){
                int firstSplit = Utility.nthIndexOf(message, "||", 1);
                int secondSplit = Utility.nthIndexOf(message,"||",2);
                int mode = Integer.parseInt(message.substring(firstSplit + 2,secondSplit));
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
                    Integer value = Integer.parseInt(message.substring(fourthSplit + 2));
                    if(key == read_key) {
                        count_read++;
                        if (timestamp.after(read_pair.timestamp)) {
                            read_pair.timestamp = timestamp;
                            read_pair.value = value;
                        }
                    }
                }
            }
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