# Benchmark Report

- Timestamp: 20261024_214112
- JMH version: 1.37

| Benchmark | Params | Mode | Score | Error | Unit | Forks |
|---|---|---|---:|---:|---|---:|
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.equalsAscii |  | avgt | 4.147 | 0.433 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.equalsAsciiIgnoreCase |  | avgt | 7.522 | 1.229 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.equalsFastAscii |  | avgt | 6.478 | 0.392 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.equalsFastAsciiIgnoreCase |  | avgt | 7.024 | 0.330 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.equalsFastNonAscii |  | avgt | 7.148 | 0.232 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.equalsNonAscii |  | avgt | 3.657 | 0.090 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.joinToString |  | avgt | 71.126 | 8.964 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.joinToStringFast |  | avgt | 60.575 | 5.337 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.joinToStringFastWithTransform |  | avgt | 97.447 | 6.570 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.joinToStringWithTransform |  | avgt | 95.877 | 6.755 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.split |  | avgt | 42.565 | 2.422 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.splitFast |  | avgt | 34.920 | 3.331 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.splitFastWithLimit |  | avgt | 24.824 | 1.257 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.splitWithLimit |  | avgt | 28.765 | 1.164 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.trim |  | avgt | 12.719 | 0.716 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsBenchmark.trimFast |  | avgt | 12.011 | 0.870 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsEqual | length=100 | avgt | 4.630 | 0.542 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsEqual | length=10000 | avgt | 190.841 | 105.571 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsEqual | length=1000000 | avgt | 20637.104 | 2887.355 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastEqual | length=100 | avgt | 27.876 | 2.261 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastEqual | length=10000 | avgt | 2375.134 | 260.410 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastEqual | length=1000000 | avgt | 235951.183 | 27559.472 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastIgnoreCaseMixed | length=100 | avgt | 76.613 | 10.617 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastIgnoreCaseMixed | length=10000 | avgt | 6293.045 | 396.243 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastIgnoreCaseMixed | length=1000000 | avgt | 742537.230 | 127433.727 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastMismatchLast | length=100 | avgt | 27.651 | 1.472 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastMismatchLast | length=10000 | avgt | 2315.900 | 193.551 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsFastMismatchLast | length=1000000 | avgt | 247040.986 | 33543.683 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsIgnoreCaseMixed | length=100 | avgt | 66.036 | 14.798 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsIgnoreCaseMixed | length=10000 | avgt | 5955.598 | 686.803 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsIgnoreCaseMixed | length=1000000 | avgt | 763133.929 | 419660.634 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsMismatchLast | length=100 | avgt | 4.235 | 0.490 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsMismatchLast | length=10000 | avgt | 179.922 | 115.826 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtensionsScalingBenchmark.equalsMismatchLast | length=1000000 | avgt | 21589.936 | 3721.586 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.matchesPath |  | avgt | 12.779 | 0.661 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.matchesPathByReference |  | avgt | 14.597 | 2.247 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedList |  | avgt | 71.041 | 9.197 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedListByReference |  | avgt | 111.220 | 6.563 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedMutableList |  | avgt | 73.109 | 3.239 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedMutableListByReference |  | avgt | 132.217 | 11.346 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedMutableSet |  | avgt | 100.849 | 12.244 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedMutableSetByReference |  | avgt | 176.549 | 8.269 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedSet |  | avgt | 101.206 | 7.660 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedSetByReference |  | avgt | 161.488 | 12.025 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedString |  | avgt | 44.594 | 2.252 | ns/op | 2 |
| icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark.toDelimitedStringByReference |  | avgt | 72.980 | 4.964 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableList | size=0 | avgt | 1.947 | 1.637 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableList | size=1 | avgt | 1.727 | 0.188 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableList | size=4 | avgt | 1.749 | 0.262 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableList | size=64 | avgt | 1.741 | 0.158 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableListByReference | size=0 | avgt | 0.610 | 0.039 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableListByReference | size=1 | avgt | 2.386 | 0.161 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableListByReference | size=4 | avgt | 27.079 | 3.394 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.asImmutableListByReference | size=64 | avgt | 54.318 | 6.057 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableList | size=0 | avgt | 0.594 | 0.075 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableList | size=1 | avgt | 1.742 | 0.220 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableList | size=4 | avgt | 10.177 | 0.995 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableList | size=64 | avgt | 87.288 | 5.281 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableListByReference | size=0 | avgt | 2.491 | 0.430 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableListByReference | size=1 | avgt | 6.907 | 0.598 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableListByReference | size=4 | avgt | 13.568 | 0.870 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableListByReference | size=64 | avgt | 137.445 | 13.480 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSet | size=0 | avgt | 0.607 | 0.058 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSet | size=1 | avgt | 1.740 | 0.267 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSet | size=4 | avgt | 59.619 | 7.165 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSet | size=64 | avgt | 573.899 | 52.106 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSetByReference | size=0 | avgt | 4.352 | 0.463 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSetByReference | size=1 | avgt | 5.280 | 0.294 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSetByReference | size=4 | avgt | 6.998 | 1.877 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastBuildersBenchmark.buildImmutableSetByReference | size=64 | avgt | 50.464 | 12.127 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.all |  | avgt | 41.775 | 16.201 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.allFast |  | avgt | 36.158 | 5.459 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.any |  | avgt | 1.770 | 0.182 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.anyFast |  | avgt | 1.687 | 0.143 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.dropFastNearEnd |  | avgt | 2.996 | 0.278 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.dropFastOne |  | avgt | 162.121 | 24.584 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.dropFastZero |  | avgt | 0.738 | 0.213 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.dropNearEnd |  | avgt | 2.785 | 0.510 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.dropOne |  | avgt | 177.329 | 15.510 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.dropZero |  | avgt | 29.327 | 6.488 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.filter |  | avgt | 223.848 | 18.411 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.filterFast |  | avgt | 383.296 | 79.626 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.filterIsInstance |  | avgt | 182.955 | 22.574 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.filterIsInstanceFast |  | avgt | 224.084 | 16.865 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.filterNotNull |  | avgt | 243.800 | 26.715 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.filterNotNullFast |  | avgt | 357.001 | 107.307 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.find |  | avgt | 20.272 | 1.176 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.findFast |  | avgt | 19.191 | 3.847 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.findLast |  | avgt | 22.156 | 4.812 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.findLastFast |  | avgt | 21.633 | 8.380 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.flatMap |  | avgt | 2141.942 | 329.069 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.flatMapFast |  | avgt | 1836.317 | 245.947 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.forEach |  | avgt | 35.345 | 2.581 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.forEachFast |  | avgt | 33.129 | 3.687 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.forEachIndexed |  | avgt | 38.012 | 4.007 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.forEachIndexedFast |  | avgt | 35.982 | 9.480 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.forEachReversed |  | avgt | 33.477 | 2.722 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.forEachReversedFast |  | avgt | 33.322 | 3.174 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.map |  | avgt | 238.241 | 32.014 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.mapFast |  | avgt | 163.392 | 26.062 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.mapFastSingle |  | avgt | 2.488 | 0.327 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.mapNotNull |  | avgt | 249.925 | 36.684 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.mapNotNullFast |  | avgt | 416.026 | 33.782 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.mapSingle |  | avgt | 5.962 | 0.891 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.none |  | avgt | 36.643 | 4.252 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.noneFast |  | avgt | 35.703 | 1.667 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.process |  | avgt | 37.628 | 3.564 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark.processFast |  | avgt | 33.785 | 2.585 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastHalf | size=0 | avgt | 0.861 | 0.167 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastHalf | size=1 | avgt | 0.940 | 0.590 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastHalf | size=4 | avgt | 13.325 | 4.162 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastHalf | size=64 | avgt | 76.675 | 34.977 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastOne | size=0 | avgt | 0.579 | 0.182 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastOne | size=1 | avgt | 0.641 | 0.381 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastOne | size=4 | avgt | 14.981 | 3.913 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastOne | size=64 | avgt | 130.151 | 49.766 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastZero | size=0 | avgt | 0.349 | 0.123 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastZero | size=1 | avgt | 0.339 | 0.037 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastZero | size=4 | avgt | 0.358 | 0.086 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropFastZero | size=64 | avgt | 0.414 | 0.121 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropHalf | size=0 | avgt | 0.908 | 0.232 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropHalf | size=1 | avgt | 4.253 | 6.199 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropHalf | size=4 | avgt | 16.151 | 5.052 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropHalf | size=64 | avgt | 100.067 | 71.854 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropOne | size=0 | avgt | 0.611 | 0.234 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropOne | size=1 | avgt | 0.604 | 0.285 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropOne | size=4 | avgt | 19.879 | 6.106 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropOne | size=64 | avgt | 166.817 | 46.874 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropZero | size=0 | avgt | 0.577 | 0.311 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropZero | size=1 | avgt | 3.959 | 6.112 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropZero | size=4 | avgt | 18.194 | 4.094 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.dropZero | size=64 | avgt | 32.120 | 8.659 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastHalf | size=0 | avgt | 0.560 | 0.115 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastHalf | size=1 | avgt | 5.500 | 3.001 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastHalf | size=4 | avgt | 20.161 | 6.906 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastHalf | size=64 | avgt | 223.293 | 48.947 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastNone | size=0 | avgt | 0.531 | 0.263 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastNone | size=1 | avgt | 3.332 | 3.151 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastNone | size=4 | avgt | 5.213 | 2.380 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterFastNone | size=64 | avgt | 35.562 | 2.306 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterHalf | size=0 | avgt | 3.894 | 9.002 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterHalf | size=1 | avgt | 10.015 | 2.954 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterHalf | size=4 | avgt | 24.407 | 8.483 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterHalf | size=64 | avgt | 324.909 | 33.214 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterNone | size=0 | avgt | 4.081 | 9.280 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterNone | size=1 | avgt | 5.800 | 4.297 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterNone | size=4 | avgt | 8.843 | 3.376 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.filterNone | size=64 | avgt | 54.043 | 18.225 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEach | size=0 | avgt | 0.523 | 0.085 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEach | size=1 | avgt | 4.916 | 1.847 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEach | size=4 | avgt | 7.018 | 0.730 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEach | size=64 | avgt | 39.579 | 19.798 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEachFast | size=0 | avgt | 0.480 | 0.144 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEachFast | size=1 | avgt | 2.716 | 0.520 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEachFast | size=4 | avgt | 5.522 | 1.863 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.forEachFast | size=64 | avgt | 37.111 | 6.221 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.map | size=0 | avgt | 3.586 | 3.842 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.map | size=1 | avgt | 13.730 | 2.746 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.map | size=4 | avgt | 34.761 | 33.317 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.map | size=64 | avgt | 433.892 | 38.780 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.mapFast | size=0 | avgt | 0.563 | 0.349 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.mapFast | size=1 | avgt | 5.143 | 4.354 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.mapFast | size=4 | avgt | 25.122 | 17.382 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsHotPathBenchmark.mapFast | size=64 | avgt | 385.386 | 46.728 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastHalf | size=100 | avgt | 101.131 | 31.842 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastHalf | size=10000 | avgt | 10856.627 | 839.158 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastHalf | size=1000000 | avgt | 994827.545 | 55226.039 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastNearEnd | size=100 | avgt | 3.496 | 0.650 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastNearEnd | size=10000 | avgt | 3.387 | 0.270 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastNearEnd | size=1000000 | avgt | 3.303 | 0.262 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastOne | size=100 | avgt | 224.964 | 32.457 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastOne | size=10000 | avgt | 22625.896 | 4886.980 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastOne | size=1000000 | avgt | 8471393.436 | 611317.280 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastZero | size=100 | avgt | 0.610 | 0.065 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastZero | size=10000 | avgt | 0.616 | 0.064 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropFastZero | size=1000000 | avgt | 0.624 | 0.050 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropHalf | size=100 | avgt | 165.676 | 11.268 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropHalf | size=10000 | avgt | 16157.816 | 1578.046 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropHalf | size=1000000 | avgt | 1607177.958 | 141969.429 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropNearEnd | size=100 | avgt | 3.015 | 0.136 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropNearEnd | size=10000 | avgt | 2.987 | 0.240 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropNearEnd | size=1000000 | avgt | 2.957 | 0.123 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropOne | size=100 | avgt | 261.080 | 29.071 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropOne | size=10000 | avgt | 34375.737 | 14871.869 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropOne | size=1000000 | avgt | 9213873.560 | 857283.671 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropZero | size=100 | avgt | 40.476 | 5.165 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropZero | size=10000 | avgt | 4218.678 | 129.416 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.dropZero | size=1000000 | avgt | 5738716.793 | 436463.117 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterAll | size=100 | avgt | 411.810 | 23.646 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterAll | size=10000 | avgt | 49037.638 | 7707.813 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterAll | size=1000000 | avgt | 10506151.733 | 2122646.069 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastAll | size=100 | avgt | 766.238 | 19.019 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastAll | size=10000 | avgt | 45742.108 | 4605.221 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastAll | size=1000000 | avgt | 9865688.865 | 828240.272 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastHalf | size=100 | avgt | 576.853 | 86.813 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastHalf | size=10000 | avgt | 33961.513 | 5874.760 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastHalf | size=1000000 | avgt | 4519968.231 | 336046.437 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastNone | size=100 | avgt | 53.432 | 1.420 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastNone | size=10000 | avgt | 4809.980 | 378.905 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterFastNone | size=1000000 | avgt | 920369.420 | 61133.408 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterHalf | size=100 | avgt | 353.839 | 57.804 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterHalf | size=10000 | avgt | 34222.813 | 3861.976 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterHalf | size=1000000 | avgt | 4970567.093 | 671712.783 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterNone | size=100 | avgt | 53.424 | 5.135 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterNone | size=10000 | avgt | 5811.748 | 851.191 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.filterNone | size=1000000 | avgt | 937483.956 | 137708.041 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.forEach | size=100 | avgt | 52.665 | 8.592 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.forEach | size=10000 | avgt | 4554.864 | 428.713 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.forEach | size=1000000 | avgt | 907555.958 | 85192.018 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.forEachFast | size=100 | avgt | 54.438 | 4.188 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.forEachFast | size=10000 | avgt | 4296.074 | 350.880 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.forEachFast | size=1000000 | avgt | 905256.538 | 171981.613 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.map | size=100 | avgt | 347.021 | 28.536 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.map | size=10000 | avgt | 48700.047 | 1493.306 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.map | size=1000000 | avgt | 11485440.222 | 1836816.215 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.mapFast | size=100 | avgt | 230.305 | 19.710 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.mapFast | size=10000 | avgt | 35891.011 | 12037.450 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtensionsScalingBenchmark.mapFast | size=1000000 | avgt | 10296429.722 | 1967131.443 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsBenchmark.removePrefixOrNull |  | avgt | 11.775 | 0.711 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsBenchmark.removePrefixOrNullByReference |  | avgt | 11.218 | 0.256 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsBenchmark.removeSuffixOrNull |  | avgt | 12.803 | 0.805 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsBenchmark.removeSuffixOrNullByReference |  | avgt | 11.747 | 0.823 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalf | size=0 | avgt | 1.783 | 0.908 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalf | size=1 | avgt | 2.496 | 0.211 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalf | size=4 | avgt | 19.229 | 1.241 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalf | size=64 | avgt | 126.964 | 21.998 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalfByReference | size=0 | avgt | 1.596 | 0.154 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalfByReference | size=1 | avgt | 2.352 | 0.229 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalfByReference | size=4 | avgt | 20.081 | 1.872 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullHalfByReference | size=64 | avgt | 152.375 | 1.115 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOne | size=0 | avgt | 1.021 | 0.321 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOne | size=1 | avgt | 7.771 | 3.131 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOne | size=4 | avgt | 20.649 | 7.035 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOne | size=64 | avgt | 137.929 | 40.511 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOneByReference | size=0 | avgt | 0.763 | 0.105 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOneByReference | size=1 | avgt | 7.697 | 3.596 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOneByReference | size=4 | avgt | 22.315 | 10.017 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removePrefixOrNullOneByReference | size=64 | avgt | 299.901 | 89.555 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalf | size=0 | avgt | 1.703 | 0.707 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalf | size=1 | avgt | 2.550 | 0.458 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalf | size=4 | avgt | 20.083 | 2.572 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalf | size=64 | avgt | 145.170 | 33.970 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalfByReference | size=0 | avgt | 1.603 | 0.563 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalfByReference | size=1 | avgt | 2.316 | 0.225 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalfByReference | size=4 | avgt | 23.718 | 9.887 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullHalfByReference | size=64 | avgt | 171.203 | 24.256 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOne | size=0 | avgt | 0.991 | 0.353 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOne | size=1 | avgt | 8.583 | 3.244 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOne | size=4 | avgt | 20.643 | 3.286 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOne | size=64 | avgt | 171.071 | 13.388 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOneByReference | size=0 | avgt | 0.781 | 0.184 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOneByReference | size=1 | avgt | 8.607 | 3.457 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOneByReference | size=4 | avgt | 25.093 | 5.057 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsHotPathBenchmark.removeSuffixOrNullOneByReference | size=64 | avgt | 231.581 | 19.486 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalf | size=100 | avgt | 149.400 | 44.915 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalf | size=10000 | avgt | 18215.992 | 6278.853 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalf | size=1000000 | avgt | 2072957.233 | 198182.131 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfByReference | size=100 | avgt | 175.388 | 20.968 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfByReference | size=10000 | avgt | 17599.648 | 2008.125 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfByReference | size=1000000 | avgt | 2843111.051 | 130101.992 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfMismatchLast | size=100 | avgt | 66.055 | 9.838 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfMismatchLast | size=10000 | avgt | 6223.053 | 886.694 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfMismatchLast | size=1000000 | avgt | 1199728.248 | 47836.403 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfMismatchLastByReference | size=100 | avgt | 65.202 | 9.297 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfMismatchLastByReference | size=10000 | avgt | 6608.067 | 835.001 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullHalfMismatchLastByReference | size=1000000 | avgt | 1253707.195 | 423383.454 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullOne | size=100 | avgt | 194.900 | 18.240 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullOne | size=10000 | avgt | 22544.539 | 6558.944 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullOne | size=1000000 | avgt | 4120524.185 | 158636.441 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullOneByReference | size=100 | avgt | 262.006 | 42.080 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullOneByReference | size=10000 | avgt | 31264.754 | 5708.358 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removePrefixOrNullOneByReference | size=1000000 | avgt | 5453146.928 | 1396060.476 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalf | size=100 | avgt | 149.153 | 16.344 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalf | size=10000 | avgt | 17662.909 | 7046.732 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalf | size=1000000 | avgt | 2091157.160 | 149756.276 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfByReference | size=100 | avgt | 229.113 | 25.951 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfByReference | size=10000 | avgt | 22397.174 | 1325.673 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfByReference | size=1000000 | avgt | 2880332.930 | 273779.658 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfMismatchLast | size=100 | avgt | 62.135 | 2.955 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfMismatchLast | size=10000 | avgt | 5907.464 | 368.514 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfMismatchLast | size=1000000 | avgt | 1204769.185 | 522675.688 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfMismatchLastByReference | size=100 | avgt | 61.470 | 3.760 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfMismatchLastByReference | size=10000 | avgt | 5682.315 | 359.970 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullHalfMismatchLastByReference | size=1000000 | avgt | 1225578.601 | 108103.394 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullOne | size=100 | avgt | 203.473 | 38.963 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullOne | size=10000 | avgt | 21020.203 | 2046.645 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullOne | size=1000000 | avgt | 4064948.071 | 592729.299 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullOneByReference | size=100 | avgt | 316.282 | 21.033 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullOneByReference | size=10000 | avgt | 33840.033 | 2076.916 | ns/op | 2 |
| icu.windea.pls.core.collections.CollectionFastExtraExtensionsScalingBenchmark.removeSuffixOrNullOneByReference | size=1000000 | avgt | 6129306.606 | 3010248.465 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.baseline |  | avgt | 0.919 | 0.054 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.matches |  | avgt | 214.441 | 17.296 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.matchesDeepMemberPath |  | avgt | 683.607 | 106.339 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.matchesFirstInPatternSet |  | avgt | 775.399 | 200.414 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.matchesIgnoreCase |  | avgt | 218.818 | 23.934 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.matchesLongFilePath |  | avgt | 2264.889 | 694.123 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.matchesMiss |  | avgt | 179.726 | 7.454 | ns/op | 2 |
| icu.windea.pls.core.match.AntFromRegexBenchmark.matchesUniquePatterns |  | avgt | 852.853 | 53.065 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.baseline |  | avgt | 0.938 | 0.100 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.matches |  | avgt | 216.950 | 11.399 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.matchesDeepMemberPath |  | avgt | 435.548 | 73.600 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.matchesFirstInPatternSet |  | avgt | 1168.270 | 54.104 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.matchesIgnoreCase |  | avgt | 167.905 | 10.173 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.matchesLongFilePath |  | avgt | 784.959 | 201.591 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.matchesMiss |  | avgt | 137.248 | 20.236 | ns/op | 2 |
| icu.windea.pls.core.match.AntMatcherBenchmark.matchesUniquePatterns |  | avgt | 148.148 | 14.254 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.baseline |  | avgt | 0.957 | 0.059 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.matches |  | avgt | 11.039 | 1.430 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.matchesByRegex |  | avgt | 57.162 | 5.144 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.matchesIgnoreCase |  | avgt | 17.329 | 1.071 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.matchesIgnoreCaseByRegex |  | avgt | 55.793 | 7.246 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.matchesLongInput |  | avgt | 359.715 | 22.639 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.matchesLongInputByRegex |  | avgt | 919.917 | 82.282 | ns/op | 2 |
| icu.windea.pls.core.match.GlobMatcherBenchmark.matchesMiss |  | avgt | 9.946 | 0.587 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesListAny |  | avgt | 19.093 | 2.556 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesListExact |  | avgt | 14.669 | 0.793 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesListExactByDirect |  | avgt | 11.753 | 0.785 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesListPattern |  | avgt | 64.207 | 4.457 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesListPatternByDirect |  | avgt | 54.951 | 4.655 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesStringAny |  | avgt | 2.058 | 0.166 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesStringAnyByDirect |  | avgt | 1.307 | 0.185 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesStringExact |  | avgt | 1.455 | 0.061 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesStringExactByDirect |  | avgt | 1.450 | 0.171 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesStringPattern |  | avgt | 10.585 | 0.300 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.matchesStringPatternByDirect |  | avgt | 10.399 | 0.870 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.relative |  | avgt | 9.903 | 1.151 | ns/op | 2 |
| icu.windea.pls.core.match.PathMatcherBenchmark.relativeByDirect |  | avgt | 8.248 | 2.088 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesDateField |  | avgt | 125.503 | 16.572 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesDateFieldInvalid |  | avgt | 13.188 | 2.080 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesDateFieldValid |  | avgt | 204.771 | 9.081 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesDateFieldWithPrecheck |  | avgt | 245.122 | 14.901 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesFloat |  | avgt | 6.264 | 0.231 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesFloatByIdiomatic |  | avgt | 15.488 | 0.828 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesFloatByRegex |  | avgt | 75.963 | 13.844 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesFloatPercentageField |  | avgt | 5.704 | 0.234 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesFloatPercentageFieldByIdiomatic |  | avgt | 28.403 | 1.359 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesFloatStrictDot |  | avgt | 6.272 | 0.300 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesFloatStrictDotByRegex |  | avgt | 48.964 | 7.507 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesInt |  | avgt | 4.620 | 0.593 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesIntByIdiomatic |  | avgt | 6.391 | 0.745 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesIntByRegex |  | avgt | 41.239 | 3.371 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesIntPercentageField |  | avgt | 4.180 | 0.215 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesIntPercentageFieldByIdiomatic |  | avgt | 16.143 | 0.964 | ns/op | 2 |
| icu.windea.pls.core.match.TextMatcherBenchmark.matchesIntWithoutLeadingUnary |  | avgt | 3.339 | 0.129 | ns/op | 2 |
| icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmark.findBestMatchesDirect | candidateSize=2000 | avgt | 127.682 | 7.370 | us/op | 1 |
| icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmark.findBestMatchesDirect | candidateSize=5000 | avgt | 342.097 | 64.978 | us/op | 1 |
| icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmark.findBestMatchesDirect | candidateSize=10000 | avgt | 699.035 | 47.078 | us/op | 1 |
| icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmark.findBestMatchesWarm | candidateSize=2000 | avgt | 45.050 | 2.966 | us/op | 1 |
| icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmark.findBestMatchesWarm | candidateSize=5000 | avgt | 131.880 | 18.271 | us/op | 1 |
| icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmark.findBestMatchesWarm | candidateSize=10000 | avgt | 263.035 | 27.330 | us/op | 1 |
