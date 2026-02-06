package icu.windea.pls.lang.index.stubs

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
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
            val gameType = parentStub.castOrNull<ParadoxStub<*>>()?.gameType ?: return@run
            if (parentStub is ParadoxScriptPropertyStub.InlineScriptUsage) {
                val stub = createInlineScriptArgumentStub(parentStub, name)
                if (stub != null) return stub
                return@run
            }
            if (ParadoxInlineScriptManager.isMatched(name)) {
                val stub = createInlineScriptUsageStub(psi, parentStub, name)
                if (stub != null) return stub
                return@run
            }
            if (ParadoxDefinitionInjectionManager.isMatched(name, gameType)) {
                val stub = createDefinitionInjectionStub(psi, parentStub, name)
                if (stub != null) return stub
                return@run
            }
            val definitionInjectionStub = createDefinitionInjectionStub(psi, parentStub, name)
            if (definitionInjectionStub != null) return definitionInjectionStub
            val stub = createDefinitionStub(psi, parentStub, name)
            if (stub != null) return stub

        }
        return ParadoxScriptPropertyStub.create(parentStub, name)
    }

    fun createPropertyStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        // 排除为空或者带参数的情况
        val name = ParadoxScriptLightTreeUtil.getNameFromPropertyNode(node, tree).orEmpty()
        if (name.isEmpty() || name.isParameterized()) return ParadoxScriptPropertyStub.createDummy(parentStub)
        run {
            val gameType = parentStub.castOrNull<ParadoxStub<*>>()?.gameType ?: return@run
            if (parentStub is ParadoxScriptPropertyStub.InlineScriptUsage) {
                val stub = createInlineScriptArgumentStub(parentStub, name)
                if (stub != null) return stub
                return@run
            }
            if (ParadoxInlineScriptManager.isMatched(name, gameType)) {
                val stub = createInlineScriptUsageStub(tree, node, parentStub, name)
                if (stub != null) return stub
                return@run
            }
            if (ParadoxDefinitionInjectionManager.isMatched(name, gameType)) {
                val stub = createDefinitionInjectionStub(parentStub, name)
                if (stub != null) return stub
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
        val definitionType = definitionInfo.type
        if (definitionType.isEmpty()) return null
        val definitionName = definitionInfo.name // NOTE 这里不处理需要内联的情况
        val definitionSubtypes = getDefinitionSubtypesWhenCreateStub(definitionInfo) // 如果无法在索引时获取，之后再懒加载
        val rootKeys = definitionInfo.rootKeys
        return ParadoxScriptPropertyStub.createDefinition(parentStub, typeKey, definitionName, definitionType, definitionSubtypes, rootKeys)
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
        val configGroup = PlsFacade.getConfigGroup(project, gameType) // 这里需要指定 project
        val maxDepth = PlsInternalSettings.getInstance().maxDefinitionDepth
        val rootKeys = ParadoxMemberService.getRootKeys(node, tree, vFile, maxDepth = maxDepth) ?: return null
        val typeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(node, tree) }
        val matchContext = CwtTypeConfigMatchContext(configGroup, path, typeKey, rootKeys, typeKeyPrefix)
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfig(matchContext, node, tree) ?: return null
        val definitionType = typeConfig.name
        if (definitionType.isEmpty()) return null
        val definitionName = ParadoxDefinitionService.resolveName(node, tree, typeKey, typeConfig) // NOTE 这里不处理需要内联的情况
        val definitionSubtypes = getDefinitionSubtypesWhenCreateStub(typeConfig, typeKey) // 如果无法在索引时获取，之后再懒加载
        return ParadoxScriptPropertyStub.createDefinition(parentStub, typeKey, definitionName, definitionType, definitionSubtypes, rootKeys)
    }

    private fun getDefinitionSubtypesWhenCreateStub(definitionInfo: ParadoxDefinitionInfo): List<String>? {
        return runCatchingCancelable { definitionInfo.subtypes }.getOrNull()
    }

    private fun getDefinitionSubtypesWhenCreateStub(typeConfig: CwtTypeConfig, typeKey: String): List<String>? {
        val subtypesConfig = typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypesConfig.values) {
            if (ParadoxConfigMatchService.matchesSubtypeFast(subtypeConfig, result, typeKey) ?: return null) {
                result.add(subtypeConfig)
            }
        }
        return result.map { it.name }
    }

    private fun createInlineScriptUsageStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?, name: String): ParadoxScriptPropertyStub.InlineScriptUsage? {
        // 排除为空或者带参数的情况
        val inlineScriptExpression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(psi).orEmpty()
        if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
        return ParadoxScriptPropertyStub.createInlineScriptUsage(parentStub, name, inlineScriptExpression)
    }

    private fun createInlineScriptUsageStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<out PsiElement>, name: String): ParadoxScriptPropertyStub.InlineScriptUsage? {
        // 排除为空或者带参数的情况
        val inlineScriptExpression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(tree, node).orEmpty()
        if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
        return ParadoxScriptPropertyStub.createInlineScriptUsage(parentStub, name, inlineScriptExpression)
    }

    private fun createInlineScriptArgumentStub(parentStub: StubElement<out PsiElement>?, name: String): ParadoxScriptPropertyStub.InlineScriptArgument? {
        // if (parentStub !is ParadoxScriptPropertyStub.InlineScriptUsage) return null
        if (name.equals("script", true)) return null
        return ParadoxScriptPropertyStub.createInlineScriptArgument(parentStub, name)
    }

    private fun createDefinitionInjectionStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?, name: String): ParadoxScriptPropertyStub? {
        // 排除带参数的情况
        // 目标或目标类型为空时，也会创建存根
        if (name.isParameterized()) return null
        val mode = ParadoxDefinitionInjectionManager.getModeFromExpression(name)
        if (mode.isNullOrEmpty()) return null
        val target = ParadoxDefinitionInjectionManager.getTargetFromExpression(name)
        val type = getDefinitionInjectionTypeWhenCreateStub(psi)
        return ParadoxScriptPropertyStub.createDefinitionInjection(parentStub, name, mode, target, type)
    }

    private fun createDefinitionInjectionStub(parentStub: StubElement<out PsiElement>, name: String): ParadoxScriptPropertyStub? {
        // 排除带参数的情况
        // 目标或目标类型为空时，也会创建存根
        if (name.isParameterized()) return null
        val mode = ParadoxDefinitionInjectionManager.getModeFromExpression(name)
        if (mode.isNullOrEmpty()) return null
        val target = ParadoxDefinitionInjectionManager.getTargetFromExpression(name)
        val type = getDefinitionInjectionTypeWhenCreateStub(parentStub, target)
        return ParadoxScriptPropertyStub.createDefinitionInjection(parentStub, name, mode, target, type)
    }

    private fun getDefinitionInjectionTypeWhenCreateStub(psi: ParadoxScriptProperty): String? {
        return psi.definitionInjectionInfo?.type
    }

    private fun getDefinitionInjectionTypeWhenCreateStub(parentStub: StubElement<out PsiElement>, target: String?): String? {
        if (target.isNullOrEmpty()) return null
        val psi = parentStub.psi
        val file = psi.containingFile
        val project = file.project
        val vFile = selectFile(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = selectGameType(vFile) ?: return null
        val path = fileInfo.path
        val configGroup = PlsFacade.getConfigGroup(project, gameType) // 这里需要指定 project
        val matchContext = CwtTypeConfigMatchContext(configGroup, path)
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfigForInjection(matchContext) ?: return null
        return typeConfig.name
    }
}
