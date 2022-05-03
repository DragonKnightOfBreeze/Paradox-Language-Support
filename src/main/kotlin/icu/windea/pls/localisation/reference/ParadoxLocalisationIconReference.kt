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
 * * 生成的图标。例如定义`job_head_researcher`拥有定义属性`icon = researcher`，将会生成图标`job_head_researcher`。
 */
class ParadoxLocalisationIconReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationIcon>(element, rangeInElement), PsiPolyVariantReference {
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
	
	//TODO 研究生成的图标究竟是个啥逻辑
	//TODO 按照definition进行解析，iconName是否需要忽略大小写？
	
	override fun resolve(): PsiElement? {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name
		val project = element.project
		//尝试解析为spriteType
		val spriteName = "GFX_text_$iconName"
		val sprite = findDefinitionByType(spriteName, "sprite|spriteType", project)
		if(sprite != null) return sprite
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val filePath = "gfx/interface/icons/,$iconName.dds"
		val ddsFile = findFileByFilePath(filePath, project, expressionType = CwtFilePathExpressionType.FilePath)
		if(ddsFile != null) return ddsFile.toPsiFile(project)
		//如果上述方式都无法解析，则作为生成的图标处理
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobName = iconName.removePrefixOrNull("job_")
		if(jobName != null) {
			val jobDefinition = findDefinition(jobName, "job", project)
			if(jobDefinition != null) return jobDefinition
		}
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name
		val project = element.project
		//尝试解析为spriteType
		val spriteName = "GFX_text_$iconName"
		val sprites = findDefinitionsByType(spriteName, "sprite|spriteType", project)
		if(sprites.isNotEmpty()) return sprites.mapToArray { PsiElementResolveResult(it) }
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val filePath = "gfx/interface/icons/,$iconName.dds"
		val ddsFiles = findFilesByFilePath(filePath, project, expressionType = CwtFilePathExpressionType.FilePath)
		if(ddsFiles.isNotEmpty()) return ddsFiles.mapNotNullTo(SmartList()) { it.toPsiFile<PsiFile>(project) }.mapToArray { PsiElementResolveResult(it) }
		//如果上述方式都无法解析，则作为生成的图标处理
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobName = iconName.removePrefixOrNull("job_")
		if(jobName != null) {
			val jobDefinitions = findDefinitions(jobName, "job", project)
			if(jobDefinitions.isNotEmpty()) return jobDefinitions.mapToArray { PsiElementResolveResult(it) }
		}
		return ResolveResult.EMPTY_ARRAY
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
		//作为生成的图标处理
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobDefinitions = findAllDefinitions("job", project)
		if(jobDefinitions.isNotEmpty()) {
			for(jobDefinition in jobDefinitions) {
				val jobName = jobDefinition.definitionInfo?.name ?: continue
				map.putIfAbsent(jobName, jobDefinition)
			}
		}
		if(map.isEmpty()) return ResolveResult.EMPTY_ARRAY
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