#!/usr/bin/env bash

root_path=$(cd "$(dirname "$0")"; pwd)
cwt_config_path=$root_path
github_url=https://github.com.cnpmjs.org
github_cwtools_url=$github_url/cwtools

cd $root_path

# ck2
rm -rf $cwt_config_path/ck2/*
git clone $github_cwtools_url/cwtools-ck2-config $cwt_config_path/ck2
rm -rf $cwt_config_path/ck2/.*

# ck3
rm -rf $cwt_config_path/ck3/*
git clone $github_cwtools_url/cwtools-ck3-config $cwt_config_path/ck3
rm -rf $cwt_config_path/ck3/.*
mv $cwt_config_path/ck3/config/* $cwt_config_path/ck3/
rm -rf $cwt_config_path/ck3/config

# eu4
rm -rf $cwt_config_path/eu4/*
git clone $github_cwtools_url/cwtools-eu4-config $cwt_config_path/eu4
rm -rf $cwt_config_path/eu4/.*

# hoi4
rm -rf $cwt_config_path/hoi4/*
git clone $github_cwtools_url/cwtools-hoi4-config $cwt_config_path/hoi4
rm -rf $cwt_config_path/hoi4/.*
mv $cwt_config_path/hoi4/config/* $cwt_config_path/hoi4/
rm -rf $cwt_config_path/hoi4/config

# ir
rm -rf $cwt_config_path/ir/*
git clone $github_cwtools_url/cwtools-ir-config $cwt_config_path/ir
rm -rf $cwt_config_path/ir/.*

# stellaris
rm -rf $cwt_config_path/stellaris/*
git clone $github_cwtools_url/cwtools-stellaris-config $cwt_config_path/stellaris
rm -rf $cwt_config_path/stellaris/.*
mv $cwt_config_path/stellaris/config/* $cwt_config_path/stellaris/
rm -rf $cwt_config_path/stellaris/config

# vic2
rm -rf $cwt_config_path/vic2/*
git clone $github_cwtools_url/cwtools-vic2-config $cwt_config_path/vic2
rm -rf $cwt_config_path/vic2/.*