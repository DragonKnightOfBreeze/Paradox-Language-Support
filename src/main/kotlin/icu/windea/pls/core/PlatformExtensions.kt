@file:Suppress("unused", "UnstableApiUsage")

package icu.windea.pls.core

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.codeInsight.template.TemplateBuilder
import com.intellij.codeInsight.template.TemplateBuilderImpl
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.ex.ScopeToolState
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.lang.ASTNode
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.tree.util.siblings
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.Segment
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.ex.WindowManagerEx
import com.intellij.platform.backend.presentation.TargetPresentationBuilder
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import com.intellij.util.Processor
import com.intellij.util.Query
import com.intellij.util.ThrowableRunnable
import com.intellij.util.application
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.psi.PsiFileService
import icu.windea.pls.core.psi.PsiReferencesAware
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to
import org.jetbrains.concurrency.CancellablePromise
import org.jetbrains.concurrency.resolvedCancellablePromise
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import kotlin.reflect.KProperty

// region Common Extensions

/** 忽略大小写的字符串比较。 */
fun String.compareToIgnoreCase(other: String): Int {
    return String.CASE_INSENSITIVE_ORDER.compare(this, other)
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

/**
 * 执行 [block]，在捕获 [ProcessCanceledException] 时原样抛出，其余异常若根因是 PCE 也原样抛出。
 *
 * 用于将“可取消”的异常语义透传到调用方，而不被统一异常处理吞掉。
 */
inline fun <T> cancelable(block: () -> T): T {
    try {
        return block()
    } catch (e: ProcessCanceledException) {
        throw e
    } catch (e: Exception) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        throw e
    }
}

/**
 * 类似 [runCatching]，但在捕获到 [ProcessCanceledException] 时直接抛出，不包装为失败结果。
 */
inline fun <R> runCatchingCancelable(block: () -> R): Result<R> {
    return runCatching(block).onFailure { if (it is ProcessCanceledException) throw it }
}

/**
 * 类似扩展接收者版本的 [runCatching]，但对 [ProcessCanceledException] 直接抛出。
 */
inline fun <T, R> T.runCatchingCancelable(block: T.() -> R): Result<R> {
    return runCatching(block).onFailure { if (it is ProcessCanceledException) throw it }
}

fun <T> createCachedValue(project: Project, trackValue: Boolean = false, provider: CachedValueProvider<T>): CachedValue<T> {
    return CachedValuesManager.getManager(project).createCachedValue(provider, trackValue)
}

fun <T> T.withDependencyItems(vararg dependencies: Any): CachedValueProvider.Result<T> {
    return CachedValueProvider.Result.create(this, *dependencies)
}

fun <T> T.withDependencyItems(dependencies: List<Any>): CachedValueProvider.Result<T> {
    return CachedValueProvider.Result.create(this, dependencies)
}

fun <T> Query<T>.process(consumer: Processor<in T>): Boolean {
    return forEach(consumer)
}

fun <T> Query<T>.processAsync(consumer: Processor<in T>): Boolean {
    return allowParallelProcessing().forEach(consumer)
}

/**
 * 得到默认项目。
 */
fun getDefaultProject(): Project {
    return ProjectManager.getInstance().defaultProject
}

/**
 * 得到当前项目（当前聚焦窗口对应的项目，或者第一个有效的已打开的项目）。
 */
fun getCurrentProject(): Project? {
    val recentFocusedWindow = WindowManagerEx.getInstanceEx().mostRecentFocusedWindow
    if (recentFocusedWindow is IdeFrame) return recentFocusedWindow.project
    return ProjectManager.getInstance().openProjects.firstOrNull { o -> o.isInitialized && !o.isDisposed }
}

// endregion

// region Text Related Extensions

operator fun Segment.contains(other: Segment): Boolean {
    return startOffset <= other.startOffset && endOffset >= other.endOffset
}

