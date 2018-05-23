cmake_minimum_required( VERSION 3.9.6 )
project( SNAPPER )

execute_process(COMMAND which grpc_cpp_plugin OUTPUT_VARIABLE GRPC_CPP_PLUGIN_DIR)
string(STRIP ${GRPC_CPP_PLUGIN_DIR} GRPC_CPP_PLUGIN_DIR)

execute_process(COMMAND protoc -I${PROJECT_SOURCE_DIR} --grpc_out=. --plugin=protoc-gen-grpc=${GRPC_CPP_PLUGIN_DIR} --proto_path=../../../common snapper.proto)
execute_process(COMMAND protoc -I${PROJECT_SOURCE_DIR} --cpp_out=. --proto_path=../../../common snapper.proto)

include_directories( SYSTEM grpc++ )
include_directories( ${CMAKE_CURRENT_BINARY_DIR} )

link_libraries( grpc++_unsecure grpc protobuf )

add_executable( snapper-client client.cpp snapper.grpc.pb.cc snapper.pb.cc )
install( TARGETS snapper-client DESTINATION bin)
