#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>

#include "snapper.grpc.pb.h"

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;
using snapper::Resolution;
using snapper::Reply;
using snapper::Snapper;

class SnapperClient {
public:
    SnapperClient(std::shared_ptr<Channel> channel)
        : _stub(Snapper::NewStub(channel)) {}



    std::string Snapshot(const Resolution& resolution)
    {
        Resolution request;
        request.set_width(resolution.width());
        request.set_height(resolution.height());

        Reply reply;

        ClientContext context;

        Status status = _stub->Snapshot(&context, request, &reply);

        if (status.ok())
        {
            return "RPC succeeded";
        }
        else
        {
            std::cout << status.error_code() << ": "<<
                         status.error_message() << std::endl;
            return "RPC failed";
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
    std::string reply = snapperClient.Snapshot(resolution);
    std::cout << "Result of snaphot: " << reply << std::endl;

    return 0;
}
