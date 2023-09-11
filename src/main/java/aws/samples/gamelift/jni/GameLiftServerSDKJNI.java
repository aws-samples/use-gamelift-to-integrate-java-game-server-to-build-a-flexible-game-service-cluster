package aws.samples.gamelift.jni;

import aws.samples.gamelift.utils.LibLoader;

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
        void onStartGameSession();

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
     * @param logPath
     * @param sdkInterface
     * @return
     */
    public native boolean initGameLift(int port, String logPath, SdkInterface sdkInterface);

    /**
     * Manually Terminate Game Session
     */
    public native void terminateGameSession();

}
