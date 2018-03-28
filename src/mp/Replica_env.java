package mp;

public class Replica_env extends Replica{
    public  BasicMulticast bMulti;
    // message encoding: key || value || operation number
    // 0 for write, 1 for read, 2 for getWriteRequest, 3 for
    @Override
    public  void write(Character key, Integer value){
        int counter = 0;
        String message = Character.toString(key) +"||" + Integer.toString(value) + "||" + "0";
        bMulti.multicast(message);
        startListen(counter);
        return;
    }
    @Override
    public  void read(Character key){
        return;
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
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                try {
                    while(counter < this.W_value){
                        String receive = deliver();
                        if(){
                        /////// to be implemented

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(listener).start();
    }
}