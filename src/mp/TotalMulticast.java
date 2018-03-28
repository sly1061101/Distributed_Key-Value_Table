package mp;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TotalMulticast {
    Unicast u;
    int curSeq;
    PriorityQueue<String> buffer;
    boolean isSequencer;
    int sequencerCurSeq;

    public TotalMulticast(Unicast u) {
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
        //if this process is a sequencer, it must start a new thread to keep listening from other processes
        if( isSequencer ) {
            sequencerCurSeq = 0;
            sequencerStartListen();
        }
    }

    //To multicast a message, add the "TOSEQ" header and send it to sequencer
    public void multicast(String message) throws IOException, InterruptedException {
        u.unicast_send(u.hostInfo.idList.get(0), "TOSEQ||" + message);
    }

    // For processes except sequencer itself, if there is any message from sequencer, put them into priority queue.
    // For sequencer, this is implemented in sequencerListen() method.
    // Deliver a message with current sequence number if exist.
    public String deliver() {
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
                curSeq++;
                //the message in the priority queue is in the format of "FROMSEQ||seq#||sender id||message"
                return (messageSplit[2] + "||" + messageSplit[3]);
            }
        }
        return null;
    }

    public void sequencerStartListen() {
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                try {
                    sequencerListen();
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
    public void sequencerListen() throws IOException, InterruptedException {
        while(true){
            for(int i = 0; i < u.hostInfo.idList.size(); ++i){
                // if this process is sequencer and receives a message from itself,
                // it need to judge if this is a "TOSEQ" or a "FROMSEQ" message
                // for "TOSEQ" message, it should broadcast it
                // for "FROMSEQ" message, it just put it into priority queue, like a normal process
                // (for other processes, this is implemented in deliever() methods)
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
        }
    }
}
