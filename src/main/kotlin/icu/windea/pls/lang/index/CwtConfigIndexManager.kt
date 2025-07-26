package icu.windea.pls.lang.index

import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.model.indexInfo.*

object CwtConfigIndexManager {
    val Symbol by lazy { findFileBasedIndex<CwtConfigSymbolIndex>() }

    val SymbolName = ID.create<String, List<CwtConfigSymbolIndexInfo>>("cwt.config.symbol.index")
}
