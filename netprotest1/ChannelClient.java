package netprotest1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ChannelClient {
    public static final int PORT = 5000;
    public static final int BUF_SIZE = 5000;
    private static Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args){
        new ChannelClient().run();
    }

    public void run(){
        ///////////////////////////////////////
        PlayerBean bean = new PlayerBean();
        bean.setBulletType(1);
        bean.setX(0);

        bean.setY(0);
        bean.setBulletBool(false);
        ///////////////////////////////////////

        SocketChannel channel = null;
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
        BufferedReader keyin = new BufferedReader(new InputStreamReader(System.in));
        try {

                System.out.println("接続するIPを入力してください");
                channel = SocketChannel.open(new InetSocketAddress(keyin.readLine(), PORT));
            while (true) {                

                System.out.println("送信する文字列を入力してください : ");
                String line = keyin.readLine();

                //channelに文字列を書き込み
                doSent(channel, line + "\n");

                while (channel.isConnected()){
                    buf.clear();
                    if (channel.read(buf) < 0){
                        return;
                    }
                    buf.flip();
                    doRead(channel, buf);
                }

                channel.close();
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

    private void doRead(SocketChannel channel, ByteBuffer buf){
        String remoteAddress = channel.socket().getRemoteSocketAddress().toString();

        try {
            String bufContent = charset.decode(buf).toString();
            System.out.println(remoteAddress + " : " + bufContent);

                    channel.close();
        } catch (Exception e) {
            e.printStackTrace();
//        } finally {
//            if (channel != null && channel.isOpen()){
//                try {
//                    channel.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    private void doSent(SocketChannel channel, String content){
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);

        try {
            channel.write(charset.encode(CharBuffer.wrap(content)));
        } catch (Exception e) {
                e.printStackTrace();
//        } finally {
//            if (channel != null && channel.isOpen()){
//                try {
//                    channel.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}