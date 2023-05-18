package icu.windea.pls.core.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于指定的PSI文件和偏移查找符合条件的目标PSI元素（也包括从引用查找）。
 */
object ParadoxPsiFinder {
    object FindDefinitionOptions {
        const val BY_ROOT_KEY = 0x01
        const val BY_DEFINITION_NAME = 0x02
        const val BY_REFERENCE = 0x04
    }
    
    /**
     * @param options 从哪些位置查找对应的定义。如果传0，则表示直接向上查找即可。
     */
    fun findDefinition(file: PsiFile, offset: Int, options: Int = 0): ParadoxScriptDefinitionElement? {
        if(options == 0) {
            val definitionElement = file.findElementAt(offset) t@{
                it.parents(false).find p@{ p -> p is ParadoxScriptDefinitionElement && p.definitionInfo != null }
            }?.castOrNull<ParadoxScriptDefinitionElement?>()
            return definitionElement
        }
        val expressionElement by lazy {
            file.findElementAt(offset) {
                it.parentOfType<ParadoxScriptExpressionElement>(false)
            }?.takeIf { it.isExpression() }
        }
        val expressionReference by lazy { 
            file.findReferenceAt(offset) {
                it.element is ParadoxScriptExpressionElement && it.canResolveDefinition()
            }
        }
        if(BitUtil.isSet(options, FindDefinitionOptions.BY_ROOT_KEY)) {
            val element = expressionElement
            if(element is ParadoxScriptPropertyKey && element.isDefinitionRootKey()) {
                return element.findParentDefinition()
            }
        }
        if(BitUtil.isSet(options, FindDefinitionOptions.BY_DEFINITION_NAME)) {
            val element = expressionElement
            if(element is ParadoxScriptString && element.isDefinitionName()) {
                return element.findParentDefinition()
            }
        }
        if(BitUtil.isSet(options, FindDefinitionOptions.BY_REFERENCE)) {
            val reference = expressionReference
            val resolved = reference?.resolve()?.castOrNull<ParadoxScriptDefinitionElement>()?.takeIf { it.definitionInfo != null }
            if(resolved != null) return resolved
        }
        return null
    }
    
    fun findLocalisation(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return file.findElementAt(offset) t@{
            it.parents(false).find p@{ p -> p is ParadoxLocalisationProperty && p.localisationInfo != null }
        }?.castOrNull()
    }
    
    fun findScriptExpression(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return file.findElementAt(offset) {
            it.parentOfType<ParadoxScriptExpressionElement>(false)
        }?.takeIf { it.isExpression() }
    }
    
    fun findLocalisationColorfulText(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationColorfulText? {
        return file.findElementAt(offset) t@{
            if(fromNameToken && it.elementType != ParadoxLocalisationElementTypes.COLOR_ID) return@t null
            it.parentOfType<ParadoxLocalisationColorfulText>(false)
        }
    }
    
    fun findLocalisationLocale(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationLocale? {
        return file.findElementAt(offset) p@{
            if(fromNameToken && it.elementType != ParadoxLocalisationElementTypes.LOCALE_ID) return@p null
            it.parentOfType<ParadoxLocalisationLocale>(false)
        }
    }
    
    fun findLocalisationProperty(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationProperty? {
        return file.findElementAt(offset) p@{
            if(fromNameToken && it.elementType != ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN) return@p null
            it.parentOfType<ParadoxLocalisationProperty>(false)
        }
    }
}