# JNIGameliftServerSDK

### Linux

1. Create the build directory.
   ```
   mkdir cmake-build
   ```
2. Make the solution and project files.
   ```
   cmake -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=Release -S . -B ./cmake-build
   ```
3. Compile the solution.
   ```
   cmake --build cmake-build --target all
   ```
