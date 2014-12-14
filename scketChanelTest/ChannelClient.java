package scketChanelTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by naoya on 14/12/14.
 */
public class ChannelClient {
    public static final int PORT = 5000;
    public static final int BUF_SIZE = 5000;

    public static void main(String[] args){
        SocketChannel channel = null;
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
        Charset charset = Charset.forName("UTF-8");
        BufferedReader keyin = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("接続するIPを入力してください");
            channel = SocketChannel.open(new InetSocketAddress(keyin.readLine(), PORT));
            System.out.println("送信 : ");
            String line = keyin.readLine();
            channel.write(charset.encode(CharBuffer.wrap(line + "\n")));

            while (channel.isConnected()){
                buf.clear();
                if (channel.read(buf) < 0){
                    return;
                }
                buf.flip();
                System.out.println("受信 : " + charset.decode(buf).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel != null && channel.isOpen()){
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
