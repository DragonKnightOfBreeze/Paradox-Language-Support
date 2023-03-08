package icu.windea.pls.lang.expression

import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptLocalisationExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Localisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
}

class ParadoxScriptSyncedLocalisationExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Localisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
}

class ParadoxScriptInlineLocalisationExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.InlineLocalisation
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isLeftQuoted()) return
        if(expression.isParameterAwareExpression()) return
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
}

class ParadoxScriptDefinitionExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Definition
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
}

class ParadoxScriptPathReferenceExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isPathReferenceType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val attributesKey = ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
}

class ParadoxScriptEnumValueExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.EnumValue
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val configGroup = config.info.configGroup
        val enumName = config.expression?.value ?: return
        val attributesKey = when {
            enumName == ParadoxConfigHandler.paramsEnumName -> ParadoxScriptAttributesKeys.ARGUMENT_KEY
            configGroup.enums[enumName] != null -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
            configGroup.complexEnums[enumName] != null -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
        }
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
}

class ParadoxScriptModifierExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Modifier
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val attributesKey = ParadoxScriptAttributesKeys.MODIFIER_KEY
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
}

class ParadoxScriptAliasNameExpressionSupport: ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        val type = config.expression?.type ?: return false
        return type == CwtDataType.AliasName || type == CwtDataType.AliasKeysField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val configGroup = config.info.configGroup
        val configExpression = config.expression
        val aliasName = configExpression?.value ?: return
        val aliasMap = configGroup.aliasGroups.get(aliasName) ?: return
        val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression, false, aliasName, configGroup) ?: return
        val aliasConfig = aliasMap[aliasSubName]?.first() ?: return
        ParadoxScriptExpressionSupport.annotate(element, rangeInElement, expression, aliasConfig, holder)
    }
}

abstract class ParadoxScriptConstantLikeExpressionSupport: ParadoxScriptExpressionSupport() {
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if(expression.isParameterAwareExpression()) return
        val annotated = annotateByAliasName(element, config, holder)
        if(annotated) return
        val configExpression = config.expression ?: return
        if(rangeInElement == null) {
            if(element is ParadoxScriptPropertyKey && configExpression is CwtKeyExpression) return //unnecessary
            if(element is ParadoxScriptString && configExpression is CwtValueExpression) return //unnecessary
        }
        val attributesKey = when(configExpression) {
            is CwtKeyExpression -> ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY
            is CwtValueExpression -> ParadoxScriptAttributesKeys.STRING_KEY
        }
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
    }
    
    private fun annotateByAliasName(element: ParadoxScriptExpressionElement, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
        val aliasConfig = config.findAliasConfig() ?: return false
        val type = aliasConfig.expression.type
        if(!type.isConstantLikeType()) return false
        val aliasName = aliasConfig.name
        val attributesKey = when {
            aliasName == "modifier" -> ParadoxScriptAttributesKeys.MODIFIER_KEY
            aliasName == "trigger" -> ParadoxScriptAttributesKeys.TRIGGER_KEY
            aliasName == "effect" -> ParadoxScriptAttributesKeys.EFFECT_KEY
            else -> return false
        }
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.textRangeAfterUnquote).textAttributes(attributesKey).create()
        return true
    }
}

class ParadoxScriptConstantExpressionSupport: ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Constant
    }
}

class ParadoxScriptTemplateExpressionSupport: ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Template
    }
}