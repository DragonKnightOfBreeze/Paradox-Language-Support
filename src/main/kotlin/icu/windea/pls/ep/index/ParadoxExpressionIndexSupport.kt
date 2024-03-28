package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.util.*
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

private val HINTS_FOR_INDEXING = PsiReferenceService.Hints()
private val CACHE_KEY_FOR_INDEXING = createKey<CachedValue<Array<out PsiReference>>>("ParadoxExpressionIndexSupport.CACHE_KEY_FOR_INDEXING")

//use another field to prevent IDE freezing problems
val PsiElement.referencesForIndexing: Array<out PsiReference> get() = doGetReferencesForIndexing()

private fun PsiElement.doGetReferencesForIndexing(): Array<out PsiReference> {
    return CachedValuesManager.getCachedValue(this, CACHE_KEY_FOR_INDEXING) {
        val value = PsiReferenceService.getService().getReferences(this, HINTS_FOR_INDEXING).toTypedArray()
        CachedValueProvider.Result.create(value, PsiModificationTracker.MODIFICATION_COUNT)
    }
}