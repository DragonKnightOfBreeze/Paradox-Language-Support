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
 * * 名字为"GFX_${iconName}"，类型为sprite的定义。
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
	
	override fun resolve(): PsiElement? {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name
		val project = element.project
		//尝试解析为spriteType
		val textSpriteName = "GFX_text_$iconName"
		val textSprite = findDefinitionByType(textSpriteName, "sprite|spriteType", project)
		if(textSprite != null) return textSprite
		val spriteName = "GFX_$iconName"
		val sprite = findDefinitionByType(spriteName, "sprite|spriteType", project)
		if(sprite != null) return sprite
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val ddsFile = findFilesByFilePath("gfx/interface/icons/", project, expressionType = CwtFilePathExpressionType.Icon).find { it.nameWithoutExtension == iconName }
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
		val textSpriteName = "GFX_text_$iconName"
		val textSprites = findDefinitionsByType(textSpriteName, "sprite|spriteType", project)
		if(textSprites.isNotEmpty()) return textSprites.mapToArray { PsiElementResolveResult(it) }
		val spriteName = "GFX_$iconName"
		val sprites = findDefinitionsByType(spriteName, "sprite|spriteType", project)
		if(sprites.isNotEmpty()) return sprites.mapToArray { PsiElementResolveResult(it) }
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val ddsFiles = findFilesByFilePath("gfx/interface/icons/", project, expressionType = CwtFilePathExpressionType.Icon).filter { it.nameWithoutExtension == iconName }
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
				val spriteName = sprite.definitionInfo?.name
				val name = spriteName?.removePrefixOrNull("GFX_")?.removePrefix("text_")
				if(name != null) map.putIfAbsent(name, sprite)
			}
		}
		val ddsFiles = findFilesByFilePath("gfx/interface/icons/", project, expressionType = CwtFilePathExpressionType.Icon, distinct = true)
		if(ddsFiles.isNotEmpty()) {
			for(ddsFile in ddsFiles) {
				val name = ddsFile.nameWithoutExtension
				val file = ddsFile.toPsiFile<PsiFile>(project)
				if(file != null) map.putIfAbsent(name, file)
			}
		}
		//作为生成的图标处理
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobDefinitions = findAllDefinitions("job", project, distinct = true) //FIXME PCE?
		if(jobDefinitions.isNotEmpty()) {
			for(jobDefinition in jobDefinitions) {
				val jobName = jobDefinition.definitionInfo?.name ?: continue
				map.putIfAbsent("job_$jobName", jobDefinition)
			}
		}
		if(map.isEmpty()) return ResolveResult.EMPTY_ARRAY
		return map.mapToArray { (name, it) ->
			when(it) {
				//val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
				is ParadoxDefinitionProperty -> {
					val icon = PlsIcons.localisationIconIcon //使用特定图标
					val definitionInfo = it.definitionInfo //不应该为null
					val tailText = if(definitionInfo != null) " from ${definitionInfo.type} definition ${definitionInfo.name}" else ""
					val typeText = it.containingFile.name
					LookupElementBuilder.create(it, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
				}
				is PsiFile -> {
					val icon = PlsIcons.localisationIconIcon //使用特定图标
					val tailText = " from dds file ${it.name}"
					val typeText = it.name
					LookupElementBuilder.create(it, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
				}
				else -> throw InternalError()
			}
		}
	}
}