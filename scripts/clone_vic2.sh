#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/.."; pwd)
cd $root_path
cd src/main/resources/config

rm -rf vic2/*
git clone https://github.com.cnpmjs.org/cwtools/cwtools-vic2-config vic2
rm -rf vic2/.*