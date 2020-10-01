cmake_minimum_required(VERSION 3.8)
set(CMAKE_CXX_FLAGS -std=c++2a)
add_library( # Specifies the name of the library.
             crosswordBuilder

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/Crossword.cpp src/main/cpp/CrosswordBuilder.cpp src/main/cpp/LimitKeeper.cpp src/main/cpp/main.cpp src/main/cpp/WordRandomizer.cpp src/main/cpp/Wrapper.cpp)
