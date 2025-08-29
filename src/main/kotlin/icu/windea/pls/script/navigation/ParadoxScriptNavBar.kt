package icu.windea.pls.script.navigation

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.icon
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember
import javax.swing.Icon

class ParadoxScriptNavBar : StructureAwareNavBarModelExtension() {
    override val language: Language = ParadoxScriptLanguage

    override fun getIcon(o: Any?): Icon? {
        return when {
            o is PsiElement -> o.icon
            else -> null
        }
    }

    override fun getPresentableText(o: Any?): String? {
        return when {
            o is ParadoxScriptScriptedVariable -> "@" + o.name
            o is ParadoxScriptProperty -> o.definitionInfo?.name?.or?.anonymous() ?: o.name
            o is ParadoxScriptValue && o.isBlockMember() -> o.value.truncateAndKeepQuotes(PlsFacade.getInternalSettings().presentableTextLengthLimit)
            o is ParadoxScriptParameterCondition -> o.conditionExpression?.let { "[$it]" }
            else -> null
        }
    }

    //FIXME 没有排除作为属性的值的值
}
