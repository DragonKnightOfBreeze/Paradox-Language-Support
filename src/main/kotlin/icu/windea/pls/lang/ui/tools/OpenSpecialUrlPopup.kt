package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.SpecialUrlProvider
import icu.windea.pls.lang.tools.SpecialUrlService
import icu.windea.pls.model.ParadoxGameType

class OpenSpecialUrlPopup(
    private val file: VirtualFile? = null,
    private val gameType: ParadoxGameType? = null,
) : BaseListPopupStep<SpecialUrlProvider>() {
    init {
        val title = PlsBundle.message("popup.title.openSpecialUrl")
        val providers = SpecialUrlProvider.EP_NAME.extensionList.filter { it.getUrl(file, gameType) != null }
        init(title, providers, null)
    }

    override fun getIconFor(value: SpecialUrlProvider) = value.icon

    override fun getTextFor(value: SpecialUrlProvider) = value.text

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: SpecialUrlProvider, finalChoice: Boolean) = doFinalStep { execute(selectedValue) }

    private fun execute(provider: SpecialUrlProvider) {
        val url = provider.getUrl(file, gameType) ?: return
        SpecialUrlService.getInstance().openUrl(url)
    }
}
