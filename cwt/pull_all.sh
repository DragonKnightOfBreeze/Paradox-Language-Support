#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")"; pwd)

cd $root_path/cwtools-ck2-config
git pull &

cd $root_path/cwtools-ck3-config
git pull &

cd $root_path/cwtools-eu4-config
git pull &

cd $root_path/cwtools-hoi4-config
git pull &

cd $root_path/cwtools-ir-config
git pull &

cd $root_path/cwtools-stellaris-config
git pull &

cd $root_path/cwtools-vic2-config
git pull &