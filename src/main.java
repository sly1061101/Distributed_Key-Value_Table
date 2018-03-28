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
        u.startListen();

        TotalMulticast m = new TotalMulticast(u);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String s = br.readLine();
            String[] strings = s.split(" ");
            System.out.println(strings.length);
            if(strings.length == 3){
                u.unicast_send(Integer.parseInt(strings[1]), strings[2]);
            }
            else if(strings.length == 2){
                // for multi cast
                m.multicast(strings[1]);
            }
        }
    }
}
