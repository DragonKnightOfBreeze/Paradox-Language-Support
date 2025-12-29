package icu.windea.pls.lang.codeInsight.hints

import icu.windea.pls.lang.settings.PlsInternalSettings

data class ParadoxHintsSettings(
    var showSubtypes: Boolean = true,
    var showOnlyIfScopeIsChanged: Boolean = true,
    var textLengthLimit: Int = PlsInternalSettings.getInstance().textLengthLimitForInlay,
    var iconHeightLimit: Int = PlsInternalSettings.getInstance().iconHeightLimitForInlay,
)
