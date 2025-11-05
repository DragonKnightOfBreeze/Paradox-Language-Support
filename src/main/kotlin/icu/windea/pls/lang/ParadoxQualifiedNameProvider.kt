package icu.windea.pls.lang

import com.intellij.ide.actions.QualifiedNameProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName

/**
 * 用于复制引用（`Edit > Copy Path/Reference... > Copy Reference`）。
 *
 * 说明：
 * - 封装变量（[ParadoxScriptScriptedVariable]） -> 封装变量的名字。
 * - 脚本属性（[ParadoxScriptProperty]） - 定义的名字（如果是定义），或者属性的名字。
 * - 本地化属性（[ParadoxLocalisationProperty]） - 属性的名字。
 */
class ParadoxQualifiedNameProvider : QualifiedNameProvider {
    override fun adjustElementToCopy(element: PsiElement): PsiElement? {
        return when (element) {
            is ParadoxScriptScriptedVariableName -> element.parent
            is ParadoxScriptPropertyKey -> element.parent
            is ParadoxLocalisationPropertyKey -> element.parent
            else -> null
        }
    }

    override fun getQualifiedName(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptScriptedVariable -> element.name
            is ParadoxScriptProperty -> element.definitionInfo?.name ?: element.name
            is ParadoxLocalisationProperty -> element.name
            else -> null
        }
    }

    override fun qualifiedNameToElement(fqn: String, project: Project): PsiElement? {
        return null // 不处理
    }
}

