package icu.windea.pls.util

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import kotlin.Pair

@Suppress("UNUSED_PARAMETER")
object ParadoxPsiManager {
    //region Find Methods
    
    object FindDefinitionOptions {
        const val DEFAULT = 0x01
        const val BY_ROOT_KEY = 0x02
        const val BY_NAME = 0x04
        const val BY_REFERENCE = 0x08
    }
    
    /**
     * @param options 从哪些位置查找对应的定义。如果传1，则表示直接向上查找即可。
     */
    fun findDefinition(file: PsiFile, offset: Int, options: Int = 1): ParadoxScriptDefinitionElement? {
        val expressionElement by lazy {
            file.findElementAt(offset) {
                it.parentOfType<ParadoxScriptExpressionElement>(false)
            }?.takeIf { it.isExpression() }
        }
        val expressionReference by lazy {
            file.findReferenceAt(offset) {
                it.element is ParadoxScriptExpressionElement && it.canResolve(ParadoxResolveConstraint.Definition)
            }
        }
        
        if(BitUtil.isSet(options, FindDefinitionOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parents(false).find p@{ p -> p is ParadoxScriptDefinitionElement && p.definitionInfo != null }
            }?.castOrNull<ParadoxScriptDefinitionElement?>()
            if(result != null) return result
        } else {
            if(BitUtil.isSet(options, FindDefinitionOptions.BY_ROOT_KEY)) {
                val element = expressionElement
                if(element is ParadoxScriptPropertyKey && element.isDefinitionRootKey()) {
                    return element.findParentDefinition()
                }
            }
            if(BitUtil.isSet(options, FindDefinitionOptions.BY_NAME)) {
                val element = expressionElement
                if(element is ParadoxScriptString && element.isDefinitionName()) {
                    return element.findParentDefinition()
                }
            }
        }
        if(BitUtil.isSet(options, FindDefinitionOptions.BY_REFERENCE)) {
            val reference = expressionReference
            val resolved = reference?.resolve()?.castOrNull<ParadoxScriptDefinitionElement>()?.takeIf { it.definitionInfo != null }
            if(resolved != null) return resolved
        }
        return null
    }
    
    object FindLocalisationOptions {
        const val DEFAULT = 0x01
        const val BY_NAME = 0x02
        const val BY_REFERENCE = 0x04
    }
    
    /**
     * @param options 从哪些位置查找对应的定义。如果传1，则表示直接向上查找即可。
     */
    fun findLocalisation(file: PsiFile, offset: Int, options: Int = 1): ParadoxLocalisationProperty? {
        if(BitUtil.isSet(options, FindLocalisationOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parents(false).find p@{ p -> p is ParadoxLocalisationProperty && p.localisationInfo != null }
            }?.castOrNull<ParadoxLocalisationProperty?>()
            if(result != null) return result
        } else {
            if(BitUtil.isSet(options, FindLocalisationOptions.BY_NAME)) {
                val result = file.findElementAt(offset) p@{
                    if(it.elementType != ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN) return@p null
                    it.parents(false).find p@{ p -> p is ParadoxLocalisationProperty && p.localisationInfo != null }
                }?.castOrNull<ParadoxLocalisationProperty?>()
                if(result != null) return result
            }
        }
        if(BitUtil.isSet(options, FindLocalisationOptions.BY_REFERENCE)) {
            val reference = file.findReferenceAt(offset) {
                it.canResolve(ParadoxResolveConstraint.Localisation)
            }
            val resolved = when {
                reference == null -> null
                reference is ParadoxLocalisationPropertyPsiReference -> reference.resolveLocalisation()
                else -> reference.resolve()
            }?.castOrNull<ParadoxLocalisationProperty?>()
            if(resolved != null) return resolved
        }
        return null
    }
    
    fun findScriptExpression(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return file.findElementAt(offset) {
            it.parentOfType<ParadoxScriptExpressionElement>(false)
        }?.takeIf { it.isExpression() }
    }
    
