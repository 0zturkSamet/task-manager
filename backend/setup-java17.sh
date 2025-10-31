#!/bin/bash

echo "================================================"
echo "Task Manager Backend - Java 17 Setup"
echo "================================================"
echo ""

# Check if Java 17 is already installed
if [ -d "/opt/homebrew/opt/openjdk@17" ]; then
    echo "✅ Java 17 is already installed!"
else
    echo "📦 Installing Java 17..."
    brew install openjdk@17
    echo "✅ Java 17 installed!"
fi

echo ""
echo "Setting up Java 17 environment..."
echo ""

# Export Java 17 environment variables
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Verify Java version
echo "Current Java version:"
java -version

echo ""
echo "================================================"
echo "Running Maven build..."
echo "================================================"
echo ""

# Clean and build
mvn clean install

if [ $? -eq 0 ]; then
    echo ""
    echo "================================================"
    echo "✅ BUILD SUCCESSFUL!"
    echo "================================================"
    echo ""
    echo "To run the application:"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Or to always use Java 17, add these lines to ~/.zshrc:"
    echo '  export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"'
    echo '  export JAVA_HOME="/opt/homebrew/opt/openjdk@17"'
    echo ""
else
    echo ""
    echo "================================================"
    echo "❌ BUILD FAILED"
    echo "================================================"
    echo "Check the error messages above"
    echo ""
fi
