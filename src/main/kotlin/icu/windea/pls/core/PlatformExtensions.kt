@file:Suppress("unused", "NOTHING_TO_INLINE", "UnstableApiUsage")

package icu.windea.pls.core

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
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.Ref
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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
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
import com.intellij.psi.util.isAncestor
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import com.intellij.util.ArrayUtil
import com.intellij.util.Processor
import com.intellij.util.Query
import com.intellij.util.application
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.psi.PsiReferencesAware
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.tupleOf
import java.io.IOException
import java.nio.file.Path
import kotlin.reflect.KProperty

// region Common Extensions

/** 忽略大小写的字符串比较。*/
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

fun <T> T.withDependencyItems(vararg dependencyItems: Any): CachedValueProvider.Result<T> {
    if (dependencyItems.isEmpty()) return CachedValueProvider.Result.create(this, ModificationTracker.NEVER_CHANGED)
    return CachedValueProvider.Result.create(this, *dependencyItems)
}

fun <T> T.withDependencyItems(dependencyItems: List<Any>): CachedValueProvider.Result<T> {
    if (dependencyItems.isEmpty()) return CachedValueProvider.Result.create(this, ModificationTracker.NEVER_CHANGED)
    return CachedValueProvider.Result.create(this, dependencyItems)
}

fun <T> Query<T>.processQuery(consumer: Processor<in T>): Boolean {
    return this.forEach(consumer)
}

fun <T> Query<T>.processQueryAsync(consumer: Processor<in T>): Boolean {
    return allowParallelProcessing().forEach(consumer)
}

inline operator fun <T> DataKey<T>.getValue(thisRef: DataContext, property: KProperty<*>): T? = thisRef.getData(this)

inline operator fun <T> DataKey<T>.getValue(thisRef: AnActionEvent, property: KProperty<*>): T? = thisRef.dataContext.getData(this)

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

fun <T> runReadActionSmartly(runnable: () -> T): T {
    return if (application.isReadAccessAllowed) {
        runnable()
    } else {
        runReadAction(runnable)
    }
}

// endregion

// region Text Related Extensions

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

// region VFS Extensions

// /**查找当前项目中指定语言文件类型和作用域的VirtualFile。*/
// fun findVirtualFiles(project: Project, type: LanguageFileType): Collection<VirtualFile> {
//	return FileTypeIndex.getFiles(type, GlobalSearchScope.projectScope(project))
// }

// /**查找当前项目中指定语言文件类型和作用域的PsiFile。*/
// inline fun <reified T : PsiFile> findFiles(project: Project, type: LanguageFileType): List<T> {
//	return FileTypeIndex.getFiles(type, GlobalSearchScope.projectScope(project)).mapNotNull {
//		PsiManager.getInstance(project).findFile(it)
//	}.filterIsInstance<T>()
// }

// /**递归得到当前VirtualFile的所有作为子节点的VirtualFile。*/
// fun VirtualFile.getAllChildFiles(destination: MutableList<VirtualFile> = mutableListOf()): List<VirtualFile> {
//	for(child in this.children) {
//		if(child.isDirectory) child.getAllChildFiles(destination) else destination.add(child)
//	}
//	return destination
// }

/** 将文件路径转换为 VirtualFile（可选刷新 VFS）。*/
fun String.toVirtualFile(refreshIfNeed: Boolean = false): VirtualFile? {
    val path = this.toPathOrNull() ?: return null
    return VfsUtil.findFile(path, refreshIfNeed)
}

/** 将 Path 转换为 VirtualFile（可选刷新 VFS）。*/
fun Path.toVirtualFile(refreshIfNeed: Boolean = false): VirtualFile? {
    val path = this
    return VfsUtil.findFile(path, refreshIfNeed)
}

/** 将 VirtualFile 转为 PsiFile。*/
inline fun VirtualFile.toPsiFile(project: Project): PsiFile? {
    if (project.isDisposed) return null
    return PsiManager.getInstance(project).findFile(this)
}

/** 将 VirtualFile 转为 PsiDirectory。*/
inline fun VirtualFile.toPsiDirectory(project: Project): PsiDirectory? {
    if (project.isDisposed) return null
    return PsiManager.getInstance(project).findDirectory(this)
}

/** 将 VirtualFile 转为 PsiFile 或 PsiDirectory。*/
inline fun VirtualFile.toPsiFileSystemItem(project: Project): PsiFileSystemItem? {
    if (project.isDisposed) return null
    return if (this.isFile) toPsiFile(project) else toPsiDirectory(project)
}

/** 判断（物理层面）是否包含指定 BOM。*/
fun VirtualFile.hasBom(bom: ByteArray): Boolean {
    return this.bom.let { it != null && it contentEquals bom }
}

/** 添加 BOM 到虚拟文件（物理层面）。*/
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

