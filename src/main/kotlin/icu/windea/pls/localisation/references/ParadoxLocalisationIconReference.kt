package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import kotlin.collections.mapNotNullTo

/**
 * 本地化图标的PSI引用。
 *
 * 图标的名字可以对应：
 * * 名字为"GFX_text_${iconName}"，类型为sprite的定义。
 * * 名字为"GFX_${iconName}"，类型为sprite的定义。
 * * "gfx/interface/icons"及其子目录中，文件名为iconName（去除后缀名）的DDS文件。
 * * 生成的图标。例如定义`job_head_researcher`拥有定义属性`icon = researcher`，将会生成图标`job_head_researcher`。
 * 
 * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxLocalisationIconCompletionProvider
*/
class ParadoxLocalisationIconReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationIcon>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名关联的sprite或definition或ddsFile
		val resolved = resolve()
		when{
			resolved is PsiFile -> resolved.setNameWithoutExtension(newElementName)
			resolved is ParadoxScriptProperty -> {
				val definitionInfo = resolved.definitionInfo
				if(definitionInfo != null) {
					if(definitionInfo.type.let { it == "sprite" || it == "spriteType" }) {
						resolved.name = resolved.name.replaceFirst(rangeInElement.substring(element.text), newElementName)
					} else {
						resolved.name = newElementName
					}
				}
			}
		}
		return element.setName(newElementName)
	}
	
	//TODO 研究生成的图标究竟是个啥逻辑
	//也许可以来自游戏启动时生成的modifier，即：mod_$modifierName？
	
	override fun resolve(): PsiElement? {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name ?: return null
		val project = element.project
		//尝试解析为spriteType
		val textSpriteName = "GFX_text_$iconName"
		val textSpriteSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
		val textSprite = ParadoxDefinitionSearch.search(textSpriteName, "sprite|spriteType", project, selector = textSpriteSelector).find()
		if(textSprite != null) return textSprite
		val spriteName = "GFX_$iconName"
		val spriteSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
		val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite|spriteType", project, selector = spriteSelector).find()
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
			val jobDefinition = ParadoxDefinitionSearch.search(jobName, "job", project, selector = definitionSelector).find()
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
		val textSprites = ParadoxDefinitionSearch.search(textSpriteName, "sprite|spriteType", project, selector = textSpriteSelector).findAll()
		if(textSprites.isNotEmpty()) return textSprites.mapToArray { PsiElementResolveResult(it) }
		val spriteName = "GFX_$iconName"
		val spriteSelector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
		val sprites = ParadoxDefinitionSearch.search(spriteName, "sprite|spriteType", project, selector = spriteSelector).findAll()
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
			val jobDefinitions = ParadoxDefinitionSearch.search(jobName, "job", project, selector = definitionSelector).findAll()
			if(jobDefinitions.isNotEmpty()) return jobDefinitions.mapToArray { PsiElementResolveResult(it) }
		}
		return ResolveResult.EMPTY_ARRAY
	}
}
