package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.model.ParadoxDefinitionInfo

/**
 * 可能是定义的 PSI 元素。
 *
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 * @see ParadoxDefinitionInfo
 */
interface ParadoxDefinitionElement : PsiNameIdentifierOwner, NavigatablePsiElement, ParadoxScriptMemberContainer {
    /**
     * 注意：如果是定义，这里得到的是定义的类型键（[ParadoxDefinitionInfo.typeKey]），不一定是定义的名字（[ParadoxDefinitionInfo.name]）。
     */
    override fun getName(): String

    override fun getNameIdentifier(): PsiElement? = null

    val block: ParadoxScriptBlockElement?
}
