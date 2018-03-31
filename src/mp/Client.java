package mp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Client {
    Replica r;
    int ID;

    public Client(Replica r, int ID) {
        this.r = r;
        this.ID = ID;
    }

    public void put(Character key, Integer value) {
        try{
            PrintWriter out = new PrintWriter(new FileOutputStream(new File("log" + ID + ".txt"), true));
            out.println("666666," + ID + ",put," + key + "," + System.currentTimeMillis() + ",req," + value);
            System.out.println("   Putting value. New command will be executed only after finishing.");
            r.write(key, value);
            System.out.println("   Putting Finished.");
            out.println("666666," + ID + ",put," + key + "," + System.currentTimeMillis() + ",resp," + value);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void get(Character key) {
        try{
            PrintWriter out = new PrintWriter(new FileOutputStream(new File("log" + ID + ".txt"), true));
            out.println("666666," + ID + ",get," + key + "," + System.currentTimeMillis() + ",req,");
            System.out.println("   Getting value. New command will be executed only after finishing.");
            Integer value = r.read(key);
            if(value != Integer.MIN_VALUE){
                System.out.println("   Getting Finished.");
                System.out.println("   The key is: "+ key + ",  and the value is: " + value);
                out.println("666666," + ID + ",get," + key + "," + System.currentTimeMillis() + ",resp," + value);
            }
            else {
                System.out.println("   There is no such key in the system");
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void dump() {
        r.dump();
    }
}