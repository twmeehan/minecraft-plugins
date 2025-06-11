#!/bin/bash

# Create main target directory if it doesn't exist
mkdir -p target

# Function to build a specific plugin
build_plugin() {
    local plugin_name=$1
    echo "Building $plugin_name..."
    cd "$plugin_name"
    mvn clean package
    
    # Copy the JAR file to the main target directory, excluding original-* files
    cp target/[!o]*.jar ../target/ 2>/dev/null || true
    cd ..
}

# List of available plugins
plugins=(
    "DynamicSpells"
    "health-display"
    "keep-inventory-alternative"
    "dungeons"
    "berry-economy"
)

# Display menu
echo "Available plugins:"
for i in "${!plugins[@]}"; do
    echo "$((i+1)). ${plugins[$i]}"
done
echo "0. Build all plugins"
echo "q. Quit"

# Get user choice
read -p "Enter the number of the plugin to build (or 0 for all, q to quit): " choice

# Process choice
case $choice in
    0)
        echo "Building all plugins..."
        for plugin in "${plugins[@]}"; do
            build_plugin "$plugin"
        done
        ;;
    [1-5])
        build_plugin "${plugins[$((choice-1))]}"
        ;;
    q|Q)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid choice. Please run the script again."
        exit 1
        ;;
esac

echo "Build complete! JAR files are in the main target directory." 