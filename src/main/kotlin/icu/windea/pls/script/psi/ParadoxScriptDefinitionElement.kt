package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 */
interface ParadoxScriptDefinitionElement : ParadoxScriptNamedElement, ParadoxScriptMemberElement {
    /**
     * 注意：如果这个对象是定义，这里得到的是定义的顶级键名（rootKey），而不一定是定义的名字（definitionName）。
     */
    override fun getName(): String

    override fun getNameIdentifier(): PsiElement? = null

    val block: ParadoxScriptBlockElement?
}
