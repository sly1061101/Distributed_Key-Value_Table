package mp.TCP;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    SocketChannel client;

    public void startClient(String address, int port) throws IOException, InterruptedException {
        InetSocketAddress hostAddress = new InetSocketAddress(address, port);
        client = SocketChannel.open(hostAddress);
    }

    public void sendMessage(String message) throws IOException{
        byte [] messageByte = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(messageByte);
        client.write(buffer);
        buffer.clear();
    }

    public void closeClient() throws IOException{
        client.close();
    }
}
