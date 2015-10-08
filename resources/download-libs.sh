#!/bin/sh

# MicroEmulator
MICROEMU=2.0.4

# ProGuard
PROGUARD_MAJOR=5.2
PROGUARD_MINOR=5.2.1

# JAD Ant Tasks (JAT)
JAT=1.3

cd lib/

# MicroEmulator
wget -nv http://microemu.googlecode.com/files/microemulator-${MICROEMU}.zip
unzip -q microemulator-${MICROEMU}.zip
ln -s microemulator-${MICROEMU} microemulator

# ProGuard
wget -nv http://downloads.sourceforge.net/project/proguard/proguard/${PROGUARD_MAJOR}/proguard${PROGUARD_MINOR}.tar.gz
tar -xzf proguard${PROGUARD_MINOR}.tar.gz
ln -s proguard${PROGUARD_MINOR} proguard

# JAT
wget -nv http://nfcip-java.googlecode.com/files/jad-ant-tasks-${JAT}.jar
ln -s jad-ant-tasks-${JAT}.jar jad-ant-tasks.jar
