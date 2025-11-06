package icu.windea.pls.config.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.isPropertyValue

object CwtConfigSchemaManager {
    fun getContextConfigs(element: PsiElement, containerElement: PsiElement, file: PsiFile, schema: CwtSchemaConfig): List<CwtMemberConfig<*>> {
        if (CwtConfigManager.isInternalFile(file)) return emptyList() // 排除内部规则文件
        val configPath = CwtConfigManager.getConfigPath(containerElement)
        if (configPath == null) return emptyList()

        var contextConfigs = mutableListOf<CwtMemberConfig<*>>()
        contextConfigs += schema.properties
        configPath.forEachIndexed f1@{ i, path ->
            val flatten = i != configPath.length - 1 || !(element is CwtString && element.isPropertyValue())
            val nextContextConfigs = mutableListOf<CwtMemberConfig<*>>()
            contextConfigs.forEach f2@{ config ->
                when (config) {
                    is CwtPropertyConfig -> {
                        val schemaExpression = CwtSchemaExpression.resolve(config.key)
                        if (!matchesSchemaExpression(path, schemaExpression, schema)) return@f2
                        nextContextConfigs += config
                    }
                    is CwtValueConfig -> {
                        if (path != "-") return@f2
                        nextContextConfigs += config
                    }
                }
            }
            contextConfigs = nextContextConfigs
            if (flatten) contextConfigs = contextConfigs.flatMapTo(mutableListOf()) { it.configs.orEmpty() }
        }
        return contextConfigs
    }

    fun matchesSchemaExpression(value: String, schemaExpression: CwtSchemaExpression, schema: CwtSchemaConfig): Boolean {
        return when (schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                schemaExpression.expressionString == value
            }
            is CwtSchemaExpression.Enum -> {
                schema.enums[schemaExpression.name]?.values?.any { it.stringValue == value } ?: false
            }
            is CwtSchemaExpression.Template -> {
                value.matchesPattern(schemaExpression.pattern)
            }
            else -> true // fast check
        }
    }
}
