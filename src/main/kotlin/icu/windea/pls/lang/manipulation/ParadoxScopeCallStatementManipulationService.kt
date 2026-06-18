package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.linkNodes
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * 用于操作脚本中的作用域调用语句（scope call statement）。
 *
 * 说明：
 * - 对于显式形式，要求 `exists = x` 必须在 `x = { ... }` 之前，且两者之间仅能有空白或注释。
 *
 * 示例 - 显式形式：
 *
 * ```paradox_script
 * exists = owner
 * owner = { ... }
 * ```
 *
 * 示例 - 安全形式）：
 *
 * [CK3/VIC3/EU5]
 * ```paradox_script
 * owner ?= { ... }
 * ```
 *
 * Stellaris:
 * ```paradox_script
 * owner? = { ... }
 * ```
 *
 * 示例 - 链式形式：
 *
 * ```paradox_script
 * root.owner = { ... }
 * ```
 *
 * 示例 - 嵌套形式：
 *
 * ```paradox_script
 * root = {
 *     owner = { ... }
 * }
 * ```
 */
object ParadoxScopeCallStatementManipulationService {
    /**
     * 判断 [element] 是否为显式形式（`exists = x x = y`）。
     * [element] 可以是显式形式中的任意一个属性（`exists = x` 或 `x = y`）。
     */
    fun isNormalForm(element: ParadoxScriptProperty, forExistsProperty: Boolean = true, forSecondProperty: Boolean = true): Boolean {
        if (forExistsProperty && isExistsPropertyOfNormalForm(element)) return true
        if (forSecondProperty && isSecondPropertyOfNormalForm(element)) return true
        return false
    }

    /**
     * 判断 [element] 是否为安全形式（使用 `?=` 或 `? =` 作为分隔符的属性）。
     */
    fun isSafeForm(element: ParadoxScriptProperty): Boolean {
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        return separatorNode.elementType == SAFE_ASSIGN_SIGN || separatorNode.elementType == SAFE_CALL_ASSIGN_SIGN
    }

    /**
     * 判断是否可以转换为安全形式。
     *
     * 说明：
     * - 必须是显式形式。
     * - 游戏类型必须支持至少一种安全运算符（宽松测试：仅检查游戏类型，不检查游戏版本）。
     * - 检测于文法级别（键必须是字符串字面量）。
     */
    fun canConvertToSafeForm(element: ParadoxScriptProperty, canBeExistsProperty: Boolean = true, canBeSecondProperty: Boolean = true): Boolean {
        if (!isNormalForm(element, canBeExistsProperty, canBeSecondProperty)) return false
        val secondProperty = getSecondProperty(element) ?: return false
        if (!ParadoxSyntaxService.isSafeAssignOperatorAllowed(secondProperty)) return false
        val gameType = ParadoxAnalysisManager.selectGameType(element)
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return ParadoxSyntaxConstraint.SafeAssignOperator.test(gameType)
            || ParadoxSyntaxConstraint.SafeCallAssignOperator.test(gameType)
    }

    /**
     * 判断是否可以转换为显式形式。
     *
     * 说明：
     * - 对于任意游戏类型和任意安全调用运算符均可用。
     * - 检测于文法级别（键必须是字符串字面量）。
     */
    fun canConvertToNormalForm(element: ParadoxScriptProperty): Boolean {
        if (!isSafeForm(element)) return false
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        return when (separatorNode.elementType) {
            SAFE_ASSIGN_SIGN -> ParadoxSyntaxService.isSafeAssignOperatorAllowed(element)
            SAFE_CALL_ASSIGN_SIGN -> ParadoxSyntaxService.isSafeCallAssignOperatorAllowed(element)
            else -> false
        }
    }

    /**
     * 将显式形式转换为安全形式。
     *
     * 说明：
     * - 根据 [ParadoxSyntaxConstraint] 来决定使用 `?=` 还是 `? =`。
     * - `exists = x` 和 `x = y` 之间的注释会保留并移到安全形式之前。
     *
     * @param element 显式形式中的任一属性（`exists = x` 或 `x = y`）。
     * @param gameType 游戏类型，用于决定使用 `?=` 还是 `? =`。
     */
    fun convertToSafeForm(element: ParadoxScriptProperty, project: Project, gameType: ParadoxGameType?) {
        val existsProperty = getExistsProperty(element) ?: return
        val secondProperty = getSecondProperty(element) ?: return
        if (existsProperty === secondProperty) return

        val valueText = secondProperty.propertyValue?.text ?: return
        val keyText = secondProperty.propertyKey.text

        val safeSeparator = when {
            ParadoxSyntaxConstraint.SafeCallAssignOperator.test(gameType) -> "? = "
            else -> " ?= "
        }

        val commentsBetween = collectCommentsBetween(existsProperty, secondProperty)

        // 移动注释到 existsProperty 前面
        val parent = existsProperty.parent
        // 从最后一个注释开始移动，确保顺序正确
        for (comment in commentsBetween.asReversed()) {
            parent.addBefore(comment, existsProperty)
        }

        // 删除 existsProperty 和 secondProperty 之间的所有内容（此时注释已经移走，只余留空白和 secondProperty）
        val firstToDelete = existsProperty.nextSibling
        if (firstToDelete != null && firstToDelete !== secondProperty) {
            parent.deleteChildRange(firstToDelete, secondProperty)
        } else {
            // 直接删除 secondProperty（两者相邻，中间无内容）
            secondProperty.delete()
        }

        // 构建安全形式属性文本（仅属性本身）
        val safeText = keyText + safeSeparator + valueText

        // 用安全形式属性替换 existsProperty
        val newElement = ParadoxScriptElementFactory.createProperty(project, safeText)
        existsProperty.replace(newElement)
    }

