package icu.windea.pls.cwt.navigation

import com.intellij.ide.navigationToolbar.*
import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.settings.*
import javax.swing.*

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
            o is CwtValue && o.isBlockValue() -> o.name.truncateAndKeepQuotes(PlsInternalSettings.presentableTextLengthLimit)
            else -> null
        }
    }
}
