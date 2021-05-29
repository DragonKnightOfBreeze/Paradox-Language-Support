#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/.."; pwd)
cd $root_path
cd src/main/resources/config

rm -rf ck3/*
git clone https://github.com.cnpmjs.org/cwtools/cwtools-ck3-config ck3
rm -rf ck3/.*
mv ck3/config/* ck3/
rm -rf ck3/config
