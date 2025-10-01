package icu.windea.pls.lang.index.stubs

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isInlineScriptUsage
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionPathManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptScriptedVariableStub

object ParadoxScriptStubManager {
    fun createScriptedVariableStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<out PsiElement>?): ParadoxScriptScriptedVariableStub {
        // 排除为空或者带参数的情况
        val name = psi.name.orEmpty()
        if (name.isEmpty() || name.isParameterized()) return ParadoxScriptScriptedVariableStub.createDummy(parentStub)
        return ParadoxScriptScriptedVariableStub.create(parentStub, name)
    }

    fun createScriptedVariableStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub {
        // 排除为空或者带参数的情况
        val name = ParadoxScriptLightTreeUtil.getNameFromScriptedVariableNode(node, tree).orEmpty()
        if (name.isEmpty() || name.isParameterized()) return ParadoxScriptScriptedVariableStub.createDummy(parentStub)
        return ParadoxScriptScriptedVariableStub.create(parentStub, name)
    }

    fun createPropertyStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?): ParadoxScriptPropertyStub {
        // 排除为空或者带参数的情况
        val name = psi.name
        if (name.isEmpty() || name.isParameterized()) return ParadoxScriptPropertyStub.createDummy(parentStub)
        run {
            if (parentStub is ParadoxScriptPropertyStub.InlineScriptUsage) {
                val inlineScriptArgumentStub = createInlineScriptArgumentStub(parentStub, name)
                if (inlineScriptArgumentStub != null) return inlineScriptArgumentStub
                return@run
            }
            if (name.isInlineScriptUsage()) {
                val inlineScriptUsageStub = createInlineScriptUsageStub(psi, parentStub, name)
                if (inlineScriptUsageStub != null) return inlineScriptUsageStub
                return@run
            }
            val definitionStub = createDefinitionStub(psi, parentStub, name)
            if (definitionStub != null) return definitionStub

        }
        return ParadoxScriptPropertyStub.create(parentStub, name)
    }

    fun createPropertyStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        // 排除为空或者带参数的情况
        val name = ParadoxScriptLightTreeUtil.getNameFromPropertyNode(node, tree).orEmpty()
        if (name.isEmpty() || name.isParameterized()) return ParadoxScriptPropertyStub.createDummy(parentStub)
        run {
            if (parentStub is ParadoxScriptPropertyStub.InlineScriptUsage) {
                val inlineScriptArgumentStub = createInlineScriptArgumentStub(parentStub, name)
                if (inlineScriptArgumentStub != null) return inlineScriptArgumentStub
                return@run
            }
            if (name.isInlineScriptUsage()) {
                val inlineScriptUsageStub = createInlineScriptUsageStub(tree, node, parentStub, name)
                if (inlineScriptUsageStub != null) return inlineScriptUsageStub
                return@run
            }
            val definitionStub = createDefinitionStub(tree, node, parentStub, name)
            if (definitionStub != null) return definitionStub
        }
        return ParadoxScriptPropertyStub.create(parentStub, name)
    }

    private fun createDefinitionStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?, name: String): ParadoxScriptPropertyStub? {
        // 定义的名字可以为空
        val typeKey = name
        val definitionInfo = psi.definitionInfo ?: return null
        val definitionName = definitionInfo.name // NOTE 这里不处理需要内联的情况
        val definitionType = definitionInfo.type
        if (definitionType.isEmpty()) return null
        val definitionSubtypes = getSubtypesWhenCreateDefinitionStub(definitionInfo) // 如果无法在索引时获取，之后再懒加载
        val elementPath = definitionInfo.elementPath
        return ParadoxScriptPropertyStub.createDefinition(parentStub, definitionName, definitionType, definitionSubtypes, typeKey, elementPath)
    }

    private fun createDefinitionStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<out PsiElement>, name: String): ParadoxScriptPropertyStub? {
        // 定义的名字可以为空
        val typeKey = name
        val psi = parentStub.psi
        val file = psi.containingFile
        val project = file.project
        val vFile = selectFile(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = selectGameType(vFile) ?: return null
        val path = fileInfo.path
        val configGroup = PlsFacade.getConfigGroup(project, gameType) // 这里需要指定project
        val elementPath = ParadoxExpressionPathManager.get(node, tree, vFile, PlsFacade.getInternalSettings().maxDefinitionDepth)
        if (elementPath == null) return null
        val typeKeyPrefix = lazy { ParadoxExpressionPathManager.getKeyPrefixes(node, tree).firstOrNull() }
        val typeConfig = ParadoxDefinitionManager.getMatchedTypeConfig(node, tree, configGroup, path, elementPath, typeKey, typeKeyPrefix) ?: return null
        val definitionName = ParadoxDefinitionManager.resolveNameFromTypeConfig(node, tree, typeKey, typeConfig) // NOTE 这里不处理需要内联的情况
        val definitionType = typeConfig.name
        if (definitionType.isEmpty()) return null
        val definitionSubtypes = getSubtypesWhenCreateDefinitionStub(typeConfig, typeKey) // 如果无法在索引时获取，之后再懒加载
        return ParadoxScriptPropertyStub.createDefinition(parentStub, definitionName, definitionType, definitionSubtypes, typeKey, elementPath)
    }

    private fun getSubtypesWhenCreateDefinitionStub(definitionInfo: ParadoxDefinitionInfo): List<String>? {
        return runCatchingCancelable { definitionInfo.subtypes }.getOrNull()
    }

    private fun getSubtypesWhenCreateDefinitionStub(typeConfig: CwtTypeConfig, typeKey: String): List<String>? {
        val subtypesConfig = typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypesConfig.values) {
            if (ParadoxDefinitionManager.matchesSubtypeFast(typeKey, subtypeConfig, result) ?: return null) {
                result.add(subtypeConfig)
            }
        }
        return result.map { it.name }
    }

    private fun createInlineScriptUsageStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?, name: String): ParadoxScriptPropertyStub.InlineScriptUsage? {
        // 排除为空或者带参数的情况
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpressionFromUsageElement(psi).orEmpty()
        if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
        return ParadoxScriptPropertyStub.createInlineScriptUsage(parentStub, name, inlineScriptExpression)
    }

    private fun createInlineScriptUsageStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<out PsiElement>, name: String): ParadoxScriptPropertyStub.InlineScriptUsage? {
        // 排除为空或者带参数的情况
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpressionFromUsageElement(tree, node).orEmpty()
        if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
        return ParadoxScriptPropertyStub.createInlineScriptUsage(parentStub, name, inlineScriptExpression)
    }

    private fun createInlineScriptArgumentStub(parentStub: StubElement<out PsiElement>?, name: String): ParadoxScriptPropertyStub.InlineScriptArgument? {
        // if (parentStub !is ParadoxScriptPropertyStub.InlineScriptUsage) return null
        if (name.equals("script", true)) return null
        return ParadoxScriptPropertyStub.createInlineScriptArgument(parentStub, name)
    }
}
