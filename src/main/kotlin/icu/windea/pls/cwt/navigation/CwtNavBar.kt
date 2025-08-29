package icu.windea.pls.cwt.navigation

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.icon
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue
import javax.swing.Icon

class CwtNavBar : StructureAwareNavBarModelExtension() {
    override val language: Language = CwtLanguage

    override fun getIcon(o: Any?): Icon? {
        return when {
            o is PsiElement -> o.icon
            else -> null
        }
    }

    override fun getPresentableText(o: Any?): String? {
        return when {
            o is CwtProperty -> o.name
            o is CwtValue && o.isBlockValue() -> o.name.truncateAndKeepQuotes(PlsFacade.getInternalSettings().presentableTextLengthLimit)
            else -> null
        }
    }
}
