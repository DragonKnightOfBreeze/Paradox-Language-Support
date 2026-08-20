package icu.windea.pls.script.psi

import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.lang.psi.ParadoxDefinitionElementVisitor
import icu.windea.pls.lang.psi.ParadoxExpressionElementVisitor
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo

/**
 * 可能是定义的 PSI 元素。
 *
 * 注意：如果更改了继承关系，需要对应地更改 [ParadoxExpressionElementVisitor]。
 *
 * @see ParadoxDefinitionInfo
 * @see ParadoxDefinitionElementVisitor
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 */
interface ParadoxDefinitionElement : PsiNameIdentifierOwner, NavigatablePsiElement, ParadoxScriptMemberContext {
    /**
     * 得到 PSI 元素的名字。注意这不一定是定义的名字。
     *
     * 可能是：
     * - 定义的名字（[ParadoxDefinitionInfo.name]）。
     * - 属性定义的类型键（[ParadoxDefinitionInfo.typeKey]）。
     * - 定义注入的表达式（[ParadoxDefinitionInjectionInfo.expression]）。
     */
    override fun getName(): String

    /**
     * 重命名定义。注意如果这是定义，将会修改定义的名字对应的 PSI 元素。
     * 否则，将会直接抛出 [IncorrectOperationException]。
     */
    override fun setName(name: @NlsSafe String): PsiElement? {
        TODO("Not yet implemented")
    }

    override fun getNameIdentifier(): PsiElement? = null

    val block: ParadoxScriptMemberContainer?
}

