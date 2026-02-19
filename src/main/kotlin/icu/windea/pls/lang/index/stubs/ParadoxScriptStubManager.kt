package icu.windea.pls.lang.index.stubs

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.firstChild
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.stubs.ParadoxScriptFileStub
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
        return createPropertyStub(psi, parentStub, name) ?: ParadoxScriptPropertyStub.create(parentStub, name)
    }


    fun createPropertyStub(psi: ParadoxScriptProperty, parentStub: StubElement<out PsiElement>?, name: String): ParadoxScriptPropertyStub? {
        val gameType = parentStub.castOrNull<ParadoxStub<*>>()?.gameType ?: return null
        run {
            if (!ParadoxDefineManager.isDefineFile(psi.containingFile)) return@run

            return when (parentStub) {
                is ParadoxScriptPropertyStub.DefineNamespace -> {
                    ParadoxScriptPropertyStub.createDefineVariable(parentStub, name)
                }
                is ParadoxScriptFileStub -> {
                    if (psi.propertyValue !is ParadoxScriptBlock) return null
                    ParadoxScriptPropertyStub.createDefineNamespace(parentStub, name)
                }
                else -> null
            }
        }
        run {
            if (parentStub !is ParadoxScriptPropertyStub.InlineScriptUsage) return@run

            if (name.equals("script", true)) return null
            return ParadoxScriptPropertyStub.createInlineScriptArgument(parentStub, name)
        }
        run {
            if (!ParadoxInlineScriptManager.isMatched(name, gameType)) return@run

            // 排除为空或者带参数的情况
            val inlineScriptExpression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(psi).orEmpty()
            if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
            return ParadoxScriptPropertyStub.createInlineScriptUsage(parentStub, name, inlineScriptExpression)
        }
        return null
    }

    fun createPropertyStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub {
        // 排除为空或者带参数的情况
        val name = ParadoxScriptLightTreeUtil.getNameFromPropertyNode(node, tree).orEmpty()
        if (name.isEmpty() || name.isParameterized()) return ParadoxScriptPropertyStub.createDummy(parentStub)
        return createPropertyStub(tree, node, parentStub, name) ?: ParadoxScriptPropertyStub.create(parentStub, name)
    }

    fun createPropertyStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>, name: String): ParadoxScriptPropertyStub? {
        val gameType = parentStub.castOrNull<ParadoxStub<*>>()?.gameType ?: return null
        run {
            if (!ParadoxDefineManager.isDefineFile(parentStub.psi.containingFile)) return@run

            return when (parentStub) {
                is ParadoxScriptPropertyStub.DefineNamespace -> {
                    ParadoxScriptPropertyStub.createDefineVariable(parentStub, name)
                }
                is ParadoxScriptFileStub -> {
                    if (node.firstChild(tree, BLOCK) == null) return null
                    ParadoxScriptPropertyStub.createDefineNamespace(parentStub, name)
                }
                else -> null
            }
        }
        run {
            if (parentStub !is ParadoxScriptPropertyStub.InlineScriptUsage) return@run

            if (name.equals("script", true)) return null
            return ParadoxScriptPropertyStub.createInlineScriptArgument(parentStub, name)
        }
        run {
            if (!ParadoxInlineScriptManager.isMatched(name, gameType)) return@run

            // 排除为空或者带参数的情况
            val inlineScriptExpression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(tree, node).orEmpty()
            if (inlineScriptExpression.isEmpty() || inlineScriptExpression.isParameterized()) return null
            return ParadoxScriptPropertyStub.createInlineScriptUsage(parentStub, name, inlineScriptExpression)
        }
        return null
    }
}
