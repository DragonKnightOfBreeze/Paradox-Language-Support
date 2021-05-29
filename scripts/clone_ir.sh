#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/.."; pwd)
cd $root_path
cd src/main/resources/config

rm -rf ir/*
git clone https://github.com.cnpmjs.org/cwtools/cwtools-ir-config ir
rm -rf ir/.*
