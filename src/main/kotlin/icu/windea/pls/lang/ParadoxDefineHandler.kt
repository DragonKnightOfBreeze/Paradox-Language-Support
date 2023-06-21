package icu.windea.pls.lang

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*
import java.lang.invoke.*

object ParadoxDefineHandler {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    val definePathExpression = CwtValueExpression.resolve("filepath[common/defines/,.txt]")
    
    @Suppress("UNCHECKED_CAST")
    fun <T> getDefineValue(contextElement: PsiElement, project: Project, path: String, type: Class<T>): T? {
        val gameType = selectGameType(contextElement) ?: return null
        ProgressManager.checkCanceled()
        try {
            val selector = fileSelector(project, contextElement).contextSensitive()
            var result: Any? = null
            ParadoxFilePathSearch.search(definePathExpression, selector).processQueryAsync p@{
                ProgressManager.checkCanceled()
                val file = it.toPsiFile(project) ?: return@p true
                if(file !is ParadoxScriptFile) return@p true
                val defines = getDefinesFromFile(file)
                val defineValue = defines.getOrPut(path) {
                    val property = file.findByPath<ParadoxScriptProperty>(path, ignoreCase = false) ?: return@getOrPut null
                    val propertyValue = property.propertyValue ?: return@getOrPut null
                    ParadoxScriptDataValueResolver.resolveValue(propertyValue, conditional = false)
                }
                if(defineValue != null) {
                    result = defineValue
                    false
                } else {
                    true
                }
            }
            return result as T?
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.warn("Cannot get define value of path '$type' in $gameType", e)
            return null
        }
    }
    
    private fun getDefinesFromFile(file: ParadoxScriptFile): MutableMap<String, Any?> {
        return CachedValuesManager.getCachedValue(file, PlsKeys.cachedDefineValuesKey) {
            //invalidated on file modification
            CachedValueProvider.Result.create(mutableMapOf(), file)
        }
    }
}