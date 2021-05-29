#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/.."; pwd)
cd $root_path
sh scripts/clone_ck2.sh
cd $root_path
sh scripts/clone_ck3.sh
cd $root_path
sh scripts/clone_eu4.sh
cd $root_path
sh scripts/clone_hoi4.sh
cd $root_path
sh scripts/clone_ir.sh
cd $root_path
sh scripts/clone_stellaris.sh
cd $root_path
sh scripts/clone_vic2.sh
cd $root_path