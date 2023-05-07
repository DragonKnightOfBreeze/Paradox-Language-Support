package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * @see ParadoxScriptFile
 * @see ParadoxScriptProperty
 */
interface ParadoxScriptDefinitionElement : ParadoxScriptNamedElement, ParadoxScriptMemberElement {
    fun getStub(): ParadoxScriptDefinitionElementStub<out ParadoxScriptDefinitionElement>?
    
    /**
     * 注意：如果这个对象是定义，这里得到的是定义的顶级键名（rootKey），而不一定是定义的名字（definitionName）。
     */
    override fun getName(): String
    
    override fun getNameIdentifier(): PsiElement? = null
    
    val block: ParadoxScriptBlockElement?
    val variableList: List<ParadoxScriptScriptedVariable>
        get() {
            return block?.scriptedVariableList.orEmpty()
        }
    val valueList: List<ParadoxScriptValue>
        get() {
            return buildList { block?.processValue(conditional = true, inline = true) { add(it) } }
        }
    val propertyList: List<ParadoxScriptProperty>
        get() {
            return buildList { block?.processProperty(conditional = true, inline = true) { add(it) } }
        }
}