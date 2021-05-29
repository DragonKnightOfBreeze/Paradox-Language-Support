#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/.."; pwd)
cd $root_path
cd src/main/resources/config

rm -rf hoi4/*
git clone https://github.com.cnpmjs.org/cwtools/cwtools-hoi4-config hoi4
rm -rf hoi4/.*
mv hoi4/Config/* hoi4/
