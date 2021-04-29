#!/usr/bin/env bash

cd src/main/resources/config

git clone https://github.com.cnpmjs.org/cwtools/cwtools-ck3-config ck3
rm -rf ck3/.*
mv ck3/config/* ck3/
rm -rf ck3/config
