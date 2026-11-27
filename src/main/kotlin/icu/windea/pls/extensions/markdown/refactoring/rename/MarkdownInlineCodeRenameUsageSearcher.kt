@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.refactoring.rename

import com.intellij.find.usages.api.PsiUsage
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.rename.api.ModifiableRenameUsage
import com.intellij.refactoring.rename.api.ModifiableRenameUsage.*
import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage
import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.util.CollectionQuery
import com.intellij.util.Query
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.extensions.markdown.MarkdownExtensionManager
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeSymbol
import icu.windea.pls.extensions.markdown.references.MarkdownInlineCodeReferencesSearcher

// org.intellij.plugins.markdown.model.psi.headers.HeaderRenameUsageSearcher

/**
 * 由 Markdown 内联代码发起的重命名时，提供目标声明以及所有引用作为重命名用法。
 *
 * 引用通过 [ReferencesSearch] 收集，因此既包括脚本、本地化等语言内引用，
 * 也包括由 [MarkdownInlineCodeReferencesSearcher] 提供的 Markdown 内联代码引用。
 */
class MarkdownInlineCodeRenameUsageSearcher : RenameUsageSearcher {
    override fun collectSearchRequests(parameters: RenameUsageSearchParameters): Collection<Query<out RenameUsage>> {
        val target = parameters.target as? MarkdownInlineCodeSymbol ?: return emptyList()
        val usages = runSmartReadAction {
            val element = target.element
            if (MarkdownExtensionManager.resolveTargetNameFromInlineCode(element).isEmpty()) return@runSmartReadAction emptyList()
            val references = ReferencesSearch.search(element, parameters.searchScope)
                .findAll()
                .map { reference -> PsiModifiableRenameUsage.defaultPsiModifiableRenameUsage(PsiUsage.textUsage(reference.element, reference.rangeInElement)) }
            references + MarkdownInlineCodeDeclarationUsage(element)
        }
        return listOf(CollectionQuery(usages))
    }
}

/**
 * 目标声明的重命名用法。
 *
 * 声明不能通过文本范围直接替换（例如定义的名字可能来自某个属性的值），
 * 因此这里使用 [ModelUpdater] 委托给 `PsiNamedElement.setName`。
 */
private class MarkdownInlineCodeDeclarationUsage(
    val element: PsiElement,
) : PsiModifiableRenameUsage {
    private val elementPointer: SmartPsiElementPointer<PsiElement> = SmartPointerManager.createPointer(element)
    private val containingFile: PsiFile = element.containingFile
    private val usageRange: TextRange = element.textRange.shiftLeft(containingFile.textRange.startOffset)

    override fun createPointer(): Pointer<out MarkdownInlineCodeDeclarationUsage> {
        val pointer = elementPointer
        return Pointer { pointer.dereference()?.let(::MarkdownInlineCodeDeclarationUsage) }
    }

    override val declaration: Boolean
        get() = true

    override val file: PsiFile
        get() = containingFile

    override val range: TextRange
        get() = usageRange

    override val modelUpdater: ModifiableRenameUsage.ModelUpdater
        get() = MarkdownInlineCodeDeclarationModelUpdater

    override fun equals(other: Any?): Boolean {
        return other is MarkdownInlineCodeDeclarationUsage && element == other.element
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }
}

private object MarkdownInlineCodeDeclarationModelUpdater : ModifiableRenameUsage.ModelUpdater {
    override fun prepareModelUpdate(usage: ModifiableRenameUsage): ModifiableRenameUsage.ModelUpdate? {
        val element = (usage as? MarkdownInlineCodeDeclarationUsage)?.element ?: return null
        return object : ModifiableRenameUsage.ModelUpdate {
            @Suppress("UsePropertyAccessSyntax")
            override fun updateModel(newName: String) {
                val namedElement = element as? PsiNamedElement ?: return
                namedElement.setName(newName)
            }
        }
    }
}
