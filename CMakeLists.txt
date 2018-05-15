cmake_minimum_required( VERSION 3.9.6 )
project( SNAPPER )

add_executable( snapper snapper.cpp )
install( TARGETS snapper DESTINATION bin)
