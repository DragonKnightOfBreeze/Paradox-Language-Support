package icu.windea.pls.model

import java.util.*

class ParadoxLocalisationInfo(
    val name: String,
    val category: ParadoxLocalisationCategory,
    val gameType: ParadoxGameType
) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxLocalisationInfo
            && name == other.name && category == other.category && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, category, gameType)
    }

    override fun toString(): String {
        return "ParadoxLocalisationInfo(name=$name, category=$category, gameType=$gameType)"
    }
}
