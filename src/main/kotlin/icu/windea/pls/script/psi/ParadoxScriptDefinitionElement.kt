package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * 可能是定义的 PSI 元素。
 *
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 */
interface ParadoxScriptDefinitionElement : ParadoxScriptNamedElement, ParadoxScriptMember {
    /**
     * 注意：如果这个对象是定义，这里得到的是定义的类型键（typeKey），而不一定是定义的名字（definitionName）。
     */
    override fun getName(): String

    override fun getNameIdentifier(): PsiElement? = null

    val block: ParadoxScriptBlockElement?
}
