package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType

@Suppress("CanBeParameter", "unused")
class ParadoxGameTypeListPopup(
    val allGameTypes: List<ParadoxGameType>,
    val currentGameType: ParadoxGameType?,
    val onSelected: (selectedValue: ParadoxGameType) -> Unit = {},
) : BaseListPopupStep<ParadoxGameType>(PlsBundle.message("ui.selectGameType.title"), allGameTypes) {
    private val defaultOptionIndex = if (currentGameType == null) -1 else allGameTypes.indexOf(currentGameType)
    private var callback = onSelected

    override fun getTextFor(value: ParadoxGameType) = when (currentGameType) {
        value -> value.title
        else -> PlsBundle.message("ui.selectGameType.current", value.title)
    }

    override fun getDefaultOptionIndex() = defaultOptionIndex

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: ParadoxGameType, finalChoice: Boolean) = doFinalStep { callback(selectedValue) }

    fun onSelected(onSelected: (selectedValue: ParadoxGameType) -> Unit) {
        callback = onSelected
    }
}
