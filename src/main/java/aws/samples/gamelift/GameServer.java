package aws.samples.gamelift;

import aws.samples.gamelift.jni.GameLiftServerSDKJNI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

public class GameServer {

    private static final Logger logger
            = LoggerFactory.getLogger(GameServer.class);

    private volatile static GameServer gameServer;

    private GameServer (){}
    private ServerSocketChannel serverChannel;

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

    private int bind() {
        try {
            serverChannel = ServerSocketChannel.open();
            InetSocketAddress socketAddress = new InetSocketAddress("localhost", 0);
            serverChannel.bind(socketAddress);
            serverChannel.configureBlocking(false); // non-blocking
            int port = serverChannel.socket().getLocalPort();
            logger.info("Server started on port: {}", port);
            return port;
        } catch (Exception e) {
            logger.error("bind server port failure", e);
        }
        logger.error("bind server port failure");
        throw new RuntimeException("bind failure");
    }

    public void start() {
        int port = bind();
        GameLiftServerSDKJNI gameLiftServerSDKJNI = new GameLiftServerSDKJNI();
        int version = gameLiftServerSDKJNI.getCurrentJavaVersion();
        logger.info("current version: {}", version);

        List<String> logPaths = List.of("logs/aws_" + port + ".log");
        boolean success = gameLiftServerSDKJNI.initGameLift(port, logPaths, new GameLiftServerSDKJNI.SdkInterface() {
            @Override
            public void onStartGameSession(String gameSessionId, String gameSessionData) {

                logger.info("Game Session Starting, sessionID:{}, session data:{}", gameSessionId, gameSessionData);

                SocketChannel clientChannel = null;
                try {
                    clientChannel = serverChannel.accept();

                    if (clientChannel != null) {
                        System.out.println("Accepted connection from: " + clientChannel.getRemoteAddress());

                        // 向客户端发送消息
                        String message = "Hello, client!";
                        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                        clientChannel.write(buffer);
                        clientChannel.close(); // 关闭客户端连接
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
            logger.info("Game Session success started on port: {}", port);
        }

        try {
            Thread.sleep(10*60*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        gameLiftServerSDKJNI.terminateGameSession();
    }


}
