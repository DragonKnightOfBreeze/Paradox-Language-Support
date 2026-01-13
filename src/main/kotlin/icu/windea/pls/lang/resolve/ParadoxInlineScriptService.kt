package icu.windea.pls.lang.resolve

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.parentBlock
import icu.windea.pls.script.psi.parentProperty
import icu.windea.pls.script.psi.stringValue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ParadoxInlineScriptService {
    @OptIn(ExperimentalContracts::class)
    private fun canBeExpressionElement(v1: PsiElement): Boolean {
        contract { returns(true) implies (v1 is ParadoxScriptValue) }
        return v1 is ParadoxScriptString || v1 is ParadoxScriptScriptedVariableReference
    }

    /**
     * 从内联脚本用法对应的 PSI，得到内联脚本表达式对应的 PSI。
     *
     * - `inline_script = "some/expression"` -> `"some/expression"`
     * - `inline_script = { script = "some/expression" }` -> `"some/expression"`
     */
    fun getExpressionElement(usageElement: ParadoxScriptProperty): ParadoxScriptValue? {
        // hardcoded
        if (!ParadoxInlineScriptManager.isMatched(usageElement.name)) return null // NOTE 2.1.0 这里目前不验证游戏类型
        val v1 = usageElement.propertyValue ?: return null
        if (canBeExpressionElement(v1)) return v1
        val v2 = selectScope { v1.properties().ofKey("script").one() }?.propertyValue ?: return null
        if (canBeExpressionElement(v2)) return v2
        return null
    }

    /**
     * 从内联脚本表达式对应的 PSI，得到内联脚本用法对应的 PSI。
     *
     * - `"some/expression"` -> `inline_script = "some/expression"`
     * - `"some/expression"` -> `inline_script = { script = "some/expression" }`
     */
    fun getUsageElement(expressionElement: PsiElement): ParadoxScriptProperty? {
        // hardcoded
        if (!canBeExpressionElement(expressionElement)) return null
        val p1 = expressionElement.parentProperty ?: return null
        if (ParadoxInlineScriptManager.isMatched(p1.name)) return p1 // NOTE 2.1.0 这里目前不验证游戏类型
        if (!p1.name.equals("script", true)) return null
        val p2 = p1.parentBlock?.parentProperty ?: return null
        if (ParadoxInlineScriptManager.isMatched(p2.name)) return p2 // NOTE 2.1.0 这里目前不验证游戏类型
        return null
    }

    /**
     * 从内联脚本用法对应的 PSI，得到对应的内联脚本表达式。
     *
     * @param resolve 如果内联脚本表达式对应的 PSI 是一个封装变量引用，是否尝试解析。
     */
    fun getInlineScriptExpressionFromUsageElement(usageElement: ParadoxScriptProperty, resolve: Boolean = false): String? {
        // hardcoded
        val expressionElement = getExpressionElement(usageElement)?.let { if (resolve) it.resolved() else it }
        if (expressionElement !is ParadoxScriptString) return null
        return expressionElement.stringValue.orNull()
    }

    /**
     * 从内联脚本用法对应的 Lighter AST，得到对应的内联脚本表达式。
     */
    fun getInlineScriptExpressionFromUsageElement(tree: LighterAST, node: LighterASTNode): String? {
        // hardcoded
        val v1 = ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(node, tree)
        if (v1 != null) return v1
        val v2 = ParadoxScriptLightTreeUtil.findPropertyFromPropertyNode(node, tree, "script")
            ?.let { ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(it, tree) }
        if (v2 != null) return v2
        return null
    }

    /**
     * 从内联脚本用法对应的 PSI，得到对应的传入参数的键值映射。
     *
     * @param resolve 如果传入参数的值对应的 PSI 是一个封装变量引用，是否尝试解析。
     */
    @Suppress("unused")
    fun getInlineScriptArgumentMapFromUsageElement(usageElement: ParadoxScriptProperty, resolve: Boolean = false): Map<String, String> {
        // hardcoded
        val v = usageElement.block ?: return emptyMap()
        return ParadoxPsiManager.getArgumentTupleList(v, "script").toMap()
    }
}
