# https://hub.docker.com/_/microsoft-dotnet-core
FROM mcr.microsoft.com/dotnet/core/aspnet:3.1 
LABEL maintainer="S. M. CORPORATION"
WORKDIR /app
EXPOSE  5001
FROM mcr.microsoft.com/dotnet/core/sdk:3.1 AS build
WORKDIR /source
# copy csproj and restore as distinct layers
COPY *.sln .
COPY ./cert-aspnetcore.pfx /root/.dotnet/https/
COPY StudiumAnalyticsBackend/*.csproj ./StudiumAnalyticsBackend/
RUN dotnet restore StudiumAnalyticsBackend/StudiumAnalyticsBackend.csproj

# copy everything else and build app
COPY StudiumAnalyticsBackend/. ./StudiumAnalyticsBackend/
WORKDIR /source/StudiumAnalyticsBackend
RUN dotnet build StudiumAnalyticsBackend.csproj -c Release -o /app
FROM build AS publish
RUN dotnet publish StudiumAnalyticsBackend.csproj -c Release -o /app
# final stage/image

COPY --from=build /app ./
ENTRYPOINT ["dotnet", "StudiumAnalyticsBackend.dll"]
