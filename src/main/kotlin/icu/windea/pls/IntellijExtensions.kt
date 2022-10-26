@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.navigation.*
import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.lang.*
import com.intellij.lang.documentation.*
import com.intellij.navigation.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import com.intellij.refactoring.actions.BaseRefactoringAction.*
import com.intellij.util.*
import com.intellij.util.containers.*
import com.intellij.util.xmlb.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.cwt.psi.*
import java.io.*
import java.util.*
import javax.swing.*

//region JRT Extensions
fun String.compareToIgnoreCase(other: String): Int {
	return String.CASE_INSENSITIVE_ORDER.compare(this, other)
}
//endregion

//region Misc Extensions
fun LookupElement.withPriority(priority: Double, scopeMatched: Boolean = true): LookupElement {
	val finalPriority = if(scopeMatched) priority else priority + PlsCompletionPriorities.scopeMismatchOffset
	return PrioritizedLookupElement.withPriority(this, finalPriority)
}

fun LookupElement.withExplicitProximity(explicitProximity: Int): LookupElement {
	return PrioritizedLookupElement.withExplicitProximity(this, explicitProximity)
}

/**导航到指定元素的位置*/
fun navigateToElement(editor: Editor, element: PsiElement?) {
	val offset = element?.textOffset ?: return
	editor.caretModel.moveToOffset(offset)
	editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
}

/**导航到指定元素的位置并且光标选择该元素*/
fun selectElement(editor: Editor, element: PsiElement?) {
	val range = element?.textRange ?: return
	editor.selectionModel.setSelection(range.startOffset, range.endOffset)
}

//com.intellij.refactoring.actions.BaseRefactoringAction.findRefactoringTargetInEditor
fun DataContext.findElement(): PsiElement? {
	var element = this.getData(CommonDataKeys.PSI_ELEMENT)
	if(element == null) {
		val editor = this.getData(CommonDataKeys.EDITOR)
		val file = this.getData(CommonDataKeys.PSI_FILE)
		if(editor != null && file != null) {
			element = getElementAtCaret(editor, file)
		}
		val languages = this.getData(LangDataKeys.CONTEXT_LANGUAGES)
		if(element == null || element is SyntheticElement || languages == null) {
			return null
		}
	}
	return element
}

/**
 * 判断指定的节点是否在文档中跨多行。
 */
fun isSpanMultipleLines(node: ASTNode, document: Document): Boolean {
	val range = node.textRange
	val limit = if(range.endOffset < document.textLength) document.getLineNumber(range.endOffset) else document.lineCount - 1
	return document.getLineNumber(range.startOffset) < limit
}

//fun intern(table: CharTable, node: LighterASTTokenNode): String {
//	return table.intern(node.text).toString()
//}

private val DEFAULT_PSI_CONVERTOR = NotNullFunction<PsiElement, Collection<PsiElement>> { element: PsiElement ->
	ContainerUtil.createMaybeSingletonList(element)
}

fun createNavigationGutterIconBuilder(icon: Icon, gotoRelatedItemProvider: (PsiElement) -> Collection<GotoRelatedItem>): NavigationGutterIconBuilder<PsiElement> {
	return NavigationGutterIconBuilder.create(icon, DEFAULT_PSI_CONVERTOR, gotoRelatedItemProvider)
}

inline fun <T> UserDataHolder.getUserDataOnValid(key: Key<T>, predicate: (element: T) -> Boolean): T? {
	val data = this.getUserData(key) ?: return null
	if(predicate(data)) return data
	runCatching {
		this.putUserData(key, null)
	}
	return null
}

inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T>, action: () -> T?): T? {
	val data = this.getUserData(key)
	if(data != null) return data
	val newValue = action()
	if(newValue != null) this.putUserData(key, newValue)
	return newValue
}

inline fun <T> Query<T>.processResult(consumer: Processor<in T>): Boolean {
	return forEach(consumer)
}
//endregion

