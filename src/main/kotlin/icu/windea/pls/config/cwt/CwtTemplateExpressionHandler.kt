package icu.windea.pls.config.cwt

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.util.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.script.psi.*

object CwtTemplateExpressionHandler {
    @JvmStatic
    fun extract(expression: CwtTemplateExpression, referenceName: String): String {
        if(expression.referenceExpressions.size != 1) throw IllegalStateException()
        return buildString {
            for(snippetExpression in expression.snippetExpressions) {
                when(snippetExpression.type) {
                    CwtDataType.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceName)
                }
            }
        }
    }
    
    @JvmStatic
    fun extract(templateExpression: CwtTemplateExpression, referenceNames: Map<CwtDataExpression, String>): String {
        if(templateExpression.referenceExpressions.size != referenceNames.size) throw IllegalStateException()
        return buildString {
            for(snippetExpression in templateExpression.snippetExpressions) {
                when(snippetExpression.type) {
                    CwtDataType.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceNames.getValue(snippetExpression))
                }
            }
        }
    }
    
    private val regexCache = CacheBuilder.newBuilder().buildCache<CwtTemplateExpression, Regex>{ doToRegex(it) }
    
    private fun toRegex(configExpression: CwtTemplateExpression): Regex {
        return regexCache.get(configExpression)
    }
    
    private fun doToRegex(configExpression: CwtTemplateExpression) : Regex { 
        return buildString { 
            configExpression.snippetExpressions.forEach { 
                if(it.type == CwtDataType.Constant) {
                    append("\\E(").append(it.expressionString).append(")\\Q")
                } else {    
                    append(".*?")
                }
            }
        }.toRegex(RegexOption.IGNORE_CASE)
    }
    
    @JvmStatic
    fun matches(text: String, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
        val snippetExpressions = configExpression.snippetExpressions
        if(snippetExpressions.isEmpty()) return false
        val expressionString = text.unquote()
        val regex = toRegex(configExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return false
        if(snippetExpressions.size != matchResult.groups.size - 1) return false
        for((index, snippetExpression) in snippetExpressions.withIndex()) {
            if(snippetExpression.type != CwtDataType.Constant) {
                val matchGroup = matchResult.groups.get(index + 1) ?: return false
                val referenceName = matchGroup.value
                val expression = ParadoxDataExpression.resolve(referenceName, false)
                val isMatched = CwtConfigHandler.matchesScriptExpression(expression, snippetExpression, null, configGroup, matchType)
                if(!isMatched) return false
            }
        }
        return true
    }
    
    @JvmStatic
    fun resolve(element: ParadoxScriptStringExpressionElement, textRange: TextRange, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        //需要保证里面的每个引用都能解析
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        val expressionString = textRange.substring(element.text)
        val references = resolveReferences(element, textRange, configExpression, configGroup) ?: return null
        return ParadoxTemplateExpressionElement(element, expressionString, configExpression, project, gameType, references)
    }
    
    
    @JvmStatic
    fun resolveReferences(element: ParadoxScriptStringExpressionElement, textRange: TextRange, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): List<ParadoxInTemplateExpressionReference>? {
        val snippetExpressions = configExpression.snippetExpressions
        if(snippetExpressions.isEmpty()) return null
        val expressionString = textRange.substring(element.text)
        val regex = toRegex(configExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return null
        if(snippetExpressions.size != matchResult.groups.size - 1) return null
        val references = SmartList<ParadoxInTemplateExpressionReference>()
        val startOffset = textRange.startOffset
        for((index, snippetExpression) in snippetExpressions.withIndex()) {
            if(snippetExpression.type != CwtDataType.Constant) {
                val matchGroup = matchResult.groups.get(index + 1) ?: return null
                val range = TextRange.create(matchGroup.range.first + startOffset, matchGroup.range.last + startOffset)
                val reference = ParadoxInTemplateExpressionReference(element, range, snippetExpression, configGroup)
                references.add(reference)
            } else {
                //ignore
            }
        }
        return references
    }
    
    @JvmStatic
    fun processResolveResult(configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup, processor: Processor<String>) {
        //TODO()
    }
}