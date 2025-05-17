package icu.windea.pls.model.codeInsight

data class ParadoxLocalisationCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxLocalisationCodeInsightInfo>,
    val children: List<ParadoxLocalisationCodeInsightContext> = emptyList(),
    val fromInspection: Boolean = false,
) {
    enum class Type {
        File,
        Definition,
        Modifier,
        LocalisationReference,
        SyncedLocalisationReference,
        Localisation,
    }
}
