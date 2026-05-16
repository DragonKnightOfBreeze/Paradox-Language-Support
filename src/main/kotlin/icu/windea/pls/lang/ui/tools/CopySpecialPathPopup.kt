package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.SpecialPathProvider
import icu.windea.pls.lang.tools.SpecialPathService
import icu.windea.pls.model.ParadoxGameType

/**
 * @see SpecialPathProvider
 */
class CopySpecialPathPopup(
    private val file: VirtualFile? = null,
    private val gameType: ParadoxGameType? = null,
) : BaseListPopupStep<SpecialPathProvider>() {
    init {
        val title = PlsBundle.message("popup.title.copySpecialPath")
        val providers = SpecialPathProvider.EP_NAME.extensionList.filter { it.getPath(file, gameType) != null }
        init(title, providers, null)
    }

    override fun getIconFor(value: SpecialPathProvider) = value.icon

    override fun getTextFor(value: SpecialPathProvider) = value.text

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: SpecialPathProvider, finalChoice: Boolean) = doFinalStep { execute(selectedValue) }

    private fun execute(provider: SpecialPathProvider) {
        val path = provider.getPath(file, gameType) ?: return
        SpecialPathService.getInstance().copyPath(path)
    }
}
