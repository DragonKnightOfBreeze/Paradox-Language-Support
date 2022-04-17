package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxLocalisationIconReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationIcon>(element, rangeInElement) {
	//这里iconName不仅可以是name有前缀"GFX_text_"的sprite，也可以直接对应"gfx/interface/icons"文件夹下相同名字的dds文件
	
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO scriptProperty的propertyName和definitionName不一致导致重命名scriptProperty时出现问题
		//重命名关联的sprite或ddsFile
		//val resolved = resolve()
		//when{
		//	resolved != null && !resolved.isWritable -> {
		//		throw IncorrectOperationException(message("cannotBeRenamed"))
		//	}
		//	resolved is ParadoxScriptProperty -> {
		//		val nameProperty = resolved.findProperty("name", true)
		//		val nameValue = nameProperty?.propertyValue?.value
		//		if(nameValue is ParadoxScriptString){
		//			nameValue.value = "GFX_text_${newElementName}".quote()
		//		}
		//	}
		//	resolved is PsiFile -> {
		//		resolved.name = "$newElementName.dds"
		//	}
		//}
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		//根据spriteName和ddsFileName进行解析
		val name = element.name
		element.resolveScope
		//尝试解析为spriteType
		val spriteName = "GFX_text_$name"
		val project = element.project
		val sprite = findDefinitionByType(spriteName, "sprite", project)
			?: findDefinitionByType(spriteName, "spriteType", project)
		if(sprite != null) return sprite
		//如果不能解析为spriteType，则尝试解析为相同名字的dds文件
		val ddsFiles = FilenameIndex.getVirtualFilesByName("$name.dds", GlobalSearchScope.allScope(project))
		val ddsIcon = ddsFiles.firstOrNull {
			val path = it.fileInfo?.path
			path != null && "gfx/interface/icons".matchesPath(path.parent)
		}
		if(ddsIcon != null) return ddsIcon.toPsiFile(project)
		
		return null
	}
	
	override fun getVariants(): Array<out Any> {
		//根据spriteName和ddsFileName进行提示
		val project = element.project
		val sprites = findDefinitionsByType("sprite", project)
			.ifEmpty { findDefinitionsByType("spriteType", project) }
		val ddsFiles = FilenameIndex.getAllFilesByExt(project, "dds").filter {
			val path = it.fileInfo?.path
			path != null && "gfx/interface/icons".matchesPath(path.parent)
		}
		val list = sprites + ddsFiles
		return list.mapToArray {
			when(it) {
				is ParadoxScriptProperty -> {
					val name = it.definitionInfo?.name?.removePrefix("GFX_text_").orEmpty()
					val icon = localisationIconIcon //使用特定图标
					val typeText = it.containingFile.name
					LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
				}
				is VirtualFile -> {
					val name = it.name.substringBeforeLast('.')
					val icon = localisationIconIcon //使用特定图标
					val typeText = it.name
					LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
				}
				else -> throw Error()
			}
		}
	}
}