/**
 * 去除文本范围首尾的引号。返回处理后的新的文本范围。
 */
fun TextRange.unquote(text: String, quote: Char = '"'): TextRange {
    val leftQuoted = text.isLeftQuoted(quote)
    val rightQuoted = text.isRightQuoted(quote)
    val startOffset = if (leftQuoted) this.startOffset + 1 else this.startOffset
    val endOffset = if (rightQuoted) this.endOffset - 1 else this.endOffset
    return TextRange.create(startOffset, endOffset)
}

/**
 * 将 [original] 中 [this] 范围替换为 [replacement]，必要时自动加引号。
 *
 * 当替换段长度与整体长度关系满足条件时，避免重复包裹引号并保留必要的转义。
 */
fun TextRange.replaceAndQuoteIfNecessary(original: String, replacement: String, quote: Char = '"', extraChars: String = "", blank: Boolean = true): String {
    if (this.length >= original.length - 1) {
        return replacement.quoteIfNecessary(quote, extraChars, blank)
    } else {
        var replacement0 = replacement.quoteIfNecessary(quote, extraChars, blank)
        if (replacement0.isLeftQuoted(quote) && replacement0.isRightQuoted(quote)) {
            replacement0 = replacement0.substring(1, replacement0.length - 1)
        }
        val prefix = original.substring(0, startOffset)
        val suffix = original.substring(endOffset)
        return prefix + replacement0 + suffix
    }
}

/**
 * 在输入的文本中查找关键字的出现位置，返回关键字与文本范围组成的元组的列表，按起始位置排序。
 */
fun String.findKeywordsWithTextRanges(keywords: Collection<String>): List<Tuple2<String, TextRange>> {
    val sortedKeywords = keywords.filter { it.isNotEmpty() }.sortedByDescending { it.length }.toSet()
    val result = mutableListOf<Tuple2<String, TextRange>>()
    var startIndex = 0
    while (startIndex < this.length) {
        val (keyword, index) = sortedKeywords.map { it to indexOf(it, startIndex) }.filter { it.second != -1 }.minByOrNull { it.second } ?: break
        result += tupleOf(keyword, TextRange.from(index, keyword.length))
        startIndex = index + keyword.length
    }
    return result.sortedBy { it.second.startOffset }
}

/**
 * 合并一组文本范围，返回其并集后的最小区间集，按起始位置排序。
 *
 * 语义说明：
 * - 合并所有**重叠**或**相邻**的区间。
 * - 不相交的区间保持分离；结果区间为半开区间 `[startOffset, endOffset)`。
 * - 时间复杂度为 O(n log n)（按起始位置排序后线性合并）。
 */
fun Iterable<TextRange>.mergeTextRanges(): List<TextRange> {
    val ranges = this.toList()
    if (ranges.size <= 1) return ranges
    val sorted = ranges.sortedBy { it.startOffset }
    val result = ArrayList<TextRange>(sorted.size)
    for (range in sorted) {
        if (result.isEmpty()) {
            result.add(range)
            continue
        }
        val last = result.last()
        // 若重叠或相邻，则合并；否则开启新区间
        if (range.startOffset <= last.endOffset) {
            val mergedStart = last.startOffset
            val mergedEnd = maxOf(last.endOffset, range.endOffset)
            // 替换为合并后的新区间
            result[result.lastIndex] = TextRange.create(mergedStart, mergedEnd)
        } else {
            result.add(range)
        }
    }
    return result
}

// endregion

// region Event Extensions

val AnActionEvent.editor: Editor? get() = getData(CommonDataKeys.EDITOR)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> DataKey<T>.getValue(thisRef: DataContext, property: KProperty<*>): T? = thisRef.getData(this)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> DataKey<T>.getValue(thisRef: AnActionEvent, property: KProperty<*>): T? = thisRef.getData(this)

// endregion

// region VFS Extensions

