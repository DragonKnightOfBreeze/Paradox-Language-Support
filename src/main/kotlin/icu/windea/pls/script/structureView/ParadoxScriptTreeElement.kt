package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.script.navigation.ParadoxScriptNavigationManager
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.tagType
import javax.swing.Icon

abstract class ParadoxScriptTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxScriptNavigationManager.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxScriptNavigationManager.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxScriptNavigationManager.getLocationString(element)
    }

    protected fun PsiElement.toTreeElement(): ParadoxScriptTreeElement<out PsiElement>? {
        return when (this) {
            is ParadoxScriptScriptedVariable -> ParadoxScriptScriptedVariableTreeElement(this)
            is ParadoxScriptProperty -> ParadoxScriptPropertyTreeElement(this)
            is ParadoxScriptValue -> {
                // 不要在结构视图中显示作为标签的字符串
                if (tagType() != null) return null
                ParadoxScriptValueTreeElement(this)
            }
            is ParadoxScriptParameterCondition -> ParadoxScriptParameterConditionTreeElement(this)
            else -> null
        }
    }
}
