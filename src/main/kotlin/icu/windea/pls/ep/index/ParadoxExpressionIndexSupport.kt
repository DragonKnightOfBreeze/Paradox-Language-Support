package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 提供对需要索引的表达式信息的支持。
 */
interface ParadoxExpressionIndexSupport<T : ParadoxExpressionInfo> {
    fun id(): Byte
    
    fun type(): Class<T>
    
    fun indexScriptElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {}
    
    fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {}
    
    fun indexLocalisationCommandIdentifier(element: ParadoxLocalisationCommandIdentifier, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {}
    
    fun compressData(value: List<T>): List<T> = value
    
    fun writeData(storage: DataOutput, info: T, previousInfo: T?, gameType: ParadoxGameType)
    
    fun readData(storage: DataInput, previousInfo: T?, gameType: ParadoxGameType): T
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxExpressionIndexSupport<*>>("icu.windea.pls.expressionIndexSupport")
    }
}

fun <T : ParadoxExpressionInfo> ParadoxExpressionIndexSupport<T>.addToFileData(info: T, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
    val list = fileData.getOrPut(id().toString()) { mutableListOf() } as MutableList
    list.add(info)
}