//region Documentation Extensions
inline fun StringBuilder.definition(block: StringBuilder.() -> Unit): StringBuilder {
	append(DocumentationMarkup.DEFINITION_START)
	block(this)
	append(DocumentationMarkup.DEFINITION_END)
	return this
}

inline fun StringBuilder.content(block: StringBuilder.() -> Unit): StringBuilder {
	append(DocumentationMarkup.CONTENT_START)
	block(this)
	append(DocumentationMarkup.CONTENT_END)
	return this
}

inline fun StringBuilder.sections(block: StringBuilder.() -> Unit): StringBuilder {
	append(DocumentationMarkup.SECTIONS_START)
	block(this)
	append(DocumentationMarkup.SECTIONS_END)
	return this
}

@Suppress("NOTHING_TO_INLINE")
inline fun StringBuilder.section(title: CharSequence, value: CharSequence): StringBuilder {
	append(DocumentationMarkup.SECTION_HEADER_START)
	append(title).append(" ")
	append(DocumentationMarkup.SECTION_SEPARATOR).append("<p>")
	append(value)
	append(DocumentationMarkup.SECTION_END)
	return this
}

inline fun StringBuilder.grayed(block: StringBuilder.() -> Unit): StringBuilder {
	append(DocumentationMarkup.GRAYED_START)
	block(this)
	append(DocumentationMarkup.GRAYED_END)
	return this
}

fun String.escapeXml() = if(this.isEmpty()) "" else StringUtil.escapeXmlEntities(this)

fun String.escapeXmlOrAnonymous() = if(this.isEmpty()) anonymousString else StringUtil.escapeXmlEntities(this)

fun String.escapeBlank(): String {
	var builder: StringBuilder? = null
	for((i, c) in this.withIndex()) {
		if(c.isWhitespace()) {
			if(builder == null) builder = StringBuilder(substring(0, i))
			builder.append("&nbsp;")
		} else {
			builder?.append(c)
		}
	}
	return builder?.toString() ?: this
}
//endregion

//region Code Insight Extensions
fun TemplateBuilder.buildTemplate() = cast<TemplateBuilderImpl>().buildTemplate()

fun TemplateBuilder.buildInlineTemplate() = cast<TemplateBuilderImpl>().buildInlineTemplate()

fun interface TemplateEditingFinishedListener : TemplateEditingListener {
	override fun beforeTemplateFinished(state: TemplateState, template: Template?) {}
	
	override fun templateCancelled(template: Template) {
		templateFinished(template, false)
	}
	
	override fun currentVariableChanged(templateState: TemplateState, template: Template, oldIndex: Int, newIndex: Int) {}
	
	override fun waitingForInput(template: Template) {}
}
//endregion

//region Editor Extensions

//endregion

//region VFS Extensions

///**查找当前项目中指定语言文件类型和作用域的VirtualFile。*/
//fun findVirtualFiles(project: Project, type: LanguageFileType): Collection<VirtualFile> {
//	return FileTypeIndex.getFiles(type, GlobalSearchScope.projectScope(project))
//}

///**查找当前项目中指定语言文件类型和作用域的PsiFile。*/
//inline fun <reified T : PsiFile> findFiles(project: Project, type: LanguageFileType): List<T> {
//	return FileTypeIndex.getFiles(type, GlobalSearchScope.projectScope(project)).mapNotNull {
//		PsiManager.getInstance(project).findFile(it)
//	}.filterIsInstance<T>()
//}

///**递归得到当前VirtualFile的所有作为子节点的VirtualFile。*/
//fun VirtualFile.getAllChildFiles(destination: MutableList<VirtualFile> = mutableListOf()): List<VirtualFile> {
//	for(child in this.children) {
//		if(child.isDirectory) child.getAllChildFiles(destination) else destination.add(child)
//	}
//	return destination
//}

/** 将VirtualFile转化为指定类型的PsiFile。 */
inline fun <reified T : PsiFile> VirtualFile.toPsiFile(project: Project): T? {
	return PsiManager.getInstance(project).findFile(this) as? T
}

