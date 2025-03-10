@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import com.google.common.util.concurrent.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.intention.*
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
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import com.intellij.patterns.*
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
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.ui.scale.*
import com.intellij.ui.table.*
import com.intellij.util.*
import com.intellij.util.containers.*
import com.intellij.util.io.*
import com.intellij.util.ui.*
import com.intellij.util.xmlb.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.model.*
import it.unimi.dsi.fastutil.*
import it.unimi.dsi.fastutil.objects.*
import java.awt.*
import java.awt.image.*
import java.io.*
import java.net.*
import java.nio.file.*
import java.util.*
import java.util.Arrays
import java.util.concurrent.*
import java.util.function.*
import java.util.logging.*
import javax.swing.*
import javax.swing.table.*
import javax.swing.text.*
import kotlin.collections.isNullOrEmpty
import kotlin.properties.*
import kotlin.reflect.*

//region Common Extensions

fun String.compareToIgnoreCase(other: String): Int {
    return String.CASE_INSENSITIVE_ORDER.compare(this, other)
}

object CaseInsensitiveStringHashingStrategy : Hash.Strategy<String?> {
    override fun hashCode(s: String?): Int {
        return if (s == null) 0 else StringUtilRt.stringHashCodeInsensitive(s)
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

inline fun <T : Any> Ref<T?>.mergeValue(value: T?, mergeAction: (T, T) -> T?): Boolean {
    val oldValue = this.get()
    val newValue = value
    if (newValue == null) {
        return true
    } else if (oldValue == null) {
        this.set(newValue)
        return true
    } else {
        val mergedValue = mergeAction(oldValue, newValue)
        this.set(mergedValue)
        return mergedValue != null
    }
}

inline fun <T> cancelable(block: () -> T): T {
    try {
        return block()
    } catch (e: ExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        throw cause ?: e
    } catch (e: UncheckedExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        throw cause ?: e
    } catch (e: ProcessCanceledException) {
        throw e
    }
}

inline fun <T> cancelable(defaultValueOnException: (Throwable) -> T, block: () -> T): T {
    try {
        return block()
    } catch (e: ExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        return defaultValueOnException(cause ?: e)
    } catch (e: UncheckedExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        return defaultValueOnException(cause ?: e)
    } catch (e: ProcessCanceledException) {
        throw e
    }
}

inline fun <R> runCatchingCancelable(block: () -> R): Result<R> {
    return runCatching(block).onFailure { if (it is ProcessCanceledException) throw it }
}

inline fun <T, R> T.runCatchingCancelable(block: T.() -> R): Result<R> {
    return runCatching(block).onFailure { if (it is ProcessCanceledException) throw it }
}

//removed since: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/100
//inline fun <R> disableLogger(block: () -> R): R {
//    val globalLogger = Logger.getLogger("") //DO NOT use Logger.getGlobalLogger(), it's incorrect
//    val loggerLevel = globalLogger.level
//    try {
//        globalLogger.level = Level.OFF
//        return block()
//    } finally {
//        globalLogger.level = loggerLevel
//    }
//}

fun TextRange.unquote(text: String): TextRange {
    val leftQuoted = text.isLeftQuoted()
    val rightQuoted = text.isRightQuoted()
    val startOffset = if (leftQuoted) this.startOffset + 1 else this.startOffset
    val endOffset = if (rightQuoted) this.endOffset - 1 else this.endOffset
    return TextRange.create(startOffset, endOffset)
}

fun TextRange.replaceAndQuoteIfNecessary(original: String, replacement: String): String {
    if (this.length >= original.length - 1) {
        return replacement.quoteIfNecessary()
    } else {
        var replacement0 = replacement.quoteIfNecessary()
        if (replacement0.isLeftQuoted() && replacement0.isRightQuoted()) {
            replacement0 = replacement0.substring(1, replacement0.length - 1)
        }
        val prefix = original.substring(0, startOffset)
        val suffix = original.substring(endOffset)
        return prefix + replacement0 + suffix
    }
}

fun String.getTextFragments(offset: Int = 0): List<Tuple2<TextRange, String>> {
    val result = mutableListOf<Tuple2<TextRange, String>>()
    var startIndex = 0
    var index = 0
    while (index < this.length) {
        val c = this[index++]
        if (c != '\\') continue
        if (index == this.length) break
        val c1 = this[index++]
        if (c1 != '\\' && c1 != '"') continue
        result += TextRange.create(offset + startIndex, offset + index - 2) to this.substring(startIndex, index - 2)
        startIndex = index - 1
    }
    result += TextRange.create(offset + startIndex, offset + length) to this.substring(startIndex, length)
    return result
}

//com.intellij.refactoring.actions.BaseRefactoringAction.findRefactoringTargetInEditor
fun DataContext.findElement(): PsiElement? {
    var element = this.getData(CommonDataKeys.PSI_ELEMENT)
    if (element == null) {
        val editor = this.getData(CommonDataKeys.EDITOR)
        val file = this.getData(CommonDataKeys.PSI_FILE)
        if (editor != null && file != null) {
            element = getElementAtCaret(editor, file)
        }
        val languages = this.getData(LangDataKeys.CONTEXT_LANGUAGES)
        if (element == null || element is SyntheticElement || languages == null) {
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
    val limit = if (range.endOffset < document.textLength) document.getLineNumber(range.endOffset) else document.lineCount - 1
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

fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

fun <T> createCachedValue(project: Project = getDefaultProject(), trackValue: Boolean = false, provider: CachedValueProvider<T>): CachedValue<T> {
    return CachedValuesManager.getManager(project).createCachedValue(provider, trackValue)
}

fun <T> T.withDependencyItems(vararg dependencyItems: Any): CachedValueProvider.Result<T> {
    if (dependencyItems.isEmpty()) return CachedValueProvider.Result.create(this, ModificationTracker.NEVER_CHANGED)
    return CachedValueProvider.Result.create(this, *dependencyItems)
}

fun <T> Query<T>.processQuery(consumer: Processor<in T>): Boolean {
    return this.forEach(consumer)
}

fun <T> Query<T>.processQueryAsync(consumer: Processor<in T>): Boolean {
    return allowParallelProcessing().forEach(consumer)
}

//endregion

//region Key & DataKey Related Extensions

inline fun <T> UserDataHolder.tryPutUserData(key: Key<T>, value: T?) {
    runCatchingCancelable { putUserData(key, value) }
}

inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T>, action: () -> T): T {
    val data = this.getUserData(key)
    if (data != null) return data
    val newValue = action()
    if (newValue != null) putUserData(key, newValue)
    return newValue
}

inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T>, nullValue: T, action: () -> T?): T? {
    val data = this.getUserData(key)
    if (data != null) return data.takeUnless { it == nullValue }
    val newValue = action()
    if (newValue != null) putUserData(key, newValue) else putUserData(key, nullValue)
    return newValue
}

fun <T, THIS : UserDataHolder> THIS.getUserDataOrDefault(key: Key<T>): T? {
    val value = this.getUserData(key)
    return when {
        value != null -> value
        key is KeyWithDefaultValue -> key.defaultValue.also { putUserData(key, it) }
        key is KeyWithFactory<*, *> -> {
            val key0 = key.cast<KeyWithFactory<T, THIS>>()
            key0.factory(this).also { putUserData(key0, it) }
        }
        else -> null
    }
}

fun <T> ProcessingContext.getOrDefault(key: Key<T>): T? {
    val value = this.get(key)
    return when {
        value != null -> value
        key is KeyWithDefaultValue -> key.defaultValue.also { put(key, it) }
        else -> null
    }
}

inline operator fun <T> Key<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T? = thisRef.getUserDataOrDefault(this)

inline operator fun <T> Key<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T? = thisRef.getOrDefault(this)

inline operator fun <T, THIS : UserDataHolder> KeyWithFactory<T, THIS>.getValue(thisRef: THIS, property: KProperty<*>): T {
    return thisRef.getUserData(this) ?: factory(thisRef).also { thisRef.putUserData(this, it) }
}

inline operator fun <T> KeyWithFactory<T, ProcessingContext>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T {
    return thisRef.get(this) ?: factory(thisRef).also { thisRef.put(this, it) }
}

inline operator fun <T> Key<T>.setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) = thisRef.putUserData(this, value)

inline operator fun <T> Key<T>.setValue(thisRef: ProcessingContext, property: KProperty<*>, value: T?) = thisRef.put(this, value)

inline operator fun <T> DataKey<T>.getValue(thisRef: DataContext, property: KProperty<*>): T? = thisRef.getData(this)

inline operator fun <T> DataKey<T>.getValue(thisRef: AnActionEvent, property: KProperty<*>): T? = thisRef.dataContext.getData(this)

//endregion

//region Code Insight Extensions

fun TemplateBuilder.buildTemplate() = cast<TemplateBuilderImpl>().buildTemplate()

fun TemplateBuilder.buildInlineTemplate() = cast<TemplateBuilderImpl>().buildInlineTemplate()

fun PsiElement.getKeyword(offsetInParent: Int): String {
    return text.substring(0, offsetInParent).unquote()
}

fun PsiElement.getFullKeyword(offsetInParent: Int): String {
    return (text.substring(0, offsetInParent) + text.substring(offsetInParent + PlsConstants.dummyIdentifier.length)).unquote()
}

/**
 * 如果不指定[CompletionType]，IDE的默认实现会让对应的[CompletionProvider]相比指定[CompletionType]的后执行。
 */
fun CompletionContributor.extend(place: ElementPattern<out PsiElement>, provider: CompletionProvider<CompletionParameters>) {
    extend(CompletionType.BASIC, place, provider)
    extend(CompletionType.SMART, place, provider)
}

//endregion

//region Editor & Document Extensions

fun Document.isAtLineStart(offset: Int, skipWhitespace: Boolean = false): Boolean {
    if (!skipWhitespace) return DocumentUtil.isAtLineStart(offset, this)
    val lineStartOffset = DocumentUtil.getLineStartOffset(offset, this)
    val charsSequence = charsSequence
    for (i in offset..lineStartOffset) {
        val c = charsSequence[i]
        if (!c.isWhitespace()) {
            return false
        }
    }
    return true
}

fun Document.isAtLineEnd(offset: Int, skipWhitespace: Boolean = false): Boolean {
    if (!skipWhitespace) return DocumentUtil.isAtLineEnd(offset, this)
    val lineEndOffset = DocumentUtil.getLineEndOffset(offset, this)
    val charsSequence = charsSequence
    for (i in offset..lineEndOffset) {
        if (i >= charsSequence.length) return true
        val c = charsSequence[i]
        if (!c.isWhitespace()) {
            return false
        }
    }
    return true
}

inline fun Document.getCharToLineStart(offset: Int, skipWhitespaceOnly: Boolean = false, predicate: (Char) -> Boolean): Int {
    val lineStartOffset = DocumentUtil.getLineStartOffset(offset, this)
    val charsSequence = charsSequence
    for (i in offset..lineStartOffset) {
        val c = charsSequence[i]
        if (predicate(c)) return i
        if (skipWhitespaceOnly && !c.isWhitespace()) return -1
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
inline fun VirtualFile.toPsiFile(project: Project): PsiFile? {
    return PsiManager.getInstance(project).findFile(this)
}

/** 将VirtualFile转化为指定类型的PsiDirectory。 */
inline fun VirtualFile.toPsiDirectory(project: Project): PsiDirectory? {
    return PsiManager.getInstance(project).findDirectory(this)
}

/** 将VirtualFile转化为指定类型的PsiFile或者PsiDirectory。 */
inline fun VirtualFile.toPsiFileSystemItem(project: Project): PsiFileSystemItem? {
    return if (this.isFile) PsiManager.getInstance(project).findFile(this) else PsiManager.getInstance(project).findDirectory(this)
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
    if (wait) {
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
    if (wait) {
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
    while (child != null) {
        val result = processor(child)
        if (!result) return false
        child = child.treeNext
    }
    return true
}

inline fun ASTNode.forEachChild(action: (ASTNode) -> Unit) {
    var child: ASTNode? = this.firstChildNode
    while (child != null) {
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
    while (child != null) {
        if (child.elementType == type) return child
        child = child.treeNext
    }
    return null
}

fun ASTNode.firstChild(types: TokenSet): ASTNode? {
    var child: ASTNode? = this.firstChildNode
    while (child != null) {
        if (child.elementType in types) return child
        child = child.treeNext
    }
    return null
}

fun ASTNode.firstChild(predicate: (ASTNode) -> Boolean): ASTNode? {
    var child: ASTNode? = this.firstChildNode
    while (child != null) {
        if (predicate(child)) return child
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
    for (i in children.indices) {
        val child = children[i]
        if (predicate(child)) return child
    }
    return null
}

fun LighterASTNode.children(tree: LighterAST): List<LighterASTNode> {
    return tree.getChildren(this)
}

fun LighterASTNode.childrenOfType(tree: LighterAST, type: IElementType): List<LighterASTNode> {
    return LightTreeUtil.getChildrenOfType(tree, this, type)
}

fun LighterASTNode.childrenOfType(tree: LighterAST, types: TokenSet): List<LighterASTNode> {
    return LightTreeUtil.getChildrenOfType(tree, this, types)
}

fun LighterASTNode.internNode(tree: LighterAST): CharSequence? {
    if (this !is LighterASTTokenNode) return null
    return tree.charTable.intern(this.text).toString()
}

//endregion

//region PSI Extensions

/**
 * @param forward 查找偏移之前还是之后的PSI元素，默认为null，表示同时考虑。
 */
fun <T : PsiElement> PsiFile.findElementAt(offset: Int, forward: Boolean? = null, transform: (element: PsiElement) -> T?): T? {
    var element0: PsiElement? = null
    if (forward != false) {
        val element = findElementAt(offset)
        if (element != null) {
            element0 = element
            val result = transform(element)
            if (result != null) {
                return result
            }
        }
    }
    if (forward != true && offset > 0) {
        val leftElement = findElementAt(offset - 1)
        if (leftElement != null && leftElement !== element0) {
            val leftResult = transform(leftElement)
            if (leftResult != null) {
                return leftResult
            }
        }
    }
    return null
}

fun <T : PsiElement> PsiFile.findElementsBetween(startOffset: Int, endOffset: Int, rootTransform: (element: PsiElement) -> PsiElement?, transform: (element: PsiElement) -> T?): List<T> {
    val startRoot = findElementAt(startOffset, true, rootTransform) ?: return emptyList()
    val endRoot = findElementAt(endOffset, true, rootTransform) ?: return emptyList()
    val root = if (startRoot.isAncestor(endRoot)) startRoot else endRoot
    val elements = mutableListOf<T>()
    root.processChild {
        val textRange = it.textRange
        if (textRange.endOffset > startOffset && textRange.startOffset < endOffset) {
            val isLast = textRange.endOffset >= endOffset
            val result = transform(it)
            if (result != null) elements.add(result)
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
    val root = if (startRoot.isAncestor(endRoot)) startRoot else endRoot
    val elements = mutableListOf<PsiElement>()
    root.processChild {
        val textRange = it.textRange
        if (textRange.endOffset > startOffset) {
            elements.add(it)
            true
        } else {
            if (textRange.startOffset < endOffset) {
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
    if (forward != false) {
        val reference = findReferenceAt(offset)
        if (reference != null && predicate(reference)) {
            return reference
        }
    }
    if (forward != true && offset > 0) {
        val reference = findReferenceAt(offset - 1)
        if (reference != null && predicate(reference)) {
            return reference
        }
    }
    return null
}

fun PsiReference.resolveFirst(): PsiElement? {
    return if (this is PsiPolyVariantReference) {
        this.multiResolve(false).firstNotNullOfOrNull { it.element }
    } else {
        this.resolve()
    }
}

fun PsiReference.collectReferences(): Array<out PsiReference> {
    if (this is PsiReferencesAware) {
        val result = mutableListOf<PsiReference>()
        doCollectReferences(this, result)
        if (result.isEmpty()) return PsiReference.EMPTY_ARRAY
        return result.toTypedArray()
    }
    return arrayOf(this)
}

private fun doCollectReferences(sourceReference: PsiReference, result: MutableList<PsiReference>) {
    if (sourceReference is PsiReferencesAware) {
        val references = sourceReference.getReferences()
        if (references.isNotNullOrEmpty()) {
            references.forEach { reference ->
                doCollectReferences(reference, result)
            }
            return
        }
    }
    result.add(sourceReference)
}

/**
 * 判断两个[PsiElement]是否在同一[VirtualFile]的同一位置。
 */
infix fun PsiElement?.isSamePosition(other: PsiElement?): Boolean {
    if (this == other) return true
    if (this == null || other == null) return false
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

inline fun PsiElement.findChild(forward: Boolean = true, predicate: (PsiElement) -> Boolean): PsiElement? {
    return findChildOfType(forward, predicate)
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

inline fun PsiElement.findChildren(forward: Boolean = true, predicate: (PsiElement) -> Boolean): List<PsiElement> {
    return findChildrenOfType(forward, predicate)
}

inline fun PsiElement.processChild(forward: Boolean = true, processor: (PsiElement) -> Boolean): Boolean {
    //不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) this.firstChild else this.lastChild
    while (child != null) {
        val result = processor(child)
        if (!result) return false
        child = if (forward) child.nextSibling else child.prevSibling
    }
    return true
}

inline fun <reified T : PsiElement> PsiElement.indexOfChild(forward: Boolean = true, element: T): Int {
    var child = if (forward) this.firstChild else this.lastChild
    var index = 0
    while (child != null) {
        when (child) {
            element -> return index
            is T -> index++
            else -> child = if (forward) child.nextSibling else child.prevSibling
        }
    }
    return -1
}

inline fun PsiElement.forEachChild(forward: Boolean = true, action: (PsiElement) -> Unit) {
    //不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) this.firstChild else this.lastChild
    while (child != null) {
        action(child)
        child = if (forward) child.nextSibling else child.prevSibling
    }
}

inline fun <reified T : PsiElement> PsiElement.processChildrenOfType(forward: Boolean = true, processor: (T) -> Boolean): Boolean {
    //不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) this.firstChild else this.lastChild
    while (child != null) {
        if (child is T) {
            val result = processor(child)
            if (!result) return false
        }
        child = if (forward) child.nextSibling else child.prevSibling
    }
    return true
}

inline fun <reified T : PsiElement> PsiElement.findChildOfType(forward: Boolean = true, predicate: (T) -> Boolean = { true }): T? {
    //不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) this.firstChild else this.lastChild
    while (child != null) {
        if (child is T && predicate(child)) return child
        child = if (forward) child.nextSibling else child.prevSibling
    }
    return null
}

inline fun <reified T> PsiElement.findChildrenOfType(forward: Boolean = true, predicate: (T) -> Boolean = { true }): List<T> {
    //不会忽略某些特定类型的子元素
    var result: MutableList<T>? = null
    var child: PsiElement? = if (forward) this.firstChild else this.lastChild
    while (child != null) {
        if (child is T && predicate(child)) {
            if (result == null) result = mutableListOf()
            result.add(child)
        }
        child = if (forward) child.nextSibling else child.prevSibling
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
    return try {
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
    } catch (e: IllegalArgumentException) {
        //Element from alien project - use empty pointer
        emptyPointer()
    }
}

fun <E : PsiElement> E.createPointer(file: PsiFile?, project: Project = this.project): SmartPsiElementPointer<E> {
    return try {
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this, file)
    } catch (e: IllegalArgumentException) {
        //Element from alien project - use empty pointer
        emptyPointer()
    }
}

fun PsiElement.isIncomplete(): Boolean {
    val file = containingFile
    val originalFile = file.originalFile
    if (originalFile === file) return false
    val startOffset = startOffset
    file.findElementAt(startOffset)
    val e1 = file.findElementAt(startOffset) ?: return false
    val e2 = originalFile.findElementAt(startOffset) ?: return true
    if (e1.elementType != e2.elementType) return true
    if (e1.textLength != e2.textLength) return true
    return false
}

fun PsiElement.isSpaceOrSingleLineBreak(): Boolean {
    return this is PsiWhiteSpace && StringUtil.getLineBreakCount(this.text) <= 1
}

fun PsiElement.isSingleLineBreak(): Boolean {
    return this is PsiWhiteSpace && StringUtil.getLineBreakCount(this.text) == 1
}

inline fun findAcceptableElementIncludeComment(element: PsiElement?, predicate: (PsiElement) -> Boolean): Any? {
    var current: PsiElement? = element ?: return null
    while (current != null && current !is PsiFile) {
        if (predicate(current)) return current
        if (current is PsiComment) return current.siblings().find { predicate(it) }
            ?.takeIf { it.prevSibling.isSpaceOrSingleLineBreak() }
        current = current.parent
    }
    return null
}

inline fun findTextStartOffsetIncludeComment(element: PsiElement, findUpPredicate: (PsiElement) -> Boolean): Int {
    //找到直到没有空行为止的最后一个注释，返回它的开始位移，或者输入元素的开始位移
    val target: PsiElement = if (element.prevSibling == null && findUpPredicate(element)) element.parent else element
    var current: PsiElement? = target
    var comment: PsiComment? = null
    while (current != null) {
        current = current.prevSibling ?: break
        when {
            current is PsiWhiteSpace && current.isSpaceOrSingleLineBreak() -> continue
            current is PsiComment -> comment = current
            else -> break
        }
    }
    if (comment != null) return comment.startOffset
    return target.startOffset
}

fun getLineCommentDocText(element: PsiElement): String? {
    //认为当前元素之前，之间没有空行的非行尾行注释，可以视为文档注释的一部分
    var lines: LinkedList<String>? = null
    var prevElement = element.prevSibling ?: element.parent?.prevSibling
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

fun getDocumentation(documentationLines: List<String>?, html: Boolean): String? {
    if (documentationLines.isNullOrEmpty()) return null
    return buildString {
        var isLineBreak = false
        for (line in documentationLines) {
            if (!isLineBreak) {
                isLineBreak = true
            } else {
                append("<br>")
            }
            if (line.endsWith('\\')) {
                isLineBreak = false
            }
            val l = line.trimEnd('\\')
            if (html) append(l) else append(l.escapeXml())
        }
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
    if (file is VirtualFileWindow) return doFindTopHostFileOrThis(file.delegate)
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
    if (host == null) return element
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

val ScopeToolState.enabledTool: InspectionProfileEntry? get() = if (isEnabled) tool.tool else null

//endregion
