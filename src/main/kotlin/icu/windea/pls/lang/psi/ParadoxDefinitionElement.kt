package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptMemberContext
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 可能是定义的 PSI 元素。
 *
 * 注意：如果更改了继承关系，需要对应地更改 [ParadoxPsiElementVisitor]。
 *
 * @see ParadoxDefinitionInfo
 * @see ParadoxPsiElementVisitor
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 */
interface ParadoxDefinitionElement : PsiNamedElement, PsiNameIdentifierOwner, NavigatablePsiElement, ParadoxScriptMemberContext {
    /**
     * 得到 PSI 元素的名字。注意这不一定是定义的名字。
     *
     * 可能是：
     * - 定义的名字（[ParadoxDefinitionInfo.name]）。
     * - 属性定义的类型键（[ParadoxDefinitionInfo.typeKey]）。
     * - 定义注入的表达式（[ParadoxDefinitionInjectionInfo.expression]）。
     * - 不属于以上任一种情况，只是一个普通的文件名、属性名等。
     */
    override fun getName(): String

    override fun getNameIdentifier(): PsiElement? = null

    val block: ParadoxScriptMemberContainer?
}