/** 将文件路径转换为 VirtualFile（可选刷新 VFS）。 */
fun String.toVirtualFile(refreshIfNeed: Boolean = false): VirtualFile? {
    val path = this.toPathOrNull() ?: return null
    return VfsUtil.findFile(path, refreshIfNeed)
}

/** 将 Path 转换为 VirtualFile（可选刷新 VFS）。 */
fun Path.toVirtualFile(refreshIfNeed: Boolean = false): VirtualFile? {
    val path = this
    return VfsUtil.findFile(path, refreshIfNeed)
}

/** 将 VirtualFile 转为 PsiFile。 */
fun VirtualFile.toPsiFile(project: Project): PsiFile? {
    if (project.isDisposed) return null
    return PsiManager.getInstance(project).findFile(this)
}

/** 将 VirtualFile 转为 PsiDirectory。 */
fun VirtualFile.toPsiDirectory(project: Project): PsiDirectory? {
    if (project.isDisposed) return null
    return PsiManager.getInstance(project).findDirectory(this)
}

/** 将 VirtualFile 转为 PsiFile 或 PsiDirectory。 */
fun VirtualFile.toPsiFileSystemItem(project: Project): PsiFileSystemItem? {
    if (project.isDisposed) return null
    return if (this.isFile) toPsiFile(project) else toPsiDirectory(project)
}

// endregion

// region AST Extensions

/** 遍历当前节点的直接子节点。 */
inline fun ASTNode.forEachChild(forward: Boolean = true, action: (ASTNode) -> Unit) {
    var child: ASTNode? = if (forward) this.firstChildNode else this.lastChildNode
    while (child != null) {
        action(child)
        child = child.treeNext
    }
}

/** 处理当前节点的直接子节点；当处理器返回 false 时提前终止。 */
inline fun ASTNode.processChild(forward: Boolean = true, processor: (ASTNode) -> Boolean): Boolean {
    var child: ASTNode? = if (forward) this.firstChildNode else this.lastChildNode
    while (child != null) {
        val result = processor(child)
        if (!result) return false
        child = child.treeNext
    }
    return true
}

/** 获取当前节点的子节点序列。 */
fun ASTNode.children(forward: Boolean = true): Sequence<ASTNode> {
    val child = if (forward) this.firstChildNode else this.lastChildNode
    if (child == null) return emptySequence()
    return child.siblings(forward, withSelf = true)
}

// fun ASTNode.isStartOfLine(): Boolean {
//    return treePrev?.let { it.elementType == TokenType.WHITE_SPACE && it.text.containsLineBreak() } ?: false
// }
//
// fun ASTNode.isEndOfLine(): Boolean {
//    return treeNext?.let { it.elementType == TokenType.WHITE_SPACE && it.text.containsLineBreak() } ?: false
// }

/** 在轻量 AST 中查找首个指定类型的子节点。 */
fun LighterASTNode.firstChild(tree: LighterAST, type: IElementType): LighterASTNode? {
    return LightTreeUtil.firstChildOfType(tree, this, type)
}

/** 在轻量 AST 中查找首个匹配类型集合的子节点。 */
fun LighterASTNode.firstChild(tree: LighterAST, types: TokenSet): LighterASTNode? {
    return LightTreeUtil.firstChildOfType(tree, this, types)
}

/** 在轻量 AST 中按谓词查找首个子节点。 */
inline fun LighterASTNode.firstChild(tree: LighterAST, predicate: (LighterASTNode) -> Boolean): LighterASTNode? {
    val children = tree.getChildren(this)
    for (i in children.indices) {
        val child = children[i]
        if (predicate(child)) return child
    }
    return null
}

/** 获取轻量 AST 的所有直接子节点。 */
fun LighterASTNode.children(tree: LighterAST): List<LighterASTNode> {
    return tree.getChildren(this)
}

/** 获取轻量 AST 中指定类型的直接子节点列表。 */
fun LighterASTNode.childrenOfType(tree: LighterAST, type: IElementType): List<LighterASTNode> {
    return LightTreeUtil.getChildrenOfType(tree, this, type)
}