    /**
     * 将安全形式转换为显式形式。
     *
     * 说明：
     * - 注释留在原位不动，仅替换属性并在其上方插入 `exists = x` 属性。
     * - 如果在多行块中，会在两个属性之间自动插入换行。
     *
     * @param element 安全形式的属性（使用 `?=` 或 `? =` 作为分隔符）。
     */
    fun convertToNormalForm(element: ParadoxScriptProperty, project: Project) {
        val keyText = element.propertyKey.text
        val valueText = element.propertyValue?.text ?: return

        val parent = element.parent

        // 判断是否需要换行（如果元素前已有换行，说明外层块是多行的）
        val parentText = parent.text
        val offsetInParent = element.textRange.startOffset - parent.textRange.startOffset
        val needsNewline = parentText.take(offsetInParent).contains('\n')

        // 创建 exists 属性和第二属性
        val firstText = "exists = $keyText"
        val secondText = "$keyText = $valueText"
        val firstProp = ParadoxScriptElementFactory.createProperty(project, firstText)
        val secondProp = ParadoxScriptElementFactory.createProperty(project, secondText)

        // 用第二属性替换原元素，然后在前面插入 exists
        val replaced = element.replace(secondProp)
        parent.addBefore(firstProp, replaced)
        if (needsNewline) {
            val newline = ParadoxScriptElementFactory.createLine(project)
            parent.addBefore(newline, replaced)
        }
    }

