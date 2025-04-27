package icu.windea.pls.localisation.navigation

import com.intellij.ide.navigationToolbar.*
import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

class ParadoxLocalisationNavBar : StructureAwareNavBarModelExtension() {
    override val language: Language = ParadoxLocalisationLanguage.INSTANCE

    override fun getIcon(o: Any?): Icon? {
        return when {
            o is PsiElement -> o.icon
            else -> null
        }
    }

    override fun getPresentableText(o: Any?): String? {
        return when {
            o is ParadoxLocalisationPropertyList -> o.locale?.name.orAnonymous()
            o is ParadoxLocalisationProperty -> o.name
            else -> null
        }
    }
}
