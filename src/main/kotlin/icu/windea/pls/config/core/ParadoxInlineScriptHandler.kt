package icu.windea.pls.config.core

import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
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
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
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
            element.propertyValue?.castOrNull<ParadoxScriptBlock>()?.processProperty { p ->
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
    fun getInlineScript(expression: String, context: PsiElement, project: Project): ParadoxScriptFile? {
        val filePath = CwtPathExpressionType.FilePath.resolve(inlineScriptPathExpression, expression)
        val selector = fileSelector().gameTypeFrom(context).preferRootFrom(context)
        val query = ParadoxFilePathSearch.search(filePath, project, selector = selector)
        return query.find()?.toPsiFile(project)
    }
    
    @JvmStatic
    fun getInlineScriptExpression(file: ParadoxScriptFile): String? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupport(gameType)) return null
        val filePath = fileInfo.path.path
        return CwtPathExpressionType.FilePath.extract(inlineScriptPathExpression, filePath)
            ?.takeIfNotEmpty()
    }
    
    /**
     * 得到内联脚本的使用位置对应的属性信息，包括是否存在冲突等。
     */
    @JvmStatic
    fun getInlineScriptUsageInfo(file: ParadoxScriptFile): ParadoxInlineScriptUsageInfo? {
        if(!getSettings().inference.inlineScriptLocation) return null
        ProgressManager.checkCanceled()
        return CachedValuesManager.getCachedValue(file, PlsKeys.cachedInlineScriptUsageInfoKey) {
            val value = doGetInlineScriptUsageInfo(file)
            val modificationTracker = file.project.service<ParadoxModificationTrackerProvider>().InlineScript
            CachedValueProvider.Result.create(value, modificationTracker)
        }
    }
    
    private fun doGetInlineScriptUsageInfo(file: ParadoxScriptFile): ParadoxInlineScriptUsageInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupport(gameType)) return null
        val expression = getInlineScriptExpression(file) ?: return null
        val project = file.project
        val scope = GlobalSearchScope.allScope(project)
        var element: ParadoxScriptPropertyKey? = null
        var hasConflict = false
        val configs: MutableList<CwtDataConfig<*>> = mutableListOf()
        ParadoxInlineScriptIndex.processAllElements(expression, project, scope) { e ->
            if(element == null) {
                element = e
            }
            ProgressManager.checkCanceled()
            val eConfigs = ParadoxCwtConfigHandler.resolveConfigs(e)
            if(eConfigs.isNotEmpty()) {
                val configsToAdd = eConfigs.mapNotNull { it.parent }
                if(configs.isEmpty()) {
                    configs.addAll(configsToAdd)
                    true
                } else {
                    if(configsToAdd.any { configs.contains(it) }) {
                        true
                    } else {
                        hasConflict = true
                        false
                    }
                }
            } else {
                //unexpected
                true
            }
        }
        val usageElement = element?.parent as? ParadoxScriptProperty ?: return null
        return ParadoxInlineScriptUsageInfo(usageElement.createPointer(), hasConflict)
    }
}