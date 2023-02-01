package icu.windea.pls.config.core

import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxInlineScriptHandler {
    private const val inlineScriptPathExpression = "common/inline_scripts/,.txt"
    
    fun isGameTypeSupport(gameType: ParadoxGameType): Boolean {
        return gameType == ParadoxGameType.Stellaris
    }
    
    @JvmStatic
    fun getInfo(element: ParadoxScriptPropertyKey): ParadoxInlineScriptInfo? {
        //注意：element.stub可能会导致ProcessCanceledException
        ProgressManager.checkCanceled()
        if(!element.isExpression()) return null
        element.stub?.inlineScriptInfo?.let { return it }
        return getInfoFromCache(element)
    }
    
    private fun getInfoFromCache(element: ParadoxScriptPropertyKey): ParadoxInlineScriptInfo? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedInlineScriptInfoKey) {
            val file = element.containingFile
            val value = resolveInfo(element, file)
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    @JvmStatic
    fun resolveInfo(element: ParadoxScriptPropertyKey, file: PsiFile = element.containingFile): ParadoxInlineScriptInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupport(gameType)) return null
        val value = element.value
        if(value != "inline_script") return null
        val matchType = CwtConfigMatchType.STATIC
        val configs = ParadoxCwtConfigHandler.resolveConfigs(element, matchType = matchType)
        if(configs.isEmpty()) return null
        var expression: String? = null
        if(configs.any { config -> isExpressionConfig(config) }) {
            expression = element.propertyValue?.castOrNull<ParadoxScriptString>()?.value
        } else {
            //直接使用查找到的第一个
            element.propertyValue?.castOrNull<ParadoxScriptBlock>()?.processProperty(includeConditional = true) {p ->
                val pConfigs = ParadoxCwtConfigHandler.resolveConfigs(p, matchType = matchType)
                if(pConfigs.isEmpty()) return@processProperty true
                if(pConfigs.any { pConfig -> isExpressionConfig(pConfig) }) {
                    expression = p.propertyValue?.castOrNull<ParadoxScriptString>()?.value
                    return@processProperty false
                } else {
                    return@processProperty true
                }
            }
        }
        val finalExpression = expression ?: return null
        return ParadoxInlineScriptInfo(finalExpression, gameType)
    }
    
    private fun isExpressionConfig(it: CwtDataConfig<*>): Boolean {
        return it.optionValues?.any { it.stringValue == "inline_script_expression" } == true
    }
    
    @JvmStatic
    fun isInlineScriptFile(file: ParadoxScriptFile): Boolean {
        return getInlineScriptExpression(file) != null
    }
    
    @JvmStatic
    fun getInlineScriptExpression(file: ParadoxScriptFile) : String? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupport(gameType)) return null
        return CwtPathExpressionType.FilePath.extract(inlineScriptPathExpression, fileInfo.path.path)
            ?.takeIfNotEmpty()
    }
    
    /**
     * 德奥内联脚本的使用位置对应的属性。如果使用存在冲突，则返回null。
     */
    @JvmStatic
    fun getInlineScriptProperty(file: ParadoxScriptFile): ParadoxScriptProperty? {
        val pointer = CachedValuesManager.getCachedValue(file, PlsKeys.cachedInlineScriptProperty) {
            val value = doGetInlineScriptProperty(file)
            val modificationTracker = file.project.service<ParadoxModificationTrackerProvider>().InlineScript
            CachedValueProvider.Result.create(value?.createPointer(), modificationTracker.modificationCount)
        }
        return pointer.element
    }
    
    private fun doGetInlineScriptProperty(file: ParadoxScriptFile): ParadoxScriptProperty? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupport(gameType)) return null
        val expression = getInlineScriptExpression(file) ?: return null
        val project = file.project
        val scope = GlobalSearchScope.allScope(project)
        var element: ParadoxScriptPropertyKey? = null
        val configs: MutableList<CwtDataConfig<*>> = mutableListOf()
        ParadoxInlineScriptIndex.processAllElements(expression, project, scope) { e ->
            if(configs.isEmpty()) {
                val eConfigs = ParadoxCwtConfigHandler.resolveConfigs(e)
                if(eConfigs.isNotEmpty()) {
                    eConfigs.mapTo(configs) { it.resolved() }
                    true
                } else {
                    if(eConfigs.any { configs.contains(it) }) {
                        if(element == null) {
                            element = e
                        }
                        true
                    } else {
                        element = null
                        false
                    }
                }
            } else {
                true
            }
        }
        return element?.parent as? ParadoxScriptProperty
    }
    
    /**
     * 检查内联脚本的使用是否存在冲突。如果是内联脚本且存在冲突，则返回true。
     */
    @JvmStatic
    fun checkInlineScript(file: ParadoxScriptFile): Boolean {
        return CachedValuesManager.getCachedValue(file, PlsKeys.cachedInlineScriptCheckResult) {
            val value = doCheckInlineScript(file)
            val modificationTracker = file.project.service<ParadoxModificationTrackerProvider>().InlineScript
            CachedValueProvider.Result.create(value, modificationTracker.modificationCount)
        }
    }
    
    private fun doCheckInlineScript(file: ParadoxScriptFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupport(gameType)) return false
        val expression = getInlineScriptExpression(file) ?: return false
        val project = file.project
        val scope = GlobalSearchScope.allScope(project)
        var result = false
        val configs: MutableList<CwtDataConfig<*>> = mutableListOf()
        ParadoxInlineScriptIndex.processAllElements(expression, project, scope) { e ->
            if(configs.isEmpty()) {
                val eConfigs = ParadoxCwtConfigHandler.resolveConfigs(e)
                if(eConfigs.isNotEmpty()) {
                    eConfigs.mapTo(configs) { it.resolved() }
                    true
                } else {
                    if(eConfigs.any { configs.contains(it) }) {
                        true
                    } else {
                        result = true
                        false
                    }
                }
            } else {
                true
            }
        }
        return result
    }
}