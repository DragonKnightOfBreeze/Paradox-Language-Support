package icu.windea.pls.translation

import cn.yiiguxing.plugin.translate.provider.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

//cn.yiiguxing.plugin.translate.provider.KotlinDocumentationElementProvider

class CwtDocumentationElementProvider : DocumentationElementProvider {
	override fun getDocumentationOwner(documentationElement: PsiElement): PsiElement {
		return documentationElement
	}
	
	override fun findDocumentationElementAt(psiFile: PsiFile, offset: Int): PsiElement? {
		return psiFile.findElementAt(offset)?.parents(true)?.find {
			it is CwtProperty || it is CwtValue
		}
	}
}

class ParadoxScriptDocumentationElementProvider : DocumentationElementProvider {
	override fun getDocumentationOwner(documentationElement: PsiElement): PsiElement {
		return documentationElement
	}
	
	override fun findDocumentationElementAt(psiFile: PsiFile, offset: Int): PsiElement? {
		return psiFile.findElementAt(offset)?.parents(true)?.find {
			it is ParadoxScriptProperty && it.definitionInfo != null
		}
	}
}

class ParadoxLocalisationDocumentationElementProvider : DocumentationElementProvider {
	override fun getDocumentationOwner(documentationElement: PsiElement): PsiElement {
		return documentationElement
	}
	
	override fun findDocumentationElementAt(psiFile: PsiFile, offset: Int): PsiElement? {
		return psiFile.findElementAt(offset)?.parents(true)?.find {
			it is ParadoxLocalisationProperty && it.localisationInfo != null
		}
	}
}