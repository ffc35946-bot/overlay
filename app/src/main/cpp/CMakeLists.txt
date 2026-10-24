cmake_minimum_required(VERSION 3.10.2)
project("native-lib")

add_library(native-lib SHARED native-lib.cpp)
target_compile_features(native-lib PRIVATE cxx_std_11)
find_library(log-lib log)
target_link_libraries(native-lib ${log-lib})