/** 从虚拟文件移除 BOM（物理层面）。*/
@Throws(IOException::class)
fun VirtualFile.removeBom(bom: ByteArray, wait: Boolean = true) {
    this.bom = null
    val bytes = this.contentsToByteArray()
    val contentWithStrippedBom = bytes.copyOfRange(bom.size, bytes.size)
    if (wait) {
        WriteAction.runAndWait<IOException> { this.setBinaryContent(contentWithStrippedBom) }
    } else {
        WriteAction.run<IOException> { this.setBinaryContent(contentWithStrippedBom) }
    }
}

// endregion

// region AST Extensions

/** 遍历当前节点的直接子节点。*/
inline fun ASTNode.forEachChild(forward: Boolean = true, action: (ASTNode) -> Unit) {
    var child: ASTNode? = if (forward) this.firstChildNode else this.lastChildNode
    while (child != null) {
        action(child)
        child = child.treeNext
    }
}

/** 处理当前节点的直接子节点；当处理器返回 false 时提前终止。*/
inline fun ASTNode.processChild(forward: Boolean = true, processor: (ASTNode) -> Boolean): Boolean {
    var child: ASTNode? = if (forward) this.firstChildNode else this.lastChildNode
    while (child != null) {
        val result = processor(child)
        if (!result) return false
        child = child.treeNext
    }
    return true
}

/** 获取当前节点的子节点序列。*/
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

/** 在轻量 AST 中查找首个指定类型的子节点。*/
fun LighterASTNode.firstChild(tree: LighterAST, type: IElementType): LighterASTNode? {
    return LightTreeUtil.firstChildOfType(tree, this, type)
}

/** 在轻量 AST 中查找首个匹配类型集合的子节点。*/
fun LighterASTNode.firstChild(tree: LighterAST, types: TokenSet): LighterASTNode? {
    return LightTreeUtil.firstChildOfType(tree, this, types)
}

/** 在轻量 AST 中按谓词查找首个子节点。*/
inline fun LighterASTNode.firstChild(tree: LighterAST, predicate: (LighterASTNode) -> Boolean): LighterASTNode? {
    val children = tree.getChildren(this)
    for (i in children.indices) {
        val child = children[i]
        if (predicate(child)) return child
    }
    return null
}

/** 获取轻量 AST 的所有直接子节点。*/
fun LighterASTNode.children(tree: LighterAST): List<LighterASTNode> {
    return tree.getChildren(this)
}

/** 获取轻量 AST 中指定类型的直接子节点列表。*/
fun LighterASTNode.childrenOfType(tree: LighterAST, type: IElementType): List<LighterASTNode> {
    return LightTreeUtil.getChildrenOfType(tree, this, type)
}

/** 获取轻量 AST 中匹配类型集合的直接子节点列表。*/
fun LighterASTNode.childrenOfType(tree: LighterAST, types: TokenSet): List<LighterASTNode> {
    return LightTreeUtil.getChildrenOfType(tree, this, types)
}

/** 获取轻量 AST 的父节点。*/
fun LighterASTNode.parent(tree: LighterAST): LighterASTNode? {
    return tree.getParent(this)
}

/** 将轻量 AST Token 节点文本驻留并返回。*/
fun LighterASTNode.internNode(tree: LighterAST): CharSequence? {
    if (this !is LighterASTTokenNode) return null
    return tree.charTable.intern(this.text).toString()
}

// endregion

// region PSI Extensions

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

