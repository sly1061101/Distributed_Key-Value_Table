import mp.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    public static BasicMulticast bm;
    public static Replica_env re;
    public static TotalMulticast tm;
    public static Replica_lin rl;
    public static Client c;

    public static void main(String[] args) throws IOException, InterruptedException {
//        if(args.length != 2) {
//            System.out.println("Wrong command line!\n");
//            System.exit(-1);
//        }

        Unicast u = new Unicast( Integer.parseInt(args[0]), Config.parseConfig("configFile") );
        if(args[1].equals("Eventual") && args.length == 4){
            bm = new BasicMulticast(u);
            re = new Replica_env(bm);
            c = new Client(re, Integer.parseInt(args[0]));
            re.W_value = Integer.parseInt(args[2]);
            re.R_value = Integer.parseInt(args[3]);
        }
        else if(args[1].equals("Linearizability")){
            tm = new TotalMulticast(u);

            rl = new Replica_lin(tm);

            c = new Client(rl, Integer.parseInt(args[0]));
        }

        else{
            System.out.println("Please choose one of the consistencies: Eventual or Linearizability. And formats: 'java main Eventual w_value r_value' or 'java main Linearizability'");
            return;
        }

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
                //clear the input stream during sleeping
                while(br.ready())
                    br.readLine();
            }
            else
                System.out.println("Illegal command!");
        }
    }
}