/** 得到当前VirtualFile相对于指定的VirtualFile的路径。去除作为前缀的"/"。 */
fun VirtualFile.relativePathTo(other: VirtualFile): String {
	return this.path.removePrefix(other.path).trimStart('/')
}

/** （物理层面上）判断虚拟文件是否拥有BOM。 */
fun VirtualFile.hasBom(bom: ByteArray): Boolean {
	return this.bom.let { it != null && it contentEquals bom }
}

/** （物理层面上）为虚拟文件添加BOM。 */
@Throws(IOException::class)
fun VirtualFile.addBom(bom: ByteArray, wait: Boolean = true) {
	this.bom = bom
	val bytes = this.contentsToByteArray()
	val contentWithAddedBom = ArrayUtil.mergeArrays(bom, bytes)
	if(wait) {
		WriteAction.runAndWait<IOException> { this.setBinaryContent(contentWithAddedBom) }
	} else {
		WriteAction.run<IOException> { this.setBinaryContent(contentWithAddedBom) }
	}
}

/** （物理层面上）为虚拟文件移除BOM。 */
@Throws(IOException::class)
fun VirtualFile.removeBom(bom: ByteArray, wait: Boolean = true) {
	this.bom = null
	val bytes = this.contentsToByteArray()
	val contentWithStrippedBom = Arrays.copyOfRange(bytes, bom.size, bytes.size)
	if(wait) {
		WriteAction.runAndWait<IOException> { this.setBinaryContent(contentWithStrippedBom) }
	} else {
		WriteAction.run<IOException> { this.setBinaryContent(contentWithStrippedBom) }
	}
}

//endregion

//region AstNode Extensions
fun <T : ASTNode> T.takeIf(elementType: IElementType): T? {
	return takeIf { it.elementType == elementType }
}

fun <T : ASTNode> T.takeUnless(elementType: IElementType): T? {
	return takeUnless { it.elementType == elementType }
}

inline fun ASTNode.processChild(processor: ProcessEntry.(ASTNode) -> Boolean): Boolean {
	var child: ASTNode? = this.firstChildNode
	while(child != null) {
		val result = ProcessEntry.processor(child)
		if(!result) return false
		child = child.treeNext
	}
	return true
}

inline fun ASTNode.forEachChild(action: (ASTNode) -> Unit) {
	var child: ASTNode? = this.firstChildNode
	while(child != null) {
		action(child)
		child = child.treeNext
	}
}

fun ASTNode.isStartOfLine(): Boolean {
	return treePrev?.let { it.elementType == TokenType.WHITE_SPACE && it.text.containsLineBreak() } ?: false
}

fun ASTNode.isEndOfLine(): Boolean {
	return treeNext?.let { it.elementType == TokenType.WHITE_SPACE && it.text.containsLineBreak() } ?: false
}
//endregion

//region PsiElement Extensions
/**
 * 判断两个[PsiElement]是否在同一[VirtualFile]的同一位置。
 */
infix fun PsiElement?.isSamePosition(other: PsiElement?): Boolean{
	if(this == null || other == null) return false
	if(this == other) return true
	return textRange.startOffset == other.textRange.startOffset
		&& containingFile.originalFile.virtualFile == other.containingFile.originalFile.virtualFile
}

fun <T : PsiElement> T.takeIf(elementType: IElementType): T? {
	return takeIf { it.elementType == elementType }
}

fun <T : PsiElement> T.takeUnless(elementType: IElementType): T? {
	return takeUnless { it.elementType == elementType }
}

inline fun <reified T : PsiElement> PsiElement.findOptionalChild(): T? {
	//不会忽略某些特定类型的子元素
	var child: PsiElement? = this.firstChild
	while(child != null) {
		if(child is T) return child
		child = child.nextSibling
	}
	return null
}

inline fun <reified T : PsiElement> PsiElement.findRequiredChild(): T {
	return findOptionalChild()!!
}