/** 在 [startOffset, endOffset) 范围内查找并转换元素。*/
fun <T : PsiElement> PsiFile.findElementsBetween(
    startOffset: Int,
    endOffset: Int,
    rootTransform: (element: PsiElement) -> PsiElement?,
    transform: (element: PsiElement) -> T?
): List<T> {
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

/** 在 [startOffset, endOffset) 范围内收集所有 PSI 元素（不转换）。*/
fun PsiFile.findAllElementsBetween(
    startOffset: Int,
    endOffset: Int,
    rootTransform: (element: PsiElement) -> PsiElement?
): List<PsiElement> {
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

/** 若为多解析引用，返回首个解析目标；否则调用 `resolve()`。*/
fun PsiReference.resolveFirst(): PsiElement? {
    return if (this is PsiPolyVariantReference) {
        this.multiResolve(false).firstNotNullOfOrNull { it.element }
    } else {
        this.resolve()
    }
}

/** 收集该引用及其子引用（若实现了 [PsiReferencesAware]）。*/
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
                ProgressManager.checkCanceled()
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

@JvmName("findChildByType")
inline fun <reified T : PsiElement> PsiElement.findChild(forward: Boolean = true, predicate: (T) -> Boolean = { true }): T? {
    return children(forward).findIsInstance<T>(predicate)
}

inline fun PsiElement.findChild(forward: Boolean = true, predicate: (PsiElement) -> Boolean = { true }): PsiElement? {
    return children(forward).findIsInstance(predicate)
}

@JvmName("findChildrenByType")
inline fun <reified T : PsiElement> PsiElement.findChildren(forward: Boolean = true, noinline predicate: (T) -> Boolean = { true }): List<T> {
    return children(forward).filterIsInstance<T>(predicate).toList()
}

inline fun PsiElement.findChildren(forward: Boolean = true, noinline predicate: (PsiElement) -> Boolean = { true }): List<PsiElement> {
    return children(forward).filter(predicate).toList()
}

/** 遍历当前 PSI 的直接子元素。*/
inline fun PsiElement.forEachChild(forward: Boolean = true, action: (PsiElement) -> Unit) {
    // 不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) this.firstChild else this.lastChild
    while (child != null) {
        action(child)
        child = if (forward) child.nextSibling else child.prevSibling
    }
}

/** 处理当前 PSI 的直接子元素；处理器返回 false 时提前终止。*/
inline fun PsiElement.processChild(forward: Boolean = true, processor: (PsiElement) -> Boolean): Boolean {
    // 不会忽略某些特定类型的子元素
    var child: PsiElement? = if (forward) this.firstChild else this.lastChild
    while (child != null) {
        val result = processor(child)
        if (!result) return false
        child = if (forward) child.nextSibling else child.prevSibling
    }
    return true
}

/** 获取指定子元素在同类元素中的索引；不存在返回 -1。*/
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

/** 获取当前 PSI 的子元素序列。*/
fun PsiElement.children(forward: Boolean = true): Sequence<PsiElement> {
    val child = if (forward) this.firstChild else this.lastChild
    if (child == null) return emptySequence()
    return child.siblings(forward, withSelf = true)
}

val PsiElement.icon get() = getIcon(0)

/** PSI 元素的空指针。*/
object EmptyPointer : SmartPsiElementPointer<PsiElement> {
    override fun getElement() = null

    override fun getContainingFile() = null

    override fun getProject() = ProjectManager.getInstance().defaultProject

    override fun getVirtualFile() = null

    override fun getRange() = null

    override fun getPsiRange() = null
}

/** 得到空指针（用于跨项目/失效元素的占位）。*/
fun <T : PsiElement> emptyPointer(): SmartPsiElementPointer<T> = EmptyPointer.cast()

/** 判断当前智能指针是否是空指针。*/
fun SmartPsiElementPointer<*>.isEmpty() = this === EmptyPointer

/** 为当前 PSI 元素创建智能指针，失败时返回空指针。*/
fun <E : PsiElement> E.createPointer(project: Project = this.project): SmartPsiElementPointer<E> {
    return try {
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
    } catch (e: IllegalArgumentException) {
        // Element from alien project - use empty pointer
        emptyPointer()
    }
}

/** 为当前 PSI 元素创建基于文件的智能指针，失败时返回空指针。*/
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

/** 将 PSI 元素包装为 [Symbol]。*/
fun PsiElement.asSymbol(): Symbol = PsiSymbolService.getInstance().asSymbol(this)

// /** 将 PSI 引用包装为 [PsiSymbolReference]。*/
// fun PsiReference.asSymbolReference(): PsiSymbolReference = PsiSymbolService.getInstance().asSymbolReference(this)
//
// /** 从 [Symbol] 中提取底层 PSI 元素。*/
// fun Symbol.extractElement(): PsiElement? = PsiSymbolService.getInstance().extractElementFromSymbol(this)

// endregion

// region Presentation Extensions

/** 在展示信息中附带文件名与图标。*/
fun TargetPresentationBuilder.withLocationIn(file: PsiFile): TargetPresentationBuilder {
    val virtualFile = file.containingFile.virtualFile ?: return this
    val fileType = virtualFile.fileType
    return locationText(virtualFile.name, fileType.icon)
}

// endregion

// region Code Insight Extensions

/** 构建模板（等价于强转为 [TemplateBuilderImpl] 后调用）。*/
fun TemplateBuilder.buildTemplate() = cast<TemplateBuilderImpl>().buildTemplate()

/** 构建行内模板。*/
fun TemplateBuilder.buildInlineTemplate() = cast<TemplateBuilderImpl>().buildInlineTemplate()

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

// endregion

// region Inspection Extensions

/** 根据检查项短名获取对应的 [ScopeToolState]。*/
fun getInspectionToolState(shortName: String, element: PsiElement?, project: Project): ScopeToolState? {
    val currentProfile = InspectionProfileManager.getInstance(project).currentProfile
    val tools = currentProfile.getToolsOrNull(shortName, project) ?: return null
    return tools.getState(element)
}

/** 若检查项启用则返回实际的 [InspectionProfileEntry]，否则返回 null。*/
val ScopeToolState.enabledTool: InspectionProfileEntry? get() = if (isEnabled) tool.tool else null

// endregion

// region Settings Extensions

// 以下的委托方法用于读写需要保存为密码的配置项

/** 委托读取：从 PasswordSafe 读取当前凭据。*/
inline operator fun CredentialAttributes.getValue(thisRef: Any?, property: KProperty<*>): String? = PasswordSafe.instance.getPassword(this)

/** 委托写入：将凭据写入 PasswordSafe。*/
inline operator fun CredentialAttributes.setValue(thisRef: Any?, property: KProperty<*>, value: String?) = PasswordSafe.instance.setPassword(this, value)

// endregion
