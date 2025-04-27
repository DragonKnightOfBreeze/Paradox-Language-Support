package icu.windea.pls.localisation

import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

open class ParadoxLocalisationFileType(val language: ParadoxLocalisationLanguage = ParadoxLocalisationLanguage) : ParadoxBaseFileType(language) {
    override fun getName() = "Paradox Localisation"

    override fun getDescription() = PlsBundle.message("language.name.localisation")

    override fun getDefaultExtension() = "yml"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation

    @Suppress("CompanionObjectInExtension")
    companion object General : ParadoxLocalisationFileType() {
        @JvmField
        val INSTANCE = this

        private val MAP = listOf(Stellaris, Ck2, Ck3, Eu4, Hoi4, Ir, Vic2, Vic3).associateBy { it.language.gameType }

        fun forGameType(gameType: ParadoxGameType?): ParadoxLocalisationFileType = if (gameType == null) INSTANCE else MAP.getValue(gameType)
    }

    object Stellaris : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Stellaris)
    object Ck2 : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Ck2)
    object Ck3 : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Ck3)
    object Eu4 : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Eu4)
    object Hoi4 : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Hoi4)
    object Ir : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Ir)
    object Vic2 : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Vic2)
    object Vic3 : ParadoxLocalisationFileType(ParadoxLocalisationLanguage.Vic3)
}