@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.findOptionalChild(type: IElementType): T? {
	return node.findChildByType(type)?.psi as T?
}

@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.findRequiredChild(type: IElementType): T {
	return node.findChildByType(type)?.psi as T
}

inline fun PsiElement.processChild(forward: Boolean = true, processor: ProcessEntry.(PsiElement) -> Boolean): Boolean {
	//不会忽略某些特定类型的子元素
	var child: PsiElement? = if(forward) this.firstChild else this.lastChild
	while(child != null) {
		val result = ProcessEntry.processor(child)
		if(!result) return false
		child = if(forward) child.nextSibling else child.prevSibling
	}
	return true
}

inline fun <reified T : PsiElement> PsiElement.processChildrenOfType(forward: Boolean = true, processor: ProcessEntry.(T) -> Boolean): Boolean {
	//不会忽略某些特定类型的子元素
	var child: PsiElement? = if(forward) this.firstChild else this.lastChild
	while(child != null) {
		if(child is T) {
			val result = ProcessEntry.processor(child)
			if(!result) return false
		}
		child = if(forward) child.nextSibling else child.prevSibling
	}
	return true
}

inline fun PsiElement.forEachChild(forward: Boolean = true, action: (PsiElement) -> Unit) {
	//不会忽略某些特定类型的子元素
	var child: PsiElement? = if(forward) this.firstChild else this.lastChild
	while(child != null) {
		action(child)
		child = if(forward) child.nextSibling else child.prevSibling
	}
}

inline fun <reified T : PsiElement> PsiElement.forEachChildOfType(forward: Boolean = true, action: (T) -> Unit) {
	//不会忽略某些特定类型的子元素
	var child: PsiElement? = if(forward) this.firstChild else this.lastChild
	while(child != null) {
		if(child is T) {
			action(child)
		}
		child = if(forward) child.nextSibling else child.prevSibling
	}
}

inline fun <reified T> PsiElement.filterChildOfType(forward: Boolean = true, predicate: (T) -> Boolean = { true }): List<T> {
	//不会忽略某些特定类型的子元素
	var result: MutableList<T>? = null
	var child: PsiElement? = if(forward) this.firstChild else this.lastChild
	while(child != null) {
		if(child is T && predicate(child)) {
			if(result == null) result = SmartList()
			result.add(child)
		}
		child = if(forward) child.nextSibling else child.prevSibling
	}
	return result ?: emptyList()
}

inline fun <reified T : PsiElement> PsiElement.findChildOfType(forward: Boolean = true, predicate: (T) -> Boolean = { true }): T? {
	//不会忽略某些特定类型的子元素
	var child: PsiElement? = if(forward) this.firstChild else this.lastChild
	while(child != null) {
		if(child is T && predicate(child)) return child
		child = if(forward) child.nextSibling else child.prevSibling
	}
	return null
}

inline fun <reified T : PsiElement> PsiElement.indexOfChild(forward: Boolean = true, element: T): Int {
	var child = if(forward) this.firstChild else this.lastChild
	var index = 0
	while(child != null) {
		when(child) {
			element -> return index
			is T -> index++
			else -> child = if(forward) child.nextSibling else child.prevSibling
		}
	}
	return -1
}

///**查找最远的相同类型的兄弟节点。可指定是否向后查找，以及是否在空行处中断。*/
//fun findFurthestSiblingOfSameType(element: PsiElement, findAfter: Boolean, stopOnBlankLine: Boolean = true): PsiElement? {
//	var node = element.node
//	val expectedType = node.elementType
//	var lastSeen = node
//	while(node != null) {
//		val elementType = node.elementType
//		when {
//			elementType == expectedType -> lastSeen = node
//			elementType == TokenType.WHITE_SPACE -> {
//				if(stopOnBlankLine && node.text.containsBlankLine()) break
//			}
//			else -> break
//		}
//		node = if(findAfter) node.treeNext else node.treePrev
//	}
//	return lastSeen.psi
//}

