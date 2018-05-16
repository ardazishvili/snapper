cmake_minimum_required( VERSION 3.9.6 )
project( SNAPPER )

execute_process(COMMAND which grpc_cpp_plugin OUTPUT_VARIABLE FOO)
string(STRIP ${FOO} FOO)

execute_process(COMMAND protoc -I${PROJECT_SOURCE_DIR} --grpc_out=. --plugin=protoc-gen-grpc=${FOO} helloworld.proto)
execute_process(COMMAND protoc -I${PROJECT_SOURCE_DIR} --cpp_out=. helloworld.proto)

include_directories( SYSTEM grpc++ )
include_directories( ${CMAKE_CURRENT_BINARY_DIR} )

link_libraries( grpc++_unsecure grpc protobuf )

add_executable( snapper snapper.cpp helloworld.grpc.pb.cc helloworld.pb.cc )
install( TARGETS snapper DESTINATION bin)
