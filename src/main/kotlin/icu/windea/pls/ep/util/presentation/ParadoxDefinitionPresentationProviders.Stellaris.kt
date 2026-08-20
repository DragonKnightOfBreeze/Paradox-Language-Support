package icu.windea.pls.ep.util.presentation

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.base.annotations.ForDefinitionType
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.presentation.StellarisTechnologyCardBuilder
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import javax.swing.JComponent

/**
 * 科技的图形展示（科技卡）。
 */
@ForGameType(ParadoxGameType.Stellaris)
@ForDefinitionType(ParadoxDefinitionTypes.technology)
class StellarisTechnologyCardPresentation(element: ParadoxDefinitionElement) : ParadoxDefinitionPresentationBase(element) {
    override fun createComponent(): JComponent? {
        return runCatchingCancelable { doCreateComponent() }.onFailure { thisLogger().warn(it) }.getOrNull()
    }

    private fun doCreateComponent(): JComponent? {
        val element = element ?: return null
        return StellarisTechnologyCardBuilder(element).build()
    }
}
