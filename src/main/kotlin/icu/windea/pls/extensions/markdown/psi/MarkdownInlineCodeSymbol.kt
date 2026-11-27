@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.psi

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigatableSymbol
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationSymbol
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.api.RenameValidator
import icu.windea.pls.extensions.markdown.MarkdownExtensionManager
import icu.windea.pls.extensions.markdown.refactoring.rename.MarkdownInlineCodeRenameValidator
import icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationTarget

// org.intellij.plugins.markdown.model.psi.headers.HeaderSymbol

/**
 * 用于在 Markdown 文件中，表示内联代码解析到的目标符号（定义、本地化、封装变量等）。
 *
 * 该符号不直接由 PSI 元素表示，因此平台无法通过符号提取对应的 PSI 元素。
 * 支持快速文档、导航、查询、重命名等语言功能。
 */
@Suppress("UnstableApiUsage")
class MarkdownInlineCodeSymbol(
    val element: PsiElement,
) : Symbol, DocumentationSymbol, NavigatableSymbol, SearchTarget, RenameTarget {
    override fun createPointer(): Pointer<MarkdownInlineCodeSymbol> {
        val elementPointer = SmartPointerManager.createPointer(element)
        return Pointer { elementPointer.dereference()?.let(::MarkdownInlineCodeSymbol) }
    }

    override val targetName: String
        get() = MarkdownExtensionManager.resolveTargetNameFromInlineCode(element)

    override val maximalSearchScope: SearchScope?
        get() = null

    override val usageHandler: UsageHandler
        get() = UsageHandler.createEmptyUsageHandler(targetName)

    override fun presentation(): TargetPresentation {
        return ParadoxDocumentationTarget(element, null).computePresentation()
    }

    override fun validator(): RenameValidator {
        return MarkdownInlineCodeRenameValidator(element)
    }

    override fun getDocumentationTarget(): DocumentationTarget {
        return ParadoxDocumentationTarget(element, null)
    }

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
        return listOf(SymbolNavigationService.getInstance().psiElementNavigationTarget(element))
    }

    override fun equals(other: Any?): Boolean {
        return other is MarkdownInlineCodeSymbol && element == other.element
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }

    override fun toString(): String {
        return "MarkdownInlineCodeSymbol($element)"
    }
}
