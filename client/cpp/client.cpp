#include <iostream>
#include <memory>
#include <string>
#include <fstream>

#include <grpc++/grpc++.h>

#include "snapper.grpc.pb.h"

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;
using snapper::Resolution;
using snapper::Snapper;
using snapper::Chunk;

class SnapperClient {
public:
    SnapperClient(std::shared_ptr<Channel> channel)
        : _stub(Snapper::NewStub(channel)) {}



    void Snapshot(const Resolution& resolution)
    {
        Resolution request;
        request.set_width(resolution.width());
        request.set_height(resolution.height());

        ClientContext context;

        Chunk chunk;
        std::unique_ptr< ::grpc::ClientReader< ::snapper::Chunk>> reader = _stub->Snapshot(&context, request);

        std::ofstream outfile("somefile.jpg", std::ofstream::binary);
        while (reader->Read(&chunk))
        {
            outfile << chunk.content();
        }
    }

private:
    std::unique_ptr<Snapper::Stub> _stub;
};

int main(int argc, char** argv) {
    SnapperClient snapperClient(
                grpc::CreateChannel("192.168.1.21:50051",
                                    grpc::InsecureChannelCredentials()));
    Resolution resolution;
    resolution.set_width(320);
    resolution.set_height(240);
    snapperClient.Snapshot(resolution);

    return 0;
}
