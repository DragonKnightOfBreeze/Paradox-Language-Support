package icu.windea.pls.csv.psi

import com.intellij.psi.util.*
import icu.windea.pls.core.collections.*

fun ParadoxCsvColumn.isHeaderColumn(): Boolean {
    val parent = parent
    return parent is ParadoxCsvHeader
}

fun ParadoxCsvColumn.getHeaderColumn(): ParadoxCsvColumn? {
    val parent = parent
    if (parent is ParadoxCsvHeader) return null
    val index = parent.firstChild?.siblings()?.filterIsInstance<ParadoxCsvColumn>()?.indexOfFirst { it == this }
    val rowHeader = parent?.parent?.firstChild?.siblings()?.findIsInstance<ParadoxCsvHeader>()
    if (rowHeader == null) return null
    rowHeader.firstChild?.siblings()?.filterIsInstance<ParadoxCsvColumn>()?.forEachIndexed { i, c -> if (i == index) return c }
    return null
}
