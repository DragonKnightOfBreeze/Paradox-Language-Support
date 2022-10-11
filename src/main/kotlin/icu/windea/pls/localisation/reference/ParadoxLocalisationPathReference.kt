package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.xml.util.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.model.ParadoxLocalisationCategory.*
import icu.windea.pls.core.selector.*

class ParadoxLocalisationPathReference(
	element: PsiElement,
	rangeInElement: TextRange,
	private val anchor: String,
	private val file: ParadoxLocalisationFile,
	private val category: ParadoxLocalisationCategory
) : PsiReferenceBase<PsiElement>(element, rangeInElement), AnchorReference, EmptyResolveMessageProvider {
	override fun resolve(): PsiElement? {
		//任意locale都可以
		val project = element.project
		val scope = GlobalSearchScope.fileScope(file)
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
		return when(category) {
			Localisation -> findLocalisation(anchor, project, scope, selector = selector)
			SyncedLocalisation -> findSyncedLocalisation(anchor, project, scope, selector = selector)
		}
	}
	
	override fun getUnresolvedMessagePattern(): String {
		return when(category) {
			Localisation -> PlsBundle.message("localisation.annotator.unresolvedLocalisationInFile", anchor, file.name)
			SyncedLocalisation -> PlsBundle.message("localisation.annotator.unresolvedSyncedLocalisationInFile", anchor, file.name)
		}
	}
}

