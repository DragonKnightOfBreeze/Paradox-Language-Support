package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isChainedForm
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isExplicitForm
import icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService.isSafeForm
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.linkNodes
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * 用于操作脚本中的作用域调用语句（scope call statement）。
 *
 * 示例 - 显式形式：
 *
 * ```paradox_script
 * exists = owner
 * owner = { k = v }
 * ```
 *
 * 示例 - 安全形式：
 *
 * ```paradox_script
 * # [CK3/VIC3/EU5]
 * owner ?= { k = v }
 *
 * # [Stellaris]
 * owner? = { k = v }
 * ```
 *
 * 示例 - 链式形式：
 *
 * ```paradox_script
 * root.owner = { k = v }
 * ```
 *
 * 示例 - 嵌套形式：
 *
 * ```paradox_script
 * root = {
 *     owner = { k = v }
 * }
 * ```
 */
object ParadoxScopeCallStatementManipulationService {
    /**
     * 判断 [element] 是否为显式形式。
     *
     * 说明：
     * - [element] 默认可以是其中的任一属性（`exists = x` 或 `x = y`）。
     * - [element] 的属性键（作为 `exists` 属性时）/属性值（作为第二个属性时）必须是字符串字面量。
     * - `exists = x` 必须在 `x = y` 之前，且两者之间仅能有空白或注释。
     */
    fun isExplicitForm(element: ParadoxScriptProperty, forExistsProperty: Boolean = true, forSecondProperty: Boolean = true): Boolean {
        if (forExistsProperty && isExistsPropertyOfExplicitForm(element)) return true
        if (forSecondProperty && isSecondPropertyOfExplicitForm(element)) return true
        return false
    }

