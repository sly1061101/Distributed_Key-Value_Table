package mp;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BasicMulticast {
    Unicast u;

    public BasicMulticast(Unicast u) throws IOException {
        this.u = u;
    }

    public void multicast(String message) throws IOException, InterruptedException {
        for (int i : u.hostInfo.idList)
            u.unicast_send(i, message);
    }

    public String deliever() {
        for (int i : u.hostInfo.idList) {
            String message;
            if( (message = u.unicast_receive(i)) != null )
                return (i + "||" + message);
        }
        return null;
    }
}