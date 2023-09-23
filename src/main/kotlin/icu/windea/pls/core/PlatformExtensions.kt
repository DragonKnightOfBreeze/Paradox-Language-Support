@file:Suppress("unused")

package icu.windea.pls.core

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.navigation.*
import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.*
import com.intellij.extapi.psi.*
import com.intellij.injected.editor.*
import com.intellij.lang.*
import com.intellij.lang.documentation.*
import com.intellij.lang.injection.*
import com.intellij.navigation.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.keymap.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import com.intellij.profile.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.impl.source.tree.injected.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import com.intellij.refactoring.*
import com.intellij.refactoring.actions.BaseRefactoringAction.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.*
import com.intellij.util.containers.*
import com.intellij.util.io.*
import com.intellij.util.xmlb.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import it.unimi.dsi.fastutil.*
import it.unimi.dsi.fastutil.objects.*
import java.io.*
import java.nio.file.*
import java.util.*
import java.util.Arrays
import javax.swing.*
import javax.swing.text.*
import kotlin.reflect.*

//region Stdlib Extensions
fun String.compareToIgnoreCase(other: String): Int {
    return String.CASE_INSENSITIVE_ORDER.compare(this, other)
}
//endregion

//region Common Extensions
object CaseInsensitiveStringHashingStrategy : Hash.Strategy<String?> {
    override fun hashCode(s: String?): Int {
        return if(s == null) 0 else StringUtilRt.stringHashCodeInsensitive(s)
    }
    
    override fun equals(s1: String?, s2: String?): Boolean {
        return s1.equals(s2, ignoreCase = true)
    }
}

fun caseInsensitiveStringSet(): MutableSet<@CaseInsensitive String> {
    //com.intellij.util.containers.CollectionFactory.createCaseInsensitiveStringSet()
    return ObjectLinkedOpenCustomHashSet(CaseInsensitiveStringHashingStrategy)
}

fun <V> caseInsensitiveStringKeyMap(): MutableMap<@CaseInsensitive String, V> {
    //com.intellij.util.containers.createCaseInsensitiveStringMap()
    return Object2ObjectLinkedOpenCustomHashMap(CaseInsensitiveStringHashingStrategy)
}

fun String.unquotedTextRange(): TextRange {
    val leftQuoted = this.isLeftQuoted()
    val rightQuoted = this.isRightQuoted()
    val startOffset = if(leftQuoted) 1 else 0
    val endOffset = if(rightQuoted) length - 1 else length
    return TextRange.create(startOffset, endOffset)
}

