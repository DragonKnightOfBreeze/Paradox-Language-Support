#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")"; pwd)
cwt_config_path=$root_path

cd $root_path

# ck2
ck2_repository=https://github.com/cwtools/cwtools-ck2-config
rm -rf $cwt_config_path/ck2/*
git clone $ck2_repository $cwt_config_path/ck2
rm -rf $cwt_config_path/ck2/.*

# ck3
ck3_repository=https://github.com/cwtools/cwtools-ck3-config
rm -rf $cwt_config_path/ck3/*
git clone $ck3_repository $cwt_config_path/ck3
rm -rf $cwt_config_path/ck3/.*
mv $cwt_config_path/ck3/config/* $cwt_config_path/ck3/
rm -rf $cwt_config_path/ck3/config

# eu4
eu4_repository=https://github.com/cwtools/cwtools-eu4-config
rm -rf $cwt_config_path/eu4/*
git clone $eu4_repository $cwt_config_path/eu4
rm -rf $cwt_config_path/eu4/.*

# hoi4
hoi4_repository=https://github.com/cwtools/cwtools-hoi4-config
rm -rf $cwt_config_path/hoi4/*
git clone $hoi4_repository $cwt_config_path/hoi4
rm -rf $cwt_config_path/hoi4/.*
mv $cwt_config_path/hoi4/config/* $cwt_config_path/hoi4/
rm -rf $cwt_config_path/hoi4/config

# ir
ir_repository=https://github.com/cwtools/cwtools-ir-config
rm -rf $cwt_config_path/ir/*
git clone $ir_repository $cwt_config_path/ir
rm -rf $cwt_config_path/ir/.*

# stellaris
stellaris_repository=https://github.com/cwtools/cwtools-stellaris-config
rm -rf $cwt_config_path/stellaris/*
git clone $stellaris_repository $cwt_config_path/stellaris
rm -rf $cwt_config_path/stellaris/.*
mv $cwt_config_path/stellaris/config/* $cwt_config_path/stellaris/
rm -rf $cwt_config_path/stellaris/config

# vic2
vic2_repository=https://github.com/cwtools/cwtools-vic2-config
rm -rf $cwt_config_path/vic2/*
git clone $vic2_repository $cwt_config_path/vic2
rm -rf $cwt_config_path/vic2/.*