/** 获取轻量 AST 中匹配类型集合的直接子节点列表。 */
fun LighterASTNode.childrenOfType(tree: LighterAST, types: TokenSet): List<LighterASTNode> {
    return LightTreeUtil.getChildrenOfType(tree, this, types)
}

/** 获取轻量 AST 的父节点。 */
fun LighterASTNode.parent(tree: LighterAST): LighterASTNode? {
    return tree.getParent(this)
}

/** 将轻量 AST Token 节点文本驻留并返回。 */
fun LighterASTNode.internNode(tree: LighterAST): CharSequence? {
    if (this !is LighterASTTokenNode) return null
    return tree.charTable.intern(this.text).toString()
}

// endregion

// region PSI Extensions

/** @see PsiFileService.findElementAt */
fun <T : PsiElement> PsiFile.findElementAt(offset: Int, forward: Boolean? = null, transform: (element: PsiElement) -> T?): T? {
    return PsiFileService.findElementAt(this, offset, forward, transform)
}

/** @see PsiFileService.findElementsBetween */
fun <T : PsiElement> PsiFile.findElementsBetween(startOffset: Int, endOffset: Int, rootTransform: (rootElement: PsiElement) -> PsiElement?): Sequence<PsiElement> {
    return PsiFileService.findElementsBetween(this, startOffset, endOffset, rootTransform)
}

/** @see PsiFileService.findReferenceAt */
fun PsiFile.findReferenceAt(offset: Int, forward: Boolean? = null, predicate: (reference: PsiReference) -> Boolean): PsiReference? {
    return PsiFileService.findReferenceAt(this, offset, forward, predicate)
}

/** 若为多解析引用，返回首个解析目标；否则调用 `resolve()`。 */
fun PsiReference.resolveFirst(): PsiElement? {
    return if (this is PsiPolyVariantReference) {
        this.multiResolve(false).firstNotNullOfOrNull { it.element }
    } else {
        this.resolve()
    }
}

/** 收集该引用及其子引用（若实现了 [PsiReferencesAware]）。 */
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
        if (references.isNotNullOrEmpty()) { // 为空数组 / 为 `null` 在这里是等价的
            references.forEach { reference ->
                ProgressManager.checkCanceled()
                doCollectReferences(reference, result)
            }
            return
        }
    }
    result.add(sourceReference)
}

/**
 * 判断两个 [PsiElement] 是否在同一 [VirtualFile] 的同一位置。
 */
infix fun PsiElement?.isSamePosition(other: PsiElement?): Boolean {
    if (this == other) return true
    if (this == null || other == null) return false
    return startOffset == other.startOffset && containingFile.originalFile.virtualFile == other.containingFile.originalFile.virtualFile
}

/** 获取当前 PSI 的子元素序列。 */
fun PsiElement.children(forward: Boolean = true): Sequence<PsiElement> {
    val child = if (forward) this.firstChild else this.lastChild
    if (child == null) return emptySequence()
    return child.siblings(forward, withSelf = true)
}

@JvmName("findChildByType")
inline fun <reified T : PsiElement> PsiElement.findChild(forward: Boolean = true, predicate: (T) -> Boolean = { true }): T? {
    return children(forward).findIsInstance<T>(predicate)
}

inline fun PsiElement.findChild(forward: Boolean = true, predicate: (PsiElement) -> Boolean = { true }): PsiElement? {
    return children(forward).findIsInstance(predicate)
}

@JvmName("findChildrenByType")
inline fun <reified T : PsiElement> PsiElement.findChildren(forward: Boolean = true, crossinline predicate: (T) -> Boolean = { true }): List<T> {
    return children(forward).filterIsInstance<T>(predicate).toList()
}

