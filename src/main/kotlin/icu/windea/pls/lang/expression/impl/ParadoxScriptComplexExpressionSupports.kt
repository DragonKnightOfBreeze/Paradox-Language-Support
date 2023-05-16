package icu.windea.pls.lang.expression.impl

import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

//提供对复杂表达式的高级语言功能支持
//由于复杂表达式包含多个节点，可能需要被解析为多个引用，引用解析的代码不在这里实现

class ParadoxScriptValueSetExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isValueSetValueType() == true 
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        //not key/value or quoted -> only value set value name, no scope info
        if(config !is CwtDataConfig<*> || expression.isLeftQuoted()) {
            val valueSetName = config.expression?.value ?: return
            val attributesKey = when(valueSetName) {
                "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
                else -> ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
            }
            ParadoxConfigHandler.annotateScriptExpression(element, element.textRange.unquote(expression), attributesKey, holder)
            return
        }
        val configGroup = config.info.configGroup
        val isKey = element is ParadoxScriptPropertyKey
        val textRange = TextRange.create(0, expression.length)
        val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(expression, textRange, config, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, valueSetValueExpression, holder, config)
    }
}

class ParadoxScriptScopeFieldExpressionSupport: ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isScopeFieldType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val isKey = element is ParadoxScriptPropertyKey
        val textRange = TextRange.create(0, expression.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, scopeFieldExpression, holder, config)
    }
}

class ParadoxScriptValueFieldExpressionSupport: ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isValueFieldType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val isKey = element is ParadoxScriptPropertyKey
        val textRange = TextRange.create(0, expression.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, valueFieldExpression, holder, config)
    }
}

class ParadoxScriptVariableFieldExpressionSupport: ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isVariableFieldType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val isKey = element is ParadoxScriptPropertyKey
        val textRange = TextRange.create(0, expression.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, variableFieldExpression, holder, config)
    }
}