fun TextRange.unquote(text: String): TextRange {
    val leftQuoted = text.isLeftQuoted()
    val rightQuoted = text.isRightQuoted()
    val textRange = this
    return when {
        leftQuoted && rightQuoted -> TextRange.create(textRange.startOffset + 1, textRange.endOffset - 1)
        leftQuoted -> TextRange.create(textRange.startOffset + 1, textRange.endOffset)
        rightQuoted -> TextRange.create(textRange.startOffset, textRange.endOffset - 1)
        else -> textRange
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

private val DEFAULT_PSI_CONVERTOR = NotNullFunction<PsiElement, Collection<PsiElement>> { element: PsiElement ->
    ContainerUtil.createMaybeSingletonList(element)
}

fun createNavigationGutterIconBuilder(icon: Icon, gotoRelatedItemProvider: (PsiElement) -> Collection<GotoRelatedItem>): NavigationGutterIconBuilder<PsiElement> {
    return NavigationGutterIconBuilder.create(icon, DEFAULT_PSI_CONVERTOR, gotoRelatedItemProvider)
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
inline fun <T> Query<T>.processQuery(onlyMostRelevant: Boolean = false, consumer: Processor<in T>): Boolean {
    if(onlyMostRelevant && this is ParadoxQuery<*, *>) {
        find()?.let { consumer.process(it as T) }
        return true
    }
    return forEach(consumer)
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
inline fun <T> Query<T>.processQueryAsync(onlyMostRelevant: Boolean = false, consumer: Processor<in T>): Boolean {
    if(onlyMostRelevant && this is ParadoxQuery<*, *>) {
        find()?.let { consumer.process(it as T) }
        return true
    }
    return allowParallelProcessing().forEach(consumer)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Result<T>.cancelable() = onFailure { if(it is ProcessCanceledException) throw it }

@Suppress("NOTHING_TO_INLINE")
inline fun <T> UserDataHolder.tryPutUserData(key: Key<T>, value: T?) {
    runCatching { putUserData(key, value) }.cancelable()
}

inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T>, action: () -> T): T {
    val data = this.getUserData(key)
    if(data != null) return data
    val newValue = action()
    if(newValue != null) putUserData(key, newValue)
    return newValue
}

inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T>, nullValue: T, action: () -> T?): T? {
    val data = this.getUserData(key)
    if(data != null) return data.takeUnless { it == nullValue }
    val newValue = action()
    if(newValue != null) putUserData(key, newValue) else putUserData(key, nullValue)
    return newValue
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> UserDataHolder.putUserDataIfAbsent(key: Key<T>, value: T) {
    if(getUserData(key) == null) putUserData(key, value)
}

fun <T> ProcessingContext.getOrDefault(key: Key<T>): T? {
    val value = this.get(key)
    if(value == null && key is KeyWithDefaultValue) {
        val defaultValue = key.defaultValue
        this.put(key, defaultValue)
        return defaultValue
    }
    return value
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> createKey(name: String) = Key.create<T>(name)
@Suppress("NOTHING_TO_INLINE")
inline fun <T> createKey(name: String, noinline defaultValueProvider: () -> T) = KeyWithDefaultValue.create(name, defaultValueProvider)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Key<T>.getValue(thisRef: KeysAware, property: KProperty<*>) = this
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> KeyWithDefaultValue<T>.getValue(thisRef: KeysAware, property: KProperty<*>) = this

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Key<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T? = thisRef.getUserData(this)
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> KeyWithDefaultValue<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T = thisRef.getUserData(this)!!
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Key<T>.setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) = thisRef.putUserData(this, value)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Key<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T? = thisRef.getOrDefault(this)
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> KeyWithDefaultValue<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T = thisRef.getOrDefault(this)!!
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> Key<T>.setValue(thisRef: ProcessingContext, property: KProperty<*>, value: T?) = thisRef.put(this, value)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> DataKey<T>.getValue(thisRef: DataContext, property: KProperty<*>): T? = thisRef.getData(this)
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> DataKey<T>.getValue(thisRef: AnActionEvent, property: KProperty<*>): T? = thisRef.dataContext.getData(this)
//endregion

//region CachedValue Extensions
@Suppress("NOTHING_TO_INLINE")
inline fun <T> createCachedValueKey(
    name: String,
    trackValue: Boolean = false,
    project: Project = getDefaultProject(),
    provider: CachedValueProvider<T>
) = CachedValueKey(name, trackValue, project, provider)

fun <T> T.withDependencyItems(vararg dependencyItems: Any): CachedValueProvider.Result<T> {
    if(dependencyItems.isEmpty()) return CachedValueProvider.Result.create(this, ModificationTracker.NEVER_CHANGED)
    return CachedValueProvider.Result.create(this, *dependencyItems)
}

fun <T> T.withDependencyItems(dependencyItems: List<Any>): CachedValueProvider.Result<T> {
    if(dependencyItems.isEmpty()) return CachedValueProvider.Result.create(this, ModificationTracker.NEVER_CHANGED)
    return CachedValueProvider.Result.create(this, dependencyItems)
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
    append(title).append(": ")
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

fun String?.orAnonymous() = if(isNullOrEmpty()) PlsConstants.anonymousString else this
fun String?.orUnknown() = if(isNullOrEmpty()) PlsConstants.unknownString else this
fun String?.orUnresolved() = if(isNullOrEmpty()) PlsConstants.unresolvedString else this

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

//region Editor & Document Extensions
fun Document.isAtLineStart(offset: Int, skipWhitespace: Boolean = false): Boolean {
    if(!skipWhitespace) return DocumentUtil.isAtLineStart(offset, this)
    val lineStartOffset = DocumentUtil.getLineStartOffset(offset, this)
    val charsSequence = charsSequence
    for(i in offset..lineStartOffset) {
        val c = charsSequence[i]
        if(!c.isWhitespace()) {
            return false
        }
    }
    return true
}

fun Document.isAtLineEnd(offset: Int, skipWhitespace: Boolean = false): Boolean {
    if(!skipWhitespace) return DocumentUtil.isAtLineEnd(offset, this)
    val lineEndOffset = DocumentUtil.getLineEndOffset(offset, this)
    val charsSequence = charsSequence
    for(i in offset..lineEndOffset) {
        if(i >= charsSequence.length) return true
        val c = charsSequence[i]
        if(!c.isWhitespace()) {
            return false
        }
    }
    return true
}

inline fun Document.getCharToLineStart(offset: Int, skipWhitespaceOnly: Boolean = false, predicate: (Char) -> Boolean): Int {
    val lineStartOffset = DocumentUtil.getLineStartOffset(offset, this)
    val charsSequence = charsSequence
    for(i in offset..lineStartOffset) {
        val c = charsSequence[i]
        if(predicate(c)) return i
        if(skipWhitespaceOnly && !c.isWhitespace()) return -1
    }
    return -1
}
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

fun String.toVirtualFile(refreshIfNeed: Boolean = false): VirtualFile? {
    val path = this.toPathOrNull() ?: return null
    return VfsUtil.findFile(path, refreshIfNeed)
}

fun Path.toVirtualFile(refreshIfNeed: Boolean = false): VirtualFile? {
    val path = this
    return VfsUtil.findFile(path, refreshIfNeed)
}

/** 将VirtualFile转化为指定类型的PsiFile。 */
@Suppress("NOTHING_TO_INLINE")
inline fun VirtualFile.toPsiFile(project: Project): PsiFile? {
    return PsiManager.getInstance(project).findFile(this)
}

/** 将VirtualFile转化为指定类型的PsiDirectory。 */
@Suppress("NOTHING_TO_INLINE")
inline fun VirtualFile.toPsiDirectory(project: Project): PsiDirectory? {
    return PsiManager.getInstance(project).findDirectory(this)
}

/** 将VirtualFile转化为指定类型的PsiFile或者PsiDirectory。 */
@Suppress("NOTHING_TO_INLINE")
inline fun VirtualFile.toPsiFileSystemItem(project: Project): PsiFileSystemItem? {
    return if(this.isFile) PsiManager.getInstance(project).findFile(this) else PsiManager.getInstance(project).findDirectory(this)
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

//region AST Extensions
fun <T : ASTNode> T.takeIf(elementType: IElementType): T? {
    return takeIf { it.elementType == elementType }
}

fun <T : ASTNode> T.takeUnless(elementType: IElementType): T? {
    return takeUnless { it.elementType == elementType }
}

inline fun ASTNode.processChild(processor: (ASTNode) -> Boolean): Boolean {
    var child: ASTNode? = this.firstChildNode
    while(child != null) {
        val result = processor(child)
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

fun ASTNode.firstChild(type: IElementType): ASTNode? {
    var child: ASTNode? = this.firstChildNode
    while(child != null) {
        if(child.elementType == type) return child
        child = child.treeNext
    }
    return null
}

fun ASTNode.firstChild(types: TokenSet): ASTNode? {
    var child: ASTNode? = this.firstChildNode
    while(child != null) {
        if(child.elementType in types) return child
        child = child.treeNext
    }
    return null
}

fun ASTNode.firstChild(predicate: (ASTNode) -> Boolean): ASTNode? {
    var child: ASTNode? = this.firstChildNode
    while(child != null) {
        if(predicate(child)) return child
        child = child.treeNext
    }
    return null
}

fun LighterASTNode.firstChild(tree: LighterAST, type: IElementType): LighterASTNode? {
    return LightTreeUtil.firstChildOfType(tree, this, type)
}

fun LighterASTNode.firstChild(tree: LighterAST, types: TokenSet): LighterASTNode? {
    return LightTreeUtil.firstChildOfType(tree, this, types)
}

inline fun LighterASTNode.firstChild(tree: LighterAST, predicate: (LighterASTNode) -> Boolean): LighterASTNode? {
    val children = tree.getChildren(this)
    for(i in children.indices) {
        val child = children[i]
        if(predicate(child)) return child
    }
    return null
}

fun LighterASTNode.internNode(tree: LighterAST): CharSequence? {
    if(this !is LighterASTTokenNode) return null
    return tree.charTable.intern(this.text).toString()
}
//endregion

//region PSI Extensions
val PsiElement.startOffset get() = if(this is ASTDelegatePsiElement) this.node.startOffset else this.textRange.startOffset

val PsiElement.endOffset get() = if(this is ASTDelegatePsiElement) this.node.let { it.startOffset + it.textLength } else this.textRange.endOffset

fun PsiElement.hasSyntaxError(): Boolean {
    return this.lastChild is PsiErrorElement
}

/**
 * @param forward 查找偏移之前还是之后的PSI元素，默认为null，表示同时考虑。
 */
fun <T : PsiElement> PsiFile.findElementAt(offset: Int, forward: Boolean? = null, transform: (element: PsiElement) -> T?): T? {
    var element0: PsiElement? = null
    if(forward != false) {
        val element = findElementAt(offset)
        if(element != null) {
            element0 = element
            val result = transform(element)
            if(result != null) {
                return result
            }
        }
    }
    if(forward != true && offset > 0) {
        val leftElement = findElementAt(offset - 1)
        if(leftElement != null && leftElement !== element0) {
            val leftResult = transform(leftElement)
            if(leftResult != null) {
                return leftResult
            }
        }
    }
    return null
}

fun <T : PsiElement> PsiFile.findElementsBetween(startOffset: Int, endOffset: Int, rootTransform: (element: PsiElement) -> PsiElement?, transform: (element: PsiElement) -> T?): List<T> {
    val startRoot = findElementAt(startOffset, true, rootTransform) ?: return emptyList()
    val endRoot = findElementAt(endOffset, true, rootTransform) ?: return emptyList()
    val root = if(startRoot.isAncestor(endRoot)) startRoot else endRoot
    val elements = mutableListOf<T>()
    root.processChild {
        val textRange = it.textRange
        if(textRange.endOffset > startOffset && textRange.startOffset < endOffset) {
            val isLast = textRange.endOffset >= endOffset
            val result = transform(it)
            if(result != null) elements.add(result)
            !isLast
        } else {
            true
        }
    }
    return elements
}

fun PsiFile.findAllElementsBetween(startOffset: Int, endOffset: Int, rootTransform: (element: PsiElement) -> PsiElement?): List<PsiElement> {
    val startRoot = findElementAt(startOffset, true, rootTransform) ?: return emptyList()
    val endRoot = findElementAt(endOffset, true, rootTransform) ?: return emptyList()
    val root = if(startRoot.isAncestor(endRoot)) startRoot else endRoot
    val elements = mutableListOf<PsiElement>()
    root.processChild {
        val textRange = it.textRange
        if(textRange.endOffset > startOffset) {
            elements.add(it)
            true
        } else {
            if(textRange.startOffset < endOffset) {
                val isLast = textRange.endOffset >= endOffset
                elements.add(it)
                !isLast
            }
            true
        }
    }
    return elements
}

/**
 * @param forward 查找偏移之前还是之后的PSI引用，默认为null，表示同时考虑。
 */
fun PsiFile.findReferenceAt(offset: Int, forward: Boolean? = null, predicate: (reference: PsiReference) -> Boolean): PsiReference? {
    if(forward != false) {
        val reference = findReferenceAt(offset)
        if(reference != null && predicate(reference)) {
            return reference
        }
    }
    if(forward != true && offset > 0) {
        val reference = findReferenceAt(offset - 1)
        if(reference != null && predicate(reference)) {
            return reference
        }
    }
    return null
}

fun PsiReference.resolveFirst(): PsiElement? {
    return if(this is PsiPolyVariantReference) {
        this.multiResolve(false).firstNotNullOfOrNull { it.element }
    } else {
        this.resolve()
    }
}

/**
 * 判断两个[PsiElement]是否在同一[VirtualFile]的同一位置。
 */
infix fun PsiElement?.isSamePosition(other: PsiElement?): Boolean {
    if(this == other) return true
    if(this == null || other == null) return false
    return startOffset == other.startOffset
        && containingFile.originalFile.virtualFile == other.containingFile.originalFile.virtualFile
}

inline fun <reified T : PsiElement> PsiElement.findChild(forward: Boolean = true): T? {
    return findChildOfType(forward)
}

fun PsiElement.findChild(type: IElementType, forward: Boolean = true): PsiElement? {
    return findChildOfType(forward) { it.elementType == type }
}

fun PsiElement.findChild(tokenSet: TokenSet, forward: Boolean = true): PsiElement? {
    return findChildOfType(forward) { it.elementType in tokenSet }
}

inline fun <reified T : PsiElement> PsiElement.findChildren(forward: Boolean = true): List<T> {
    return findChildrenOfType(forward)
}

fun PsiElement.findChildren(type: IElementType, forward: Boolean = true): List<PsiElement> {
    return findChildrenOfType(forward) { it.elementType == type }
}

fun PsiElement.findChildren(tokenSet: TokenSet, forward: Boolean = true): List<PsiElement> {
    return findChildrenOfType(forward) { it.elementType in tokenSet }
}

inline fun PsiElement.processChild(forward: Boolean = true, processor: (PsiElement) -> Boolean): Boolean {
    //不会忽略某些特定类型的子元素
    var child: PsiElement? = if(forward) this.firstChild else this.lastChild
    while(child != null) {
        val result = processor(child)
        if(!result) return false
        child = if(forward) child.nextSibling else child.prevSibling
    }
    return true
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

inline fun PsiElement.forEachChild(forward: Boolean = true, action: (PsiElement) -> Unit) {
    //不会忽略某些特定类型的子元素
    var child: PsiElement? = if(forward) this.firstChild else this.lastChild
    while(child != null) {
        action(child)
        child = if(forward) child.nextSibling else child.prevSibling
    }
}

inline fun <reified T : PsiElement> PsiElement.processChildrenOfType(forward: Boolean = true, processor: (T) -> Boolean): Boolean {
    //不会忽略某些特定类型的子元素
    var child: PsiElement? = if(forward) this.firstChild else this.lastChild
    while(child != null) {
        if(child is T) {
            val result = processor(child)
            if(!result) return false
        }
        child = if(forward) child.nextSibling else child.prevSibling
    }
    return true
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

inline fun <reified T> PsiElement.findChildrenOfType(forward: Boolean = true, predicate: (T) -> Boolean = { true }): List<T> {
    //不会忽略某些特定类型的子元素
    var result: MutableList<T>? = null
    var child: PsiElement? = if(forward) this.firstChild else this.lastChild
    while(child != null) {
        if(child is T && predicate(child)) {
            if(result == null) result = mutableListOf()
            result.add(child)
        }
        child = if(forward) child.nextSibling else child.prevSibling
    }
    return result ?: emptyList()
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

val PsiElement.icon
    get() = getIcon(0)

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

fun SmartPsiElementPointer<*>.isEmpty() = this === EmptyPointer

fun <E : PsiElement> E.createPointer(project: Project = this.project): SmartPsiElementPointer<E> {
    return SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
}

fun <E : PsiElement> E.createPointer(file: PsiFile?, project: Project = this.project): SmartPsiElementPointer<E> {
    return try {
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this, file)
    } catch(e: IllegalArgumentException) {
        //Element from alien project - use empty pointer
        emptyPointer()
    }
}

fun PsiElement.isSpaceOrSingleLineBreak(): Boolean {
    return this is PsiWhiteSpace && StringUtil.getLineBreakCount(this.text) <= 1
}

fun PsiElement.isSingleLineBreak(): Boolean {
    return this is PsiWhiteSpace && StringUtil.getLineBreakCount(this.text) == 1
}

///**
// * 搭配[com.intellij.psi.util.PsiUtilCore.getElementAtOffset]使用。
// */
//fun PsiElement.getSelfOrPrevSiblingNotWhitespace(): PsiElement {
//	if(this !is PsiWhiteSpace) return this
//	var current = this.prevSibling ?: return this
//	while(current is PsiWhiteSpace){
//		current = current.prevSibling ?: return this
//	}
//	return current
//}

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
    if(comment != null) return comment.startOffset
    return target.startOffset
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

fun getReferenceElement(originalElement: PsiElement?): PsiElement? {
    val element = when {
        originalElement == null -> return null
        originalElement.elementType == TokenType.WHITE_SPACE -> originalElement.prevSibling ?: return null
        else -> originalElement
    }
    return when {
        element is LeafPsiElement -> element.parent
        else -> element
    }
}
//endregion

//region Language Injection Extensions
/**
 * 向上找到最顶层的作为语言注入宿主的虚拟文件，或者返回自身。
 */
fun VirtualFile.findTopHostFileOrThis(): VirtualFile {
    return doFindTopHostFileOrThis(this)
}

private tailrec fun doFindTopHostFileOrThis(file: VirtualFile): VirtualFile {
    if(file is VirtualFileWindow) return doFindTopHostFileOrThis(file.delegate)
    return file
}

/**
 * 向上找到最顶层的作为语言注入宿主的PSI元素，或者返回自身。
 */
fun PsiElement.findTopHostElementOrThis(project: Project): PsiElement {
    return doFindTopHostElementOrThis(this, project)
}

private tailrec fun doFindTopHostElementOrThis(element: PsiElement, project: Project): PsiElement {
    val host = InjectedLanguageManager.getInstance(project).getInjectionHost(element)
    if(host == null) return element
    return doFindTopHostElementOrThis(host, project)
}

fun PsiFile.getShreds(): Place? {
    //why it's deprecated and internal???
    //@Suppress("UnstableApiUsage", "DEPRECATION")
    //return InjectedLanguageUtilBase.getShreds(this)
    
    return viewProvider.document.castOrNull<DocumentWindow>()?.getShreds()
}
//endregion

//region Inspection Extensions
fun getInspectionToolState(shortName: String, element: PsiElement?, project: Project): ScopeToolState? {
    val currentProfile = InspectionProfileManager.getInstance(project).currentProfile
    val tools = currentProfile.getToolsOrNull(shortName, project) ?: return null
    return tools.getState(element)
}

val ScopeToolState.enabledTool: InspectionProfileEntry? get() = if(isEnabled) tool.tool else null
//endregion

//region Xml Converters
class CommaDelimitedStringListConverter : Converter<List<String>>() {
    override fun fromString(value: String): List<String> {
        return value.toCommaDelimitedStringList()
    }
    
    override fun toString(value: List<String>): String {
        return value.toCommaDelimitedString()
    }
}

class CommaDelimitedStringSetConverter : Converter<Set<String>>() {
    override fun fromString(value: String): Set<String> {
        return value.toCommaDelimitedStringSet()
    }
    
    override fun toString(value: Set<String>): String {
        return value.toCommaDelimitedString()
    }
}
//endregion
