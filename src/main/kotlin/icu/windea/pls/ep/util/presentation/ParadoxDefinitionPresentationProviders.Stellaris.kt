package icu.windea.pls.ep.util.presentation

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.annotations.WithDefinitionType
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.util.presentation.StellarisTechnologyCardBuilder
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import javax.swing.JComponent

/**
 * 科技的图形表示（科技卡）。
 */
@WithGameType(ParadoxGameType.Stellaris)
@WithDefinitionType(ParadoxDefinitionTypes.Technology)
class StellarisTechnologyCardPresentation(element: ParadoxScriptDefinitionElement) : ParadoxDefinitionPresentationBase(element) {
    override fun createComponent(): JComponent? {
        return runCatchingCancelable { doCreateComponent() }.onFailure { thisLogger().warn(it) }.getOrNull()
    }

    private fun doCreateComponent(): JComponent? {
        val element = element ?: return null
        return StellarisTechnologyCardBuilder(element).build()
    }
}
