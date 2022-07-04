package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.containers.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.selector.*

/**
 * 提供图标名字的代码补全。
 */
@Suppress("UnstableApiUsage")
class ParadoxIconCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val originalFile = parameters.originalFile
		val project = originalFile.project
		
		//需要避免ProcessCanceledException导致完全不作任何提示
		
		val map = CollectionFactory.createSmallMemoryFootprintLinkedMap<String, PsiElement>() //优化性能
		//根据spriteName进行提示
		runBlockingCancellable {
			val sprites = findDefinitionsByType("sprite|spriteType", project, distinct = true)
			if(sprites.isNotEmpty()) {
				for(sprite in sprites) {
					val spriteName = sprite.definitionInfo?.name
					val name = spriteName?.removePrefixOrNull("GFX_")?.removePrefix("text_")
					if(name != null) map.putIfAbsent(name, sprite)
				}
			}
		}
		//根据ddsFileName进行提示
		runBlockingCancellable {
			val selector = fileSelector().gameTypeFrom(originalFile).preferRootFrom(originalFile)
			val ddsFiles = findFilesByFilePath("gfx/interface/icons/", project, expressionType = CwtFilePathExpressionTypes.Icon, distinct = true, selector = selector)
			if(ddsFiles.isNotEmpty()) {
				for(ddsFile in ddsFiles) {
					val name = ddsFile.nameWithoutExtension
					val file = ddsFile.toPsiFile<PsiFile>(project)
					if(file != null) map.putIfAbsent(name, file)
				}
			}
		}
		//作为生成的图标处理（解析为其他类型的定义）
		runBlockingCancellable {
			//如果iconName为job_head_researcher，定义head_researcher包含定义属性`icon = researcher`，则解析为该定义属性
			val jobDefinitions = findAllDefinitions("job", project, distinct = true)
			if(jobDefinitions.isNotEmpty()) {
				for(jobDefinition in jobDefinitions) {
					val jobName = jobDefinition.definitionInfo?.name ?: continue
					map.putIfAbsent("job_$jobName", jobDefinition)
				}
			}
		}
		if(map.isEmpty()) return
		map.forEach { (name, element) ->
			when(element) {
				//val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
				is ParadoxDefinitionProperty -> {
					val icon = PlsIcons.LocalisationIcon //使用特定图标
					val definitionInfo = element.definitionInfo //不应该为null
					val tailText = if(definitionInfo != null) " from ${definitionInfo.type} definition ${definitionInfo.name}" else ""
					val typeText = element.containingFile.name
					val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
					result.addElement(lookupElement)
				}
				is PsiFile -> {
					val icon = PlsIcons.LocalisationIcon //使用特定图标
					val tailText = " from dds file ${element.name}"
					val typeText = element.name
					val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
					result.addElement(lookupElement)
				}
				else -> pass()
			}
		}
	}
}