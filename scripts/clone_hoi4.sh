#!/usr/bin/env bash

cd src/main/resources/config

git clone https://github.com.cnpmjs.org/cwtools/cwtools-hoi4-config hoi4
rm -rf hoi4/.*
mv hoi4/Config/* hoi4/
