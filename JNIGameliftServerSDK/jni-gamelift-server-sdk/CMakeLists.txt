# CMakeList.txt: linux_camke_test 的 CMake 项目，在此处包括源代码并定义
# 项目特定的逻辑。
#
CMAKE_MINIMUM_REQUIRED(VERSION 3.27)
set(CMAKE_CXX_STANDARD 11)

# 打印信息
message(STATUS "This is SOURCE dir " ${PROJECT_SOURCE_DIR})
message(STATUS "This is BINARY dir " ${PROJECT_BINARY_DIR})
message(STATUS "This is CMAKE SOURCE dir " ${CMAKE_SOURCE_DIR})

set(ROOT_DIR ${CMAKE_SOURCE_DIR})

if(WIN32)
	# windows路径配置
	# 设置jni依赖路径
	set(USED_JDK_LIB "$ENV{JAVA_HOME}/lib")

	# 设置依赖的头文件路径
	set(TARGET_INCULDES 
		# jni头文件
		"$ENV{JAVA_HOME}/include" 
		"$ENV{JAVA_HOME}/include/win32"
		# aws头文件
		"${ROOT_DIR}/aws/include"
		# openssl头文件
		"$ENV{OPENSSL_INCLUDE_DIR}"
	)
	
	# 设置库依赖路径
	set(TARGET_LIBS_PATH 
		# jni库路径
		 "$ENV{JAVA_HOME}/lib"
		 # aws库路径
		 "${ROOT_DIR}/aws/lib"
		 # openssl 库路径
		 "$ENV{OPENSSL_LIBRARIES}"
	)

	# 设置库文件路径
	set(TARGET_LIBS_FILES 
		# aws libs
		"aws-cpp-sdk-gamelift-server.lib" 
		# openssl libs
		"libcrypto.lib" 
		"libssl.lib"
	)
else()
	# 设置jni依赖路径
	set(USED_JDK_HOME "$ENV{JAVA_HOME}")
    if(USED_JDK_HOME STREQUAL "")
        set(USED_JDK_HOME "/usr/lib/jvm/java-11-amazon-corretto.x86_64")
    endif()
    set(USED_JDK_LIB "${USED_JDK_HOME}/lib")

    # 设置依赖的头文件路径
    set(TARGET_INCULDES
        # jni头文件
        "${USED_JDK_HOME}/include"
        "${USED_JDK_HOME}/include/linux"
        # aws头文件
        "${ROOT_DIR}/sdk/include"
        # openssl头文件
        "/usr/include/openssl"

    )
    # 设置库依赖路径
    set(TARGET_LIBS_PATH
        # jni库路径
         "${USED_JDK_HOME}/lib"
         # aws库路径
         "${ROOT_DIR}/sdk/lib"
         # openssl 库路径
         "/usr/lib64"
    )

    # 设置库文件文件
    set(TARGET_LIBS_FILES
        # sdk libs
        "aws-cpp-sdk-gamelift-server.a"
        # openssl libs
        "libcrypto.so"
        "libssl.so"
    )
    # 设置pthread
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
endif()


# 输出文件路径
message(STATUS "This is USED_JDK_LIB " "${USED_JDK_LIB}")
message(STATUS "This is TARGET_INCULDES " "${TARGET_INCULDES}")
message(STATUS "This is TARGET_LIBS_FILES " "${TARGET_LIBS_FILES}")

# 指定库名称
set(LIBRARY_NAME "JNIGameLiftServerSDK")
# 将所有源文件添加到DIR_SRCS目录中
aux_source_directory(. DIR_SRCS)

# 将源代码添加到此项目的可执行文件。
#add_executable (${LIBRARY_NAME} "${DIR_SRCS}")
add_library (${LIBRARY_NAME}  SHARED ${DIR_SRCS})

# 添加头文件
target_include_directories(${LIBRARY_NAME}  PUBLIC "${TARGET_INCULDES}")

# 添加库路径
target_link_directories(${LIBRARY_NAME}  PUBLIC ${TARGET_LIBS_PATH})

# 添加链接库
target_link_libraries(${LIBRARY_NAME}  PUBLIC "${TARGET_LIBS_FILES}")	

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET ${LIBRARY_NAME}  PROPERTY CXX_STANDARD 20)
endif()
