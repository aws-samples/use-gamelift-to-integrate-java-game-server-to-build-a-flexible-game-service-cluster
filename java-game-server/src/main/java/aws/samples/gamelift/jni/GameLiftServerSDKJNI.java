package aws.samples.gamelift.jni;

import aws.samples.gamelift.utils.LibLoader;

import java.util.List;

/**
 * javac -h . GameLiftServerSDKJNI.java
 */
public class GameLiftServerSDKJNI {

    static {
        String tmpPath = "gameLiftSdk";
        String libName = LibLoader.determineLibName("JNIGameLiftServerSDK");
        LibLoader.loadLib(GameLiftServerSDKJNI.class, tmpPath, libName);
        System.out.printf("load library from %s, lib name is %s%n", GameLiftServerSDKJNI.class, libName);
    }

    public interface SdkInterface
    {
        void onStartGameSession(String gameSessionId, String gameSessionData);

        void onProcessTerminate();
    }

    /**
     * get current version
     * @return
     */
    public native int getCurrentJavaVersion();

    /**
     * Initial GameLift SDK
     * @param port
     * @param logPaths
     * @param sdkInterface
     * @return
     */
    public native boolean initGameLift(int port, List<String> logPaths, SdkInterface sdkInterface);

    /**
     * Manually Terminate Game Session
     */
    public native void terminateGameSession();

}
