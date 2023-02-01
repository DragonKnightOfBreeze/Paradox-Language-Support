package icu.windea.pls.config.core

import com.intellij.openapi.progress.*
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
            val value = resolveInfo(element)
            CachedValueProvider.Result.create(value, file)//invalidated on file modification
        }
    }
    
    @JvmStatic
    fun resolveInfo(element: ParadoxScriptPropertyKey): ParadoxInlineScriptInfo? {
        val value = element.value
        if(value != "inline_script") return null
        val configs = ParadoxCwtConfigHandler.resolveConfigs(element)
        val config = configs.firstOrNull() ?: return null
        val configGroup = config.info.configGroup
        val gameType = configGroup.gameType
        var expression: String? = null
        if(isExpressionConfig(config)) {
            expression = element.propertyValue?.castOrNull<ParadoxScriptString>()?.value
        } else {
            //直接使用查找到的第一个
            element.propertyValue?.castOrNull<ParadoxScriptBlock>()?.processProperty(includeConditional = true) {p ->
                val pConfigs = ParadoxCwtConfigHandler.resolveConfigs(p)
                val pConfig = pConfigs.firstOrNull() ?: return@processProperty true
                if(isExpressionConfig(pConfig)) {
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
        val fileInfo = file.fileInfo ?: return false
        val inlineScriptPath = CwtPathExpressionType.FilePath.extract(inlineScriptPathExpression, fileInfo.path.path)
        return inlineScriptPath != null
    }
    
    @JvmStatic
    fun linkElement(file: ParadoxScriptFile): ParadoxScriptProperty? {
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path.path
        val expression = CwtPathExpressionType.FilePath.extract(inlineScriptPathExpression, path) ?: return null
        val project = file.project
        val scope = GlobalSearchScope.allScope(project)
        var element: ParadoxScriptPropertyKey? = null
        //如果有多个，需要检查所在位置是否存在冲突，如果存在冲突则返回null
        ParadoxInlineScriptIndex.processAllElements(expression, project, scope) {
            //TODO
            element = it
            true
        }
        return element?.parent as? ParadoxScriptProperty
    }
}