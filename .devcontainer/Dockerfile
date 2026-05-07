FROM mcr.microsoft.com/vscode/devcontainers/java:0-21-bullseye

# Install Maven (image may already include, ensure present)
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Set working dir
WORKDIR /workspace
