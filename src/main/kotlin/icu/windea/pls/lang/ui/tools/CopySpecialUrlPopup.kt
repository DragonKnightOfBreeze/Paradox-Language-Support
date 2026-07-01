package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ep.tools.SpecialUrlProvider
import icu.windea.pls.lang.tools.SpecialUrlService
import icu.windea.pls.model.ParadoxGameType

/**
 * @see SpecialUrlProvider
 */
class CopySpecialUrlPopup(
    private val file: VirtualFile? = null,
    private val gameType: ParadoxGameType? = null,
) : BaseListPopupStep<SpecialUrlProvider>() {
    init {
        val title = ChronicleBundle.message("popup.title.copySpecialUrl")
        val providers = SpecialUrlProvider.EP_NAME.extensionList.filter { it.getUrl(file, gameType) != null }
        init(title, providers, null)
    }

    override fun getIconFor(value: SpecialUrlProvider) = value.icon

    override fun getTextFor(value: SpecialUrlProvider) = value.text + " - " + value.getUrl(file, gameType)

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: SpecialUrlProvider, finalChoice: Boolean) = doFinalStep { execute(selectedValue) }

    private fun execute(provider: SpecialUrlProvider) {
        val url = provider.getUrl(file, gameType) ?: return
        SpecialUrlService.getInstance().copyUrl(url)
    }
}