    /**
     * 判断 [element] 是否为安全形式。
     *
     * 说明：
     * - [element] 必须使用 `?=` 或 `? =` 作为分隔符。
     * - [element] 的属性键必须是字符串字面量。
     */
    fun isSafeForm(element: ParadoxScriptProperty): Boolean {
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType !in ParadoxScriptTokenSets.SAFE_OPERATOR_TOKENS) return false
        return ParadoxSyntaxService.isSafeOperatorAllowed(element)
    }

    /**
     * 判断 [element] 是否可以转换为显式形式。
     *
     * 说明：
     * - 对于任意游戏类型和任意安全调用运算符均可用。
     * - [element] 必须是安全形式。参见 [isSafeForm]。
     */
    fun canConvertToExplicitForm(element: ParadoxScriptProperty): Boolean {
        return isSafeForm(element)
    }

    /**
     * 判断 [element] 是否可以转换为安全形式。
     *
     * 说明：
     * - 适用于支持安全（调用）赋值运算符的游戏类型（CK3/VIC3/EU5 使用 `?=`，Stellaris 使用 `? =`）。
     * - [element] 必须是显式形式。参见 [isExplicitForm]。
     */
    fun canConvertToSafeForm(element: ParadoxScriptProperty, canBeExistsProperty: Boolean = true, canBeSecondProperty: Boolean = true): Boolean {
        if (!isExplicitForm(element, canBeExistsProperty, canBeSecondProperty)) return false
        val gameType = ParadoxAnalysisManager.selectGameType(element)
        if (gameType == null || gameType == ParadoxGameType.Core) return true
        return ParadoxSyntaxConstraint.SafeAssignOperator.test(gameType) || ParadoxSyntaxConstraint.SafeCallAssignOperator.test(gameType)
    }

    /**
     * 将 [element] 从安全形式转换为显式形式。
     *
     * 示例：
     *
     * ```paradox_script
     * # [CK3/VIC3/EU5] before
     * owner ?= { ... }
     *
     * # [Stellaris] before
     * owner? = { ... }
     *
     * # after
     * exists = owner
     * owner = { ... }
     * ```
     *
     * 说明：
     * - 替换属性文本并在其上方插入 `exists = x` 属性。
     * - 如果在多行块中，会在 `exists = x` 和 `x = y` 之间自动插入换行。
     */
    fun convertToExplicitForm(element: ParadoxScriptProperty, project: Project) {
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

    /**
     * 将 [element] 从显式形式转换为安全形式。
     *
     * ```paradox_script
     * # before
     * exists = owner
     * # comment
     * owner = { ... }
     *
     * # [CK3/VIC3/EU5] after
     * # comment
     * owner ?= { ... }
     *
     * # [Stellaris] after
     * # comment
     * owner? = { ... }
     * ```
     *
     * 说明：
     * - [element] 可以是其中的任一属性（`exists = x` 或 `x = y`）。
     * - `exists = x` 和 `x = y` 之间的注释会保留，并移到转换后的语句之前。
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

        // 移动注释到 existsProperty 前面
        val parent = existsProperty.parent
        // 从最后一个注释开始移动，确保顺序正确
        val commentsBetween = PsiService.collectCommentsBetween(secondProperty, existsProperty, forward = false)
        for (comment in commentsBetween) {
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

    // region Helpers for Normal/Safe Form

    private fun isExistsProperty(element: ParadoxScriptProperty): Boolean {
        if (element.name != "exists") return false
        if (element.propertyValue !is ParadoxScriptProperty) return false
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType != EQUAL_SIGN) return false
        return true
    }

    private fun isSecondProperty(element: ParadoxScriptProperty): Boolean {
        if (element.name == "exists") return false
        val separatorNode = element.node.findChildByType(ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) ?: return false
        if (separatorNode.elementType != EQUAL_SIGN) return false
        return true
    }

    private fun isExistsPropertyOfExplicitForm(element: ParadoxScriptProperty): Boolean {
        if (!isExistsProperty(element)) return false
        val next = findSecondPropertyAfter(element) ?: return false
        return matchesExistsValue(element, next)
    }

    private fun isSecondPropertyOfExplicitForm(element: ParadoxScriptProperty): Boolean {
        if (!isSecondProperty(element)) return false
        val prev = findExistsPropertyBefore(element) ?: return false
        return matchesExistsValue(prev, element)
    }

    private fun matchesExistsValue(existsProperty: ParadoxScriptProperty, targetProperty: ParadoxScriptProperty): Boolean {
        val existsValue = existsProperty.propertyValue?.stringValue() ?: return false
        val targetKey = targetProperty.propertyKey.stringValue()
        return targetKey == existsValue
    }

    private fun getExistsProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isExistsPropertyOfExplicitForm(element)) return element
        if (isSecondProperty(element)) {
            val next = findExistsPropertyBefore(element)
            if (next != null && matchesExistsValue(element, next)) return next
        }
        return null
    }

    private fun getSecondProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        if (isSecondPropertyOfExplicitForm(element)) return element
        if (isExistsProperty(element)) {
            val next = findSecondPropertyAfter(element)
            if (next != null && matchesExistsValue(element, next)) return next
        }
        return null
    }

    private fun findSecondPropertyAfter(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = true, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull<ParadoxScriptProperty>()?.takeIf { isSecondProperty(it) }
    }

    private fun findExistsPropertyBefore(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        // 跳过（且仅能跳过）空白和注释
        return element.siblings(forward = false, withSelf = false).dropWhile { it is PsiWhiteSpace || it is PsiComment }
            .firstOrNull()?.castOrNull<ParadoxScriptProperty>()?.takeIf { isExistsProperty(it) }
    }

    // endregion

    /**
     * 判断 [element] 是否为嵌套形式。
     *
     * 说明：
     * - 作为外层属性的 [element] 的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]）。
     * - 内层属性的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]）。
     * - 块内的内层属性前后仅允许空白和注释。
     *
     * @see ParadoxLinkedExpression
     */
    fun isNestedForm(element: ParadoxScriptProperty): Boolean {
        // 属性键不能已经包含点号（即不能已经是链式形式）
        val expressionText = ParadoxExpressionManager.getExpressionText(element.propertyKey)
        if (expressionText.contains('.')) return false
        return findSingleInnerProperty(element) != null
    }

    /**
     * 判断 [element] 是否为链式形式。
     *
     * 说明：
     * - [element] 的属性键必须能解析为链式表达式（[ParadoxLinkedExpression]），且含有至少2个链接节点（[ParadoxLinkNode]）。
     *
     * @see ParadoxLinkedExpression
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
     * - [element] 必须是链式形式。参见 [isChainedForm]。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToNestedForm(element: ParadoxScriptProperty): Boolean {
        return isChainedForm(element)
    }

    /**
     * 判断 [element] 是否可以转换为链式形式。
     *
     * 说明：
     * - [element] 必须是嵌套形式。参见 [isSafeForm]。
     *
     * @see ParadoxLinkedExpression
     */
    fun canConvertToChainedForm(element: ParadoxScriptProperty): Boolean {
        return isNestedForm(element)
    }

    /**
     * 将 [element] 从链式形式转换为嵌套形式（单步展开）。
     *
     * 示例（`<caret>` 表示光标位置）：
     *
     * ```paradox_script
     * # before
     * <caret>root.owner = { a = 1 }
     *
     * # after
     * <caret>root = {
     *     owner = { a = 1 }
     * }
     * ```
     *
     * ```paradox_script
     * # before
     * root.<caret>owner = { a = 1 }
     *
     * # after
     * root = {
     *     <caret>owner = { a = 1 }
     * }
     * ```
     *
     * ```paradox_script
     * # before
     * root.<caret>owner.event_target:x
     *
     * # after
     * root = {
     *     <caret>owner.event_target:x
     * }
     * ```
     *
     * 说明：
     * - 找到链式表达式（[ParadoxLinkedExpression]）的直接子节点中，[caretOffset] 之前最后一个（或者链接中第一个）作为分隔符的点号，在此处分隔，
     * - 如果 [element] 的属性键用双引号包围，转换后也保留。反之亦然。
     * - 总是会在 `{` 之后和 `}` 之前自动插入换行。
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToNestedForm(element: ParadoxScriptProperty, project: Project, caretOffset: Int) {
        val propertyKey = element.propertyKey
        val expressionText = ParadoxExpressionManager.getExpressionText(propertyKey)
        val expressionOffset = ParadoxExpressionManager.getExpressionOffset(propertyKey)
        val cursorOffsetInExpression = caretOffset - propertyKey.textRange.startOffset - expressionOffset

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
     * 将 [element] 从嵌套形式转换为链式形式（单步合并）。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * root = {
     *     # comment
     *     owner = { a = 1 }
     * }
     *
     * # after
     * # comment
     * root.owner = { a = 1 }
     * ```
     *
     * 说明：
     * - 内层属性前后的注释会保留，并移到转换后的语句之前。
     * - 如果作为外层属性的 [element] 的属性键用双引号包围，转换后也保留。反之亦然。
     *
     * @see ParadoxLinkedExpression
     */
    fun convertToChainedForm(element: ParadoxScriptProperty, project: Project) {
        val innerProperty = findSingleInnerProperty(element) ?: return
        val outerKeyText = element.propertyKey.value
        val innerKeyText = innerProperty.propertyKey.value
        val valueText = innerProperty.propertyValue?.text ?: return

        val newText = "$outerKeyText.$innerKeyText = $valueText"
        val newElement = ParadoxScriptElementFactory.createProperty(project, newText)
        element.replace(newElement)
    }

    // region Helpers for Chained/Nested Form

    private fun resolveComplexExpressionFromKey(element: ParadoxScriptProperty): ParadoxComplexExpression? {
        val propertyKey = element.propertyKey
        val expressionText = ParadoxExpressionManager.getExpressionText(propertyKey)
        if (!expressionText.contains('.')) return null
        val file = element.containingFile ?: return null
        val configGroup = PlsFacade.getConfigGroup(file.project, selectGameType(file))
        val config = ParadoxConfigManager.getConfigs(propertyKey).firstOrNull() ?: return null
        return ParadoxComplexExpression.resolveByConfig(expressionText, null, configGroup, config)
    }

    private fun findSingleInnerProperty(element: ParadoxScriptProperty): ParadoxScriptProperty? {
        val block = element.block ?: return null
        var foundProperty: ParadoxScriptProperty? = null
        for (child in block.children) {
            when (child) {
                is PsiWhiteSpace, is PsiComment -> { /* 允许 */
                }
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

    private fun collectCommentsBeforeInBlock(block: ParadoxScriptBlock?, innerProperty: ParadoxScriptProperty): List<PsiComment> {
        if (block == null) return emptyList()
        return block.children.takeWhile { it !== innerProperty }.filterIsInstance<PsiComment>().toList()
    }

    private fun collectCommentsAfterInBlock(block: ParadoxScriptBlock?, innerProperty: ParadoxScriptProperty): List<PsiComment> {
        if (block == null) return emptyList()
        val innerIndex = block.children.indexOf(innerProperty)
        return block.children.drop(innerIndex + 1).filterIsInstance<PsiComment>().toList()
    }

    private fun isPropertyKeyQuoted(element: ParadoxScriptProperty): Boolean {
        val firstChild = element.propertyKey.node.findChildByType(STRING_TOKEN)
        return firstChild != null
    }

    // endregion
}
