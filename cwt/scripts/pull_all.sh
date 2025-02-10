#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")/../"; pwd)

cd $root_path/cwtools-ck2-config
git pull https://github.com/cwtools/cwtools-ck2-config master && echo "git pull cwtools/cwtools-ck2-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-ck2-config: DONE" 2>&1

cd $root_path/cwtools-ck3-config
git pull https://github.com/cwtools/cwtools-ck3-config master && echo "git pull cwtools/cwtools-ck3-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-ck3-config: DONE" 2>&1

cd $root_path/cwtools-eu4-config
git pull https://github.com/cwtools/cwtools-eu4-config master && echo "git pull cwtools/cwtools-eu4-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-eu4-config: DONE" 2>&1

cd $root_path/cwtools-hoi4-config
git pull https://github.com/cwtools/cwtools-hoi4-config master && echo "git pull cwtools/cwtools-hoi4-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-hoi4-config: DONE" 2>&1

cd $root_path/cwtools-ir-config
git pull https://github.com/cwtools/cwtools-ir-config master && echo "git pull cwtools/cwtools-ir-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-ir-config: DONE" 2>&1

cd $root_path/cwtools-stellaris-config
# git pull https://github.com/cwtools/cwtools-stellaris-config master && echo "git pull cwtools/cwtools-stellaris-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-stellaris-config: DONE" 2>&1

cd $root_path/cwtools-vic2-config
git pull https://github.com/cwtools/cwtools-vic2-config master && echo "git pull cwtools/cwtools-vic2-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-vic2-config: DONE" 2>&1

cd $root_path/cwtools-vic3-config
# git pull https://github.com/cwtools/cwtools-vic3-config master && echo "git pull cwtools/cwtools-vic3-config: DONE" 2>&1
git pull origin master && echo "git pull cwtools-vic3-config: DONE" 2>&1
