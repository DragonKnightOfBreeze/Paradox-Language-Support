package icu.windea.pls.localisation.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.util.selector.*

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
	//应当可以来自游戏启动时生成的modifier，mod_$modifierName
	
	override fun resolve(): PsiElement? {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name ?: return null
		val project = element.project
		//尝试解析为spriteType
		val textSpriteName = "GFX_text_$iconName"
		val textSpriteSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
		val textSprite = findDefinitionByType(textSpriteName, "sprite|spriteType", project, selector = textSpriteSelector)
		if(textSprite != null) return textSprite
		val spriteName = "GFX_$iconName"
		val spriteSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
		val sprite = findDefinitionByType(spriteName, "sprite|spriteType", project, selector = spriteSelector)
		if(sprite != null) return sprite
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val fileSelector = fileSelector().gameTypeFrom(element).preferRootFrom(element)
		val ddsFile = findFilesByFilePath("gfx/interface/icons/", project, expressionType = CwtFilePathExpressionTypes.Icon, selector = fileSelector).find { it.nameWithoutExtension == iconName }
		if(ddsFile != null) return ddsFile.toPsiFile(project)
		//如果上述方式都无法解析，则作为生成的图标处理（解析为其他类型的定义）
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobName = iconName.removePrefixOrNull("job_")
		if(jobName != null) {
			val definitionSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
			val jobDefinition = findDefinition(jobName, "job", project, selector = definitionSelector)
			if(jobDefinition != null) return jobDefinition
		}
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name ?: return ResolveResult.EMPTY_ARRAY
		val project = element.project
		//尝试解析为spriteType
		val textSpriteName = "GFX_text_$iconName"
		val textSpriteSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
		val textSprites = findDefinitionsByType(textSpriteName, "sprite|spriteType", project, selector = textSpriteSelector)
		if(textSprites.isNotEmpty()) return textSprites.mapToArray { PsiElementResolveResult(it) }
		val spriteName = "GFX_$iconName"
		val spriteSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
		val sprites = findDefinitionsByType(spriteName, "sprite|spriteType", project, selector = spriteSelector)
		if(sprites.isNotEmpty()) return sprites.mapToArray { PsiElementResolveResult(it) }
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val fileSelector = fileSelector().gameTypeFrom(element).preferRootFrom(element)
		val ddsFiles = findFilesByFilePath("gfx/interface/icons/", project, expressionType = CwtFilePathExpressionTypes.Icon, selector = fileSelector).filter { it.nameWithoutExtension == iconName }
		if(ddsFiles.isNotEmpty()) return ddsFiles.mapNotNullTo(SmartList()) { it.toPsiFile(project) }.mapToArray { PsiElementResolveResult(it) }
		//如果上述方式都无法解析，则作为生成的图标处理（解析为其他类型的定义）
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobName = iconName.removePrefixOrNull("job_")
		if(jobName != null) {
			val definitionSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
			val jobDefinitions = findDefinitions(jobName, "job", project, selector = definitionSelector)
			if(jobDefinitions.isNotEmpty()) return jobDefinitions.mapToArray { PsiElementResolveResult(it) }
		}
		return ResolveResult.EMPTY_ARRAY
	}
	
	/**
	 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxIconCompletionProvider
	 */
	@Suppress("RedundantOverride")
	override fun getVariants(): Array<Any> {
		return super<PsiReferenceBase>.getVariants() //not here
	}
}