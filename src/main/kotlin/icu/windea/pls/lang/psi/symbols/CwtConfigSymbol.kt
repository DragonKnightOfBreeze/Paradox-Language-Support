@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.find.usages.api.*
import com.intellij.model.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.navigation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.psi.search.*
import com.intellij.refactoring.rename.api.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.model.*
import java.util.*

//org.intellij.plugins.markdown.model.psi.headers.HeaderSymbol

class CwtConfigSymbol(
    val element: CwtStringExpressionElement,
    val rangeInElement: TextRange,
    val name: String,
    val configType: CwtConfigType,
    val gameType: ParadoxGameType,
) : Symbol, NavigationTarget, SearchTarget, RenameTarget, DocumentationTarget {
    val id = "${gameType.id}@${configType.id}.$name"

    override fun createPointer(): Pointer<out CwtConfigSymbol> {
        return createPointer(element, rangeInElement, name, configType, gameType)
    }

    override fun computePresentation(): TargetPresentation {
        return presentation()
    }

    override fun navigationRequest(): NavigationRequest? {
        return NavigationRequest.sourceNavigationRequest(element.containingFile, element.textRange)
    }

    override val targetName: String
        get() = name

    override val maximalSearchScope: SearchScope?
        get() = GlobalSearchScope.allScope(element.containingFile.project)

    override val usageHandler: UsageHandler
        get() = UsageHandler.createEmptyUsageHandler(name)

    override fun presentation(): TargetPresentation {
        val builder = TargetPresentation.builder(name).icon(configType.icon)
        return builder.withLocationIn(element.containingFile).presentation()
    }

    override fun computeDocumentationHint(): String? {
        return CwtDocumentationManager.computeLocalDocumentation(this, hint = true)
    }

    override fun computeDocumentation(): DocumentationResult? {
        return DocumentationResult.asyncDocumentation {
            val html = runReadAction { CwtDocumentationManager.computeLocalDocumentation(this, hint = false) } ?: return@asyncDocumentation null
            DocumentationResult.documentation(html)
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtConfigSymbol && id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    companion object {
        fun createPointer(element: CwtStringExpressionElement, rangeInElement: TextRange, text: String, type: CwtConfigType, gameType: ParadoxGameType): Pointer<out CwtConfigSymbol> {
            return Pointer.delegatingPointer(element.createPointer()) {
                CwtConfigSymbol(element, rangeInElement, text, type, gameType)
            }
        }
    }
}
