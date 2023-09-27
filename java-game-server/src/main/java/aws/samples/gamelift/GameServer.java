package aws.samples.gamelift;

import aws.samples.gamelift.jni.GameLiftServerSDKJNI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GameServer {

    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);

    private volatile static GameServer gameServer;

    private GameServer (){}
    private static Selector selector = null;

    public static GameServer getInstance() {
        if (gameServer == null) {
            synchronized (GameServer.class) {
                if (gameServer == null) {
                    gameServer = new GameServer();
                }
            }
        }
        return gameServer;
    }

    public void start() {
        try {
            selector = Selector.open();
            // We have to set connection host,port and
            // non-blocking mode
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress("0.0.0.0", 0));
            serverSocketChannel.configureBlocking(false);
            int ops = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, ops, null);
            int port = serverSocket.getLocalPort();
            logger.info("Server started on port: {}", port);

            GameLiftServerSDKJNI gameLiftServerSDKJNI = new GameLiftServerSDKJNI();
            int version = gameLiftServerSDKJNI.getCurrentJavaVersion();
            logger.info("current version: {}", version);
            List<String> logPaths = List.of("logs/aws_" + port + ".log");
            boolean success = gameLiftServerSDKJNI.initGameLift(port, logPaths, new GameLiftServerSDKJNI.SdkInterface() {
                @Override
                public void onStartGameSession(String gameSessionId, String gameSessionData) {
                    logger.info("Game Session Starting, sessionID:{}, session data:{}", gameSessionId, gameSessionData);
                }
                @Override
                public void onProcessTerminate() {
                    logger.info("game session terminated");
                }
            });
            if (!success) {
                logger.error("init gameLift sdk failed!");
                return;
            } else {
                logger.info("Game Process success started on port: {}", port);
            }

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> i = selectedKeys.iterator();

                while (i.hasNext()) {
                    SelectionKey key = i.next();
                    if (key.isAcceptable()) {
                        // New client has been  accepted
                        handleAccept(serverSocketChannel, key);
                    } else if (key.isReadable()) {
                        // We can run non-blocking operation
                        // READ on our client
                        handleRead(key);
                    }
                    i.remove();
                }
            }
        } catch (IOException e) {
            logger.error("game server started failed", e);
            e.printStackTrace();
        }
    }

    private static void handleAccept(ServerSocketChannel mySocket, SelectionKey key) throws IOException {

        logger.info("Connection Accepted..");
        // Accept the connection and set non-blocking mode
        SocketChannel client = mySocket.accept();
        client.configureBlocking(false);
        // Register that client is reading this channel
        client.register(selector, SelectionKey.OP_READ);
    }

    private static void handleRead(SelectionKey key) throws IOException {
        logger.info("Reading client's message.");
        // create a ServerSocketChannel to read the request
        SocketChannel client = (SocketChannel) key.channel();
        // Create buffer to read data
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);
        // Parse data from buffer to String
        String data = new String(buffer.array()).trim();
        if (!data.isEmpty()) {
            logger.info("Received message: {}", data);
        }
    }
}
