package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.SmartList
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 本地化图标的PSI引用。
 * 
 * 图标的名字可以对应：
 * * 名字为"GFX_text_${iconName}"，类型为sprite的定义。
 * * "gfx/interface/icons"及其子目录中，文件名为iconName（去除后缀名）的DDS文件。
 */
class ParadoxLocalisationIconReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationIcon>(element, rangeInElement), PsiPolyVariantReference {
	//这里iconName可以对应：
	//name有前缀"GFX_text_"的sprite，也可以直接对应"gfx/interface/icons"文件夹下相同名字的dds文件
	
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
		val project = element.project
		//尝试解析为spriteType
		val spriteName = "GFX_text_$name"
		val sprite = findDefinitionByType(spriteName, "sprite|spriteType", project)
		if(sprite != null) return sprite
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val filePath = "gfx/interface/icons/,$name.dds"
		val ddsFile = findFileByFilePath(filePath, project, expressionType = CwtFilePathExpressionType.FilePath)
		if(ddsFile != null) return ddsFile.toPsiFile(project)
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//根据spriteName和ddsFileName进行解析
		val name = element.name
		val project = element.project
		//尝试解析为spriteType
		val spriteName = "GFX_text_$name"
		val sprites = findDefinitionsByType(spriteName, "sprite|spriteType", project)
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val filePath = "gfx/interface/icons/,$name.dds"
		val ddsFiles = findFilesByFilePath(filePath, project, expressionType = CwtFilePathExpressionType.FilePath)
		if(ddsFiles.isEmpty()){
			return sprites.mapToArray { PsiElementResolveResult(it) }
		} else {
			//直接通过dds文件名得到的需要按照对应的filePath去重
			val filePathsToDistinct = mutableSetOf<String>()
			val list = SmartList<ResolveResult>()
			for (item in sprites) {
				val filePathToDistinct = getSpriteDdsFilePath(item)
				if(filePathToDistinct != null) filePathsToDistinct.add(filePathToDistinct)
				list.add(PsiElementResolveResult(item))
			}
			for(ddsFile in ddsFiles) {
				val filePathToDistinct = ddsFile.fileInfo?.path?.path
				if(filePathToDistinct == null || filePathsToDistinct.add(filePathToDistinct)){ //找不到filePath时不排除
					val file = ddsFile.toPsiFile<PsiFile>(project)
					if(file != null) list.add(PsiElementResolveResult(file))
				}
			}
			return list.toTypedArray()
		}
	}
	
	override fun getVariants(): Array<out Any> {
		//根据spriteName和ddsFileName进行提示
		val project = element.project
		val map = mutableMapOf<String, PsiElement>()
		val sprites = findDefinitionsByType("sprite|spriteType", project, distinct = true)
		if(sprites.isNotEmpty()) {
			for(sprite in sprites) {
				val name = sprite.definitionInfo?.name?.removePrefixOrNull("GFX_text_")
				if(name != null) map.putIfAbsent(name, sprite)
			}
		}
		val ddsFiles = findFilesByFilePath("gfx/interface/icons/,.dds", project, expressionType = CwtFilePathExpressionType.FilePath, distinct = true)
		if(ddsFiles.isNotEmpty()) {
			for(ddsFile in ddsFiles) {
				val name = ddsFile.nameWithoutExtension
				val file = ddsFile.toPsiFile<PsiFile>(project)
				if(file != null) map.putIfAbsent(name, file)
			}
		}
		return map.mapToArray { (name, it) ->
			when(it) {
				is ParadoxDefinitionProperty -> {
					val icon = PlsIcons.localisationIconIcon //使用特定图标
					val typeText = it.containingFile.name
					LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
				}
				is PsiFile -> {
					val icon = PlsIcons.localisationIconIcon //使用特定图标
					val typeText = it.name
					LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
				}
				else -> throw InternalError()
			}
		}
	}
}