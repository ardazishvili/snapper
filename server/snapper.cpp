#include <iostream>
#include <memory>
#include <string>
#include <fstream>

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
using snapper::Reply;
//system("v4l2-ctl --stream-mmap=3 --stream-count=1 --stream-to=somefile.jpg");



class SnapperImpl final : public Snapper::Service
{
    Status Snapshot(grpc::ServerContext *context,
                    const snapper::Resolution *request,
                    ::grpc::ServerWriter<Chunk> *writer) override
    {
        Chunk chunk;

        std::ifstream file("/home/root/somefile.jpg", std::ifstream::binary);
        char buffer[128];

        std::streamsize dataSize;
        while (!file.eof())
        {

            file.read(buffer, 128);
            dataSize = file.gcount();

            chunk.set_content(buffer, dataSize);
            writer->Write(chunk);

        }

        return Status::OK;
    }


    Status SetResolution(grpc::ServerContext *context,
                         const snapper::Resolution *request,
                         snapper::Reply *response) override
    {
        std::cout << "attempt to set resolution " << request->width() << "x" << request->height() << std::endl;

        return Status::OK;
    }
};



void RunServer() {
  std::string server_address("0.0.0.0:50051");


  ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());


  SnapperImpl snapperService;
  builder.RegisterService(&snapperService);

  std::unique_ptr<Server> server(builder.BuildAndStart());
  std::cout << "Server listening on1 " << server_address << std::endl;

  server->Wait();
}

int main(int argc, char** argv) {
  RunServer();

  return 0;
} 
