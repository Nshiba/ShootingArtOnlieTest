package netprotest1;

import scketChanelTest.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class ChannelServ {
    public static final int PORT = 5000;
    public static final int BUF_SIZE = 1000;

    private Selector selector;

    public static void main(String[] args){
        new ChannelServ().run();
    }

    public void run() {
        ServerSocketChannel serverSocketChannel = null;
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NonBlockingModeで起動しました。port=" + serverSocketChannel.socket().getLocalPort());

            while (selector.select() > 0) {                
                for (Iterator iterator = selector.selectedKeys().iterator(); iterator.hasNext();) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        doAccept((ServerSocketChannel) key.channel());
                    }else if(key.isReadable()){
                        doRead((SocketChannel) key.channel());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(serverSocketChannel != null && serverSocketChannel.isOpen()){
                try {
                    System.out.println("NonblockingModeを終了し停止します。");
                    serverSocketChannel.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void doAccept(ServerSocketChannel serverSocketChannel){
        try {
            SocketChannel channel = serverSocketChannel.accept();
            String remoteAddress = channel.socket().getRemoteSocketAddress().toString();

            System.out.println(remoteAddress + " : [接続されました]");

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRead(SocketChannel channel){
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
        Charset charset = Charset.forName("UTF-8");
        String remoteAddress = channel.socket().getRemoteSocketAddress().toString();

        try {
            if (channel.read(buf) < 0) {
                return;
            }
            buf.flip();
            System.out.println(remoteAddress + " : " + charset.decode(buf).toString());
            buf.flip();
            channel.write(charset.encode(CharBuffer.wrap(charset.decode(buf).toString() + "送り返します")));
            channel.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println(remoteAddress + " : [切断しました]");
            try {
                channel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
