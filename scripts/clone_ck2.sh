#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/.."; pwd)
cd $root_path
cd src/main/resources/config

rm -rf ck2/*
git clone https://github.com.cnpmjs.org/cwtools/cwtools-ck2-config ck2
rm -rf ck2/.*