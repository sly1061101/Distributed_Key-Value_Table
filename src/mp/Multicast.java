package mp;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Multicast {
    Unicast u;
    int curSeq;
    PriorityQueue<String> buffer;
    boolean isSequencer;
    int sequencerCurSeq;

    public Multicast(Unicast u) {
        this.u = u;
        curSeq = 0;
        buffer = new PriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] o1Split = o1.split("\\u007C\\u007C");
                String[] o2Split = o2.split("\\u007C\\u007C");
                int seq1 = Integer.parseInt( o1Split[1] );
                int seq2 = Integer.parseInt( o2Split[1] );
                return seq1 - seq2;
            }
        });

        isSequencer = (u.hostInfo.idList.get(0) == u.ID);
        if( isSequencer ) {
            sequencerCurSeq = 0;
        }
        startListen();
    }

    //To multicast a message, add the "TOSEQ" header and send it to sequencer
    public void multicast(String message) throws IOException, InterruptedException {
        u.unicast_send(u.hostInfo.idList.get(0), "TOSEQ||" + message);
    }

    //Deliver a message with current sequence number if exist.
    public void deliver() {
        String message;

        if( !isSequencer ) {
            while ( (message = u.unicast_receive(u.hostInfo.idList.get(0))) != null )
                buffer.offer(message);
        }
        if( buffer.size() != 0 ) {
            String[] msgSplit = buffer.peek().split("\\u007C\\u007C");
            int seq = Integer.parseInt(msgSplit[1]);
            if (seq == curSeq) {
                String[] messageSplit = buffer.poll().split("\\u007c\\u007c");
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                System.out.println("Sender ID: " + messageSplit[2] + " System Time: "
                        + sdf.format(cal.getTime()) + " Message: " + messageSplit[3]);
                curSeq++;
            }
        }
    }

    public void startListen() {
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                try {
                    if(isSequencer)
                        listenAndDeliver();
                    else
                        while(true)
                            deliver();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(listener).start();
    }

    //For sequencer, decode the message and add the headers of "FROMSEQ", SEQ#, Original host ID. Then broadcast it.
    public void listenAndDeliver() throws IOException, InterruptedException {
        while(true){
            for(int i = 0; i < u.hostInfo.idList.size(); ++i){
                if(i == 0){
                    String message;
                    while ((message = u.unicast_receive(u.hostInfo.idList.get(i))) != null) {
                        String[] msgSplit = message.split("\\u007C\\u007C");
                        if (msgSplit[0].equals("TOSEQ")) {
                            String messageSent = message.substring(7); //remove the "TOSEQ||" header
                            for (Integer ID : u.hostInfo.idList) {
                                u.unicast_send(ID, "FROMSEQ||" + sequencerCurSeq
                                        + "||" + u.hostInfo.idList.get(i) + "||" + messageSent);
                            }
                            sequencerCurSeq++;
                        } else if (msgSplit[0].equals("FROMSEQ")) {
                            buffer.offer(message);
                        }
                    }
                }
                //if the message is not from sequencer, add header add deliever to all the hosts
                else if(i != 0) {
                    String message;
                    while ((message = u.unicast_receive(u.hostInfo.idList.get(i))) != null) {
                        String messageSent = message.substring(7); //remove "TOSEQ||" header
                        for (Integer ID : u.hostInfo.idList) {
                            u.unicast_send(ID, "FROMSEQ||" + sequencerCurSeq
                                    + "||" + u.hostInfo.idList.get(i) + "||" + messageSent);
                        }
                        sequencerCurSeq++;
                    }
                }
            }
            deliver();
        }
    }
}
