#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>

#include "snapper.grpc.pb.h"
#include "snapper.pb.h"

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;

using snapper::Resolution;
using snapper::Snapper;
using snapper::Chunk;

//class SnapperSnapshotImpl final : public Snapper::Service
//{
//  Status Snapshot(ServerContext* context,
//                  const Resolution* resolution,
//                  Reply* reply) override
//  {
//    std::cout << "resolution is: " << resolution->width() << "x" <<
//                 resolution->height() <<std::endl;
//    system("v4l2-ctl --stream-mmap=3 --stream-count=1 --stream-to=somefile.jpg");
//    return Status::OK;
//  }
//};



class SnapperSnapshotImpl final : public Snapper::Service
{
    Status Snapshot(grpc::ServerContext *context,
                    const snapper::Resolution *request,
                    ::grpc::ServerWriter<Chunk> *writer) override
    {
        Chunk chunk;

//        system("v4l2-ctl --stream-mmap=3 --stream-count=1 --stream-to=somefile.jpg");

        chunk.set_content(std::string("this is test content V2").c_str());

        writer->Write(chunk);

        return Status::OK;
    }
};




void RunServer() {
  std::string server_address("0.0.0.0:50051");


  ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());


  SnapperSnapshotImpl snapshotService;
  builder.RegisterService(&snapshotService);

  std::unique_ptr<Server> server(builder.BuildAndStart());
  std::cout << "Server listening on " << server_address << std::endl;

  server->Wait();
}

int main(int argc, char** argv) {
  RunServer();

  return 0;
} 
