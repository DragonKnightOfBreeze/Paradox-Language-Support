#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/.."; pwd)
cd $root_path
cd src/main/resources/config

rm -rf stellaris/*
git clone https://github.com.cnpmjs.org/cwtools/cwtools-stellaris-config stellaris
rm -rf stellaris/.*
mv stellaris/config/* stellaris/
rm -rf stellaris/config
