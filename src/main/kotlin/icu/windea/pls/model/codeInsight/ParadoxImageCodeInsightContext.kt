package icu.windea.pls.model.codeInsight

data class ParadoxImageCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxImageCodeInsightInfo>,
    val children: List<ParadoxImageCodeInsightContext> = emptyList(),
    val fromInspection: Boolean = false,
) {
    enum class Type {
        File,
        Definition,
        Modifier,
    }
}
