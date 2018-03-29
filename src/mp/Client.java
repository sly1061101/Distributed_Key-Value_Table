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
            r.write(key, value);
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
            Integer value = r.read(key);
            out.println("666666," + ID + ",get," + key + "," + System.currentTimeMillis() + ",resp," + value);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}