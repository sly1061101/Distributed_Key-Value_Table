package mp;

import java.io.IOException;

public class Replica_env extends Replica{
    public int W_value;
    public int R_value;
    public  BasicMulticast bMulti;
    // message encoding: key || value || operation number
    // 0 for write, 1 for read, 2 for getWriteRequest, 3 for getReadRequest
    @Override
    public  void write(Character key, Integer value) {
        int counter = 0;
        String message = Character.toString(key) +"||" + Integer.toString(value) + "||" + "0";
        try {
            bMulti.multicast(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startListen(counter);
        return;
    }
    @Override
    public  Integer read(Character key){

        return 0;
    }
    @Override
    public  void getWriteRequest(String message){
        return;
    }
    @Override
    public  void getReadRequest(String message){
        return;
    }

    public void startListen(int counter) {
//        Runnable listener = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while(counter < W_value){
//                        String receive = bMulti.deliver();
//                        if(false){
//                        /////// to be implemented
//
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        new Thread(listener).start();
        return;
    }
}