@Suppress("NOTHING_TO_INLINE")
inline fun PsiElement.findChildren(forward: Boolean = true, noinline predicate: (PsiElement) -> Boolean = { true }): List<PsiElement> {
    return children(forward).filter(predicate).toList()
}

/** 遍历当前 PSI 的直接子元素。 */
inline fun PsiElement.forEachChild(forward: Boolean = true, action: (PsiElement) -> Unit) {
    // 不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) firstChild else lastChild
    while (child != null) {
        action(child)
        child = if (forward) child.nextSibling else child.prevSibling
    }
}

/** 处理当前 PSI 的直接子元素。处理器返回 `false` 时提前终止。 */
inline fun PsiElement.processChild(forward: Boolean = true, processor: (PsiElement) -> Boolean): Boolean {
    // 不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) firstChild else lastChild
    while (child != null) {
        val result = processor(child)
        if (!result) return false
        child = if (forward) child.nextSibling else child.prevSibling
    }
    return true
}

// /** 获取指定子元素在同类元素中的索引。如果不存在则返回-1。 */
// inline fun <reified T : PsiElement> PsiElement.indexOfChild(forward: Boolean = true, element: T): Int {
//     var child = if (forward) firstChild else lastChild
//     var index = 0
//     while (child != null) {
//         when (child) {
//             element -> return index
//             is T -> index++
//             else -> child = if (forward) child.nextSibling else child.prevSibling
//         }
//     }
//     return -1
// }

/** 遍历当前 PSI 的所有父元素，直到 PSI 文件为止。 */
inline fun PsiElement.forEachParent(withSelf: Boolean = false, action: (PsiElement) -> Unit) {
    var current = when {
        withSelf -> this
        this is PsiFile -> return
        else -> parent
    }
    while (current != null) {
        action(current)
        if (current is PsiFile) break
        current = current.parent
    }
}

/** 处理当前 PSI 的所有父元素，直到 PSI 文件为止。处理器返回 `false` 时提前终止。 */
inline fun PsiElement.processParent(withSelf: Boolean = false, processor: (PsiElement) -> Boolean): Boolean {
    var current = when {
        withSelf -> this
        this is PsiFile -> return true
        else -> parent
    }
    while (current != null) {
        val result = processor(current)
        if (!result) return false
        if (current is PsiFile) break
        current = current.parent
    }
    return true
}

inline val PsiElement.icon get() = getIcon(0)

/** PSI 元素的空指针。 */
object EmptyPointer : SmartPsiElementPointer<PsiElement> {
    override fun getElement() = null

    override fun getContainingFile() = null

    override fun getProject() = ProjectManager.getInstance().defaultProject

    override fun getVirtualFile() = null

    override fun getRange() = null

    override fun getPsiRange() = null
}

/** 得到空指针（用于跨项目/失效元素的占位）。 */
fun <T : PsiElement> emptyPointer(): SmartPsiElementPointer<T> = EmptyPointer.cast()

/** 判断当前智能指针是否是空指针。 */
fun SmartPsiElementPointer<*>.isEmpty() = this === EmptyPointer

/** 为当前 PSI 元素创建智能指针，失败时返回空指针。 */
fun <E : PsiElement> E.createPointer(project: Project = this.project): SmartPsiElementPointer<E> {
    return try {
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
    } catch (e: IllegalArgumentException) {
        // Element from alien project - use empty pointer
        emptyPointer()
    }
}

/** 为当前 PSI 元素创建基于文件的智能指针，失败时返回空指针。 */
fun <E : PsiElement> E.createPointer(file: PsiFile?, project: Project = file?.project ?: this.project): SmartPsiElementPointer<E> {
    return try {
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this, file)
    } catch (e: IllegalArgumentException) {
        // Element from alien project - use empty pointer
        emptyPointer()
    }
}

