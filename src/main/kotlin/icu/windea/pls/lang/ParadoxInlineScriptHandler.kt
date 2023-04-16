package icu.windea.pls.lang

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxInlineScriptHandler {
    private const val inlineScriptName = "inline_script"
    
    val inlineScriptPathExpression = CwtValueExpression.resolve("filepath[common/inline_scripts/,.txt]")
    
    val cachedInlineScriptInfoKey = Key.create<CachedValue<ParadoxInlineScriptInfo>>("paradox.cached.inlineScriptInfo")
    val cachedInlineScriptUsageInfoKey = Key.create<CachedValue<ParadoxInlineScriptUsageInfo>>("paradox.cached.inlineScriptUsageInfo")
    
    fun isGameTypeSupported(gameType: ParadoxGameType): Boolean {
        return gameType == ParadoxGameType.Stellaris
    }
    
    fun getInfo(element: ParadoxScriptProperty): ParadoxInlineScriptInfo? {
        val name = element.name
        if(name.lowercase() != inlineScriptName) return null
        return getInfoFromCache(element)
    }
    
    private fun getInfoFromCache(element: ParadoxScriptProperty): ParadoxInlineScriptInfo? {
        return CachedValuesManager.getCachedValue(element, cachedInlineScriptInfoKey) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = resolveInfo(element, file)
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun resolveInfo(element: ParadoxScriptProperty, file: PsiFile = element.containingFile): ParadoxInlineScriptInfo? {
        //这里不能调用ParadoxConfigHandler.getConfigs，因为需要处理内联的情况，会导致StackOverflow
        
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        
        val matchType = CwtConfigMatchType.STATIC //这里需要静态匹配，不能访问索引
        val project = file.project
        val configGroup = getCwtConfig(project).get(gameType)
        val inlineConfigs = configGroup.inlineConfigGroup[inlineScriptName] ?: return null
        val propertyValue = element.propertyValue ?: return null
        val inlineConfig = inlineConfigs.find {
            val expression = ParadoxDataExpression.resolve(propertyValue, matchType)
            ParadoxConfigHandler.matchesScriptExpression(propertyValue, expression, it.config.valueExpression, it.config, configGroup, matchType)
        }
        if(inlineConfig == null) return null
        val expression = getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        if(expression.isParameterAwareExpression()) return null
        val elementOffset = element.startOffset
        return ParadoxInlineScriptInfo(expression, elementOffset, gameType)
    }
    
    private fun getExpressionLocation(it: CwtDataConfig<*>): String? {
        return it.options?.find { it.key == "inline_script_expression" }?.stringValue
    }
    
    fun getExpressionFromInlineConfig(propertyValue: ParadoxScriptValue, inlineConfig: CwtInlineConfig): String? {
        if(inlineConfig.name != inlineScriptName) return null
        val expressionLocation = getExpressionLocation(inlineConfig.config) ?: return null
        val expressionElement = if(expressionLocation.isEmpty()) {
            propertyValue.castOrNull<ParadoxScriptString>()
        } else {
            propertyValue.findProperty(expressionLocation, conditional = true)?.propertyValue?.castOrNull<ParadoxScriptString>()
        }
        return expressionElement?.stringValue()
    }
    
    fun getInlineScript(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptFile? {
        if(DumbService.isDumb(project)) return null //NOTE 防止重入索引
        
        return runReadAction { doGetInlineScript(expression, project, contextElement) }
    }
    
    private fun doGetInlineScript(expression: String, project: Project, contextElement: PsiElement): ParadoxScriptFile? {
        val filePath = getInlineScriptFilePath(expression)
        val selector = fileSelector(project, contextElement).contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFile(project)
    }
    
    fun processInlineScript(expression: String, contextElement: PsiElement, project: Project, processor: (ParadoxScriptFile) -> Boolean): Boolean {
        if(DumbService.isDumb(project)) return true //NOTE 防止重入索引
        
        return runReadAction { doProcessInlineScript(expression, project, contextElement, processor) }
    }
    
    private fun doProcessInlineScript(expression: String, project: Project, contextElement: PsiElement, processor: (ParadoxScriptFile) -> Boolean): Boolean {
        val filePath = getInlineScriptFilePath(expression)
        val selector = fileSelector(project, contextElement).contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).processQuery {
            val file = it.toPsiFile<ParadoxScriptFile>(project)
            if(file != null) processor(file)
            true
        }
    }
    
    fun getInlineScriptFilePath(pathReference: String): String? {
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.resolvePath(configExpression, pathReference.normalizePath())
    }
    
    fun getInlineScriptExpression(file: VirtualFile): String? {
        if(file.fileType != ParadoxScriptFileType) return null
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        return doGetInlineScriptExpression(fileInfo)
    }
    
    fun getInlineScriptExpression(file: ParadoxScriptFile): String? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        return doGetInlineScriptExpression(fileInfo)
    }
    
    private fun doGetInlineScriptExpression(fileInfo: ParadoxFileInfo): String? {
        val filePath = fileInfo.path.path
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, null, filePath)?.takeIfNotEmpty()
    }
    
    /**
     * 得到内联脚本的使用位置对应的属性信息，包括是否存在冲突等。
     */
    fun getInlineScriptUsageInfo(file: ParadoxScriptFile): ParadoxInlineScriptUsageInfo? {
        ProgressManager.checkCanceled()
        val usageInfo = getUsageInfoFromCache(file) ?: return null
        //处理缓存对应的锚点属性已经不存在的情况
        if(usageInfo.pointer.element == null) {
            file.putUserData(cachedInlineScriptUsageInfoKey, null)
            return getUsageInfoFromCache(file)
        }
        return usageInfo
    }
    
    private fun getUsageInfoFromCache(file: ParadoxScriptFile): ParadoxInlineScriptUsageInfo? {
        return CachedValuesManager.getCachedValue(file, cachedInlineScriptUsageInfoKey) {
            ProgressManager.checkCanceled()
            val value = runReadAction { doGetInlineScriptUsageInfo(file) }
            val tracker = ParadoxModificationTrackerProvider.getInstance().ScriptFile
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetInlineScriptUsageInfo(file: ParadoxScriptFile): ParadoxInlineScriptUsageInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        val expression = getInlineScriptExpression(file) ?: return null
        val project = file.project
        var element: ParadoxScriptProperty? = null
        var hasConflict = false
        val configs: MutableList<CwtDataConfig<*>> = mutableListOf()
        val selector = inlineScriptSelector(project, file)
        ParadoxInlineScriptSearch.search(expression, selector).processQuery p@{ info ->
            ProgressManager.checkCanceled()
            val e = info.file?.findElementAt(info.elementOffset) ?: return@p true
            val p = e.parentOfType<ParadoxScriptProperty>() ?: return@p true
            if(p.name.lowercase() != inlineScriptName) return@p true
            if(element == null) {
                element = p
            }
            //检查内联脚本定义所在的规则的上下文是否匹配
            val eConfigs = ParadoxConfigHandler.getConfigs(p)
            if(eConfigs.isNotEmpty()) {
                val configsToAdd = eConfigs.mapNotNull { it.parent }
                if(configs.isEmpty()) {
                    configs.addAll(configsToAdd)
                    true
                } else {
                    if(configsToAdd.any { c1 -> configs.any { c2 -> c1 pointerEquals c2 } }) {
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
        val usageElement = element ?: return null
        return ParadoxInlineScriptUsageInfo(usageElement.createPointer(), hasConflict)
    }
}