    private fun getExistsProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isExistsProperty(element)) {
            val next = findNextProperty(element)
            if (next != null && matchesExistsValue(element, next)) return element
            return null
        }
        var prev = element.prevSibling
        while (prev != null) {
            if (prev is ParadoxScriptProperty) {
                if (isExistsProperty(prev) && matchesExistsValue(prev, element)) return prev
                break
            }
            prev = prev.prevSibling
        }
        return null
    }

    private fun getSecondProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isSecondPropertyOfNormalForm(element)) return element
        if (isExistsProperty(element)) {
            val next = findNextProperty(element)
            if (next != null && matchesExistsValue(element, next)) return next
        }
        return null
    }

    private fun collectCommentsBetween(first: ParadoxScriptProperty, second: ParadoxScriptProperty): List<PsiComment> {
        return first.siblings(withSelf = false).takeWhile { it !== second }.filterIsInstance<PsiComment>().toList()
    }

    private fun isExistsProperty(element: ParadoxScriptProperty): Boolean {
        return element.name == "exists" && element.propertyValue is ParadoxScriptString
    }

    private fun isExistsPropertyOfNormalForm(element: ParadoxScriptProperty): Boolean {
        if (!isExistsProperty(element)) return false
        val next = findNextProperty(element) ?: return false
        return matchesExistsValue(element, next)
    }

    private fun isSecondPropertyOfNormalForm(element: ParadoxScriptProperty): Boolean {
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType != EQUAL_SIGN) return false
        val prev = findExistsPropertyBefore(element) ?: return false
        return matchesExistsValue(prev, element)
    }

    private fun matchesExistsValue(existsProperty: ParadoxScriptProperty, targetProperty: ParadoxScriptProperty): Boolean {
        val existsValue = existsProperty.propertyValue?.stringValue() ?: return false
        val targetKey = targetProperty.propertyKey.stringValue()
        return targetKey == existsValue
    }

    private fun findNextProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = true, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull()
    }

    private fun findExistsPropertyBefore(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = false, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull<ParadoxScriptProperty>()?.takeIf { isExistsProperty(it) }
    }

    // region Chained/Nested Form

    /**
     * 判断 [element] 的属性键是否为链式形式。
     *
     * 说明：
     * - 链式形式指属性键能够解析为 [ParadoxLinkedExpression] 且含有至少 2 个 [icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkNode]。
     * - 需要规则分组数据已初始化。
     * - 需要属性键能够解析为复杂表达式。
     *
     * 示例：
     * ```paradox_script
     * root.owner = { ... }
     * ```
     */
    fun isChainedForm(element: ParadoxScriptProperty): Boolean {
        val propertyKey = element.propertyKey
        val expressionText = ParadoxExpressionManager.getExpressionText(propertyKey)
        // 快速语法检查
        if (!expressionText.contains('.')) return false
        // 尝试语义检查（需要规则分组数据）
        val file = element.containingFile
        if (file != null && PlsFacade.checkConfigGroupInitialized(file.project, file)) {
            val resolvedComplexExpression = resolveComplexExpressionFromKey(element)
            if (resolvedComplexExpression is ParadoxLinkedExpression && resolvedComplexExpression.linkNodes.size >= 2) {
                return true
            }
        }
        // 回退：语法检查（至少一个点号）
        return expressionText.count { it == '.' } >= 1
    }

    /**
     * 判断 [element] 是否可以转换为嵌套形式。
     *
     * 说明：
     * - 要求 [element] 是链式形式。
     * - 检测于文法级别和语义级别。
     *
     * @see isChainedForm
     */
    fun canConvertToNestedForm(element: ParadoxScriptProperty): Boolean {
        return isChainedForm(element)
    }

    /**
     * 将链式形式转换为嵌套形式。
     *
     * 说明：
     * - 根据光标位置确定分割点：找到光标之前最后一个作为分隔符的点号（[ParadoxMarkerNode]），
     *   在该点号处分割属性键。
     * - 单步转换（仅展开一层）。
     * - 展开后总是会换行（`{` 之后和 `}` 之前插入换行）。
     *
     * 示例：
     * [光标位于 `root` 上]
     * ```paradox_script
     * # before
     * root.owner = { a = 1 }
     *
     * # after
     * root = {
     *     owner = { a = 1 }
     * }
     * ```
     *
     * [光标位于 `owner` 上]
     * ```paradox_script
     * # before
     * root.owner.event_target:x
     *
     * # after
     * root = {
     *     owner.event_target:x
     * }
     * ```
     *
     * @param element 链式形式的属性。
     * @param cursorOffset 光标在文件中的偏移量。
     */
    fun convertToNestedForm(element: ParadoxScriptProperty, project: Project, cursorOffset: Int) {
        val propertyKey = element.propertyKey
        val expressionText = ParadoxExpressionManager.getExpressionText(propertyKey)
        val expressionOffset = ParadoxExpressionManager.getExpressionOffset(propertyKey)
        val cursorOffsetInExpression = cursorOffset - propertyKey.textRange.startOffset - expressionOffset

        // 尝试语义解析
        val resolvedComplexExpression = resolveComplexExpressionFromKey(element) as? ParadoxLinkedExpression

        val outerKeyText: String
        val innerKeyText: String
        if (resolvedComplexExpression != null && resolvedComplexExpression.linkNodes.size >= 2) {
            // 语义级别分割：基于复杂表达式节点
            val linkNodes = resolvedComplexExpression.linkNodes
            var lastMarkerBeforeCursor: ParadoxMarkerNode? = null
            for (node in resolvedComplexExpression.nodes) {
                if (node is ParadoxMarkerNode && node.rangeInExpression.startOffset < cursorOffsetInExpression) {
                    lastMarkerBeforeCursor = node
                }
            }
            if (lastMarkerBeforeCursor != null) {
                val splitStart = lastMarkerBeforeCursor.rangeInExpression.startOffset
                val splitEnd = lastMarkerBeforeCursor.rangeInExpression.endOffset
                outerKeyText = expressionText.substring(0, splitStart)
                innerKeyText = expressionText.substring(splitEnd)
            } else {
                val firstLinkEnd = linkNodes.first().rangeInExpression.endOffset
                outerKeyText = expressionText.substring(0, firstLinkEnd)
                innerKeyText = expressionText.substring(firstLinkEnd).removePrefix(".")
            }
        } else {
            // 回退到语法级别分割：基于点号字符位置
            val dotPositions = expressionText.mapIndexedNotNull { i, c -> if (c == '.') i else null }
            if (dotPositions.size < 1) return
            val splitPos = dotPositions.lastOrNull { it < cursorOffsetInExpression }
            if (splitPos != null) {
                outerKeyText = expressionText.substring(0, splitPos)
                innerKeyText = expressionText.substring(splitPos + 1)
            } else {
                val firstDot = dotPositions.first()
                outerKeyText = expressionText.substring(0, firstDot)
                innerKeyText = expressionText.substring(firstDot + 1)
            }
        }

        val wasQuoted = isPropertyKeyQuoted(element)
        val outerKeyFormatted = if (wasQuoted) "\"$outerKeyText\"" else outerKeyText
        val innerKeyFormatted = if (wasQuoted) "\"$innerKeyText\"" else innerKeyText

        val valueText = element.propertyValue?.text ?: return

        val newText = "$outerKeyFormatted = {\n$innerKeyFormatted = $valueText\n}"
        val newElement = ParadoxScriptElementFactory.createProperty(project, newText)
        element.replace(newElement)
    }

    /**
     * 判断 [element] 是否为嵌套形式。
     *
     * 说明：
     * - 输入元素必须有块值，且块内有且仅有一个属性。
     * - 块内属性前后仅允许注释和空白。
     *
     * 示例：
     * ```paradox_script
     * root = {
     *     owner = { ... }
     * }
     * ```
     */
    fun isNestedForm(element: ParadoxScriptProperty): Boolean {
        // 属性键不能已经包含点号（即不能已经是链式形式）
        val expressionText = ParadoxExpressionManager.getExpressionText(element.propertyKey)
        if (expressionText.contains('.')) return false
        return findSingleInnerProperty(element) != null
    }

    /**
     * 判断 [element] 是否可以转换为链式形式。
     *
     * 说明：
     * - 要求 [element] 是嵌套形式。
     *
     * @see isNestedForm
     */
    fun canConvertToChainedForm(element: ParadoxScriptProperty): Boolean {
        return isNestedForm(element)
    }

    /**
     * 将嵌套形式转换为链式形式。
     *
     * 说明：
     * - 如果原属性键用双引号包围，转换后也保留。反之亦然。
     *
     * 示例：
     * ```paradox_script
     * # before
     * root = {
     *     owner = { a = 1 }
     * }
     *
     * # after
     * root.owner = { a = 1 }
     * ```
     *
     * @param element 嵌套形式的外层属性。
     */
    fun convertToChainedForm(element: ParadoxScriptProperty, project: Project) {
        val innerProperty = findSingleInnerProperty(element) ?: return
        val outerKeyText = element.propertyKey.getValue()
        val innerKeyText = innerProperty.propertyKey.getValue()
        val valueText = innerProperty.propertyValue?.text ?: return

        val newText = "$outerKeyText.$innerKeyText = $valueText"
        val newElement = ParadoxScriptElementFactory.createProperty(project, newText)
        element.replace(newElement)
    }

    // endregion

    // region Helpers for Chained/Nested Form

    /**
     * 从 [element] 的属性键解析复杂表达式。
     */
    private fun resolveComplexExpressionFromKey(element: ParadoxScriptProperty): ParadoxComplexExpression? {
        val propertyKey = element.propertyKey
        val expressionText = ParadoxExpressionManager.getExpressionText(propertyKey)
        if (!expressionText.contains('.')) return null
        val file = element.containingFile ?: return null
        val configGroup = PlsFacade.getConfigGroup(file.project, selectGameType(file))
        val config = ParadoxConfigManager.getConfigs(propertyKey).firstOrNull() ?: return null
        return ParadoxComplexExpression.resolveByConfig(expressionText, null, configGroup, config)
    }

    /**
     * 在 [element] 的块中查找唯一的内部属性。
     * 如果块不为空且仅包含一个属性（允许注释和空白），则返回该属性，否则返回 `null`。
     */
    private fun findSingleInnerProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        val block = element.block ?: return null
        var foundProperty: ParadoxScriptProperty? = null
        for (child in block.children) {
            when (child) {
                is PsiWhiteSpace, is PsiComment -> { /* 允许 */ }
                is ParadoxScriptProperty -> {
                    if (foundProperty != null) return null // 多于一个属性
                    foundProperty = child
                }
                else -> {
                    // 块的括号自身也是子节点
                    if (child.text == "{" || child.text == "}") continue
                    return null // 意外的节点
                }
            }
        }
        return foundProperty
    }

    /**
     * 收集 block 中 innerProperty 之前的注释。
     */
    private fun collectCommentsBeforeInBlock(block: ParadoxScriptBlock?, innerProperty: ParadoxScriptProperty): List<PsiComment> {
        if (block == null) return emptyList()
        return block.children.takeWhile { it !== innerProperty }.filterIsInstance<PsiComment>().toList()
    }

    /**
     * 收集 block 中 innerProperty 之后的注释。
     */
    private fun collectCommentsAfterInBlock(block: ParadoxScriptBlock?, innerProperty: ParadoxScriptProperty): List<PsiComment> {
        if (block == null) return emptyList()
        val innerIndex = block.children.indexOf(innerProperty)
        return block.children.drop(innerIndex + 1).filterIsInstance<PsiComment>().toList()
    }

    /**
     * 判断属性的键是否用双引号包围。
     */
    private fun isPropertyKeyQuoted(element: ParadoxScriptProperty): Boolean {
        val firstChild = element.propertyKey.node.findChildByType(STRING_TOKEN)
        return firstChild != null
    }

    // endregion
}
