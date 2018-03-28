package mp;

public class Replica_lin extends Replica{
    public TotalMulticast m;

    @Override
    public  void write(Character key, Integer value)){
        return;
    }
    @Override
    public  void read(Character key){
        return;
    }
    @Override
    public  void getWriteRequest(String message)){
        return;
    }
    @Override
    public  void getReadRequest(String message)){
        return;
    }
}