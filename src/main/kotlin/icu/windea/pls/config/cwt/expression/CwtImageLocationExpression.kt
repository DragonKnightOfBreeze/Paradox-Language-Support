package icu.windea.pls.config.cwt.expression

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.dds.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.selector.*

private val validValueTypes = arrayOf(
	CwtDataTypes.FilePath,
	CwtDataTypes.Icon,
	CwtDataTypes.TypeExpression
)

/**
 * CWT图片的位置表达式。
 *
 * 用于推断定义的相关图片（relatedImage）的位置。
 *
 * 示例：`"$"`, `"$_desc"`, `"#icon"`, "#icon|#icon_frame"`
 * @property placeholder 占位符（表达式文本包含"$"时，为整个字符串，"$"会在解析时替换成definitionName）。
 * @property propertyName 属性名（表达式文本以"#"开始时，为"#"之后和可能的"|"之前的子字符串，可以为空字符串）。
 * @property extraPropertyNames 额外的属性名（表达式文本以"#"开始且之后包含"|"时，为"|"之后的按","分割的子字符串）。
 */
class CwtImageLocationExpression(
	expressionString: String,
	val placeholder: String? = null,
	val propertyName: String? = null,
	val extraPropertyNames: List<String>? = null
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtImageLocationExpression>() {
		val EmptyExpression = CwtImageLocationExpression("")
		
		override fun doResolve(expressionString: String): CwtImageLocationExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString.startsWith('#') -> {
					val pipeIndex = expressionString.indexOf('|', 1)
					if(pipeIndex == -1) {
						val propertyName = expressionString.substring(1)
						CwtImageLocationExpression(expressionString, null, propertyName)
					} else {
						val propertyName = expressionString.substring(1, pipeIndex)
						val extraPropertyNames = expressionString.substring(pipeIndex + 1)
							.splitToSequence(',').mapTo(SmartList()) { it.drop(1) }
						CwtImageLocationExpression(expressionString, null, propertyName, extraPropertyNames)
					}
				}
				else -> CwtImageLocationExpression(expressionString, expressionString, null, null)
			}
		}
	}
	
	operator fun component1() = placeholder
	
	operator fun component2() = propertyName
	
	operator fun component3() = extraPropertyNames
	
	//(key, file(s), frame)
	
	fun resolve(definitionName: String, definition: ParadoxDefinitionProperty, project: Project, frame: Int = 0): Tuple3<String, PsiFile?, Int>? {
		if(placeholder != null) {
			//假定这里的filePath以.dds结尾
			val filePath = buildString { for(c in placeholder) if(c == '$') append(definitionName) else append(c) }
			val selector = fileSelector().gameTypeFrom(definition).preferRootFrom(definition)
			val file = findFileByFilePath(filePath, project, selector = selector)?.toPsiFile<PsiFile>(project)
			return tupleOf(filePath, file, frame)
		} else if(propertyName != null) {
			//目前只接收类型为string的值
			//propertyName可以为空字符串，这时直接查找定义的字符串类型的值（如果存在）
			val value = definition.findProperty(propertyName)?.findPropertyValue<ParadoxScriptString>() ?: return null
			val frameToUse = when {
				frame != 0 -> frame
				extraPropertyNames == null || extraPropertyNames.isEmpty() -> 0
				else -> extraPropertyNames.mapAndFirst { propertyName ->
					definition.findProperty(propertyName)?.findPropertyValue<ParadoxScriptInt>()?.intValue ?: 0
				} ?: 0
			}
			while(true) {
				val resolved = CwtConfigHandler.resolveValue(value) { it.type in validValueTypes } ?: return null
				when {
					//由filePath解析为DDS文件
					resolved is PsiFile && resolved.fileType == DdsFileType -> {
						val filePath = resolved.fileInfo?.path?.path ?: return null
						val selector = fileSelector().gameTypeFrom(definition).preferRootFrom(definition)
						val file = findFileByFilePath(filePath, project, selector = selector)?.toPsiFile<PsiFile>(project)
						return tupleOf(filePath, file, frameToUse)
					}
					//由filePath解析为definition，这里也可能是sprite之外的definition
					resolved is ParadoxDefinitionProperty -> {
						val resolvedProject = resolved.project
						val resolvedDefinition = resolved
						val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
						val primaryImageConfigs = resolvedDefinitionInfo.primaryImageConfigs
						if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
						return primaryImageConfigs.mapAndFirst({ it?.second != null }) { primaryImageConfig ->
							val locationExpression = primaryImageConfig.locationExpression
							locationExpression.resolve(definitionName, resolvedDefinition, resolvedProject, frameToUse)
						}
					}
					else -> return null //解析失败或不支持
				}
			}
		} else {
			return null //不应该出现
		}
	}
	
	fun resolveAll(definitionName: String, definition: ParadoxDefinitionProperty, project: Project, frame: Int = 0): Tuple3<String, Set<PsiFile>, Int>? {
		if(placeholder != null) {
			//假定这里的filePath以.dds结尾
			val filePath = buildString { for(c in placeholder) if(c == '$') append(definitionName) else append(c) }
			val selector = fileSelector().gameTypeFrom(definition).preferRootFrom(definition)
			val files = findFilesByFilePath(filePath, project, selector = selector).mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
			return tupleOf(filePath, files, frame)
		} else if(propertyName != null && propertyName.isNotEmpty()) {
			//目前只接收类型为string的值
			val value = definition.findProperty(propertyName)?.findPropertyValue<ParadoxScriptString>() ?: return null
			val frameToUse = when {
				frame != 0 -> frame
				extraPropertyNames == null || extraPropertyNames.isEmpty() -> 0
				else -> extraPropertyNames.mapAndFirst { propertyName ->
					definition.findProperty(propertyName)?.findPropertyValue<ParadoxScriptInt>()?.intValue ?: 0
				} ?: 0
			}
			while(true) {
				val resolved = CwtConfigHandler.resolveValue(value) { it.type in validValueTypes } ?: return null
				when {
					//由filePath解析为DDS文件
					resolved is PsiFile && resolved.fileType == DdsFileType -> {
						val filePath = resolved.fileInfo?.path?.path ?: return null
						val selector = fileSelector().gameTypeFrom(definition).preferRootFrom(definition)
						val files = findFilesByFilePath(filePath, project, selector = selector).mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
						return tupleOf(filePath, files, frameToUse)
					}
					//由filePath解析为definition，这里也可能是sprite之外的definition
					resolved is ParadoxDefinitionProperty -> {
						val resolvedProject = resolved.project
						val resolvedDefinition = resolved
						val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
						val primaryImageConfigs = resolvedDefinitionInfo.primaryImageConfigs
						if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
						var resolvedFilePath: String? = null
						var resolvedSet: MutableSet<PsiFile>? = null
						for(primaryImageConfig in primaryImageConfigs) {
							val locationExpression = primaryImageConfig.locationExpression
							val (filePath, set) = locationExpression.resolveAll(resolvedDefinitionInfo.name, resolvedDefinition, resolvedProject, frameToUse) ?: continue
							if(resolvedFilePath == null) resolvedFilePath = filePath
							if(resolvedSet == null) resolvedSet = mutableSetOf()
							resolvedSet.addAll(set)
						}
						if(resolvedFilePath == null) return null
						return tupleOf(resolvedFilePath, resolvedSet ?: emptySet(), frameToUse)
					}
					else -> return null //解析失败或不支持
				}
			}
		} else {
			return null //不应该出现
		}
	}
}