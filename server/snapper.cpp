#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>

#include "snapper.grpc.pb.h"

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;

using snapper::Resolution;
using snapper::Reply;
using snapper::Snapper;

class SnapperServiceImpl final : public Snapper::Service
{
  Status Snapshot(ServerContext* context,
                  const Resolution* resolution,
                  Reply* reply) override
  {
    std::cout << "resolution is: " << resolution->width() << "x" <<
                 resolution->height() <<std::endl;
    return Status::OK;
  }
};

void RunServer() {
  std::string server_address("0.0.0.0:50051");
  SnapperServiceImpl service;

  ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
  builder.RegisterService(&service);
  std::unique_ptr<Server> server(builder.BuildAndStart());
  std::cout << "Server listening on " << server_address << std::endl;

  server->Wait();
}

int main(int argc, char** argv) {
  RunServer();

  return 0;
}
