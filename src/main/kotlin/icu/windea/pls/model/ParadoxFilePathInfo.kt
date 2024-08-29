package icu.windea.pls.model

import java.util.*

class ParadoxFilePathInfo(
    val directory: String,
    val gameType: ParadoxGameType,
    val included: Boolean
) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFilePathInfo
            && directory == other.directory && gameType == other.gameType && included == other.included
    }
    
    override fun hashCode(): Int {
        return Objects.hash(directory, gameType, included)
    }
}
