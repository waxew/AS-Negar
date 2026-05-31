#!/bin/bash
sed \
  -e 's/<uses-permission android:name="android.permission.INTERNET"\/>/<uses-permission android:name="android.permission.INTERNET" tools:node="remove" \/>/' \
  -e 's/<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"\/>/<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" tools:node="remove" \/>/' \
  -e 's/<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"\/>/<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" tools:node="remove" \/>/' \
  -e 's/<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"\/>/<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" tools:node="remove" \/>/' \
  app/src/main/AndroidManifest.xml > app/src/main/AndroidManifest2.xml
mv app/src/main/AndroidManifest2.xml app/src/main/AndroidManifest.xml