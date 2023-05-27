@file:Suppress("UnstableApiUsage")

package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.localisation.psi.*

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
class ParadoxLocalisationIconPsiReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationIcon>(element, rangeInElement) {
	val project by lazy { element.project }
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	//缓存解析结果以优化性能
	
	override fun resolve(): PsiElement? {
		return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
	}
	
	private fun doResolve(): PsiElement? {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name ?: return null
		//尝试解析为spriteType
		val textSpriteName = "GFX_text_$iconName"
		val textSpriteSelector = definitionSelector(project, element).contextSensitive()
		val textSprite = ParadoxDefinitionSearch.search(textSpriteName, "sprite", textSpriteSelector).find()
		if(textSprite != null) return textSprite
		val spriteName = "GFX_$iconName"	
		val spriteSelector = definitionSelector(project, element).contextSensitive()
		val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite", spriteSelector).find()
		if(sprite != null) return sprite
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val fileSelector = fileSelector(project, element).contextSensitive()
		val ddsFileExpression = CwtValueExpression.resolve("icon[gfx/interface/icons/]")
		val ddsFile = ParadoxFilePathSearch.search(iconName, ddsFileExpression, fileSelector).find()
		if(ddsFile != null) return ddsFile.toPsiFile(project)
		//如果上述方式都无法解析，则作为生成的图标处理（解析为其他类型的定义）
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobName = iconName.removePrefixOrNull("job_")
		if(jobName != null) {
			val definitionSelector = definitionSelector(project, element).contextSensitive()
			val jobDefinition = ParadoxDefinitionSearch.search(jobName, "job", definitionSelector).find()
			if(jobDefinition != null) return jobDefinition
		}
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
	}
	
	private fun doMultiResolve(): Array<out ResolveResult> {
		//根据spriteName和ddsFileName进行解析
		val iconName = element.name ?: return ResolveResult.EMPTY_ARRAY
		//尝试解析为spriteType
		val textSpriteName = "GFX_text_$iconName"
		val textSpriteSelector = definitionSelector(project, element).contextSensitive()
		val textSprites = ParadoxDefinitionSearch.search(textSpriteName, "sprite", textSpriteSelector).findAll()
		if(textSprites.isNotEmpty()) return textSprites.mapToArray { PsiElementResolveResult(it) }
		val spriteName = "GFX_$iconName"
		val spriteSelector = definitionSelector(project, element).contextSensitive()
		val sprites = ParadoxDefinitionSearch.search(spriteName, "sprite", spriteSelector).findAll()
		if(sprites.isNotEmpty()) return sprites.mapToArray { PsiElementResolveResult(it) }
		//如果不能解析为spriteType，则尝试解析为gfx/interface/icons及其子目录中为相同名字的dds文件
		val fileSelector = fileSelector(project, element).contextSensitive()
		val ddsFileExpression = CwtValueExpression.resolve("icon[gfx/interface/icons/]")
		val ddsFiles = ParadoxFilePathSearch.search(iconName, ddsFileExpression, fileSelector).findAll()
		if(ddsFiles.isNotEmpty()) return ddsFiles.mapNotNullTo(mutableListOf()) { it.toPsiFile(project) }.mapToArray { PsiElementResolveResult(it) }
		//如果上述方式都无法解析，则作为生成的图标处理（解析为其他类型的定义）
		//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
		val jobName = iconName.removePrefixOrNull("job_")
		if(jobName != null) {
			val definitionSelector = definitionSelector(project, element).contextSensitive()
			val jobDefinitions = ParadoxDefinitionSearch.search(jobName, "job", definitionSelector).findAll()
			if(jobDefinitions.isNotEmpty()) return jobDefinitions.mapToArray { PsiElementResolveResult(it) }
		}
		return ResolveResult.EMPTY_ARRAY
	}
	
	private object Resolver: ResolveCache.AbstractResolver<ParadoxLocalisationIconPsiReference, PsiElement> {
		override fun resolve(ref: ParadoxLocalisationIconPsiReference, incompleteCode: Boolean): PsiElement? {
			return ref.doResolve()
		}
	}
	
	private object MultiResolver: ResolveCache.PolyVariantResolver<ParadoxLocalisationIconPsiReference> {
		override fun resolve(ref: ParadoxLocalisationIconPsiReference, incompleteCode: Boolean): Array<out ResolveResult> {
			return ref.doMultiResolve()
		}
	}
}
