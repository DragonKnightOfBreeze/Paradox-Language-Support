package icu.windea.pls.lang.codeInsight

data class ParadoxLocalisationCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxLocalisationCodeInsightInfo> = emptyList(),
    val children: List<ParadoxLocalisationCodeInsightContext> = emptyList(),
    val fromInspection: Boolean = false,
) {
    enum class Type {
        File,
        Definition,
        Modifier,
        LocalisationReference, // 注意：可以与定义的相关本地化重复
        SyncedLocalisationReference,
        Localisation,
        ;

        fun isGroup(): Boolean {
            return this == Definition || this == Modifier
        }

        fun isReference(): Boolean {
            return this == LocalisationReference || this == SyncedLocalisationReference
        }
    }
}
