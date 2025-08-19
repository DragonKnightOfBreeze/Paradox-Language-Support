package icu.windea.pls.lang.index

import com.intellij.util.indexing.*
import icu.windea.pls.model.indexInfo.*

object CwtConfigIndexKeys {
    val Symbol = ID.create<String, List<CwtConfigSymbolIndexInfo>>("cwt.config.symbol.index")
}
