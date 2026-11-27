@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.psi

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
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.SmartPointerManager
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.api.RenameValidationResult
import com.intellij.refactoring.rename.api.RenameValidator
import com.intellij.refactoring.rename.symbol.RenameableSymbol
import icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationTarget
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxNameValidators
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于在 Markdown 文件中，表示内联代码解析到的目标符号（定义、本地化、封装变量等）。
 *
 * 该符号不直接由 PSI 元素表示，因此平台无法通过符号提取对应的 PSI 元素。
 * 文档、导航和重命名能力分别通过 [DocumentationSymbol]、[NavigatableSymbol]
 * 和 [RenameableSymbol] 提供。
 */
@Suppress("UnstableApiUsage")
class MarkdownInlineCodeSymbol(
    val element: PsiElement,
) : Symbol, DocumentationSymbol, NavigatableSymbol, RenameableSymbol {
    override fun createPointer(): Pointer<MarkdownInlineCodeSymbol> {
        val elementPointer = SmartPointerManager.createPointer(element)
        return Pointer { elementPointer.dereference()?.let(::MarkdownInlineCodeSymbol) }
    }

    override fun getDocumentationTarget(): DocumentationTarget {
        return ParadoxDocumentationTarget(element, null)
    }

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
        return listOf(SymbolNavigationService.getInstance().psiElementNavigationTarget(element))
    }

    override val renameTarget: RenameTarget
        get() = MarkdownInlineCodeRenameTarget(element)

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

/**
 * 由 Markdown 内联代码符号提供的重命名目标。
 *
 * 目标名字不包含用于区分封装变量的前缀 `@`。
 */
internal class MarkdownInlineCodeRenameTarget(
    val element: PsiElement,
) : RenameTarget {
    override fun createPointer(): Pointer<out RenameTarget> {
        val elementPointer = SmartPointerManager.createPointer(element)
        return Pointer { elementPointer.dereference()?.let(::MarkdownInlineCodeRenameTarget) }
    }

    override val targetName: String
        get() = getMarkdownInlineCodeTargetName(element)

    override fun presentation(): TargetPresentation {
        return ParadoxDocumentationTarget(element, null).computePresentation()
    }

    override fun validator(): RenameValidator {
        return MarkdownInlineCodeRenameValidator(element)
    }

    override fun equals(other: Any?): Boolean {
        return other is MarkdownInlineCodeRenameTarget && element == other.element
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }
}

internal fun getMarkdownInlineCodeTargetName(element: PsiElement): String {
    return when (element) {
        is ParadoxDefinitionElement -> element.definitionInfo?.name ?: element.name
        is ParadoxLocalisationProperty -> element.name
        is ParadoxScriptScriptedVariable -> element.name ?: ""
        else -> (element as? PsiNamedElement)?.name ?: ""
    }
}

private class MarkdownInlineCodeRenameValidator(
    private val element: PsiElement,
) : RenameValidator {
    override fun validate(newName: String): RenameValidationResult {
        val valid = when {
            newName.isEmpty() -> false
            element is ParadoxScriptScriptedVariable -> ParadoxNameValidators.checkScriptedVariableName(newName)
            element is ParadoxLocalisationProperty -> ParadoxNameValidators.checkLocalisationPropertyName(newName)
            element is ParadoxDefinitionElement -> ParadoxNameValidators.checkDefinitionName(newName)
            else -> true
        }
        return if (valid) RenameValidationResult.ok() else RenameValidationResult.invalid()
    }
}
