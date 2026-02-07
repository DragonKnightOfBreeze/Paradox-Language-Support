package icu.windea.pls.model

import icu.windea.pls.lang.util.ParadoxDefineManager

data class ParadoxDefineInfo(
    val namespace: String,
    val variable: String?,
    val gameType: ParadoxGameType,
) {
    val expression: String get() = ParadoxDefineManager.getExpression(namespace, variable)
}
