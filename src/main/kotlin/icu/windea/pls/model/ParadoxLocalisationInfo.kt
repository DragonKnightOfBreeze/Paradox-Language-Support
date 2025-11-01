package icu.windea.pls.model

import java.util.*

/**
 * 本地化信息。
 *
 *
 */
class ParadoxLocalisationInfo(
    val name: String,
    val type: ParadoxLocalisationType,
    val gameType: ParadoxGameType
) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxLocalisationInfo
            && name == other.name && type == other.type && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, type, gameType)
    }

    override fun toString(): String {
        return "ParadoxLocalisationInfo(name=$name, type=$type, gameType=$gameType)"
    }
}