fun PsiElement.isIncomplete(): Boolean {
    val file = containingFile
    val originalFile = file.originalFile
    if (originalFile === file) return false
    val startOffset = startOffset
    val e1 = file.findElementAt(startOffset) ?: return false
    val e2 = originalFile.findElementAt(startOffset) ?: return true
    if (e1.elementType != e2.elementType) return true
    if (e1.textLength != e2.textLength) return true
    return false
}

context(reference: PsiReference)
fun PsiElement?.createResults(): Array<out ResolveResult> {
    if (this == null) return ResolveResult.EMPTY_ARRAY
    return arrayOf(PsiElementResolveResult(this))
}

context(reference: PsiReference)
fun Collection<PsiElement>.createResults(): Array<out ResolveResult> {
    return PsiElementResolveResult.createResults(this)
}

fun PsiBuilder.lookupWithOffset(steps: Int, skipWhitespaces: Boolean = true, forward: Boolean = true): Tuple2<IElementType?, Int> {
    var offset = steps
    var token = rawLookup(offset)
    if (skipWhitespaces) {
        while (token == TokenType.WHITE_SPACE) {
            if (forward) offset++ else offset--
            token = rawLookup(offset)
        }
    }
    return token to offset
}

// endregion

// region Symbol Extensions

/** 将 PSI 元素包装为 [Symbol]。 */
fun PsiElement.asSymbol(): Symbol = PsiSymbolService.getInstance().asSymbol(this)

// /** 将 PSI 引用包装为 [PsiSymbolReference]。 */
// fun PsiReference.asSymbolReference(): PsiSymbolReference = PsiSymbolService.getInstance().asSymbolReference(this)
//
// /** 从 [Symbol] 中提取底层 PSI 元素。 */
// fun Symbol.extractElement(): PsiElement? = PsiSymbolService.getInstance().extractElementFromSymbol(this)

// endregion

// region Presentation Extensions

/** 在展示信息中附带文件名与图标。 */
fun TargetPresentationBuilder.withLocationIn(file: PsiFile): TargetPresentationBuilder {
    val virtualFile = file.containingFile.virtualFile ?: return this
    val fileType = virtualFile.fileType
    return locationText(virtualFile.name, fileType.icon)
}

// endregion

// region Code Insight Extensions

typealias ReadWriteAccess = ReadWriteAccessDetector.Access

/**
 * 获取包含当前位置（[offsetInParent]）之前的文本的关键字。用于代码补全。
 */
fun PsiElement.getKeyword(offsetInParent: Int): String {
    return text.substring(0, offsetInParent).unquote()
}

/**
 * 获取包含当前位置（[offsetInParent]）之前与之后的文本的完整关键字。用于代码补全。
 */
fun PsiElement.getFullKeyword(offsetInParent: Int, dummyIdentifier: String): String {
    return (text.substring(0, offsetInParent) + text.substring(offsetInParent + dummyIdentifier.length)).unquote()
}

/** 构建模板（等价于强转为 [TemplateBuilderImpl] 后调用）。 */
fun TemplateBuilder.buildTemplate() = cast<TemplateBuilderImpl>().buildTemplate()

/** 构建行内模板。 */
fun TemplateBuilder.buildInlineTemplate() = cast<TemplateBuilderImpl>().buildInlineTemplate()

// endregion

// region Inspection Extensions

/** 根据检查项短名获取对应的 [ScopeToolState]。 */
fun getInspectionToolState(shortName: String, element: PsiElement?, project: Project): ScopeToolState? {
    val currentProfile = InspectionProfileManager.getInstance(project).currentProfile
    val tools = currentProfile.getToolsOrNull(shortName, project) ?: return null
    return tools.getState(element)
}

/** 若检查项启用则返回实际的 [InspectionProfileEntry]，否则返回 null。 */
val ScopeToolState.enabledTool: InspectionProfileEntry? get() = if (isEnabled) tool.tool else null

// endregion

// region Settings Extensions

// 以下委托方法用于读写需要保存为密码的配置项

