package icu.windea.pls.lang.codeInsight.hints

import icu.windea.pls.base.settings.ChronicleInternalSettings

data class ParadoxHintsSettings(
    var showScopeContextOnlyIfIsChanged: Boolean = true,
    var localisationTextLengthLimit: Int = ChronicleInternalSettings.getInstance().localisationLengthLimitForInlay,
    var iconHeightLimit: Int = ChronicleInternalSettings.getInstance().iconHeightLimitForInlay,
)
