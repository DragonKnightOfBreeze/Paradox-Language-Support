package icu.windea.pls.config.cwt.expression

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.script.psi.*

/**
 * CWT图片的位置表达式。
 *
 * 用于推断定义的相关图片（relatedPicture）的位置。
 *
 * 示例：`"$"`, `"$_desc"`, `"#name"`, "#icon|#icon_frame"`
 * @property placeholder 占位符（表达式文本包含"$"时，为整个字符串，"$"会在解析时替换成definitionName）。
 * @property propertyName 属性名（表达式文本以"#"开始时，为"#"之后和可能的"|"之前的子字符串）。
 * @property extraPropertyNames 额外的属性名（表达式文本以"#"开始且之后包含"|"时，为"|"之后的按","分割的子字符串）。
 */
class CwtPictureLocationExpression(
	expressionString: String,
	val placeholder: String? = null,
	val propertyName: String? = null,
	val extraPropertyNames: List<String>? = null
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtPictureLocationExpression>() {
		val EmptyExpression = CwtPictureLocationExpression("")
		
		override fun doResolve(expressionString: String): CwtPictureLocationExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString.startsWith('#') -> {
					val pipeIndex = expressionString.indexOf('|', 1)
					if(pipeIndex == -1) {
						val propertyName = expressionString.substring(1)
						CwtPictureLocationExpression(expressionString, null, propertyName)
					} else {
						val propertyName = expressionString.substring(1, pipeIndex)
						val extraPropertyNames = expressionString.substring(pipeIndex + 1).split(',')
						CwtPictureLocationExpression(expressionString, null, propertyName, extraPropertyNames)
					}
				}
				else -> {
					CwtPictureLocationExpression(expressionString, expressionString, null, null)
				}
			}
		}
	}
	
	operator fun component1() = placeholder
	
	operator fun component2() = propertyName
	
	operator fun component3() = extraPropertyNames
	
	fun resolve(definitionInfo: ParadoxDefinitionInfo, definition: ParadoxDefinitionProperty, project: Project): Pair<String, PsiFile?>? {
		if(placeholder != null) {
			//假定这里的filePath以.dds结尾
			val filePath = buildString { for(c in placeholder) if(c == '$') append(definitionInfo.name) else append(c) }
			val file = findFileByFilePath(filePath, project)?.toPsiFile<PsiFile>(project)
			return filePath to file
		} else if(propertyName != null && propertyName.isNotEmpty()) {
			//目前只接收类型为string的值
			val value = definition.findProperty(propertyName)?.propertyValue?.value?.castOrNull<ParadoxScriptString>() ?: return null
			while(true) {
				val resolved = resolveValue(value) ?: return null
				when {
					//由filePath解析为DDS文件
					resolved is PsiFile && resolved.fileType == DdsFileType -> {
						val filePath = resolved.fileInfo?.path?.path ?: return null
						val file = findFileByFilePath(filePath, project)?.toPsiFile<PsiFile>(project)
						return filePath to file
					}
					//由filePath解析为definition，这里也可能是sprite之外的definition
					resolved is ParadoxDefinitionProperty -> {
						val resolvedProject = resolved.project
						val resolvedDefinition = resolved
						val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
						val primaryPictureConfigs = resolvedDefinitionInfo.primaryPictureConfigs
						if(primaryPictureConfigs.isEmpty()) return null //CWT规则不完善
						return primaryPictureConfigs.mapAndFirst({ it?.second != null }) { primaryPictureConfig ->
							val locationExpression = primaryPictureConfig.location
							locationExpression.resolve(resolvedDefinitionInfo, resolvedDefinition, resolvedProject)
						}
					}
					else -> return null //解析失败或不支持
				}
			}
		} else {
			return null //不应该出现
		}
	}
	
	fun resolveAll(definitionInfo: ParadoxDefinitionInfo, definition: ParadoxDefinitionProperty, project: Project): Pair<String, Set<PsiFile>>? {
		if(placeholder != null) {
			//假定这里的filePath以.dds结尾
			val filePath = buildString { for(c in placeholder) if(c == '$') append(definitionInfo.name) else append(c) }
			val files = findFilesByFilePath(filePath, project).mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
			return filePath to files
		} else if(propertyName != null && propertyName.isNotEmpty()) {
			//目前只接收类型为string的值
			val value = definition.findProperty(propertyName)?.propertyValue?.value?.castOrNull<ParadoxScriptString>() ?: return null
			while(true) {
				val resolved = resolveValue(value) ?: return null
				when {
					//由filePath解析为DDS文件
					resolved is PsiFile && resolved.fileType == DdsFileType -> {
						val filePath = resolved.fileInfo?.path?.path ?: return null
						val files = findFilesByFilePath(filePath, project).mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
						return filePath to files
					}
					//由filePath解析为definition，这里也可能是sprite之外的definition
					resolved is ParadoxDefinitionProperty -> {
						val resolvedProject = resolved.project
						val resolvedDefinition = resolved
						val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
						val primaryPictureConfigs = resolvedDefinitionInfo.primaryPictureConfigs
						if(primaryPictureConfigs.isEmpty()) return null //CWT规则不完善
						var resolvedFilePath: String? = null
						var resolvedSet: MutableSet<PsiFile>? = null
						for(primaryPictureConfig in primaryPictureConfigs) {
							val locationExpression = primaryPictureConfig.location
							val (filePath, set) = locationExpression.resolveAll(resolvedDefinitionInfo, resolvedDefinition, resolvedProject) ?: continue
							if(resolvedFilePath == null) resolvedFilePath = filePath
							if(resolvedSet == null) resolvedSet = mutableSetOf()
							resolvedSet.addAll(set)
						}
						if(resolvedFilePath == null) return null
						return resolvedFilePath to (resolvedSet ?: emptySet())
					}
					else -> return null //解析失败或不支持
				}
			}
		} else {
			return null //不应该出现
		}
	}
}