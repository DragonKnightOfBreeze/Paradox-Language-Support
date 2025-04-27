package icu.windea.pls.localisation

import icu.windea.pls.lang.*
import icu.windea.pls.model.*

sealed class ParadoxLocalisationLanguage(ID: String, gameType: ParadoxGameType? = null) : ParadoxBaseLanguage(ID, gameType) {
    override fun getBaseLanguage() = if (gameType != null) ParadoxLocalisationLanguage else ParadoxBaseLanguage

    companion object General : ParadoxLocalisationLanguage("PARADOX_LOCALISATION") {
        @JvmField
        val INSTANCE = this

        private val MAP = listOf(Stellaris, Ck2, Ck3, Eu4, Hoi4, Ir, Vic2, Vic3).associateBy { it.gameType }

        fun forGameType(gameType: ParadoxGameType?): ParadoxLocalisationLanguage = if (gameType == null) INSTANCE else MAP.getValue(gameType)
    }

    object Stellaris : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_STELLARIS", ParadoxGameType.Stellaris)
    object Ck2 : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_CK2", ParadoxGameType.Ck2)
    object Ck3 : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_CK3", ParadoxGameType.Ck3)
    object Eu4 : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_EU4", ParadoxGameType.Eu4)
    object Hoi4 : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_HOI4", ParadoxGameType.Hoi4)
    object Ir : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_IR", ParadoxGameType.Ir)
    object Vic2 : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_VIC2", ParadoxGameType.Vic2)
    object Vic3 : ParadoxLocalisationLanguage("PARADOX_LOCALISATION_VIC3", ParadoxGameType.Vic3)
}
