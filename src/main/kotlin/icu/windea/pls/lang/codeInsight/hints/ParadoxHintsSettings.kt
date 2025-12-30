package icu.windea.pls.lang.codeInsight.hints

import icu.windea.pls.lang.settings.PlsInternalSettings

data class ParadoxHintsSettings(
    var showScopeContextOnlyIfIsChanged: Boolean = true,
    var textLengthLimit: Int = PlsInternalSettings.getInstance().textLengthLimitForInlay,
    var iconHeightLimit: Int = PlsInternalSettings.getInstance().iconHeightLimitForInlay,
)
