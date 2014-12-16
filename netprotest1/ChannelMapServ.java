/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netprotest1;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import sun.misc.HexDumpEncoder;

/**
 *
 * @author naoya
 */
public class ChannelMapServ extends Thread {
    private static int PORT = 5000;
    private static int BUF_SIZE = 1024;

    private LinkedList<SocketChannel> channelList = new LinkedList<>();

    private Map<SocketChannel, ByteArrayOutputStream> bufferMap = new HashMap<>();
    
    private Selector selector = null;

    public static void main(String[] args){
        new ChannelMapServ().start();
    }

    @Override
    public void run(){
        ServerSocketChannel serverSocketChannel = null;

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("サーバー起動");

            while(selector.select() > 0) {
                Iterator<SelectionKey> keyIt = selector.selectedKeys().iterator();

                while(keyIt.hasNext()) {
                    SelectionKey key = keyIt.next();
                    keyIt.remove();

                    if (key.isAcceptable()) {
                        doAccept((ServerSocketChannel) key.channel());
                    } else if (key.isReadable()) {
                        doRead((SocketChannel) key.channel());
                    } else if (key.isWritable()) {
                        doWrite((SocketChannel) key.channel());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                System.out.println("サーバー停止");
                serverSocketChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doAccept(ServerSocketChannel acceptServerChannel){
        try {
            SocketChannel channel = acceptServerChannel.accept();
            System.out.println("Accesptに入りました" + channel);
            channel.configureBlocking(false);

            channel .register(selector, SelectionKey.OP_READ);

            channelList.add(channel);

            String remoteAddr = channel.socket().getRemoteSocketAddress().toString();
            System.out.println("接続されました : " + remoteAddr);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRead(SocketChannel channel){
        try {
            String remoteAddr = channel.socket().getRemoteSocketAddress().toString();

            ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);

            if (channel.read(buf) > 0) {
                buf.flip();

                byte[] bytes = new byte[buf.limit()];
                buf.get(bytes);

                System.out.println("受信しました : " + remoteAddr);

                //ログをダンプ
                HexDumpEncoder hex = new HexDumpEncoder();
                System.out.println(hex.encode(bytes));
                //////////////////////////////////////////////

                for (SocketChannel client : channelList) {
                    System.out.println("送ります" + client);
                    
                    ByteArrayOutputStream bout = bufferMap.get(client);

                    if (bout == null) {
                        bout = new ByteArrayOutputStream();
                        bufferMap.put(client, bout);
                    }
                    bout.write(bytes);

                    //宛先が書込可能になるのを監視する
                    client.register(selector, SelectionKey.OP_WRITE);
                }
            }
        } catch (Exception e) {
            //Socketが切断されたらログアウト
            logout(channel);
        }

    }

    private void doWrite(SocketChannel channel){
        ByteArrayOutputStream bout = bufferMap.get(channel);

        if(bout != null) {
            System.out.println("書込します");

            try {
                ByteBuffer bbuf = ByteBuffer.wrap(bout.toByteArray());
                int size = channel.write(bbuf);

                System.out.println("送信サイズ : " + size + "/" + bbuf.limit());

                if (bbuf.hasRemaining()) {
                    //bbufの中を送信しきれなかった場合、残りのBufferMapに書き戻し
                    ByteArrayOutputStream rest = new ByteArrayOutputStream();
                    rest.write(bbuf.array(), bbuf.position(), bbuf.remaining());
                    bufferMap.put(channel, rest);

                    //宛先が書込可能になるのを監視
                    //宛先が切断された時の為に読込も監視
                    channel.register(selector, SelectionKey.OP_WRITE + SelectionKey.OP_READ);
                } else {
                    //bbufの送信がすべて完了したのでbufferMapから今回送信した分を削除
                    bufferMap.remove(channel);
                    //宛先が書込可能になるのを監視するのをやめる
                    channel.register(selector, SelectionKey.OP_READ);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void logout(SocketChannel channel) {
        String remoteAddr = channel.socket().getRemoteSocketAddress().toString();
        System.out.println("ログアウト : " + remoteAddr);

        try {
                channel.finishConnect();
                channel.close();

                if (channelList.remove(channel)) {
                    System.out.println("リストから" + channel + "を削除しました");
                } else {
                    System.out.println("リストから" + channel + "の削除を失敗しました");
                }
        } catch (Exception e) {
            System.out.println("channelの切断に失敗しました。");
            e.printStackTrace();
            e = null;

            return;
        }

        System.out.println("channelを切断しました。");
    }
}