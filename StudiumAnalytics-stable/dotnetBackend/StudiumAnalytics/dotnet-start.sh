#!/usr/bin/env bash
# Stop
docker stop dotnetserver

# Remove previuos container 
docker container rm dotnetserver

# Build
docker build ./ --tag tap:dotnetserver

docker run -it -p 5001:5001 -e ASPNETCORE_HTTPS_PORT=5001 -e ASPNETCORE_URLS="https://+;http://+"  -e ASPNETCORE_Kestrel__Certificates__Default__Path=/root/.dotnet/https/cert-aspnetcore.pfx -e ASPNETCORE_Kestrel__Certificates__Default__Password=sergio --network host  tap:dotnetserver