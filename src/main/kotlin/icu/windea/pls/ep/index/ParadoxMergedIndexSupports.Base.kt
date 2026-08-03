package icu.windea.pls.ep.index

import icu.windea.pls.core.collections.asMutable
import icu.windea.pls.lang.index.ChronicleIndexStatisticService
import icu.windea.pls.model.index.ParadoxIndexInfo

abstract class ParadoxMergedIndexSupportBase<T : ParadoxIndexInfo> : ParadoxMergedIndexSupport<T> {
    protected fun <T : ParadoxIndexInfo> addToFileData(info: T, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        ChronicleIndexStatisticService.recordMerged(info.gameType, indexInfoType)

        fileData.getOrPut(indexInfoType.key.toString()) { mutableListOf() }.asMutable() += info
    }
}
