package icu.windea.pls.tool.script

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

@Suppress("unused")
object ParadoxScriptDataValueResolver {
    /**
     * 解析脚本文件的数据。跳过不合法的[PsiElement]。
     */
    fun resolve(file: PsiFile, conditional: Boolean = false, inline: Boolean = false): List<Entry<String?, Any>> {
        if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
        val rootBlock = file.findChild<ParadoxScriptRootBlock>() ?: return emptyList()
        return resolveBlock(rootBlock, conditional, inline)
    }
    
    private fun resolveBlock(block: ParadoxScriptBlockElement, conditional: Boolean = false, inline: Boolean = false): List<Entry<String?, Any>> {
        val result: MutableList<Entry<String?, Any>> = mutableListOf()
        block.processData(conditional, inline) p@{ e ->
            when {
                e is ParadoxScriptValue -> resolveValue(e)?.let { result.add(Entry(null, it)) }
                e is ParadoxScriptProperty -> resolveProperty(e)?.let { result.add(it) }
            }
            true
        }
        return result
    }
    
    fun resolveValue(value: ParadoxScriptValue, conditional: Boolean = false, inline: Boolean = false): Any? {
        return when(value) {
            is ParadoxScriptBoolean -> value.value.toBooleanYesNo()
            is ParadoxScriptInt -> value.value.toInt()
            is ParadoxScriptFloat -> value.value.toFloat()
            is ParadoxScriptString -> value.value
            is ParadoxScriptColor -> value.color
            is ParadoxScriptScriptedVariableReference -> value.referenceValue?.let { resolveValue(it) }
            is ParadoxScriptBlock -> resolveBlock(value, conditional, inline)
            else -> value.value
        }
    }
    
    fun resolveProperty(property: ParadoxScriptProperty, conditional: Boolean = false, inline: Boolean = false): Entry<String?, Any>? {
        val propertyValue = property.propertyValue
        if(propertyValue == null) return null //ignore
        
        //注意property的名字可以重复
        val key = property.name
        val value = resolveValue(propertyValue, conditional, inline) ?: return null
        return Entry(key, value)
    }
}