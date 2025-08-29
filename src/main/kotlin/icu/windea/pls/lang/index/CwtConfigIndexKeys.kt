package icu.windea.pls.lang.index

import com.intellij.util.indexing.ID
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo

object CwtConfigIndexKeys {
    val Symbol = ID.create<String, List<CwtConfigSymbolIndexInfo>>("cwt.config.symbol.index")
}
