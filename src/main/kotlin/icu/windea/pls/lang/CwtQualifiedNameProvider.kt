package icu.windea.pls.lang

import com.intellij.ide.actions.QualifiedNameProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyKey

/**
 * 用于复制引用（`Edit > Copy Path/Reference... > Copy Reference`）。
 *
 * 说明：
 * - 选项（[CwtOption]） -> 选项的名字。
 * - 属性（[CwtProperty]） - 属性的名字。
 */
class CwtQualifiedNameProvider : QualifiedNameProvider {
    override fun adjustElementToCopy(element: PsiElement): PsiElement? {
        return when (element) {
            is CwtOptionKey -> element.parent
            is CwtPropertyKey -> element.parent
            else -> null
        }
    }

    override fun getQualifiedName(element: PsiElement): String? {
        return when (element) {
            is CwtOption -> element.name
            is CwtProperty -> element.name
            else -> null
        }
    }

    override fun qualifiedNameToElement(fqn: String, project: Project): PsiElement? {
        return null // 不处理
    }
}
