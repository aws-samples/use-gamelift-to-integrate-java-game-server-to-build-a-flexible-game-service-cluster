package aws.samples.gamelift;

public class Main {
    public static void main(String[] args) {
        GameServer gameServer = GameServer.getInstance();
        gameServer.start();
    }
}