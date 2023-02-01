package icu.windea.pls.config.core

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import java.lang.invoke.*

object ParadoxDefineHandler {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> getDefineValue(contextElement: PsiElement, project: Project, path: String, type: Class<T>): T? {
        val gameType = selectGameType(contextElement) ?: return null
        ProgressManager.checkCanceled()
        try {
            val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement)
            val query = ParadoxFilePathSearch.search("common/defines/,.txt", project, CwtPathExpressionType.FilePath, selector = selector)
            var result: Any? = null
            query.processQuery {
                val file = it.toPsiFile<ParadoxScriptFile>(project) ?: return@processQuery true
                val defines = getDefinesFromFile(file)
                val defineValue = defines.getOrPut(path) { 
                    val property = file.findByPath<ParadoxScriptProperty>(path, ignoreCase = false) ?: return@getOrPut null
                    val propertyValue = property.propertyValue ?: return@getOrPut null
                    ParadoxScriptDataResolver.resolveValue(propertyValue)
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