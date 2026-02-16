package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo

/**
 * 可能是定义的 PSI 元素。
 *
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 * @see ParadoxDefinitionInfo
 */
interface ParadoxDefinitionElement : PsiNameIdentifierOwner, NavigatablePsiElement, ParadoxScriptMemberContainer {
    /**
     * 注意：这里得到的不一定是定义的名字（[ParadoxDefinitionInfo.name]）。
     * 也可能只是属性定义的类型键（[ParadoxDefinitionInfo.typeKey]），或者注入定义的表达式（[ParadoxDefinitionInjectionInfo.expression]）。
     */
    override fun getName(): String

    override fun getNameIdentifier(): PsiElement? = null

    val block: ParadoxScriptBlockElement?
}
