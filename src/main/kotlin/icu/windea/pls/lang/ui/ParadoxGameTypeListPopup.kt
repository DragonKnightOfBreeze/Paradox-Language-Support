package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType

@Suppress("CanBeParameter", "unused")
class ParadoxGameTypeListPopup(
    val allGameTypes: List<ParadoxGameType>,
    val currentGameType: ParadoxGameType?,
) : BaseListPopupStep<ParadoxGameType>(PlsBundle.message("ui.selectGameType.title"), allGameTypes) {
    var selectedGameType: ParadoxGameType? = null

    private val defaultOptionIndex = if (currentGameType == null) -1 else allGameTypes.indexOf(currentGameType)

    override fun getTextFor(value: ParadoxGameType): String {
        return if (currentGameType == value) value.title else PlsBundle.message("ui.selectGameType.current", value.title)
    }

    override fun getDefaultOptionIndex() = defaultOptionIndex

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: ParadoxGameType, finalChoice: Boolean): PopupStep<*>? {
        selectedGameType = selectedValue
        return FINAL_CHOICE
    }
}
