#define GAMELIFT_USE_STD

#include "aws_samples_gamelift_jni_GameLiftServerSDKJNI.h"
#include  <jni.h>

#include <functional> 
#include <iostream>
#include <vector>
#include <aws/gamelift/server/GameLiftServerAPI.h>
#include "utils.h"
#include <sys/stat.h>

JavaVM* globalJavaVM;
jobject sdk_interface = nullptr;
bool mActivated = false;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	globalJavaVM = vm;
	return JNI_VERSION_1_1;
}

JNIEXPORT jint JNICALL Java_aws_samples_gamelift_jni_GameLiftServerSDKJNI_getCurrentJavaVersion
(JNIEnv*, jobject)
{
	return 6676;
}



void TerminateGameSession()
{
	Aws::GameLift::Server::ProcessEnding();
	mActivated = false;
}

void OnStartGameSession(Aws::GameLift::Server::Model::GameSession myGameSession)
{
	if (!sdk_interface)
	{
		std::cout << "can not call OnStartGameSession, SDK interface is null...\n";
		return;
	}

	if (!globalJavaVM)
	{
		std::cout << "can not call OnStartGameSession, global javaVm is null...\n";
		return;
	}

	JNIEnv* env = nullptr;
	const jint result = globalJavaVM->AttachCurrentThread((void**)&env, nullptr);
	if (result != JNI_OK) 
	{
		std::cout << "can not call OnStartGameSession, jni env is null...\n";
		return;
	}

	const jclass sdk_interface_class = env->GetObjectClass(sdk_interface);
	if (!sdk_interface_class)
	{
		std::cout << "can not call OnStartGameSession, sdk interface class not found!\n";
		globalJavaVM->DetachCurrentThread();
		return;
	}

	const jmethodID onstart_method_id = env->GetMethodID(sdk_interface_class, "onStartGameSession", "(Ljava/lang/String;Ljava/lang/String;)V");
	if (!onstart_method_id)
	{
		std::cout << "can not call OnStartGameSession, onstart_method_id not found!\n";
		globalJavaVM->DetachCurrentThread();
		return;
	}

	try
	{
		std::cout << "OnStartGameSession...\n";
		// inform gamelift start success
		Aws::GameLift::Server::ActivateGameSession();

		// inform java start serving
		std::string gameSessionId = myGameSession.GetGameSessionId();
		std::string gameSessionData = myGameSession.GetGameSessionData();
		jstring jGamesessionId = string_to_jstring(env, gameSessionId);
		jstring jGameSessionData = string_to_jstring(env, gameSessionData);
		env->CallVoidMethod(sdk_interface, onstart_method_id, jGamesessionId, jGameSessionData);
		std::cout << "OnStartGameSession Success!\n";

		// release thread
		globalJavaVM->DetachCurrentThread();
	}
	catch (int exception)
	{
		std::cout << "[OnStartGameSession] Exception Code: " << exception << "\n";
		// release thread
		globalJavaVM->DetachCurrentThread();
	}
}

void OnProcessTerminate()
{
	if (!mActivated)
	{
		std::cout << "can not call OnProcessTerminate, not active...\n";
		return;
	}

	if (!sdk_interface)
	{
		std::cout << "can not call OnProcessTerminate, SDK interface is null...\n";
		return;
	}

	if (!globalJavaVM)
	{
		std::cout << "can not call OnProcessTerminate, global javaVm is null...\n";
		return;
	}

	// get JNIEnv
	JNIEnv* env = nullptr;
	const jint result = globalJavaVM->AttachCurrentThread((void**)&env, nullptr);
	if (result != JNI_OK)
	{
		std::cout << "can not call OnProcessTerminate, jni env is null...\n";
		return;
	}

	const jclass sdk_interface_class = env->GetObjectClass(sdk_interface);
	if (!sdk_interface_class)
	{
		std::cout << "can not call OnProcessTerminate, sdk interface class not found!\n";
		globalJavaVM->DetachCurrentThread();
		return;
	}

	const jmethodID on_process_terminate_method_id = env->GetMethodID(sdk_interface_class, "onProcessTerminate", "()V");
	if (!on_process_terminate_method_id)
	{
		std::cout << "can not call OnProcessTerminate, on_process_terminate_method_id not found!\n";
		globalJavaVM->DetachCurrentThread();
		return;
	}

	try
	{
		std::cout << "OnProcessTerminate...\n";
		// invoke onProcessTerminate
		env->CallVoidMethod(sdk_interface, on_process_terminate_method_id);

		//close session
		TerminateGameSession();
		std::cout << "OnProcessTerminate Finish!\n";

		// release thread
		globalJavaVM->DetachCurrentThread();
	}
	catch (int exception)
	{
		std::cout << "Exception Code: " << exception << "\n";
		// release thread
		globalJavaVM->DetachCurrentThread();
	}
}

