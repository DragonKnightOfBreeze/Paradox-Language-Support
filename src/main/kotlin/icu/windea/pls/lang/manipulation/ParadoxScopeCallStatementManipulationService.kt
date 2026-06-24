package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.quote
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isChainedForm
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isNormalForm
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isSafeForm
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 用于操作脚本文件中的作用域调用语句（scope call statement）。
 *
 * 示例 - 普通形式：
 *
 * ```paradox_script
 * owner = v
 * ```
 *
 * 示例 - 安全形式：
 *
 * ```paradox_script
 * # [CK3/VIC3/EU5]
 * owner ?= v
 *
 * # [Stellaris]
 * owner? = v
 * ```
 *
 * 示例 - 链式形式：
 *
 * ```paradox_script
 * root.owner = v
 * ```
 *
 * 示例 - 嵌套形式：
 *
 * ```paradox_script
 * root = {
 *     owner = v
 * }
 * ```
 *
 * @see ParadoxLinkedExpression
 */
object ParadoxScopeCallStatementManipulationService {
    /**
     * 判断 [element] 是否为普通形式。
     *
     * 说明：
     * - [element] 的属性键必须是字符串字面量，属性分隔符必须是普通的赋值运算符（`=`）。
     * - [element] 的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]）。
     *
     * @see ParadoxLinkedExpression
     */
    fun isNormalForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        if (!ParadoxSyntaxService.isStringLiteral(propertyKey)) return false
        val separator = propertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) } ?: return false
        if (!ParadoxSyntaxService.isNormalAssignOperator(separator)) return false
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return false

        return true
    }

    /**
     * 判断 [element] 是否为安全形式。
     *
     * 说明：
     * - [element] 的属性键必须是字符串字面量，属性分隔符必须是安全的赋值运算符（`?=` 或 `? =`）。
     * - [element] 的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]）。
     */
    fun isSafeForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        if (!ParadoxSyntaxService.isStringLiteral(propertyKey)) return false
        val separator = propertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) } ?: return false
        if (!ParadoxSyntaxService.isSafeAssignOperator(separator)) return false
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return false

        return true
    }

    /**
     * 判断 [element] 是否为嵌套形式。
     *
     * 说明：
     * - 作为外层属性的 [element] 以及内层属性的属性键必须是字符串字面量，属性分隔符必须是某种赋值运算符。
     * - 作为外层属性的 [element] 以及内层属性的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]）。
     * - 块内的内层属性前后仅允许空白和注释。
     *
     * @see ParadoxLinkedExpression
     */
    fun isNestedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        val separator = propertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) } ?: return false
        if (!ParadoxSyntaxService.isStringLiteral(propertyKey)) return false
        if (!ParadoxSyntaxService.isAssignOperator(separator)) return false
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return false

        val innerProperty = element.properties().singleOrNull() ?: return false
        val innerPropertyKey = innerProperty.propertyKey
        if (!ParadoxSyntaxService.isStringLiteral(innerPropertyKey)) return false
        val innerSeparator = innerPropertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) } ?: return false
        if (!ParadoxSyntaxService.isAssignOperator(innerSeparator)) return false
        val innerComplexExpression = ParadoxComplexExpression.resolve(innerPropertyKey, configGroup)
        if (innerComplexExpression !is ParadoxLinkedExpression) return false

        return true
    }

    /**
     * 判断 [element] 是否为链式形式。
     *
     * 说明：
     * - [element] 的属性键必须是字符串字面量，属性分隔符必须是某种赋值运算符。
     * - [element] 的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]），且含有至少2个链接节点（[ParadoxLinkNode]）。
     *
     * @see ParadoxLinkedExpression
     */
    fun isChainedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        if (!ParadoxSyntaxService.isStringLiteral(propertyKey)) return false
        val separator = propertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) } ?: return false
        if (!ParadoxSyntaxService.isAssignOperator(separator)) return false
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return false
        val linkNodes = complexExpression.linkNodes
        if (linkNodes.size <= 1) return false

        return true
    }

    /**
     * 判断 [element] 是否可以转换为普通形式。
     *
     * 说明：
     * - [element] 必须是安全形式。参见 [isSafeForm]。
     * - 适用于所有游戏类型和任意安全调用运算符（`?=` 或 `? =`）。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToNormalForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        return isSafeForm(element, gameType)
    }

    /**
     * 判断 [element] 是否可以转换为安全形式。
     *
     * 说明：
     * - [element] 必须是普通形式。参见 [isNormalForm]。
     * - 适用于支持安全（调用）赋值运算符的游戏类型（CK3/VIC3/EU5 使用 `?=`，Stellaris 使用 `? =`）。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToSafeForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        if (!isNormalForm(element, gameType)) return false
        val gameType = ParadoxAnalysisManager.selectGameType(element)
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return ParadoxSyntaxConstraint.SafeAssignOperator.test(gameType) || ParadoxSyntaxConstraint.SafeCallAssignOperator.test(gameType)
    }

    /**
     * 判断 [element] 是否可以转换为嵌套形式。
     *
     * 说明：
     * - [element] 必须是链式形式。参见 [isChainedForm]。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToNestedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        return isChainedForm(element, gameType)
    }

    /**
     * 判断 [element] 是否可以转换为链式形式。
     *
     * 说明：
     * - [element] 必须是嵌套形式。参见 [isSafeForm]。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToChainedForm(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): Boolean {
        return isNestedForm(element, gameType)
    }

    /**
     * 将 [element] 从安全形式转换为普通形式。
     *
     * 示例：
     *
     * ```paradox_script
     * # [CK3/VIC3/EU5] before
     * owner ?= v
     *
     * # [Stellaris] before
     * owner? = v
     *
     * # after
     * owner = v
     * ```
     *
     * 说明：
     * - 转换后可能会改变语义。
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToNormalForm(element: ParadoxScriptProperty, project: Project) {
        val propertyKey = element.propertyKey
        val separator = propertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) } ?: return
        if (!ParadoxSyntaxService.isSafeAssignOperator(separator)) return

        // reformat around spaces
        ParadoxSyntaxManipulationService.reformatAroundPropertySeparator(element, separator, project)

        // replace separator
        val newSeparatorText = "="
        ParadoxSyntaxManipulationService.replacePropertySeparator(separator, project, newSeparatorText)
    }

    /**
     * 将 [element] 从普通形式转换为安全形式。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * owner = v
     *
     * # [CK3/VIC3/EU5] after
     * owner ?= v
     *
     * # [Stellaris] after
     * owner?= v
     * ```
     *
     * ```paradox_script
     * # before
     * exists = owner
     * owner = v
     *
     * # [CK3/VIC3/EU5] after
     * owner ?= v
     *
     * # [Stellaris] after
     * owner?= v
     * ```
     *
     * 说明：
     * - 转换后可能会改变语义。
     * - 转换后会移除 [element] 之前紧邻的所有作用域与之匹配的 `exists` 属性。
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToSafeForm(element: ParadoxScriptProperty, project: Project, gameType: ParadoxGameType? = selectGameType(element)) {
        val propertyKey = element.propertyKey
        val separator = propertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) } ?: return
        if (!ParadoxSyntaxService.isNormalAssignOperator(separator)) return

        // delete prev adjacent matched `exists` properties
        run {
            val existsProperties = element.siblings(forward = false, withSelf = false)
                .filterNot { it is PsiWhiteSpace || it is PsiComment }
                .takeWhile { it is ParadoxScriptProperty && it.name == "exists" }
                .filterIsInstance<ParadoxScriptProperty>()
                .toList()
            if (existsProperties.isEmpty()) return@run
            val configGroup = PlsFacade.getConfigGroup(gameType)
            val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
            if (complexExpression !is ParadoxLinkedExpression) return@run
            val linkNodes = complexExpression.linkNodes
            val allScopes = linkNodes
                .scan("") { acc, node -> if (acc.isEmpty()) node.text else "$acc.${node.text}" }
                .toSet()
            // "from.owner" -> "" + "from" + "from.owner"
            val matchedExistsProperties = existsProperties.filter { it.value in allScopes }
            matchedExistsProperties.forEach { it.delete() }
        }

        // reformat around spaces
        ParadoxSyntaxManipulationService.reformatAroundPropertySeparator(element, separator, project)

        // replace separator
        val newSeparatorText = when {
            ParadoxSyntaxConstraint.SafeCallAssignOperator.test(gameType) -> "? ="
            else -> "?="
        }
        ParadoxSyntaxManipulationService.replacePropertySeparator(separator, project, newSeparatorText)
    }

    /**
     * 将 [element] 从链式形式转换为嵌套形式（单步展开）。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * root.owner = v
     *
     * # after
     * root = {
     *     owner = v
     * }
     * ```
     *
     * ```paradox_script
     * # before
     * root.owner ?= v
     *
     * # after
     * root ?= {
     *     owner ?= v
     * }
     * ```
     *
     * 说明：
     * - 找到链式表达式（[ParadoxLinkedExpression]）的直接子节点中，[caretOffset] 之前最后一个（或者链接中第一个）作为分隔符的点号，在此处分隔，
     * - 如果 [element] 的属性键用双引号包围，转换后也保留。反之亦然。
     * - 如果 [element] 的属性分隔符使用安全的赋值运算符（如 `?=`），转换后也保留（所有深度）。
     * - 总是会在 `{` 之后和 `}` 之前自动插入换行。
     * - 如果返回值为正数，则为转换后需要移动到的光标位置的偏移。
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToNestedForm(element: ParadoxScriptProperty, project: Project, caretOffset: Int, gameType: ParadoxGameType? = selectGameType(element)): Int {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return -1
        val linkNodes = complexExpression.linkNodes
        if (linkNodes.size <= 1) return -1

        val expressionOffset = ParadoxExpressionManager.getExpressionOffset(propertyKey)
        val cursorOffsetInExpression = caretOffset - propertyKey.startOffset - expressionOffset

        val outerKey: String
        val innerKey: String
        val moveToInInnerExpression: Int

        // 语义级别分割：基于复杂表达式节点
        val lastMarkerBeforeCursor = complexExpression.nodes.findLast { it is ParadoxOperatorNode && it.rangeInExpression.startOffset < cursorOffsetInExpression }
        if (lastMarkerBeforeCursor != null) {
            val splitStart = lastMarkerBeforeCursor.rangeInExpression.startOffset
            val splitEnd = lastMarkerBeforeCursor.rangeInExpression.endOffset
            outerKey = complexExpression.text.substring(0, splitStart).trimEnd()
            innerKey = complexExpression.text.substring(splitEnd).trimStart()
            moveToInInnerExpression = cursorOffsetInExpression - (complexExpression.rangeInExpression.endOffset - innerKey.length)
        } else {
            val firstLinkEnd = linkNodes[0].rangeInExpression.endOffset
            val secondLinkStart = linkNodes[1].rangeInExpression.startOffset
            outerKey = complexExpression.text.substring(0, firstLinkEnd).trimEnd()
            innerKey = complexExpression.text.substring(secondLinkStart).trimStart()
            moveToInInnerExpression = -1
        }

        val separator = propertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) }
        val separatorText = separator?.text ?: "="

        val wasQuoted = propertyKey.text.isLeftQuoted()
        val newOuterKeyText = if (wasQuoted) outerKey.quote() else outerKey
        val newInnerKeyText = if (wasQuoted) innerKey.quote() else innerKey
        val newSeparatorText = separatorText // NOTE separator should be kept in all depths
        val newValueText = element.propertyValue?.text.orEmpty() // property value can be null here
        val newText = "$newOuterKeyText $newSeparatorText {\n$newInnerKeyText $newSeparatorText $newValueText\n}"
        val newElement = ParadoxScriptElementFactory.createPropertyFromText(project, newText)
        val replacedElement = element.replace(newElement)

        // 如果有必要，准备移动光标位置
        run {
            if (moveToInInnerExpression < 0) return@run
            if (replacedElement !is ParadoxScriptProperty) return@run
            val document = replacedElement.containingFile?.fileDocument ?: return@run
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document) // 提交文档更改（这会重新格式化更改内容）
            val moveTo = replacedElement.properties().first().startOffset + moveToInInnerExpression + (if (wasQuoted) 1 else 0)
            return moveTo
        }

        return -1
    }

    /**
     * 将 [element] 从嵌套形式转换为链式形式（单步合并）。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * root = {
     *     owner = v
     * }
     *
     * # after
     * root.owner = v
     * ```
     *
     * ```paradox_script
     * # before
     * root = {
     *     owner ?= v
     * }
     *
     * # after
     * root.owner ?= v
     * ```
     *
     * 说明：
     * - 如果作为外层属性的 [element] 的属性键用双引号包围，转换后也保留。反之亦然。
     * - 如果作为外层属性的 [element] 的属性分隔符使用安全的赋值运算符（如 `?=`），转换后也保留。
     * - 内层属性前后的注释会保留，并移到转换后的语句之前。
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToChainedForm(element: ParadoxScriptProperty, project: Project, gameType: ParadoxGameType? = selectGameType(element)) {
        val configGroup = PlsFacade.getConfigGroup(gameType)

        val propertyKey = element.propertyKey
        val complexExpression = ParadoxComplexExpression.resolve(propertyKey, configGroup)
        if (complexExpression !is ParadoxLinkedExpression) return

        val innerProperty = element.properties().singleOrNull() ?: return
        val innerPropertyKey = innerProperty.propertyKey
        val innerComplexExpression = ParadoxComplexExpression.resolve(innerPropertyKey, configGroup)
        if (innerComplexExpression !is ParadoxLinkedExpression) return

        // 移动注释到 element 前面
        val parent = element.parent ?: return // unexpected
        val block = element.block ?: return // unexpected
        val comments = PsiService.collectBetweenBounds(block, forward = false)?.filterIsInstance<PsiComment>().orEmpty()
        var anchor: PsiElement = element
        for (comment in comments) anchor = parent.addBefore(comment, anchor)

        val outerKey = propertyKey.value
        val innerKey = innerProperty.propertyKey.value

        val innerSeparator = innerPropertyKey.siblings(withSelf = false).find { ParadoxSyntaxService.isPropertySeparator(it) }
        val innerSeparatorText = innerSeparator?.text ?: "="

        val newKey = "$outerKey.$innerKey"
        val wasQuoted = propertyKey.text.isLeftQuoted()
        val newKeyText = if (wasQuoted) newKey.quote() else newKey
        val newSeparatorText = innerSeparatorText // NOTE outer separator text will be lost here, if is not `=`
        val newValueText = innerProperty.propertyValue?.text.orEmpty() // property value can be null here
        val newText = "$newKeyText $newSeparatorText $newValueText"
        val newElement = ParadoxScriptElementFactory.createPropertyFromText(project, newText)
        element.replace(newElement)
    }
}
