#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")"; pwd)

cd $root_path

# rm -rf cwtools-ck2-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-ck2-config && echo "git clone cwtools-ck2-config: DONE" 2>&1

# rm -rf cwtools-ck3-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-ck3-config && echo "git clone cwtools-ck3-config: DONE" 2>&1

# rm -rf cwtools-eu4-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-eu4-config && echo "git clone cwtools-eu4-config: DONE" 2>&1

# rm -rf cwtools-hoi4-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-hoi4-config && echo "git clone cwtools-hoi4-config: DONE" 2>&1

# rm -rf cwtools-ir-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-ir-config && echo "git clone cwtools-ir-config: DONE" 2>&1

# rm -rf cwtools-stellaris-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-stellaris-config && echo "git clone cwtools-stellaris-config: DONE" 2>&1

# rm -rf cwtools-vic2-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-vic2-config && echo "git clone cwtools-vic2-config: DONE" 2>&1

# rm -rf cwtools-vic3-config
git clone https://github.com/DragonKnightOfBreeze/cwtools-vic3-config && echo "git clone cwtools-vic3-config: DONE" 2>&1