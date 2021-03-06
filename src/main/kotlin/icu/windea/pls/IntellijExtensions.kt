package icu.windea.pls

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.documentation.*
import com.intellij.codeInsight.lookup.*
import com.intellij.lang.*
import com.intellij.lang.documentation.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import com.intellij.refactoring.actions.BaseRefactoringAction.*
import com.intellij.util.*

//region Misc Extensions
val iconSize get() = DocumentationComponent.getQuickDocFontSize().size

/**得到当前AST节点的除了空白节点之外的所有子节点。*/
fun ASTNode.nodes(): List<ASTNode> {
	val result = mutableListOf<ASTNode>()
	var child = this.firstChildNode
	while(child != null) {
		if(child.elementType !== TokenType.WHITE_SPACE) result += child
		child = child.treeNext
	}
	return result
}

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

/**将VirtualFile转化为指定类型的PsiFile。*/
inline fun <reified T : PsiFile> VirtualFile.toPsiFile(project: Project): T? {
	return PsiManager.getInstance(project).findFile(this) as? T
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

fun LookupElement.withPriority(priority: Double): LookupElement {
	return PrioritizedLookupElement.withPriority(this, priority)
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

/**
 * 得到处理后的[VirtualFile]，以便查看它的子节点。
 *
 * 如果当前的[VirtualFile]是一个压缩文件，则进行特殊处理，否则返回自身。
 */
fun VirtualFile.optimized(): VirtualFile {
	val extension = this.extension
	return when {
		extension == "jar" || extension == "zip" -> JarFileSystem.getInstance().getRootByLocal(this) ?: this
		else -> this
	}
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
//endregion

//region Documentation Extensions
inline fun StringBuilder.definition(block: StringBuilder.() -> Unit) {
	append(DocumentationMarkup.DEFINITION_START)
	block(this)
	append(DocumentationMarkup.DEFINITION_END)
}

inline fun StringBuilder.content(block: StringBuilder.() -> Unit) {
	append(DocumentationMarkup.CONTENT_START)
	block(this)
	append(DocumentationMarkup.CONTENT_END)
}

inline fun StringBuilder.sections(block: StringBuilder.() -> Unit) {
	append(DocumentationMarkup.SECTIONS_START)
	block(this)
	append(DocumentationMarkup.SECTIONS_END)
}

@Suppress("NOTHING_TO_INLINE")
inline fun StringBuilder.section(title: CharSequence, value: CharSequence) {
	append(DocumentationMarkup.SECTION_HEADER_START)
	append(title).append(" ")
	append(DocumentationMarkup.SECTION_SEPARATOR).append("<p>")
	append(value)
	append(DocumentationMarkup.SECTION_END)
}

inline fun StringBuilder.grayed(block: StringBuilder.() -> Unit) {
	append(DocumentationMarkup.GRAYED_START)
	block(this)
	append(DocumentationMarkup.GRAYED_END)
}

fun String.escapeXml() = if(this.isEmpty()) "" else StringUtil.escapeXmlEntities(this)

fun String.escapeXmlOrAnonymous() = if(this.isEmpty()) anonymousEscapedString else StringUtil.escapeXmlEntities(this)
//endregion

//region PsiElement Extensions
inline fun PsiElement.forEachChild(block: (PsiElement) -> Unit) {
	//不会忽略某些特定类型的子元素
	var child = this.firstChild
	while(child != null) {
		block(child)
		child = child.nextSibling
	}
}

inline fun PsiElement.findChild(predicate: (PsiElement) -> Boolean): PsiElement? {
	//不会忽略某些特定类型的子元素
	var child = this.firstChild
	while(child != null) {
		if(predicate(child)) return child
		child = child.nextSibling
	}
	return null
}

inline fun <T : PsiElement, R> PsiElement.mapChildOfType(type: Class<out T>, transform: (T) -> R): List<R> {
	//为了优化性能，使用SmartList，并且不保存中间结果
	//参考：com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList
	
	var result: MutableList<R>? = null
	var child: PsiElement? = this.firstChild
	while(child != null) {
		if(type.isInstance(child)) {
			if(result == null) result = SmartList()
			val r = transform(type.cast(child))
			result.add(r)
		}
		child = child.nextSibling
	}
	return result ?: emptyList()
}

inline fun <T : PsiElement, R> PsiElement.mapChildOfTypeNotNull(type: Class<out T>, transform: (T) -> R?): List<R> {
	//为了优化性能，使用SmartList，并且不保存中间结果
	//参考：com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList
	
	var result: MutableList<R>? = null
	var child: PsiElement? = this.firstChild
	while(child != null) {
		if(type.isInstance(child)) {
			if(result == null) result = SmartList()
			val r = transform(type.cast(child))
			if(r != null) result.add(r)
		}
		child = child.nextSibling
	}
	return result ?: emptyList()
}

inline fun <reified T : PsiElement> PsiElement.indexOfChild(element: T): Int {
	var child = firstChild
	var index = 0
	while(child != null) {
		when(child) {
			element -> return index
			is T -> index++
			else -> child = child.nextSibling
		}
	}
	return -1
}

val PsiElement.virtualFile: VirtualFile?
	get() {
		return PsiUtilCore.getVirtualFile(this)
	}

val PsiElement.firstLeafOrSelf: PsiElement
	get() {
		val firstChild = firstChild
		return firstChild?.firstLeafOrSelf ?: this
	}

val PsiElement.icon
	get() = getIcon(Iconable.ICON_FLAG_VISIBILITY)

val PsiElement.keyword
	get() = text.removeSurrounding("\"", "\"").let { s ->
		runCatching { s.dropLast(dummyIdentifierLength) }.getOrElse { s }
	}

object EmptyPointer : SmartPsiElementPointer<PsiElement> {
	override fun getElement() = null
	
	override fun getContainingFile() = null
	
	override fun getProject() = getDefaultProject()
	
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
//endregion

//region Index Extensions
inline fun <reified T : PsiElement> StringStubIndexExtension<T>.existsElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope
): Boolean {
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
	crossinline predicate: (T) -> Boolean
): Boolean {
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
	scope: GlobalSearchScope
): T? {
	var result: T? = null
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		result = element
		false
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findFirstElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope,
	hasDefault: Boolean = false,
	crossinline predicate: (T) -> Boolean
): T? {
	var result: T? = null
	var defaultResult: T? = null
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(hasDefault) defaultResult = element
		if(predicate(element)) {
			result = element
			return@processElements false
		}
		true
	}
	return result ?: defaultResult
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findLastElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope
): T? {
	var result: T? = null
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		result = element
		true
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findLastElement(
	key: String,
	project: Project,
	scope: GlobalSearchScope,
	hasDefault: Boolean = false,
	crossinline predicate: (T) -> Boolean
): T? {
	var result: T? = null
	var defaultResult: T? = null
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(hasDefault) defaultResult = element
		if(predicate(element)) {
			result = element
		}
		true
	}
	return result ?: defaultResult
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findAllElements(
	key: String,
	project: Project,
	scope: GlobalSearchScope,
	cancelable: Boolean = true,
	maxSize: Int = 0,
	crossinline predicate: (T) -> Boolean = { true }
): List<T> {
	val result: SmartList<T> = SmartList()
	var size = 0
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(cancelable) ProgressManager.checkCanceled()
		if(predicate(element)) {
			result += element
			if(maxSize > 0) {
				size++
				if(size == maxSize) return@processElements false
			}
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
	crossinline action: (T, MutableList<T>) -> Boolean
): List<T> {
	val result: SmartList<T> = SmartList()
	StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
		if(cancelable) ProgressManager.checkCanceled()
		action(element, result)
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findAllElementsByKeys(
	project: Project,
	scope: GlobalSearchScope,
	cancelable: Boolean = true,
	maxSize: Int = 0,
	crossinline keyPredicate: (String) -> Boolean = { true },
	crossinline predicate: (T) -> Boolean = { true }
): List<T> {
	val result: SmartList<T> = SmartList()
	var size = 0
	StubIndex.getInstance().processAllKeys(this.key, project) { key ->
		if(cancelable) ProgressManager.checkCanceled()
		if(keyPredicate(key)) {
			StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
				if(cancelable) ProgressManager.checkCanceled()
				if(predicate(element)) {
					result += element
				}
				if(maxSize > 0) {
					size++
					if(size == maxSize) return@processElements false
				}
				true
			}
		}
		if(maxSize > 0) {
			if(size == maxSize) return@processAllKeys false
		}
		true
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.processAllElementsByKeys(
	project: Project,
	scope: GlobalSearchScope,
	cancelable: Boolean = true,
	crossinline keyPredicate: (String) -> Boolean = { true },
	crossinline action: (T, MutableList<T>) -> Boolean
): List<T> {
	val result: SmartList<T> = SmartList()
	StubIndex.getInstance().processAllKeys(this.key, project) { key ->
		if(cancelable) ProgressManager.checkCanceled()
		if(keyPredicate(key)) {
			StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
				if(cancelable) ProgressManager.checkCanceled()
				action(element, result)
			}
		}
		true
	}
	return result
}

inline fun <reified T : PsiElement> StringStubIndexExtension<T>.findFirstElementByKeys(
	project: Project,
	scope: GlobalSearchScope,
	cancelable: Boolean = true,
	hasDefault: Boolean = false,
	maxSize: Int = 0,
	crossinline keyPredicate: (String) -> Boolean = { true },
	crossinline predicate: (T) -> Boolean = { true }
): List<T> {
	val result: SmartList<T> = SmartList()
	var size = 0
	StubIndex.getInstance().processAllKeys(this.key, project) { key ->
		if(cancelable) ProgressManager.checkCanceled()
		if(keyPredicate(key)) {
			var value: T? = null
			var defaultValue: T? = null
			StubIndex.getInstance().processElements(this.key, key, project, scope, T::class.java) { element ->
				if(hasDefault) defaultValue = element
				if(predicate(element)) {
					value = element
					return@processElements false
				}
				true
			}
			val finalValue = value ?: defaultValue
			if(finalValue != null) {
				result.add(finalValue)
				if(maxSize > 0) {
					size++
					if(size == maxSize) return@processAllKeys false
				}
			}
		}
		true
	}
	return result
}
//endregion