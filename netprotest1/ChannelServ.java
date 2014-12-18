package netprotest1;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import static netprotest1.StaticData.*;

public class ChannelServ {

    private Selector selector;

    private ArrayList<PlayerBean> players;

    public static void main(String[] args){
//        new ChannelServ().run();
    }

    public void run() {
        ServerSocketChannel serverSocketChannel = null;
        try {
            //セレクタの初期化、ノンブロッキングモード指定、ポートのバインド`
            //新規接続のチャネルなのでOP_ACCEPT
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NonBlockingModeで起動しました。port=" + serverSocketChannel.socket().getLocalPort());

            //利用可能なチャネルがどの操作が可能なチャネルであるか判定し処理を分岐
            while (selector.select() > 0) {                
                for (Iterator iterator = selector.selectedKeys().iterator(); iterator.hasNext();) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    
                    //新規接続受付処理が可能の場合
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
                    e.printStackTrace();
                }
            }
        }
    }

    private void doAccept(ServerSocketChannel serverSocketChannel){
        try {
            //接続の待受
            SocketChannel channel = serverSocketChannel.accept();

            String remoteAddress = channel.socket().getRemoteSocketAddress().toString();
            System.out.println(remoteAddress + " : [接続されました]");

            //ノンブロッキングモード
            //channelに登録
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRead(SocketChannel channel){
        //バッファを初期化、エンコード指定
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
        Charset charset = Charset.forName("UTF-8");

        String remoteAddress = channel.socket().getRemoteSocketAddress().toString();

        try {
            if (channel.read(buf) < 0) {
                return;
            }
            buf.flip();

            String bufContent = charset.decode(buf).toString();
            System.out.println(remoteAddress + " : " + bufContent);

            //channelに文字列を書き込み
            doSent(channel, "送り返します : " + bufContent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSent(SocketChannel channel, String content){
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
        Charset charset = Charset.forName("UTF-8");

        try {
            channel.write(charset.encode(CharBuffer.wrap(content)));
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
}
