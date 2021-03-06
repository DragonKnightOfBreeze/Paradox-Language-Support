package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.xml.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxLocalisationCategory.*

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
		return when(category) {
			Localisation -> findLocalisation(anchor, null, project, scope)
			SyncedLocalisation -> findSyncedLocalisation(anchor, null, project, scope)
		}
	}
	
	override fun getUnresolvedMessagePattern(): String {
		return when(category) {
			Localisation -> message("paradox.localisation.annotator.unresolvedLocalisationInFile", anchor, file.name)
			SyncedLocalisation -> message("paradox.localisation.annotator.unresolvedSyncedLocalisationInFile", anchor, file.name)
		}
	}
}

