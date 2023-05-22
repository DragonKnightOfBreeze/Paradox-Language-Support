package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 定义的查询器。
 */
class ParadoxDefinitionSearcher : QueryExecutorBase<ParadoxScriptDefinitionElement, ParadoxDefinitionSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefinitionSearch.SearchParameters, consumer: Processor<in ParadoxScriptDefinitionElement>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val name = queryParameters.name
        val typeExpression = queryParameters.typeExpression
        val project = queryParameters.project
        
        DumbService.getInstance(project).runReadActionInSmartMode action@{
            if(typeExpression == null) {
                if(name == null) {
                    //查找所有定义
                    ParadoxDefinitionNameIndex.KEY.processAllElementsByKeys(project, scope) { _, it ->
                        consumer.process(it)
                    }
                } else {
                    //按照名字查找定义
                    ParadoxDefinitionNameIndex.KEY.processAllElements(name, project, scope) {
                        consumer.process(it)
                    }
                }
            } else {
                //按照类型表达式查找定义
                doProcessQueryByTypeExpression(typeExpression, project, scope, name, consumer)
                
                //如果是切换类型，也要按照基础类型的类型表达式查找定义
                val gameType = queryParameters.selector.gameType
                val configGroup = getCwtConfig(project).get(gameType.id)
                val baseTypeExpression = configGroup.typeToSwapTypeMap.get(typeExpression)
                if(baseTypeExpression != null) {
                    doProcessQueryByTypeExpression(baseTypeExpression, project, scope, name, consumer)
                }
            }
        }
    }
    
    private fun doProcessQueryByTypeExpression(typeExpression: String, project: Project, scope: GlobalSearchScope, name: String?, consumer: Processor<in ParadoxScriptDefinitionElement>) {
        if(name == null) {
            val (type, subtypes) = ParadoxDefinitionTypeExpression.resolve(typeExpression)
            ParadoxDefinitionTypeIndex.KEY.processAllElements(type, project, scope) p@{
                if(subtypes.isNotEmpty() && !matchesSubtypes(it, subtypes)) return@p true
                consumer.process(it)
            }
        } else {
            val (type, subtypes) = ParadoxDefinitionTypeExpression.resolve(typeExpression)
            ParadoxDefinitionNameIndex.KEY.processAllElements(name, project, scope) p@{
                if(!matchesType(it, type)) return@p true
                if(subtypes.isNotEmpty() && !matchesSubtypes(it, subtypes)) return@p true
                consumer.process(it)
            }
        }
    }
    
    private fun matchesName(element: ParadoxScriptDefinitionElement, name: String): Boolean {
        return ParadoxDefinitionHandler.getName(element) == name
    }
    
    private fun matchesType(element: ParadoxScriptDefinitionElement, type: String): Boolean {
        return ParadoxDefinitionHandler.getType(element) == type
    }
    
    private fun matchesSubtypes(element: ParadoxScriptDefinitionElement, subtypes: List<String>): Boolean {
        return ParadoxDefinitionHandler.getSubtypes(element)?.containsAll(subtypes) == true
    }
}
