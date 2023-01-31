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
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*
import java.util.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxInlineScriptHandler {
    private const val inlineScriptPathExpression = "common/inline_scripts/,.txt"
    
    fun isInlineScriptFile(file: ParadoxScriptFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val inlineScriptPath = CwtPathExpressionType.FilePath.extract(inlineScriptPathExpression, fileInfo.path.path)
        return inlineScriptPath != null
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