    fun findLocalisationColorfulText(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationColorfulText? {
        return file.findElementAt(offset) t@{
            if(fromNameToken && it.elementType != ParadoxLocalisationElementTypes.COLOR_TOKEN) return@t null
            it.parentOfType<ParadoxLocalisationColorfulText>(false)
        }
    }
    
    fun findLocalisationLocale(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationLocale? {
        return file.findElementAt(offset) p@{
            if(fromNameToken && it.elementType != ParadoxLocalisationElementTypes.LOCALE_TOKEN) return@p null
            it.parentOfType<ParadoxLocalisationLocale>(false)
        }
    }
    
    //endregion
    
    //region Inline Methods
    
    fun inlineScriptedVariable(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptScriptedVariable, project: Project) {
        if(element !is ParadoxScriptedVariableReference) return
        
        val toInline = declaration.scriptedVariableValue ?: return
        var newText = rangeInElement.replace(element.text, toInline.text)
        //某些情况下newText会以"@"开始，需要去掉
        if(element !is ParadoxScriptInlineMathScriptedVariableReference && newText.startsWith('@')) {
            newText = newText.drop(1)
        }
        val language = element.language
        when {
            language == ParadoxScriptLanguage -> {
                //这里会把newText识别为一个值，但是实际上newText可以是任何文本，目前不进行额外的处理
                val newRef = ParadoxScriptElementFactory.createValue(project, newText)
                element.replace(newRef)
            }
            language == ParadoxLocalisationLanguage -> {
                //这里会把newText识别为一个字符串，但是实际上newText可以是任何文本，目前不进行额外的处理
                newText = newText.unquote() //内联到本地化文本中时，需要先尝试去除周围的双引号
                val newRef = ParadoxLocalisationElementFactory.createString(project, newText)
                //element.parent should be something like "$@var$"  
                element.parent.replace(newRef)
            }
            else -> return //unexpected
        }
    }
    
    fun inlineScriptedTrigger(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptProperty, project: Project) {
        //必须是一个调用而非任何引用
        if(element !is ParadoxScriptPropertyKey) return
        if(element.text.unquote().length != rangeInElement.length) return
        
        val property = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return
        val toInline = declaration.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return
        var newText = toInline.text.trim()
        var reverse = false
        val valueElement = element.propertyValue?.resolved() ?: return
        when(valueElement) {
            is ParadoxScriptBoolean -> {
                if(!valueElement.booleanValue) {
                    reverse = true
                    newText = newText.removeSurroundingOrNull("{", "}")?.trim() ?: return
                    val multiline = newText.containsLineBreak()
                    if(multiline) {
                        newText = "NOT = {\n${newText}\n}"
                    } else {
                        newText = "NOT = { ${newText} }"
                    }
                }
            }
            is ParadoxScriptBlock -> {
                val args = getArgs(valueElement)
                if(args.isNotEmpty()) {
                    val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
                    newText = inlineWithArgs(newRef, args)
                }
            }
            else -> return
        }
        if(reverse) {
            val newRef = ParadoxScriptElementFactory.createPropertyFromText(project, newText)
            newRef.block?.let { handleBlockToInline(it, "scripted_trigger") }
            property.parent.addAfter(newRef, property)
        } else {
            val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
            handleBlockToInline(newRef, "scripted_trigger")
            val (start, end) = findMemberElementsToInline(newRef)
            if(start != null && end != null) {
                property.parent.addRangeAfter(start, end, property)
            }
        }
        property.delete()
    }
    
    fun inlineScriptedEffect(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptProperty, project: Project) {
        //必须是一个调用而非任何引用
        if(element !is ParadoxScriptPropertyKey) return
        if(element.text.unquote().length != rangeInElement.length) return
        
        val property = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return
        val toInline = declaration.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return
        var newText = toInline.text.trim()
        val valueElement = element.propertyValue?.resolved() ?: return
        when(valueElement) {
            is ParadoxScriptBoolean -> {
                if(!valueElement.booleanValue) {
                    property.delete()
                    return
                }
            }
            is ParadoxScriptBlock -> {
                val args = getArgs(valueElement)
                if(args.isNotEmpty()) {
                    val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
                    newText = inlineWithArgs(newRef, args)
                }
            }
            else -> return
        }
        val newRef = ParadoxScriptElementFactory.createBlock(project, newText)
        handleBlockToInline(newRef, "scripted_effect")
        val (start, end) = findMemberElementsToInline(newRef)
        if(start != null && end != null) {
            property.parent.addRangeAfter(start, end, property)
        }
        property.delete()
    }
    
    fun inlineInlineScript(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxScriptFile, project: Project) {
        if(element !is ParadoxScriptValue) return
        
        var newText = declaration.text.trim()
        val contextReferenceElement = ParadoxInlineScriptHandler.getContextReferenceElement(element) ?: return
        val valueElement = contextReferenceElement.propertyValue?.resolved() ?: return
        when(valueElement) {
            is ParadoxScriptString -> pass()
            is ParadoxScriptBlock -> {
                val args = getArgs(valueElement, "script")
                if(args.isNotEmpty()) {
                    val newRef = ParadoxScriptElementFactory.createRootBlock(project, newText)
                    newText = inlineWithArgs(newRef, args, unquoteValue = true)
                }
            }
            else -> return
        }
        val newRef = ParadoxScriptElementFactory.createRootBlock(project, newText)
        val (start, end) = findMemberElementsToInline(newRef)
        if(start != null && end != null) {
            contextReferenceElement.parent.addRangeAfter(start, end, contextReferenceElement)
        }
        contextReferenceElement.delete()
    }
    
    fun inlineLocalisation(element: PsiElement, rangeInElement: TextRange, declaration: ParadoxLocalisationProperty, project: Project) {
        if(element !is ParadoxLocalisationPropertyReference) return
        
        val toInline = declaration.propertyValue ?: return
        val newText = toInline.text.unquote()
        val newRef = ParadoxLocalisationElementFactory.createPropertyValue(project, newText)
        val (start, end) = findRichTextElementsToInline(newRef)
        if(start != null && end != null) {
            element.parent.addRangeAfter(start, end, element)
        }
        element.delete()
    }
    
    private fun getArgs(element: ParadoxScriptBlockElement, vararg excludeArgNames: String): Map<String, String> {
        return buildMap {
            element.propertyList.forEach f@{ p ->
                val pk = p.propertyKey.text
                if(excludeArgNames.isNotEmpty() && pk.lowercase() in excludeArgNames) return@f
                val pv = p.propertyValue?.text ?: return@f
                this += tupleOf(pk, pv)
            }
        }
    }
    
    private fun inlineWithArgs(element: ParadoxScriptBlockElement, args: Map<String, String>, unquoteValue: Boolean = false): String {
        //参数实际上可以被替换成任何文本（而不仅仅是严格意义上的字符串）
        //如果必要，也处理条件代码块
        
        val offset = element.startOffset
        val replacements = mutableListOf<Tuple2<TextRange, String>>()
        
        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun elementFinished(element: PsiElement) {
                run {
                    if(element !is ParadoxScriptParameterCondition) return@run
                    val conditionExpression = element.parameterConditionExpression ?: return@run
                    val parameter = conditionExpression.parameterConditionParameter
                    val name = parameter.name
                    if(!args.containsKey(name)) return@run
                    val operator = conditionExpression.findChild { it.elementType == ParadoxScriptElementTypes.NOT_SIGN } == null
                    if(operator) {
                        val (start, end) = findMemberElementsToInline(element)
                        if(start != null && end != null) {
                            element.parent.addRangeAfter(start, end, element)
                        }
                    }
                    element.delete()
                }
            }
        })
        
        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                run {
                    //TODO 1.2.2+ 目前不确定这里是否需要在内联后去除参数值周围的双引号（对于内联脚本来说，应当需要）
                    if(element !is ParadoxParameter) return@run
                    val n = element.name ?: return@run
                    val v0 = args[n] ?: return@run
                    val v = if(unquoteValue) v0.unquote() else v0
                    replacements.add(tupleOf(element.textRange.shiftLeft(offset), v))
                    return
                }
                super.visitElement(element)
            }
        })
        
        var newText = element.text
        replacements.reversed().forEach { (range, v) ->
            newText = newText.replaceRange(range.startOffset, range.endOffset, v)
        }
        return newText
    }
    
    private fun handleBlockToInline(element: ParadoxScriptBlock, type: String) {
        if(type == "scripted_trigger" || type == "scripted_effect") {
            //特殊处理
            element.findChildren { it is ParadoxScriptString && it.value.lowercase() == "optimize_memory" }.forEach { it.delete() }
        }
    }
    
    //endregion
    
    //region Introduce Methods
    
    /**
     * 在所属定义之前另起一行（跳过注释和空白），声明指定名字和值的封装变量。
     */
    fun introduceLocalScriptedVariable(name: String, value: String, parentDefinitionOrFile: ParadoxScriptDefinitionElement, project: Project): ParadoxScriptScriptedVariable {
        val (parent, anchor) = parentDefinitionOrFile.findParentAndAnchorToIntroduceLocalScriptedVariable()
        var newVariable = ParadoxScriptElementFactory.createScriptedVariable(project, name, value)
        val newLine = ParadoxScriptElementFactory.createLine(project)
        newVariable = parent.addAfter(newVariable, anchor).cast()
        if(anchor != null) parent.addBefore(newLine, newVariable) else parent.addAfter(newLine, newVariable)
        return newVariable
    }
    
    private fun ParadoxScriptDefinitionElement.findParentAndAnchorToIntroduceLocalScriptedVariable(): Pair<PsiElement, PsiElement?> {
        if(this is ParadoxScriptFile) {
            val anchor = this.findChildOfType<ParadoxScriptScriptedVariable>(forward = false)
                ?: return this to this.lastChild
            return this to anchor
        } else {
            val parent = parent
            val anchor: PsiElement? = this.siblings(forward = false, withSelf = false).find {
                it !is PsiWhiteSpace && it !is PsiComment
            }
            if(anchor == null && parent is ParadoxScriptRootBlock) {
                return parent.parent to null //(file, null)
            }
            return parent to anchor
        }
    }
    
    /**
     * 在指定文件的最后一个封装变量声明后或者最后一个PSI元素后另起一行，声明指定名字和值的封装变量。
     */
    fun introduceGlobalScriptedVariable(name: String, value: String, targetFile: ParadoxScriptFile, project: Project): ParadoxScriptScriptedVariable {
        val (parent, anchor) = targetFile.findParentAndAnchorToIntroduceGlobalScriptedVariable()
        var newVariable = ParadoxScriptElementFactory.createScriptedVariable(project, name, value)
        val newLine = ParadoxScriptElementFactory.createLine(project)
        newVariable = parent.addAfter(newVariable, anchor).cast()
        parent.addBefore(newLine, newVariable)
        return newVariable
    }
    
    private fun ParadoxScriptFile.findParentAndAnchorToIntroduceGlobalScriptedVariable(): Pair<PsiElement, PsiElement> {
        val anchor = this.findChildOfType<ParadoxScriptScriptedVariable>(forward = false)
            ?: return this to this.lastChild
        return this to anchor
    }
    
    //endregion
    
    //region Misc Methods
    
    /**
     * 判断当前位置应当是一个[ParadoxLocalisationLocale]，还是一个[ParadoxLocalisationPropertyKey]。
     */
    fun isLocalisationLocaleLike(element: PsiElement): Boolean {
        val elementType = element.elementType
        when {
            elementType == ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN -> {
                //后面只能是空白或冒号,接下来的后面只能是空白，接着前面只能是空白，并且要在一行的开头
                val prevElement = element.prevLeaf(false)
                if(prevElement != null) {
                    val prevElementType = prevElement.elementType
                    if(prevElementType != TokenType.WHITE_SPACE || !prevElement.text.last().isExactLineBreak()) return false
                }
                val nextElement = element.nextSibling
                if(nextElement != null) {
                    val nextElementType = nextElement.elementType
                    if(nextElementType != ParadoxLocalisationElementTypes.COLON && nextElementType != TokenType.WHITE_SPACE) return false
                    val nextNextElement = nextElement.nextSibling
                    if(nextNextElement != null) {
                        val nextNextElementType = nextElement.elementType
                        if(nextNextElementType != TokenType.WHITE_SPACE) return false
                    }
                }
                return true
            }
            elementType == ParadoxLocalisationElementTypes.LOCALE_TOKEN -> {
                return true
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
    }
    
    fun isGlobalScriptedVariable(element: ParadoxScriptScriptedVariable): Boolean {
        val path = selectFile(element)?.fileInfo?.pathToEntry?.path ?: return false
        return "common/scripted_variables".matchesPath(path)
    }
    
    fun isInvocationReference(element: PsiElement, referenceElement: PsiElement): Boolean {
        if(element !is ParadoxScriptProperty) return false
        if(referenceElement !is ParadoxScriptPropertyKey) return false
        val name = element.definitionInfo?.name?.orNull() ?: return false
        if(name != referenceElement.text.unquote()) return false
        return true
    }
    
    fun findMemberElementsToInline(element: PsiElement): Tuple2<PsiElement?, PsiElement?> {
        return when {
            element is ParadoxScriptRootBlock -> {
                val e1 = element.firstChild?.siblings(forward = true, withSelf = true)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                val e2 = element.lastChild?.siblings(forward = false, withSelf = true)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                e1 to e2
            }
            element is ParadoxScriptBlock -> {
                val e1 = element.firstChild?.siblings(forward = true, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxScriptElementTypes.LEFT_BRACE }?.drop(1)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                val e2 = element.lastChild?.siblings(forward = false, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxScriptElementTypes.RIGHT_BRACE }?.drop(1)
                    ?.find { it.elementType != TokenType.WHITE_SPACE }
                e1 to e2
            }
            element is ParadoxScriptParameterCondition -> {
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
                val e1 = element.firstChild?.siblings(forward = true, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxLocalisationElementTypes.LEFT_QUOTE }?.drop(1)
                    ?.firstOrNull()
                val e2 = element.lastChild?.siblings(forward = false, withSelf = true)
                    ?.dropWhile { it.elementType != ParadoxLocalisationElementTypes.RIGHT_QUOTE }?.drop(1)
                    ?.firstOrNull()
                e1 to e2
            }
            else -> null to null
        }
    }
    
    //endregion
}