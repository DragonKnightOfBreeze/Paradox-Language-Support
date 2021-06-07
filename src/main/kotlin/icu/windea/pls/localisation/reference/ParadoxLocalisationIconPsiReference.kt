package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxLocalisationIconPsiReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationIcon>(element,rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		return element
	}
	
	//这里iconName不仅可以对应匹配name的spriteType，也可以直接对应相同名字的dds文件
	
	override fun resolve(): PsiElement? {
		val name = element.name
		val project = element.project
		val result = findIcon(name,project)
		if(result!= null) return result
		
		//如果不能解析为spriteType，则尝试解析为相同名字的dds文件
		val ddsFileName = "$name.dds"
		val ddsFiles = FilenameIndex.getFilesByName(project,ddsFileName, GlobalSearchScope.allScope(project))
		val ddsFile = ddsFiles.firstOrNull() ?: return null
		return ddsFile
	}
	
	override fun getVariants(): Array<out Any> {
		val project = element.project
		return findIcons(project).mapToArray {
			val name = it.paradoxDefinitionInfo?.name?.let { n -> resolveIconName(n)}.orEmpty()
			val icon = localisationIconIcon
			val path = it.paradoxFileInfo?.path?.toString().orEmpty()  
			val fileName = it.containingFile.name
			LookupElementBuilder.create(it,name).withIcon(icon).withTailText(path,true)
				.withTypeText(fileName,true)
		}
	}
}