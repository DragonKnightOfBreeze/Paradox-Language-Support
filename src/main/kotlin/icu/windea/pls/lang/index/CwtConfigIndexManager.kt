package icu.windea.pls.lang.index

import com.intellij.util.indexing.ID
import icu.windea.pls.core.findFileBasedIndex
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo

object CwtConfigIndexManager {
    val Symbol by lazy { findFileBasedIndex<CwtConfigSymbolIndex>() }

    val SymbolName = ID.create<String, List<CwtConfigSymbolIndexInfo>>("cwt.config.symbol.index")
}
