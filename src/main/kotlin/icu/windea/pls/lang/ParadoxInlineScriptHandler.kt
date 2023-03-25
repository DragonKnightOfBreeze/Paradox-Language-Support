package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxInlineScriptHandler {
    const val inlineScriptDirPath = "common/inline_scripts"
    val inlineScriptPathExpression = CwtValueExpression.resolve("filepath[common/inline_scripts/,.txt]")
    
    val cachedInlineScriptInfoKey = Key.create<CachedValue<ParadoxInlineScriptInfo>>("paradox.cached.inlineScriptInfo")
    val cachedInlineScriptUsageInfoKey = Key.create<CachedValue<ParadoxInlineScriptUsageInfo>>("paradox.cached.inlineScriptUsageInfo")
    
    fun isGameTypeSupported(gameType: ParadoxGameType): Boolean {
        return gameType == ParadoxGameType.Stellaris
    }
    
    @JvmStatic
    fun getInfo(element: ParadoxScriptPropertyKey): ParadoxInlineScriptInfo? {
        ProgressManager.checkCanceled()
        if(!element.isExpression()) return null
        element.stub?.inlineScriptInfo?.let { return it }
        return getInfoFromCache(element)
    }
    
    private fun getInfoFromCache(element: ParadoxScriptPropertyKey): ParadoxInlineScriptInfo? {
        return CachedValuesManager.getCachedValue(element, cachedInlineScriptInfoKey) {
            val file = element.containingFile
            val value = resolveInfo(element, file)
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    @JvmStatic
    fun resolveInfo(element: ParadoxScriptPropertyKey, file: PsiFile = element.containingFile): ParadoxInlineScriptInfo? {
        //这里不能调用ParadoxConfigHandler.getConfigs，因为需要处理内联的情况，会导致StackOverFlow
        
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        val name = element.name.lowercase()
        if(name != "inline_script") return null
        val matchType = CwtConfigMatchType.STATIC
        val project = file.project
        val configGroup = getCwtConfig(project).getValue(gameType)
        val inlineConfigs = configGroup.inlineConfigGroup[name] ?: return null
        val propertyValue = element.propertyValue ?: return null
        //TODO 更加准确的匹配，目前没必要
        val inlineConfig = inlineConfigs.find {
            val expression = ParadoxDataExpression.resolve(propertyValue)
            ParadoxConfigHandler.matchesScriptExpression(propertyValue, expression, it.config.valueExpression, it.config, configGroup, matchType)
        }
        if(inlineConfig == null) return null
        val expression = getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        return ParadoxInlineScriptInfo(expression, gameType)
    }
    
    private fun getExpressionLocation(it: CwtDataConfig<*>): String? {
        return it.options?.find { it.key == "inline_script_expression" }?.stringValue
    }
    
    @JvmStatic
    fun getExpressionFromInlineConfig(propertyValue: ParadoxScriptValue, inlineConfig: CwtInlineConfig): String? {
        if(inlineConfig.name != "inline_script") return null
        val expressionLocation = getExpressionLocation(inlineConfig.config) ?: return null
        val expressionElement = if(expressionLocation.isEmpty()) {
            propertyValue.castOrNull<ParadoxScriptString>()
        } else {
            propertyValue.findProperty(expressionLocation, conditional = true)?.propertyValue?.castOrNull<ParadoxScriptString>()
        }
        return expressionElement?.stringValue()
    }
    
    @JvmStatic
    fun getInlineScript(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptFile? {
        if(DumbService.isDumb(project)) return null //NOTE 防止重入索引
        
        val filePath = getInlineScriptFilePath(expression)
        val selector = fileSelector(project, contextElement).contextSensitive()
        val query = ParadoxFilePathSearch.search(filePath, null, selector)
        return query.find()?.toPsiFile(project)
    }
    
    @JvmStatic
    fun processInlineScript(expression: String, contextElement: PsiElement, project: Project, processor: (ParadoxScriptFile) -> Boolean): Boolean {
        if(DumbService.isDumb(project)) return true //NOTE 防止重入索引
        
        val filePath = getInlineScriptFilePath(expression)
        val selector = fileSelector(project, contextElement).contextSensitive()
        val query = ParadoxFilePathSearch.search(filePath, null, selector)
        return query.processQuery {
            val file = it.toPsiFile<ParadoxScriptFile>(project)
            if(file != null) processor(file)
            true
        }
    }
    
    @JvmStatic
    fun getInlineScriptFilePath(pathReference: String): String? {
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.resolvePath(configExpression, pathReference.normalizePath())
    }
    
    @JvmStatic
    fun getInlineScriptExpression(file: VirtualFile): String? {
        if(file.fileType != ParadoxScriptFileType) return null
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        return doGetInlineScriptExpression(fileInfo)
    }
    
    @JvmStatic
    fun getInlineScriptExpression(file: ParadoxScriptFile): String? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        return doGetInlineScriptExpression(fileInfo)
    }
    
    private fun doGetInlineScriptExpression(fileInfo: ParadoxFileInfo): String? {
        val filePath = fileInfo.path.path
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, filePath)?.takeIfNotEmpty()
    }
    
    /**
     * 得到内联脚本的使用位置对应的属性信息，包括是否存在冲突等。
     */
    @JvmStatic
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
            val value = doGetInlineScriptUsageInfo(file)
            val tracker = ParadoxModificationTrackerProvider.getInstance().InlineScripts
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetInlineScriptUsageInfo(file: ParadoxScriptFile): ParadoxInlineScriptUsageInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if(!isGameTypeSupported(gameType)) return null
        val expression = getInlineScriptExpression(file) ?: return null
        val project = file.project
        val scope = GlobalSearchScope.allScope(project)
        var element: ParadoxScriptPropertyKey? = null
        var hasConflict = false
        val configs: MutableList<CwtDataConfig<*>> = mutableListOf()
        ParadoxInlineScriptIndex.KEY.processAllElements(expression, project, scope) { e ->
            ProgressManager.checkCanceled()
            if(element == null) {
                element = e
            }
            val eConfigs = ParadoxConfigHandler.getConfigs(e)
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
        val usageElement = element?.parent as? ParadoxScriptProperty ?: return null
        return ParadoxInlineScriptUsageInfo(usageElement.createPointer(), hasConflict)
    }
}