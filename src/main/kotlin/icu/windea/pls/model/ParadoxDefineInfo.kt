package icu.windea.pls.model

data class ParadoxDefineInfo(
    val namespace: String,
    val variable: String?,
    val gameType: ParadoxGameType,
) {
    val expression: String get() = if (variable == null) namespace else "$namespace.$variable"
}
