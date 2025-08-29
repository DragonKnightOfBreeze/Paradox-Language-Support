package icu.windea.pls.localisation.navigation

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import javax.swing.Icon

class ParadoxLocalisationNavBar : StructureAwareNavBarModelExtension() {
    override val language: Language = ParadoxLocalisationLanguage

    override fun getIcon(o: Any?): Icon? {
        return when {
            o is PsiElement -> o.icon
            else -> null
        }
    }

    override fun getPresentableText(o: Any?): String? {
        return when {
            o is ParadoxLocalisationPropertyList -> o.locale?.name.or.anonymous()
            o is ParadoxLocalisationProperty -> o.name
            else -> null
        }
    }
}
