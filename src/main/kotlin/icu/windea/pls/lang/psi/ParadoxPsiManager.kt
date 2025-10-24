package icu.windea.pls.lang.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.containsBlankLine
import icu.windea.pls.core.containsLineBreak
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.findChild
import icu.windea.pls.core.findChildren
import icu.windea.pls.core.orNull
import icu.windea.pls.core.pass
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue
import icu.windea.pls.model.constants.PlsPatternConstants
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.resolved
import java.util.LinkedList
import kotlin.collections.ArrayDeque

object ParadoxPsiManager {
    object Keys : KeyRegistry() {
        val cachedArgumentTupleList by createKey<CachedValue<List<Tuple2<String, String>>>>(Keys)
    }

    // region Common Methods

    fun getArgumentTupleList(element: ParadoxScriptBlock, vararg excludeNames: String): List<Tuple2<String, String>> {
        val r = doGetArgumentTupleListFromCache(element)
        return if (excludeNames.isEmpty()) r else r.filter { (k) -> k !in excludeNames }
    }

    private fun doGetArgumentTupleListFromCache(element: ParadoxScriptBlock): List<Tuple2<String, String>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedArgumentTupleList) {
            val value = doGetArgumentTupleList(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetArgumentTupleList(element: ParadoxScriptBlock): List<Tuple2<String, String>> {
        return buildList {
            element.propertyList.mapNotNull f@{ p ->
                // 对于传入参数的名字，要求不为空，且不要求必须严格合法（匹配 PlsPatternConstants.argumentName）
                val k = p.propertyKey.name.orNull() ?: return@f null
                if (!PlsPatternConstants.argumentName.matches(k)) return@f null
                val v = p.propertyValue?.text ?: return@f null
                tupleOf(k, v)
            }
        }
    }

    fun findMemberElementsToInline(element: PsiElement): Tuple2<PsiElement?, PsiElement?> {
        return when (element) {
            is ParadoxScriptRootBlock -> {
                val e1 = element.firstChild?.siblings(forward = true, withSelf = true)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                val e2 = element.lastChild?.siblings(forward = false, withSelf = true)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                e1 to e2
            }
            is ParadoxScriptBlock -> {
                val e1 = element.firstChild?.siblings(forward = true, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxScriptElementTypes.LEFT_BRACE }?.drop(1)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                val e2 = element.lastChild?.siblings(forward = false, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxScriptElementTypes.RIGHT_BRACE }?.drop(1)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                e1 to e2
            }
            is ParadoxScriptParameterCondition -> {
                val e1 = element.firstChild?.siblings(forward = true, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxScriptElementTypes.NESTED_RIGHT_BRACKET }?.drop(1)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                val e2 = element.lastChild?.siblings(forward = false, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxScriptElementTypes.RIGHT_BRACKET }?.drop(1)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                e1 to e2
            }
            else -> null to null
        }
    }

    fun findRichTextElementsToInline(element: PsiElement): Tuple2<PsiElement?, PsiElement?> {
        return when {
            element is ParadoxLocalisationPropertyValue -> {
                val element0 = element.findChild { it.elementType == ParadoxLocalisationElementTypes.PROPERTY_VALUE_TOKEN }
                val e1 = element0?.firstChild?.siblings(forward = true, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxLocalisationElementTypes.LEFT_QUOTE }?.drop(1)
                    ?.firstOrNull()
                val e2 = element0?.lastChild?.siblings(forward = false, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxLocalisationElementTypes.RIGHT_QUOTE }?.drop(1)
                    ?.firstOrNull()
                e1 to e2
            }
            else -> null to null
        }
    }

    fun getLineCommentText(element: PsiElement, lineSeparator: String = "\n"): String? {
        // 认为当前元素之前，之间没有空行的非行尾行注释，可以视为文档注释的一部分

        var lines: LinkedList<String>? = null
        var prevElement = element.prevSibling ?: element.parent?.prevSibling // 兼容comment在rootBlock之外的特殊情况
        while (prevElement != null) {
            val text = prevElement.text
            if (prevElement !is PsiWhiteSpace) {
                if (prevElement !is PsiComment) break
                val docText = text.trimStart('#').trim().escapeXml()
                if (lines == null) lines = LinkedList()
                lines.addFirst(docText)
            } else {
                if (text.containsBlankLine()) break
            }
            prevElement = prevElement.prevSibling
        }
        if (lines.isNullOrEmpty()) return null
        return lines.joinToString(lineSeparator)
    }

    fun getDocCommentText(element: PsiElement, documentationElementType: IElementType, lineSeparator: String = "\n"): String? {
        // 如果某行注释以'#'开始，则输出时需要全部忽略
        // 如果某行注释以'\'结束，则输出时不要在这里换行

        var lines: MutableList<String>? = null
        var current: PsiElement = element
        while (true) {
            current = current.prevSibling ?: break
            when {
                current.elementType == documentationElementType -> {
                    if (lines == null) lines = ArrayDeque()
                    val line = current.text.trimStart('#').trim()
                    lines.addFirst(line)
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        if (lines.isNullOrEmpty()) return null
        return lines.joinToString(lineSeparator).replace("\\$lineSeparator", "")
    }

    // endregion

    // region Inline Methods

    fun inlineScriptedVariable(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptScriptedVariable, project: Project) {
        if (element !is ParadoxScriptedVariableReference) return

        val toInline = declaration.scriptedVariableValue ?: return
        var newText = rangeInElement.replace(element.text, toInline.text)
        // 某些情况下newText会以"@"开始，需要去掉
        if (element !is ParadoxScriptInlineMathScriptedVariableReference && newText.startsWith('@')) {
            newText = newText.drop(1)
        }
        val language = element.language
        when (language) {
            is ParadoxScriptLanguage -> {
                // 这里会把newText识别为一个值，但是实际上newText可以是任何文本，目前不进行额外的处理
                val newRef = ParadoxScriptElementFactory.createValue(project, newText)
                element.replace(newRef)
            }
            is ParadoxLocalisationLanguage -> {
                // 这里会把newText识别为一个字符串，但是实际上newText可以是任何文本，目前不进行额外的处理
                newText = newText.unquote() // 内联到本地化文本中时，需要先尝试去除周围的双引号
                val newRef = ParadoxLocalisationElementFactory.createString(project, newText)
                // element.parent should be something like "$@var$"
                element.parent.replace(newRef)
            }
        }
    }

    fun inlineScriptedTrigger(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptProperty, project: Project) {
        // 必须是一个调用而非任何引用
        if (element !is ParadoxScriptPropertyKey) return
        if (element.text.unquote().length != rangeInElement.length) return

        val property = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return
        val toInline = declaration.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return
        var newText = toInline.text.trim()
        var reverse = false
        val valueElement = element.propertyValue?.resolved() ?: return
        when (valueElement) {
            is ParadoxScriptBoolean -> {
                if (!valueElement.booleanValue) {
                    reverse = true
                    newText = newText.removeSurroundingOrNull("{", "}")?.trim() ?: return
                    val multiline = newText.containsLineBreak()
                    if (multiline) {
                        newText = "NOT = {\n${newText}\n}"
                    } else {
                        newText = "NOT = { ${newText} }"
                    }
                }
            }
            is ParadoxScriptBlock -> {
                val args = getArgumentTupleList(valueElement)
                if (args.isNotEmpty()) {
                    val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
                    newText = ParadoxParameterManager.replaceTextWithArgs(newRef, args, direct = false)
                }
            }
            else -> return
        }
        if (reverse) {
            val newRef = ParadoxScriptElementFactory.createProperty(project, newText)
            newRef.block?.let { handleInlinedScriptedTrigger(it) }
            property.parent.addAfter(newRef, property)
        } else {
            val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
            handleInlinedScriptedTrigger(newRef)
            val (start, end) = findMemberElementsToInline(newRef)
            if (start != null && end != null) {
                property.parent.addRangeAfter(start, end, property)
            }
        }
        property.delete()
    }

    fun handleInlinedScriptedTrigger(element: PsiElement) {
        // 特殊处理
        element.findChildren { it is ParadoxScriptString && it.value.lowercase() == "optimize_memory" }.forEach { it.delete() }
    }

    fun inlineScriptedEffect(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptProperty, project: Project) {
        // 必须是一个调用而非任何引用
        if (element !is ParadoxScriptPropertyKey) return
        if (element.text.unquote().length != rangeInElement.length) return

        val property = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return
        val toInline = declaration.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return
        var newText = toInline.text.trim()
        val valueElement = element.propertyValue?.resolved() ?: return
        when (valueElement) {
            is ParadoxScriptBoolean -> {
                if (!valueElement.booleanValue) {
                    property.delete()
                    return
                }
            }
            is ParadoxScriptBlock -> {
                val args = getArgumentTupleList(valueElement)
                if (args.isNotEmpty()) {
                    val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
                    newText = ParadoxParameterManager.replaceTextWithArgs(newRef, args, direct = false)
                }
            }
            else -> return
        }
        val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
        handleInlinedScriptedEffect(newRef)
        val (start, end) = findMemberElementsToInline(newRef)
        if (start != null && end != null) {
            property.parent.addRangeAfter(start, end, property)
        }
        property.delete()
    }

    fun handleInlinedScriptedEffect(element: PsiElement) {
        // 特殊处理
        element.findChildren { it is ParadoxScriptString && it.value.lowercase() == "optimize_memory" }.forEach { it.delete() }
    }

    @Suppress("unused")
    fun inlineInlineScript(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptFile, project: Project) {
        if (element !is ParadoxScriptValue) return

        var newText = declaration.text.trim()
        val usageElement = ParadoxInlineScriptManager.getUsageElement(element) ?: return
        val valueElement = usageElement.propertyValue?.resolved() ?: return
        when (valueElement) {
            is ParadoxScriptString -> pass()
            is ParadoxScriptBlock -> {
                val args = getArgumentTupleList(valueElement, "script")
                if (args.isNotEmpty()) {
                    val newRef = ParadoxScriptElementFactory.createRootBlock(project, newText)
                    newText = ParadoxParameterManager.replaceTextWithArgs(newRef, args, direct = true)
                }
            }
            else -> return
        }
        val newRef = ParadoxScriptElementFactory.createRootBlock(project, newText)
        val (start, end) = findMemberElementsToInline(newRef)
        if (start != null && end != null) {
            usageElement.parent.addRangeAfter(start, end, usageElement)
        }
        usageElement.delete()
    }

    @Suppress("unused")
    fun inlineLocalisation(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxLocalisationProperty, project: Project) {
        if (element !is ParadoxLocalisationParameter) return

        val toInline = declaration.propertyValue ?: return
        val newText = toInline.text.unquote()
        val newRef = ParadoxLocalisationElementFactory.createPropertyValue(project, newText)
        val (start, end) = findRichTextElementsToInline(newRef)
        if (start != null && end != null) {
            element.parent.addRangeAfter(start, end, element)
        }
        element.delete()
    }

    // endregion

    // region Introduce Methods

    /**
     * 在所属定义之前另起一行（跳过注释和空白），声明指定名字和值的封装变量。
     */
    fun introduceLocalScriptedVariable(name: String, value: String, parentDefinitionOrFile: ParadoxScriptDefinitionElement, project: Project): ParadoxScriptScriptedVariable {
        val (parent, anchor) = parentDefinitionOrFile.findParentAndAnchorToIntroduceLocalScriptedVariable()
        var newVariable = ParadoxScriptElementFactory.createScriptedVariable(project, name, value.quoteIfNecessary())
        val newLine = ParadoxScriptElementFactory.createLine(project)
        newVariable = parent.addAfter(newVariable, anchor).cast()
        if (anchor != null) parent.addBefore(newLine, newVariable) else parent.addAfter(newLine, newVariable)
        return newVariable
    }

    private fun ParadoxScriptDefinitionElement.findParentAndAnchorToIntroduceLocalScriptedVariable(): Pair<PsiElement, PsiElement?> {
        if (this is ParadoxScriptFile) {
            val anchor = this.findChild<ParadoxScriptScriptedVariable>(forward = false)
            if (anchor == null) return this to this.lastChild
            return this to anchor
        } else {
            val parent = parent
            val anchor: PsiElement? = this.siblings(forward = false, withSelf = false).find {
                it !is PsiWhiteSpace && it !is PsiComment
            }
            if (anchor == null && parent is ParadoxScriptRootBlock) {
                return parent.parent to null // (file, null)
            }
            return parent to anchor
        }
    }

    /**
     * 在指定文件的最后一个封装变量声明后或者最后一个PSI元素后另起一行，声明指定名字和值的封装变量。
     */
    fun introduceGlobalScriptedVariable(name: String, value: String, targetFile: ParadoxScriptFile, project: Project): ParadoxScriptScriptedVariable {
        val (parent, anchor) = targetFile.findParentAndAnchorToIntroduceGlobalScriptedVariable()
        var newVariable = ParadoxScriptElementFactory.createScriptedVariable(project, name, value.quoteIfNecessary())
        val newLine = ParadoxScriptElementFactory.createLine(project)
        newVariable = parent.addAfter(newVariable, anchor).cast()
        parent.addBefore(newLine, newVariable)
        return newVariable
    }

    private fun ParadoxScriptFile.findParentAndAnchorToIntroduceGlobalScriptedVariable(): Pair<PsiElement, PsiElement> {
        val anchor = this.findChild<ParadoxScriptScriptedVariable>(forward = false)
        if (anchor == null) return this to this.lastChild
        return this to anchor
    }

    // endregion

    // region Rename Methods

    fun handleElementRename(element: ParadoxExpressionElement, rangeInElement: TextRange, newElementName: String): PsiElement {
        val element = element
        val resolvedElement = if (element is ParadoxScriptExpressionElement) element.resolved() else element
        return when {
            resolvedElement == null -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
            resolvedElement.language is CwtLanguage -> throw IncorrectOperationException() // cannot rename cwt config
            resolvedElement.language is ParadoxBaseLanguage -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
            else -> throw IncorrectOperationException()
        }
    }

    // endregion
}
