#!/usr/bin/env bash

cd src/main/resources/config
git clone https://github.com.cnpmjs.org/cwtools/cwtools-stellaris-config stellaris
rm -rf stellaris/.*
mv stellaris/config/* stellaris/
rm -rf stellaris/config
