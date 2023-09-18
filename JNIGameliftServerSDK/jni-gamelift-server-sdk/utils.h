#include <functional>
#include <iostream>
#include <vector>
#include  <jni.h>

jstring string_to_jstring(JNIEnv* env, std::string cppString)
{
	// convert utf string
	const char* utfString = cppString.c_str();

	// using utf8 create jstring
	jstring javaString = env->NewStringUTF(utfString);

	// return jstring object
	return javaString;
}

std::string jstring_to_string(JNIEnv* env, jstring string)
{
	const char* utfString = env->GetStringUTFChars(string, nullptr);
	if (utfString == nullptr) {
		return nullptr;
	}
	std::string cppString(utfString);
	// release resource
	env->ReleaseStringUTFChars(string, utfString);
	return cppString;
}

std::vector<std::string> java_list_to_vector(JNIEnv* env, jobject obj, jobject list)
{
	// get List object
	jclass listClass = env->GetObjectClass(list);
	// get size Method from List class
	jmethodID sizeMethod = env->GetMethodID(listClass, "size", "()I");
	// call size()
	jint size = env->CallIntMethod(list, sizeMethod);

	// get() method
	jmethodID getMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");

	// create std::vector<std::string>
	std::vector<std::string> cppVector;

	// add item from List to std::vector<std::string>
	for (int i = 0; i < size; i++) {
		jstring item = (jstring)env->CallObjectMethod(list, getMethod, i);
		const char* str = env->GetStringUTFChars(item, nullptr);
		cppVector.push_back(str);
		env->ReleaseStringUTFChars(item, str);
		env->DeleteLocalRef(item);
	}
	return cppVector;
}


std::string getEnvironmentVariable(const char* envVarName) {
	const char* envVarValue = std::getenv(envVarName);
	if (envVarValue != nullptr) {
		return std::string(envVarValue);
	}
	else {
		return "";
	}
}