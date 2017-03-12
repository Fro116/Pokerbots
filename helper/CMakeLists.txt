cmake_minimum_required(VERSION 2.8.11)
project(pokerbots)
add_executable (pokerbots main.cpp Card.cpp Random.cpp)
SET(CMAKE_CXX_FLAGS "-O3 -std=c++14 -pedantic -Wall -Wextra -Wcast-align -Wcast-qual -Wctor-dtor-privacy -Wdisabled-optimization -Wformat=2 -Winit-self -Wmissing-declarations -Wmissing-include-dirs -Wold-style-cast -Woverloaded-virtual -Wredundant-decls -Wshadow -Wsign-conversion -Wsign-promo -Wstrict-overflow=5 -Wswitch-default -Wundef -Werror -Wno-unused -Wno-unused-parameter")

find_library(PBOT pbots_calc)
target_link_libraries(pokerbots ${PBOT})
find_library(PEVAL poker-eval)
target_link_libraries(pokerbots ${PEVAL})

