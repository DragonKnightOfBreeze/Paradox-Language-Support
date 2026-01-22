package icu.windea.pls.lang.codeInsight.hints

import icu.windea.pls.lang.settings.PlsInternalSettings

data class ParadoxHintsSettings(
    var showScopeContextOnlyIfIsChanged: Boolean = true,
    var localisationTextLengthLimit: Int = PlsInternalSettings.getInstance().localisationLengthLimitForInlay,
    var iconHeightLimit: Int = PlsInternalSettings.getInstance().iconHeightLimitForInlay,
)
