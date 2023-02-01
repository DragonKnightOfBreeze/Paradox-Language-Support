package icu.windea.pls.config.core

import com.intellij.openapi.progress.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*
import java.util.*

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
            element.propertyValue?.castOrNull<ParadoxScriptString>()?.value
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
    fun linkElement(file: ParadoxScriptFile): ParadoxScriptPropertyKey? {
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
        return element
    }
    
    @JvmStatic
    fun getLinkedDefinition(file: ParadoxScriptFile, originalSubPaths: LinkedList<String>): ParadoxScriptDefinitionElement? {
        val project = file.project
        ProgressManager.checkCanceled()
        val scope = GlobalSearchScope.allScope(project)
        val referenceQuery = ReferencesSearch.search(file, scope)
        var linkedSubPaths: List<String>? = null
        var linkedDefinition: ParadoxScriptDefinitionElement? = null
        var positionConfig: CwtDataConfig<*>? = null
        var multiplePosition = false
        referenceQuery.processQuery {
            if(it !is ParadoxScriptExpressionPsiReference) return@processQuery true
            ProgressManager.checkCanceled()
            val linkedProperty = it.element.parents(withSelf = false)
                .filterIsInstance<ParadoxScriptProperty>()
                .find { p ->
                    if(p.name != "inline_script") return@find false
                    val config = ParadoxCwtConfigHandler.resolveConfigs(p).firstOrNull() ?: return@find false
                    config.expression.expressionString == "inline[inline_script]"
                }
            if(linkedProperty == null) return@processQuery true
            val definitionMemberInfo = linkedProperty.definitionMemberInfo
            if(definitionMemberInfo == null) return@processQuery true
            val config = ParadoxCwtConfigHandler.resolvePropertyConfigs(linkedProperty).firstOrNull()
            if(config == null) return@processQuery false
            if(positionConfig == null) {
                positionConfig = config
            } else {
                if(positionConfig!!.path != config.path) {
                    //存在多个入口且入口间存在冲突
                    multiplePosition = true
                    return@processQuery false
                } else {
                    return@processQuery true
                }
            }
            linkedSubPaths = definitionMemberInfo.elementPath.subPaths.dropLast(1)
            positionConfig = config
            linkedDefinition = linkedProperty.findParentDefinition()
            return@processQuery true
        }
        if(multiplePosition) return null
        if(linkedSubPaths == null || linkedDefinition == null) return null
        originalSubPaths.addAll(linkedSubPaths!!)
        return linkedDefinition
    }
}