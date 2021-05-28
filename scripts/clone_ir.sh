#!/usr/bin/env bash

cd src/main/resources/config

git clone https://github.com.cnpmjs.org/cwtools/cwtools-ir-config ir
rm -rf ir/.*
