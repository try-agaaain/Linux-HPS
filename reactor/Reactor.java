import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.Iterator;
import java.nio.channels.SelectionKey;

public class Reactor {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(1234));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            Set<java.nio.channels.SelectionKey> keys = selector.selectedKeys();
            Iterator<java.nio.channels.SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = acceptServerSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    System.out.println("accept from "+socketChannel.socket().getInetAddress().toString());
                  //  LOGGER.info("Accept request from {}", socketChannel.getRemoteAddress());
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable() && key.isValid()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = socketChannel.read(buffer);
                    if (count <= 0) {
                        socketChannel.close();
                        key.cancel();
                        System.out.println("Received invalide data, close the connection");
                        //LOGGER.info("Received invalide data, close the connection");
                        continue;
                    }
                    System.out.println("Received message"+new String(buffer.array()));
                    //LOGGER.info("Received message {}", new String(buffer.array()));
                }
                keys.remove(key);
            }
        }
    }
}
