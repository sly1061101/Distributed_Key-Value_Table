package mp;

import java.io.IOException;
import java.util.*;

public class Unicast {
    int ID;
    Config hostInfo;

    Server s;
    Client c;

    //buffer to store the messages received but not delivered yet.
    //it is a fifo queue for each ID
    Map<Integer, Queue<String>> messageBuffer;

    public Unicast(int ID, Config hostInfo) throws IOException{
        this.ID = ID;
        this.hostInfo = hostInfo;
        s = new Server(hostInfo.addrMap.get(ID).getHostName(), hostInfo.addrMap.get(ID).getPort());
        c = new Client();

        messageBuffer = new HashMap<>();
        for(Integer i : hostInfo.idList)
            messageBuffer.put(i, new LinkedList<>());
        startListen();
    }

    public void startListen(){
        Runnable server = new Runnable() {
            @Override
            public void run() {
                try {
                    s.startServer(messageBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(server).start();
    }

    public void unicast_send(int destination, String message) throws IOException, InterruptedException{
        int delay = (int) (hostInfo.minDelay + (hostInfo.maxDelay - hostInfo.minDelay) * Math.random());
        TimerTask unicastSend = new TimerTask() {
            @Override
            public void run() {
                synchronizedSend.send(ID, c, hostInfo, destination, message);
            }
        };
        new Timer().schedule(unicastSend, delay);
        //System.out.println( "Sent \"" + message + "\" to process " + destination + ", system time is " + System.currentTimeMillis() );
    }

    public String unicast_receive(int source){
        return messageBuffer.get(source).poll();
    }
}

class synchronizedSend {
    public static synchronized void send(int ID, Client c, Config hostInfo, int destination, String message){
        try {
            c.startClient(hostInfo.addrMap.get(destination).getHostName(), hostInfo.addrMap.get(destination).getPort());
            c.sendMessage(ID + "||" + message);
            c.closeClient();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}