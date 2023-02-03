package icu.windea.pls.tool.script

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

/**
 * Paradox脚本文件的数据的值的解析器。
 */
@Suppress("unused")
object ParadoxScriptDataValueResolver {
    /**
     * 解析脚本文件的数据。跳过不合法的[PsiElement]。
     */
    fun resolve(file: PsiFile): List<BlockEntry<String?, Any>> {
        if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
        val rootBlock = file.findChild<ParadoxScriptRootBlock>() ?: return emptyList()
        return resolveBlock(rootBlock)
    }
    
    private fun resolveBlock(block: ParadoxScriptBlockElement): List<BlockEntry<String?, Any>> {
        val result: MutableList<BlockEntry<String?, Any>> = SmartList()
        block.processData(includeConditional = true, inline = true) p@{ e ->
            when {
                e is ParadoxScriptValue -> resolveValue(e)?.let { result.add(BlockEntry(null, it)) }
                e is ParadoxScriptProperty -> resolveProperty(e)?.let { result.add(it) }
            }
            true
        }
        return result
    }
    
    fun resolveValue(value: ParadoxScriptValue): Any? {
        return when(value) {
            is ParadoxScriptBoolean -> value.value.toBooleanYesNo()
            is ParadoxScriptInt -> value.value.toInt()
            is ParadoxScriptFloat -> value.value.toFloat()
            is ParadoxScriptString -> value.value
            is ParadoxScriptColor -> value.color
            is ParadoxScriptScriptedVariableReference -> value.referenceValue?.let { resolveValue(it) }
            is ParadoxScriptBlock -> resolveBlock(value)
            else -> value.value
        }
    }
    
    fun resolveProperty(property: ParadoxScriptProperty): BlockEntry<String?, Any>? {
        val propertyValue = property.propertyValue
        if(propertyValue == null) return null //ignore
        
        //注意property的名字可以重复
        val key = property.name
        val value = resolveValue(propertyValue) ?: return null
        return BlockEntry(key, value)
    }
}
