package mp;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BasicMulticast{
    Unicast u;
    Config config;
    int ID;

    public BasicMulticast(Unicast u) throws IOException {
        this.u = u;
        this.config = Config.parseConfig("configFile");
        this.ID = u.ID;
    }

    public void multicast(String message) throws IOException, InterruptedException {
        for (int i : config.idList) {
            if (i != this.ID) {
                u.unicast_send(i, message);
            }
        }
    }
}