fun PsiElement.firstLeafOrSelf(): PsiElement {
	var current = firstChild ?: return this
	while(true){
		current = current.firstChild ?: return current
	}
}

fun PsiElement.lastLeafOrSelf(): PsiElement {
	var current = lastChild ?: return this
	while(true){
		current = current.lastChild ?: return current
	}
}

val PsiElement.icon
	get() = getIcon(0)

fun PsiElement.getKeyword(offset: Int): String {
	return text.unquote().substring(0, offset)
}

fun PsiFile.setNameWithoutExtension(name: String): PsiElement {
	return setName(name + "." + this.name.substringAfterLast('.', ""))
}

object EmptyPointer : SmartPsiElementPointer<PsiElement> {
	override fun getElement() = null
	
	override fun getContainingFile() = null
	
	override fun getProject() = ProjectManager.getInstance().defaultProject
	
	override fun getVirtualFile() = null
	
	override fun getRange() = null
	
	override fun getPsiRange() = null
}

fun <T : PsiElement> emptyPointer(): SmartPsiElementPointer<T> = EmptyPointer.cast()

fun <E : PsiElement> E.createPointer(): SmartPsiElementPointer<E> {
	return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
}

fun <E : PsiElement> E.createPointer(file: PsiFile): SmartPsiElementPointer<E> {
	return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this, file)
}

fun PsiElement.reformatted(canChangeWhiteSpacesOnly: Boolean = false): PsiElement = let {
	CodeStyleManager.getInstance(it.project).reformat(it, canChangeWhiteSpacesOnly)
}

fun PsiElement.isSpaceOrSingleLineBreak(): Boolean {
	return this is PsiWhiteSpace && StringUtil.getLineBreakCount(this.text) <= 1
}

fun PsiElement.isSingleLineBreak(): Boolean {
	return this is PsiWhiteSpace && StringUtil.getLineBreakCount(this.text) == 1
}

inline fun findAcceptableElementIncludeComment(element: PsiElement?, predicate: (PsiElement) -> Boolean): Any? {
	var current: PsiElement? = element ?: return null
	while(current != null && current !is PsiFile) {
		if(predicate(current)) return current
		if(current is PsiComment) return current.siblings().find { it is CwtProperty }
			?.takeIf { it.prevSibling.isSpaceOrSingleLineBreak() }
		current = current.parent
	}
	return null
}

inline fun findTextStartOffsetIncludeComment(element: PsiElement, findUpPredicate: (PsiElement) -> Boolean): Int {
	//找到直到没有空行为止的最后一个注释，返回它的开始位移，或者输入元素的开始位移
	val target: PsiElement = if(element.prevSibling == null && findUpPredicate(element)) element.parent else element
	var current: PsiElement? = target
	var comment: PsiComment? = null
	while(current != null) {
		current = current.prevSibling ?: break
		when {
			current is PsiWhiteSpace && current.isSpaceOrSingleLineBreak() -> continue
			current is PsiComment -> comment = current
			else -> break
		}
	}
	if(comment != null) return comment.textRange.startOffset
	return target.textRange.startOffset
}

fun getLineCommentDocText(element: PsiElement): String? {
	//认为当前元素之前，之间没有空行的非行尾行注释，可以视为文档注释的一部分
	var lines: LinkedList<String>? = null
	var prevElement = element.prevSibling ?: element.parent?.prevSibling
	while(prevElement != null) {
		val text = prevElement.text
		if(prevElement !is PsiWhiteSpace) {
			if(prevElement !is PsiComment) break
			val docText = text.trimStart('#').trim().escapeXml()
			if(lines == null) lines = LinkedList()
			lines.addFirst(docText)
		} else {
			if(text.containsBlankLine()) break
		}
		// 兼容comment在rootBlock之外的特殊情况
		prevElement = prevElement.prevSibling
	}
	return lines?.joinToString("<br>")
}
//endregion

