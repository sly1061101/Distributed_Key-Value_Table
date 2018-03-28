package mp;

public class Replica_env extends Replica{
    public  BasicMulticast m;

    @Override
    public  void write(Character key, Integer value){
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
}