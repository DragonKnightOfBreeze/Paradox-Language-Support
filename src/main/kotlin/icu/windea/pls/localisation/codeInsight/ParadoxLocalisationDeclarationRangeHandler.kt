package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.hint.DeclarationRangeHandler
import com.intellij.openapi.util.TextRange
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 用于在本地化文件中提供上下文信息（语言环境标识，如，`l_english:`）。
 */
class ParadoxLocalisationDeclarationRangeHandler : DeclarationRangeHandler<ParadoxLocalisationPropertyList> {
    override fun getDeclarationRange(container: ParadoxLocalisationPropertyList): TextRange? {
        return container.locale?.textRange
    }
}