@Suppress("NOTHING_TO_INLINE")
inline operator fun CredentialAttributes.getValue(thisRef: Any?, property: KProperty<*>): String? {
    return PasswordSafe.instance.getPassword(this)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun CredentialAttributes.setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
    PasswordSafe.instance.setPassword(this, value)
}

// endregion

// region RWA Extensions

fun <T> runSmartReadAction(
    parentDisposable: Disposable? = null,
    task: Callable<T>,
): T {
    if (application.isReadAccessAllowed) {
        return task.call()
    }

    var action = ReadAction.nonBlocking(task)
    if (parentDisposable != null) action = action.expireWith(parentDisposable)
    return action.executeSynchronously()
}

fun <T> runSmartReadActionAsync(
    executor: Executor,
    parentDisposable: Disposable? = null,
    task: Callable<T>,
): CancellablePromise<T> {
    if (application.isReadAccessAllowed) {
        return resolvedCancellablePromise(task.call())
    }

    var action = ReadAction.nonBlocking(task)
    if (parentDisposable != null) action = action.expireWith(parentDisposable)
    return action.submit(executor)
}

fun <T> runSmartReadAction(
    project: Project,
    parentDisposable: Disposable? = null,
    inSmartMode: Boolean = false,
    withDocumentsCommitted: Boolean = false,
    task: Callable<T>,
): T {
    if (application.isReadAccessAllowed && (!inSmartMode || !DumbService.isDumb(project)) && !withDocumentsCommitted) {
        return task.call()
    }

    var action = ReadAction.nonBlocking(task)
    if (parentDisposable != null) action = action.expireWith(parentDisposable)
    if (inSmartMode) action = action.inSmartMode(project)
    if (withDocumentsCommitted) action = action.withDocumentsCommitted(project)
    return action.executeSynchronously()
}

fun <T> runSmartReadActionAsync(
    executor: Executor,
    project: Project,
    parentDisposable: Disposable? = null,
    inSmartMode: Boolean = true,
    withDocumentsCommitted: Boolean = false,
    task: Callable<T>,
): CancellablePromise<T> {
    if (application.isReadAccessAllowed && (!inSmartMode || !DumbService.isDumb(project)) && !withDocumentsCommitted) {
        return resolvedCancellablePromise(task.call())
    }

    var action = ReadAction.nonBlocking(task)
    if (parentDisposable != null) action = action.expireWith(parentDisposable)
    if (inSmartMode) action = action.inSmartMode(project)
    if (withDocumentsCommitted) action = action.withDocumentsCommitted(project)
    return action.submit(executor)
}

// endregion

// region Command Extensions

fun executeCommand(
    project: Project? = null,
    @NlsContexts.Command name: String? = null,
    groupId: String? = null,
    action: Runnable,
) {
    CommandProcessor.getInstance().executeCommand(project, action, name, groupId)
}

fun executeWriteCommand(
    project: Project? = null,
    @NlsContexts.Command name: String? = null,
    groupId: String? = null,
    action: ThrowableRunnable<Throwable>
) {
    WriteCommandAction.writeCommandAction(project)
        .withName(name).withGroupId(groupId)
        .run(action)
}

fun executeWriteCommand(
    project: Project? = null,
    @NlsContexts.Command name: String? = null,
    groupId: String? = null,
    makeWritable: PsiElement? = null,
    action: ThrowableRunnable<Throwable>,
) {
    WriteCommandAction.writeCommandAction(project, makeWritable.to.singletonSetOrEmpty())
        .withName(name).withGroupId(groupId)
        .run(action)
}

fun executeWriteCommand(
    project: Project? = null,
    @NlsContexts.Command name: String? = null,
    groupId: String? = null,
    makeWritable: Collection<PsiElement> = emptyList(),
    action: ThrowableRunnable<Throwable>,
) {
    WriteCommandAction.writeCommandAction(project, makeWritable)
        .withName(name).withGroupId(groupId)
        .run(action)
}

// endregion