//region Index Extensions
inline fun <reified T : PsiElement> StringStubIndexExtension<T>.existsElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope
): Boolean {
	if(DumbService.isDumb(project)) return false
	
	var result = false
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) {
		result = true
		return@processElements false
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.existsElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope,
	crossinline predicate: (element: T) -> Boolean
): Boolean {
	if(DumbService.isDumb(project)) return false
	
	var result = false
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(predicate(element)) {
			result = true
			return@processElements false
		}
		true
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findFirstElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope,
	crossinline predicate: (element: T) -> Boolean = { true }
): T? {
	if(DumbService.isDumb(project)) return null
	
	var result: T? = null
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(predicate(element)) {
			result = element
			return@processElements false
		}
		true
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findLastElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope,
	crossinline predicate: (element: T) -> Boolean = { true }
): T? {
	if(DumbService.isDumb(project)) return null
	
	var result: T? = null
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(predicate(element)) {
			result = element
		}
		true
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.processAllElements(
	key: String,
	project: Project,
	scope: GlobalSearchScope,
	cancelable: Boolean = true,
	crossinline action: (T) -> Boolean
): Boolean {
	if(DumbService.isDumb(project)) return true
	
	return StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(cancelable) ProgressManager.checkCanceled()
		action(element)
	}
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.processAllElementsByKeys(
	project: Project,
	scope: GlobalSearchScope,
	cancelable: Boolean = true,
	crossinline keyPredicate: (key: String) -> Boolean = { true },
	crossinline action: (key: String, element: T) -> Boolean
): Boolean {
	if(DumbService.isDumb(project)) return true
	
	return StubIndex.getInstance().processAllKeys(this.key, project) { key ->
		if(cancelable) ProgressManager.checkCanceled()
		if(keyPredicate(key)) {
			StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
				if(cancelable) ProgressManager.checkCanceled()
				action(key, element)
			}
		}
		true
	}
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.processFirstElementByKeys(
	project: Project,
	scope: GlobalSearchScope,
	cancelable: Boolean = true,
	maxSize: Int = 0,
	crossinline keyPredicate: (key: String) -> Boolean = { true },
	crossinline predicate: (T) -> Boolean = { true },
	crossinline getDefaultValue: () -> T? = { null },
	crossinline resetDefaultValue: () -> Unit = {},
	crossinline processor: ProcessEntry.(element: T) -> Boolean
): Boolean {
	if(DumbService.isDumb(project)) return true
	
	var size = 0
	var value: T?
	return StubIndex.getInstance().processAllKeys(this.key, project) { key ->
		if(cancelable) ProgressManager.checkCanceled()
		if(keyPredicate(key)) {
			value = null
			resetDefaultValue()
			StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
				if(predicate(element)) {
					value = element
					return@processElements false
				}
				true
			}
			val finalValue = value ?: getDefaultValue()
			if(finalValue != null) {
				val result = ProcessEntry.processor(finalValue)
				if(result && maxSize > 0 && ++size == maxSize) return@processAllKeys false
			}
		}
		true
	}
}
//endregion

//region Xml Converters
class RegexIgnoreCaseConverter : Converter<Regex>() {
	override fun fromString(value: String): Regex {
		return value.toRegex(RegexOption.IGNORE_CASE)
	}
	
	override fun toString(t: Regex): String {
		return t.pattern
	}
}

class CommaDelimitedStringListConverter : Converter<List<String>>() {
	override fun fromString(value: String): List<String> {
		return value.toCommaDelimitedStringList()
	}
	
	override fun toString(value: List<String>): String {
		return value.toCommaDelimitedString()
	}
}

class CommaDelimitedStringSetIgnoreCaseConverter : Converter<Set<String>>() {
	override fun fromString(value: String): Set<String> {
		return value.toCommaDelimitedStringSet(ignoreCase = true)
	}
	
	override fun toString(value: Set<String>): String {
		return value.toCommaDelimitedString()
	}
}
//endregion