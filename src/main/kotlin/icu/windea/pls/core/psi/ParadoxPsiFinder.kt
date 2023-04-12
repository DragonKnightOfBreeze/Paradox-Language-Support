package icu.windea.pls.core.psi

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于指定的PSI文件和偏移查找符合条件的目标PSI元素以及它的引用。
 */
object ParadoxPsiFinder {
    fun findDefinition(file: PsiFile, offset: Int): ParadoxScriptDefinitionElement? {
        return file.findElementAt(offset) t@{
            it.parentsOfType<ParadoxScriptDefinitionElement>(true)
                .find { p -> p.definitionInfo != null }
        }?.castOrNull<ParadoxScriptDefinitionElement?>()
    }
    
    fun findLocalisation(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return file.findElementAt(offset) t@{
            it.parentOfType<ParadoxLocalisationProperty>()
        }?.takeIf { it.localisationInfo != null }
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