bool OnHealthCheck()
{
	return mActivated;
}

void OnUpdateGameSession()
{
}

JNIEXPORT jboolean JNICALL Java_aws_samples_gamelift_jni_GameLiftServerSDKJNI_initGameLift
(JNIEnv* env, jobject obj, jint port, jobject log_path_list, jobject sinterface)
{
	printf("Native Log: start init gamelift on port: %d\n", port);
	std::string logfile = std::string("logs/myserver");
	logfile += std::to_string(port) + ".log";
	freopen(logfile.c_str(), "a", stdout);
	freopen(logfile.c_str(), "a", stderr);
	if (chmod(logfile.c_str(), 777) != 0) {
        std::cerr << "Failed to set file permissions." << std::endl;
        return 1;
    }
	std::cout << "Server port: " << port << std::endl;

	sdk_interface = env->NewGlobalRef(sinterface);
	if (!sdk_interface)
	{
		std::cout << "InitGameLift, sdk interface is null.\n";
		return false;
	}

	try
	{
		std::cout << "Init GameLift SDK...\n";
		
		const char* envGameLiftMode = "GAMELIFT_MODE";
		std::string gameLiftMode = getEnvironmentVariable(envGameLiftMode);
		std::cout << "InitGameLift, GameLift Mode: " << gameLiftMode << "\n";

		Aws::GameLift::Server::InitSDKOutcome initSdkOutcome;
		if (gameLiftMode == "ANYWHERE") {
			const char* envWebsocketUrl = "WEBSOCKET_URL";
			const char* envProcessId = "PROCESS_ID";
			const char* envFleetId = "FLEET_ID";
			const char* envHostId = "HOST_ID";
			const char* envAuthToken = "AUTH_TOKEN";
			std::string websocketUrl = getEnvironmentVariable(envWebsocketUrl);
			std::string processId = getEnvironmentVariable(envProcessId);
			std::string fleetId = getEnvironmentVariable(envFleetId);
			std::string hostId = getEnvironmentVariable(envHostId);
			std::string authToken = getEnvironmentVariable(envAuthToken);
			std::cout << "websocketUrl " << websocketUrl << "\n";
			std::cout << "processId " << processId << "\n";
			std::cout << "fleetId " << fleetId << "\n";
			std::cout << "hostId " << hostId << "\n";
			std::cout << "authToken " << authToken << "\n";

			if (websocketUrl.empty() || processId.empty() || fleetId.empty() || hostId.empty() || authToken.empty()) {
				throw std::runtime_error("One or more environment variables are empty.");
			}

			Aws::GameLift::Server::Model::ServerParameters serverParameters =
				Aws::GameLift::Server::Model::ServerParameters(websocketUrl, authToken, fleetId, hostId, processId);

			//Call InitSDK to establish a local connection with the GameLift agent to enable further communication.
			initSdkOutcome = Aws::GameLift::Server::InitSDK(serverParameters);
		}
		else {
			initSdkOutcome = Aws::GameLift::Server::InitSDK();
		}
		//Define the server parameters
		
		if (!initSdkOutcome.IsSuccess())
		{
			Aws::GameLift::GameLiftError error = initSdkOutcome.GetError();
			std::cout << "InitGameLift,  failed!, error " << error.GetErrorName() << ", message " + error.GetErrorMessage() << "\n";
			return false;
		}

		std::cout << "Init GameLift SDK Done!\n";

		// Set parameters and call ProcessReady
		std::vector<std::string> log_paths = java_list_to_vector(env, obj, log_path_list);
		auto processReadyParameter = Aws::GameLift::Server::ProcessParameters(
			&OnStartGameSession,
			&OnProcessTerminate,
			&OnHealthCheck,
			port,
			Aws::GameLift::Server::LogParameters(log_paths)
		);

		std::cout << "InitGameLift, Process Ready...\n";
		auto readyOutcome = Aws::GameLift::Server::ProcessReady(processReadyParameter);
		if (!readyOutcome.IsSuccess())
		{
			Aws::GameLift::GameLiftError error = readyOutcome.GetError();
			std::cout << "InitGameLift,  ProcessReady failed!, error " << error.GetErrorName() << ", message " + error.GetErrorMessage() << "\n";
			return false;
		}

		std::cout << "InitGameLift, Process Ready Done!\n";
		mActivated = true;
		return true;
	}
	catch (int exception)
	{
		std::cout << "InitGameLift, Exception Code: " << exception << "\n";
		return false;
	}
}

JNIEXPORT void JNICALL Java_aws_samples_gamelift_jni_GameLiftServerSDKJNI_terminateGameSession(JNIEnv* env, jobject obj)
{
	TerminateGameSession();
}