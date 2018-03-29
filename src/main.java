import mp.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length != 1) {
            System.out.println("Wrong command line!\n");
            System.exit(-1);
        }

        Unicast u = new Unicast( Integer.parseInt(args[0]), Config.parseConfig("configFile") );

        TotalMulticast tm = new TotalMulticast(u);

        Replica_lin rl = new Replica_lin(tm);

        Client c = new Client(rl, Integer.parseInt(args[0]));

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            String s = br.readLine();
            String[] strings = s.split(" ");
            if(strings[0].equals("put")) {
                char key = strings[1].charAt(0);
                int value = Integer.valueOf(strings[2]);
                c.put(key, value);
            }
            else if(strings[0].equals("get")) {
                char key = strings[1].charAt(0);
                c.get(key);
            }
            else if(strings[0].equals("dump"))
                c.dump();
            else if(strings[0].equals("delay")) {
                int ms = Integer.valueOf(strings[1]);
                Thread.sleep(ms);
                while(br.ready())
                    br.readLine();
            }
            else
                System.out.println("Illegal command!");
        }
    }
}
