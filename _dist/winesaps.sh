#!/bin/bash

# winesaps launch script
# https://winesaps.com

if type -p java; then
    java -jar winesaps.jar &
else
    echo "Java not found! Please download java from http://java.com/download" or install package "openjdk v1.6.0" or higher
fi
