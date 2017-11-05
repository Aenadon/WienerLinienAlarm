#!/bin/bash

SOURCE="app/src/main/assets/ic_launcher.svg"

inkscape -f "$SOURCE" -h 48 -e app/src/main/res/mipmap-mdpi/ic_launcher.png
inkscape -f "$SOURCE" -h 72 -e app/src/main/res/mipmap-hdpi/ic_launcher.png
inkscape -f "$SOURCE" -h 96 -e app/src/main/res/mipmap-xhdpi/ic_launcher.png
inkscape -f "$SOURCE" -h 144 -e app/src/main/res/mipmap-xxhdpi/ic_launcher.png
inkscape -f "$SOURCE" -h 192 -e app/src/main/res/mipmap-xxxhdpi/ic_launcher.png

# for google play store
inkscape -f "$SOURCE" -h 512 -e app/src/main/ic_launcher-web.png
