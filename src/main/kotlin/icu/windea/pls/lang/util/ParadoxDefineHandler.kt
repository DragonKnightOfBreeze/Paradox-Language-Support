package icu.windea.pls.lang.util

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.data.*
import icu.windea.pls.script.psi.*
import java.lang.invoke.*

object ParadoxDefineHandler {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    val definePathExpression = CwtDataExpression.resolve("filepath[common/defines/,.txt]", false)
    
    @Suppress("UNCHECKED_CAST")
    fun <T> getDefineValue(contextElement: PsiElement, project: Project, path: String, type: Class<T>): T? {
        val gameType = selectGameType(contextElement) ?: return null
        try {
            val selector = fileSelector(project, contextElement).contextSensitive()
            var result: Any? = null
            ParadoxFilePathSearch.search(definePathExpression, selector).processQueryAsync p@{
                ProgressManager.checkCanceled()
                val file = it.toPsiFile(project) ?: return@p true
                if(file !is ParadoxScriptFile) return@p true
                val defines = getDefinesFromFile(file)
                val defineValue = defines.getOrPut(path) action@{
                    val property = file.findByPath(path, ParadoxScriptProperty::class.java, ignoreCase = false) ?: return@action null
                    val propertyValue = property.propertyValue ?: return@action null
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
            logger.warn("Cannot get define value of path '$type' for $gameType", e)
            return null
        }
    }
    
    private fun getDefinesFromFile(file: ParadoxScriptFile): MutableMap<String, Any?> {
        return CachedValuesManager.getCachedValue(file, PlsKeys.cachedDefineValues) {
            //invalidated on file modification
            CachedValueProvider.Result.create(mutableMapOf(), file)
        }
    }
}