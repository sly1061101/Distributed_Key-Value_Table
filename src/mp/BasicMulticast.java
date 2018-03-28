package mp;

import java.io.IOException;

public class BasicMulticast {
    Unicast u;

    // BasicMulticast is implemented based on Unicast layer, so it need an Unicast object to initialize.
    public BasicMulticast(Unicast u) throws IOException {
        this.u = u;
    }

    // multicast a message
    public void multicast(String message) throws IOException, InterruptedException {
        for (int i : u.hostInfo.idList)
            u.unicast_send(i, message);
    }

    // deliever() method in BasicMulticast can deliever a message if there is a message in buffer.
    // The return value is a string, in the format of "ID||message", where ID is the source of the message
    // If there is currently no message, the method will return null.
    public String deliver() {
        for (int i : u.hostInfo.idList) {
            String message;
            if( (message = u.unicast_receive(i)) != null )
                return (i + "||" + message);
        }
        return null;
    }
}