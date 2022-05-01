@file:Suppress("unused")

package icu.windea.pls

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import java.util.*

//region Keys
val paradoxFileInfoKey = Key<ParadoxFileInfo>("paradoxFileInfo")
val cachedParadoxDefinitionInfoKey = Key<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
val cachedParadoxDefinitionPropertyInfoKey = Key<CachedValue<ParadoxDefinitionPropertyInfo>>("cachedParadoxDefinitionPropertyInfo")
val cachedParadoxLocalisationInfoKey = Key<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")
//endregion

//region Misc Extensions
fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getSettings() = ParadoxSettingsState.getInstance()

fun getInternalConfig() = ApplicationManager.getApplication().getService(InternalConfigProvider::class.java).configGroup

fun getCwtConfig(project: Project) = project.getService(CwtConfigProvider::class.java).configGroups

fun inferParadoxLocale() = when(System.getProperty("user.language")) {
	"zh" -> getInternalConfig().localeMap.getValue("l_simp_chinese")
	"en" -> getInternalConfig().localeMap.getValue("l_english")
	"pt" -> getInternalConfig().localeMap.getValue("l_braz_por")
	"fr" -> getInternalConfig().localeMap.getValue("l_french")
	"de" -> getInternalConfig().localeMap.getValue("l_german")
	"pl" -> getInternalConfig().localeMap.getValue("l_ponish")
	"ru" -> getInternalConfig().localeMap.getValue("l_russian")
	"es" -> getInternalConfig().localeMap.getValue("l_spanish")
	else -> getInternalConfig().localeMap.getValue("l_english")
}

/**得到指定元素之前的所有直接的注释的文本，作为文档注释，跳过空白。*/
fun getDocTextFromPreviousComment(element: PsiElement): String {
	//我们认为当前元素之前，之间没有空行的非行尾行注释，可以视为文档注释，但这并非文档注释的全部
	val lines = LinkedList<String>()
	var prevElement = element.prevSibling ?: element.parent?.prevSibling
	while(prevElement != null) {
		val text = prevElement.text
		if(prevElement !is PsiWhiteSpace) {
			if(!isPreviousComment(prevElement)) break
			val documentText = text.trimStart('#').trim().escapeXml()
			lines.addFirst(documentText)
		} else {
			if(text.containsBlankLine()) break
		}
		// 兼容comment在rootBlock之外的特殊情况
		prevElement = prevElement.prevSibling
	}
	return lines.joinToString("<br>")
}

/**
 * 判断指定的注释是否可认为是之前的注释。
 */
fun isPreviousComment(element: PsiElement): Boolean {
	val elementType = element.elementType
	return elementType == ParadoxLocalisationElementTypes.COMMENT || elementType == ParadoxScriptElementTypes.COMMENT
}

/**
 * 判断指定的定义子类型表达式是否匹配一组子类型。
 * @param expression 表达式。示例：`origin`, `!origin`
 */
fun matchesDefinitionSubtypeExpression(expression: String, subtypes: List<String>): Boolean {
	return when {
		expression.startsWith('!') -> expression.drop(1) !in subtypes
		else -> expression in subtypes
	}
}
//endregion

//region PsiElement Extensions
fun PsiElement.isQuoted(): Boolean {
	return firstLeafOrSelf.text.startsWith('"') //判断第一个叶子节点或本身的文本是否以引号开头
}

/**
 * 判断当前scriptValue是否是独立的value（不作为变量或属性的值）。
 */
fun ParadoxScriptValue.isLonelyValue(): Boolean {
	return this.parent is ParadoxScriptBlock // ParadoxScriptBlock | ParadoxScriptRootBlock
}   

val CwtProperty.configType: CwtConfigType? get() = doGetConfigType(this)

private fun doGetConfigType(element: CwtProperty): CwtConfigType? {
	val name = element.name
	return when {
		name.surroundsWith("type[", "]") -> CwtConfigType.Type
		name.surroundsWith("subtype[", "]") -> CwtConfigType.Subtype
		name.surroundsWith("enum[", "]") -> CwtConfigType.Enum
		name.surroundsWith("complex_enum[", "]") -> CwtConfigType.ComplexEnum
		name.surroundsWith("value[", "]") -> CwtConfigType.Value
		name.surroundsWith("single_alias[", "]") -> CwtConfigType.SingleAlias
		name.surroundsWith("alias[", "]") -> CwtConfigType.Alias
		else -> {
			val parentProperty = element.parent?.parent.castOrNull<CwtProperty>() ?: return null
			val parentName = parentProperty.name
			when {
				parentName == "links" -> CwtConfigType.Link
				parentName == "localisation_links" -> CwtConfigType.LocalisationLink
				parentName == "localisation_commands" -> CwtConfigType.LocalisationCommand
				parentName == "modifier_categories" -> CwtConfigType.ModifierCategory
				parentName == "modifiers" -> CwtConfigType.Modifier
				parentName == "scopes" -> CwtConfigType.Scope
				parentName == "scope_groups" -> CwtConfigType.ScopeGroup
				else -> null
			}
		}
	}
}

val CwtValue.configType: CwtConfigType? get() = doGetConfigType(this)

private fun doGetConfigType(element: CwtValue): CwtConfigType? {
	val parentProperty = element.parent?.parent.castOrNull<CwtProperty>() ?: return null
	val parentName = parentProperty.name
	return when {
		parentName.surroundsWith("enum[", "]") -> CwtConfigType.EnumValue
		parentName.surroundsWith("value[", "]") -> CwtConfigType.ValueValue
		else -> null
	}
}

val PsiElement.gameType: ParadoxGameType? get() = doGetGameType(this)

private fun doGetGameType(element: PsiElement): ParadoxGameType? {
	return element.containingFile.fileInfo?.gameType
}

val PsiElement.localeConfig: ParadoxLocaleConfig? get() = doGetLocale(this)

private fun doGetLocale(element: PsiElement): ParadoxLocaleConfig? {
	return when(val file = element.containingFile) {
		is ParadoxLocalisationFile -> file.locale?.localeConfig
		else -> inferParadoxLocale()
	}
}

val VirtualFile.fileInfo: ParadoxFileInfo? get() = this.getUserData(paradoxFileInfoKey)

val PsiFile.fileInfo: ParadoxFileInfo? get() = this.originalFile.virtualFile.fileInfo //使用原始文件

val PsiElement.fileInfo: ParadoxFileInfo? get() = this.containingFile.fileInfo

val ParadoxDefinitionProperty.definitionInfo: ParadoxDefinitionInfo? get() = doGetDefinitionInfo(this)

private fun doGetDefinitionInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxDefinitionInfoKey) {
		val value = resolveDefinitionInfo(element)
		CachedValueProvider.Result.create(value, element)
	}
}

private fun resolveDefinitionInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionInfo? {
	//NOTE 目前认为cwt文件中定义的definition的propertyPath的maxDepth是4（最多跳过3个rootKey）
	val propertyPath = ParadoxElementPath.resolveFromFile(element, 4) ?: return null
	val fileInfo = element.fileInfo ?: return null
	val path = fileInfo.path
	val gameType = fileInfo.gameType
	val rootKey = element.pathName //如果是文件名，不要包含扩展名
	val project = element.project
	val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
	return configGroup.resolveDefinitionInfo(element, rootKey, path, propertyPath)
}

val ParadoxDefinitionProperty.definitionElementInfo: ParadoxDefinitionPropertyInfo? get() = doGetDefinitionElementInfo(this)

private fun doGetDefinitionElementInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionPropertyInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxDefinitionPropertyInfoKey) {
		val value = resolveDefinitionElementInfo(element)
		CachedValueProvider.Result.create(value, element)
	}
}

private fun resolveDefinitionElementInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionPropertyInfo? {
	//NOTE 注意这里获得的definitionInfo可能会过时！因为对应的type和subtypes可能基于其他的definitionProperty
	val elementPath = ParadoxElementPath.resolveFromDefinition(element) ?: return null
	val definition = elementPath.rootPointer?.element ?: return null
	val definitionInfo = definition.definitionInfo ?: return null
	val scope = definitionInfo.subtypeConfigs.find { it.pushScope != null }?.pushScope
	val pointer = element.createPointer()
	val gameType = definitionInfo.gameType
	val project = element.project
	val configGroup = getCwtConfig(project).getValue(gameType)
	return ParadoxDefinitionPropertyInfo(elementPath, scope, gameType, definitionInfo, configGroup, pointer)
}

val ParadoxScriptProperty.propertyConfig: CwtPropertyConfig? get() = doGetPropertyConfig(this)

private fun doGetPropertyConfig(element: ParadoxScriptProperty): CwtPropertyConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val definitionElementInfo = element.definitionElementInfo ?: return null
	return definitionElementInfo.propertyConfig
}

val ParadoxScriptPropertyKey.propertyConfig: CwtPropertyConfig? get() = doGetPropertyConfig(this)

private fun doGetPropertyConfig(element: ParadoxScriptPropertyKey): CwtPropertyConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val property = element.parent.castOrNull<ParadoxScriptProperty>() ?: return null
	val definitionElementInfo = property.definitionElementInfo ?: return null
	return definitionElementInfo.propertyConfig
}

val ParadoxScriptValue.valueConfig: CwtValueConfig? get() = doGetValueConfig(this)

private fun doGetValueConfig(element: ParadoxScriptValue): CwtValueConfig? {
	//NOTE 暂时不使用缓存，因为很容易就会过时
	val parent = element.parent
	when(parent) {
		//如果value是property的value
		is ParadoxScriptPropertyValue -> {
			val property = parent.parent as? ParadoxScriptProperty ?: return null
			val definitionElementInfo = property.definitionElementInfo ?: return null
			return definitionElementInfo.matchedPropertyConfig?.valueConfig
				?: definitionElementInfo.propertyConfigs.firstOrNull()?.valueConfig //NOTE 如果已经匹配propertyConfig但是无法匹配valueConfig，使用第一个
		}
		//如果value是block中的value
		is ParadoxScriptBlock -> {
			val property = parent.parent?.parent as? ParadoxScriptProperty ?: return null
			val definitionElementInfo = property.definitionElementInfo ?: return null
			val childValueConfigs = definitionElementInfo.childValueConfigs
			if(childValueConfigs.isEmpty()) return null
			val gameType = definitionElementInfo.gameType
			val configGroup = getCwtConfig(element.project).getValue(gameType)
			return childValueConfigs.find { matchesValue(it.valueExpression, element, configGroup) }
				?: childValueConfigs.firstOrNull() // NOTE 如果已经匹配propertyConfig但是无法匹配valueConfig，使用第一个
		}
		else -> return null
	}
}

val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo? get() = doGetLocalisationInfo(this)

private fun doGetLocalisationInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
	return CachedValuesManager.getCachedValue(element, cachedParadoxLocalisationInfoKey) {
		val value = resolveLocalisationInfo(element)
		CachedValueProvider.Result.create(value, element)
	}
}

private fun resolveLocalisationInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
	val name = element.name
	val type = ParadoxLocalisationCategory.resolve(element) ?: return null
	return ParadoxLocalisationInfo(name, type)
}

val ParadoxScriptFile.eventNamespace: String?
	get() {
		val fileInfo = this.fileInfo ?: return null
		if(!fileInfo.path.parent.startsWith("events")) return null
		//必须是第一个属性且名为"namespace"，忽略大小写
		val block = block ?: return null
		val firstProperty = PsiTreeUtil.findChildOfType(block, ParadoxScriptProperty::class.java)
		if(firstProperty == null || !firstProperty.name.equals("namespace", true)) return null
		return firstProperty.value
	}

val ParadoxLocalisationLocale.localeConfig: ParadoxLocaleConfig?
	get() {
		return getInternalConfig().localeMap[name]
	}

val ParadoxLocalisationPropertyReference.colorConfig: ParadoxColorConfig?
	get() {
		val colorId = this.propertyReferenceParameter?.text?.firstOrNull() //TODO 需要确认
		if(colorId != null && colorId.isUpperCase()) {
			return getInternalConfig().colorMap[colorId.toString()]
		}
		return null
	}

val ParadoxLocalisationSequentialNumber.sequentialNumberConfig: ParadoxSequentialNumberConfig?
	get() = getInternalConfig().sequentialNumberMap[name]

val ParadoxLocalisationColorfulText.colorConfig: ParadoxColorConfig?
	get() = getInternalConfig().colorMap[name]
//endregion

//region Type Extensions
fun ParadoxScriptValue.inferValueType(): String? {
	return when(this) {
		is ParadoxScriptBoolean -> "boolean"
		is ParadoxScriptInt -> "int"
		is ParadoxScriptFloat -> "float"
		is ParadoxScriptString -> "string"
		is ParadoxScriptColor -> "color"
		is ParadoxScriptBlock -> "block"
		is ParadoxScriptCode -> "code"
		else -> null
	}
}

fun ParadoxScriptValue.isNullLike(): Boolean {
	return when {
		this is ParadoxScriptBlock -> this.isEmpty
		this is ParadoxScriptString -> this.textMatches("")
		this is ParadoxScriptNumber -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
		this is ParadoxScriptBoolean -> this.textMatches("no")
		else -> false
	}
}

fun ParadoxScriptBlock.isAlwaysYes(): Boolean {
	return this.isObject && this.propertyList.singleOrNull()?.let { it.name == "always" && it.value == "yes" } ?: false
}
//endregion

//region Find From PsiElement Extensiosn
/**
 * 得到上一级definitionProperty，可能为自身，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionProperty(): ParadoxDefinitionProperty? {
	var current: PsiElement = this
	do {
		if(current is ParadoxDefinitionProperty) {
			return current
		}
		current = current.parent ?: break
	} while(current !is PsiFile)
	return null
}

/**
 * 得到上一级definitionProperty，跳过正在填写的，可能为自身，可能为null，可能也是definition。
 */
fun PsiElement.findParentDefinitionPropertySkipThis(): ParadoxDefinitionProperty? {
	var current: PsiElement = this
	do {
		if(current is ParadoxScriptRootBlock) {
			return (current.parent ?: break) as ParadoxDefinitionProperty
		} else if(current is ParadoxScriptBlock) {
			return (current.parent.parent ?: break) as ParadoxDefinitionProperty
		}
		current = current.parent ?: break
	} while(current !is PsiFile)
	return null
}
//endregion

//region Find Extensions
/**
 * 根据名字在指定文件中递归查找脚本变量（scriptedVariable）。（不一定定义在顶层）
 * @param name 变量的名字，以"@"开始。
 * @param context 需要从哪个[PsiElement]开始，在整个文件内，递归向上查找。
 */
fun findScriptVariableInFile(name: String, context: PsiElement): ParadoxScriptVariable? {
	//在整个脚本文件中递归向上查找，返回查找到的第一个
	var result: ParadoxScriptVariable? = null
	var current = context
	while(current !is PsiFile) { //NOTE 目前不检查是否是paradoxScriptFile
		var prevSibling = current.prevSibling
		while(prevSibling != null) {
			if(prevSibling is ParadoxScriptVariable && prevSibling.name == name) {
				result = prevSibling
				break
			}
			prevSibling = prevSibling.prevSibling
		}
		current = current.parent ?: break
	}
	return result
}

/**
 * 根据名字在指定文件中递归查找所有的脚本变量（scriptedVariable）。（不一定定义在顶层）
 * @param name 变量的名字，以"@"开始。
 * @param context 需要从哪个[PsiElement]开始，在整个文件内，向上查找。
 */
fun findScriptVariablesInFile(name: String, context: PsiElement): List<ParadoxScriptVariable> {
	//在整个脚本文件中递归向上查找，返回查找到的所有结果，按查找到的顺序排序
	var result: MutableList<ParadoxScriptVariable>? = null
	var current = context
	while(current !is PsiFile) { //NOTE 目前不检查是否是paradoxScriptFile
		var prevSibling = current.prevSibling
		while(prevSibling != null) {
			if(prevSibling is ParadoxScriptVariable && prevSibling.name == name) {
				if(result == null) result = SmartList()
				result.add(prevSibling)
				break
			}
			prevSibling = prevSibling.prevSibling
		}
		current = current.parent ?: break
	}
	if(result == null) return emptyList()
	return result
}

/**
 * 在当前文件中递归查找所有的脚本变量（scriptedVariable）。（不一定定义在顶层）
 * @param distinct 是否需要对相同名字的变量进行去重。默认为`false`。
 */
fun findAllScriptVariablesInFile(context: PsiElement, distinct: Boolean = false): List<ParadoxScriptVariable> {
	//在整个脚本文件中递归向上查找，返回查找到的所有结果，按查找到的顺序排序
	var result: MutableList<ParadoxScriptVariable>? = null
	val namesToDistinct = if(distinct) mutableSetOf<String>() else null
	var current = context
	while(current !is PsiFile) { //NOTE 目前不检查是否是paradoxScriptFile
		var prevSibling = current.prevSibling
		while(prevSibling != null) {
			if(prevSibling is ParadoxScriptVariable) {
				if(namesToDistinct == null || namesToDistinct.add(prevSibling.name)) {
					if(result == null) result = SmartList()
					result.add(prevSibling)
				}
			}
			prevSibling = prevSibling.prevSibling
		}
		current = current.parent ?: break
	}
	if(result == null) return emptyList()
	return result
}

/**
 * 基于脚本变量名字索引，根据名字查判断是否存在脚本变量（scriptedVariable）。
 * @param name 变量的名字，以"@"开始。
 */
fun existsScriptVariable(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): Boolean {
	return ParadoxScriptVariableNameIndex.exists(name, project, scope)
}

/**
 * 基于脚本变量名字索引，根据名字查找脚本变量（scriptedVariable）。
 * @param name 变量的名字，以"@"开始。
 */
fun findScriptVariable(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxScriptVariable? {
	return ParadoxScriptVariableNameIndex.findOne(name, project, scope, !getSettings().preferOverridden)
}

/**
 * 基于脚本变量名字索引，根据名字查找所有的脚本变量（scriptedVariable）。
 * @param name 变量的名字，以"@"开始。
 */
fun findScriptVariables(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.findAll(name, project, scope)
}

/**
 * 基于脚本变量名字索引，查找所有的脚本变量（scriptedVariable）。
 * @param distinct 是否需要对相同名字的变量进行去重。默认为`false`。
 */
fun findAllScriptVariables(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	distinct: Boolean = false
): List<ParadoxScriptVariable> {
	return ParadoxScriptVariableNameIndex.findAll(project, scope, distinct)
}

/**
 * 基于定义名字索引，根据名字、类型表达式判断是否存在脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun hasDefinition(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): Boolean {
	return ParadoxDefinitionNameIndex.exists(name, typeExpression, project, scope)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinition(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxDefinitionProperty? {
	return ParadoxDefinitionNameIndex.findOne(name, typeExpression, project, scope, !getSettings().preferOverridden)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinitions(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxDefinitionProperty> {
	return ParadoxDefinitionNameIndex.findAll(name, typeExpression, project, scope)
}

/**
 * 基于定义名字索引，根据类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 * @param distinct 是否需要对同一基本类型而相同名字的定义进行去重。默认为`false`。
 */
fun findAllDefinitions(
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	distinct: Boolean = false
): List<ParadoxDefinitionProperty> {
	return ParadoxDefinitionNameIndex.findAll(typeExpression, project, scope, distinct)
}

/**
 * 基于定义类型索引，根据名字、类型表达式判断是否存在脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun existsDefinitionByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): Boolean {
	return ParadoxDefinitionTypeIndex.exists(name, typeExpression, project, scope)
}

/**
 * 基于定义类型索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinitionByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): ParadoxDefinitionProperty? {
	return ParadoxDefinitionTypeIndex.findOne(name, typeExpression, project, scope, !getSettings().preferOverridden)
}

/**
 * 基于定义类型索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinitionsByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxDefinitionProperty> {
	return ParadoxDefinitionTypeIndex.findAll(name, typeExpression, project, scope)
}

/**
 * 基于定义类型索引，根据类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 * @param distinct 是否需要对同一基本类型而相同名字的定义进行去重。默认为`false`。
 */
fun findDefinitionsByType(
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	distinct: Boolean = false
): List<ParadoxDefinitionProperty> {
	return ParadoxDefinitionTypeIndex.findAll(typeExpression, project, scope, distinct)
}

//NOTE 查找定义时不需要预先过滤结果
///**
// * 基于定义类型索引，根据关键字和类型表达式查找所有的脚本文件的定义（definition）。
// * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
// */
//fun findDefinitionsByKeywordByType(
//	keyword: String,
//	typeExpression: String,
//	project: Project,
//	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
//): List<ParadoxDefinitionProperty> {
//	return ParadoxDefinitionTypeIndex.findAllByKeyword(keyword, typeExpression, project, scope, getSettings().maxCompleteSize)
//}

/**
 * 基于本地化名字索引，根据名字、语言区域判断是否存在本地化（localisation）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化。
 */
fun hasLocalisation(
	name: String,
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
): Boolean {
	return ParadoxLocalisationNameIndex.exists(name, localeConfig, project, scope)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找本地化（localisation）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化。
 * @param hasDefault 如果没有查找到对应语言区域的本地化，是否需要改为查找任意语言的本地化。默认为`false`。
 */
fun findLocalisation(
	name: String,
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): ParadoxLocalisationProperty? {
	return ParadoxLocalisationNameIndex.findOne(name, localeConfig, project, scope, hasDefault, !getSettings().preferOverridden)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找所有的本地化（localisation）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化，否则将用户的本地化放到该组的最前面。
 * @param hasDefault 如果没有查找到对应语言区域的本地化，是否需要改为查找任意语言的本地化。默认为`false`。
 * @see inferParadoxLocale
 */
fun findLocalisations(
	name: String,
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findAll(name, localeConfig, project, scope, hasDefault)
}

/**
 * 基于本地化名字索引，根据语言区域查找所有的本地化（localisation）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化，否则将用户的本地化放到该组的最前面。
 * @param hasDefault 如果没有查找到对应语言区域的本地化，是否需要改为查找任意语言的本地化。默认为`false`。
 * @param distinct 是否需要对相同键的本地化进行去重。默认为`false`。
 * @see inferParadoxLocale
 */
fun findAllLocalisations(
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false,
	distinct: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findAll(localeConfig, project, scope, hasDefault, distinct)
}

/**
 * 基于本地化名字索引，根据关键字查找所有的本地化（localisation）。
 * * 如果名字包含关键字（不忽略大小写），则放入结果。
 * * 返回的结果有数量限制。
 */
@Deprecated("Consider for removal.")
fun findLocalisationsByKeyword(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.findAllByKeyword(keyword, project, scope, getSettings().maxCompleteSize)
}

/**
 * 基于本地化名字索引，根据名字、语言区域判断是否存在同步本地化（localisation_synced）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化。
 */
fun hasSyncedLocalisation(
	name: String,
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
): Boolean {
	return ParadoxSyncedLocalisationNameIndex.exists(name, localeConfig, project, scope)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找同步本地化（localisation_synced）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化。
 * @param hasDefault 如果没有查找到对应语言区域的本地化，是否需要改为查找任意语言的本地化。默认为`false`。
 */
fun findSyncedLocalisation(
	name: String,
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): ParadoxLocalisationProperty? {
	return ParadoxSyncedLocalisationNameIndex.getOne(name, localeConfig, project, scope, hasDefault, !getSettings().preferOverridden)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找所有的同步本地化（localisation_synced）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化，否则将用户的本地化放到该组的最前面。
 * @param hasDefault 如果没有查找到对应语言区域的本地化，是否需要改为查找任意语言的本地化。默认为`false`。。
 * @see inferParadoxLocale
 */
fun findSyncedLocalisations(
	name: String,
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.findAll(name, localeConfig, project, scope, hasDefault)
}

/**
 * 基于同步本地化名字索引，根据语言区域查找所有的同步本地化（localisation_synced）。
 * @param localeConfig 如果不为`null`，则仅查找对应语言区域的本地化，否则将用户的本地化放到该组的最前面。
 * @param hasDefault 如果没有查找到对应语言区域的本地化，是否需要改为查找任意语言的本地化。默认为`false`。
 * @param distinct 是否需要对相同键的本地化进行去重。默认为`false`。
 * @see inferParadoxLocale
 */
fun findAllSyncedLocalisations(
	localeConfig: ParadoxLocaleConfig?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	hasDefault: Boolean = false,
	distinct: Boolean = false
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.findAll(localeConfig, project, scope, hasDefault, distinct)
}

/**
 * 基于同步本地化名字索引，根据关键字查找所有的同步本地化（localisation_synced）。
 * * 如果名字包含关键字（不忽略大小写），则放入结果。
 * * 返回的结果有数量限制。
 */
@Deprecated("Consider for removal.")
fun findSyncedLocalisationsByKeyword(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
): List<ParadoxLocalisationProperty> {
	return ParadoxSyncedLocalisationNameIndex.findAllByKeyword(keyword, project, scope, getSettings().maxCompleteSize)
}

/**
 * 基于文件索引，根据相对于游戏或模组目录的文件路径查找匹配的文件（非目录）。
 * @param expressionType 使用何种文件路径表达式类型。默认使用精确路径。
 * @param ignoreCase 匹配路径时是否忽略大小写。 默认为`true`。
 */
fun findFileByFilePath(
	filePath: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	expressionType: CwtFilePathExpressionType = CwtFilePathExpressionType.Exact,
	ignoreCase: Boolean = true
): VirtualFile? {
	return ParadoxFilePathIndex.findOne(filePath, scope, expressionType, ignoreCase)
}

/**
 * 基于文件索引，根据相对于游戏或模组目录的文件路径查找所有匹配的文件（非目录）。
 * @param expressionType 使用何种文件路径表达式类型。默认使用精确路径。
 * @param ignoreCase 匹配路径时是否忽略大小写。默认为`true`。
 * @param distinct 是否需要对相同路径的文件进行去重。默认为`false`。
 */
fun findFilesByFilePath(
	filePath: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	expressionType: CwtFilePathExpressionType = CwtFilePathExpressionType.Exact,
	ignoreCase: Boolean = true,
	distinct: Boolean = false
): Set<VirtualFile> {
	return ParadoxFilePathIndex.findAll(filePath, scope, expressionType, ignoreCase, distinct)
}

/**
 * 基于文件索引，根据相查找所有匹配的（位于游戏或模组根目录或其子目录中的）文件（非目录）。
 * @param ignoreCase 匹配路径时是否忽略大小写。默认为`true`。
 * @param distinct 是否需要对相同路径的文件进行去重。默认为`false`。
 */
fun findAllFilesByFilePath(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	ignoreCase: Boolean = true,
	distinct: Boolean = false
): Set<VirtualFile> {
	return ParadoxFilePathIndex.findAll(project, scope, ignoreCase, distinct)
}

/**
 * @param location 参见[ParadoxRelatedLocalisationInfo.location]。
 */
fun findLocalisationByLocation(location: String, localeConfig: ParadoxLocaleConfig?, project: Project): ParadoxLocalisationProperty? {
	return findLocalisation(location, localeConfig, project, hasDefault = true)
}

/**
 * @param location 参见[ParadoxRelatedLocalisationInfo.location]。
 */
fun findLocalisationsByLocation(location: String, localeConfig: ParadoxLocaleConfig?, project: Project): List<ParadoxLocalisationProperty> {
	return findLocalisations(location, localeConfig, project, hasDefault = true)
}

/**
 * @param location 参见[ParadoxRelatedLocalisationInfo.location]。
 */
fun findPictureByLocation(location: String, project: Project): PsiElement? /* ParadoxDefinitionProperty? | PsiFile? */ {
	//根据是否以dds后缀名结尾，判断location是filepath还是definitionKey
	if(location.endsWith(".dds", true)) {
		return findFileByFilePath(location, project)?.toPsiFile(project)
	} else {
		return findDefinitionByType(location, "sprite|spriteType", project)
	}
}

/**
 * @param location 参见[ParadoxRelatedLocalisationInfo.location]。
 */
fun findPicturesByLocation(location: String, project: Project): List<PsiElement> /* ParadoxDefinitionProperty? | PsiFile? */ {
	//根据是否以dds后缀名结尾，判断location是filepath还是definitionKey
	if(location.endsWith(".dds", true)) {
		return findFilesByFilePath(location, project).mapNotNull { it.toPsiFile(project) }
	} else {
		return findDefinitionsByType(location, "sprite|spriteType", project)
	}
}
//endregion

//region Psi Link Extensions

//com.jetbrains.python.documentation.PyDocumentationLink

private const val cwtLinkPrefix = "cwt#"
private const val definitionLinkPrefix = "def#"
private const val localisationLinkPrefix = "loc#"

fun resolveLink(linkWithPrefix: String, context: PsiElement): PsiElement? {
	return when {
		linkWithPrefix.startsWith(cwtLinkPrefix) -> resolveCwtLink(linkWithPrefix.drop(cwtLinkPrefix.length), context)
		linkWithPrefix.startsWith(definitionLinkPrefix) -> resolveDefinitionLink(linkWithPrefix.drop(definitionLinkPrefix.length), context)
		linkWithPrefix.startsWith(localisationLinkPrefix) -> linkWithoutPrefix(linkWithPrefix.drop(localisationLinkPrefix.length), context)
		else -> null
	}
}

//stellaris.types.building
//stellaris.types.civic_or_origin.civic
private fun resolveCwtLink(linkWithoutPrefix: String, context: PsiElement): CwtProperty? {
	return runCatching {
		val project = context.project
		val tokens = linkWithoutPrefix.split('.')
		val gameType = tokens[0]
		val configType = tokens[1]
		when(configType) {
			"types" -> {
				val name = tokens.getOrNull(2)
				val subtypeName = tokens.getOrNull(3)
				return when {
					name == null -> null
					subtypeName == null -> getCwtConfig(project).getValue(gameType).types.getValue(name)
						.pointer.element
					else -> getCwtConfig(project).getValue(gameType).types.getValue(name)
						.subtypes.getValue(subtypeName).pointer.element
				}
			}
			"scopes" -> {
				val name = tokens.getOrNull(2) ?: return null
				return getCwtConfig(project).getValue(gameType).scopeAliasMap.getValue(name).pointer.element
			}
			else -> null
		}
	}.getOrNull()
}

//ethos.ethic_authoritarian
//job.head_researcher
//civic_or_origin.origin.origin_default
private fun resolveDefinitionLink(linkWithoutPrefix: String, context: PsiElement): ParadoxDefinitionProperty? {
	return runCatching {
		val lastDotIndex = linkWithoutPrefix.lastIndexOf('.')
		val type = linkWithoutPrefix.substring(0, lastDotIndex)
		val name = linkWithoutPrefix.substring(lastDotIndex + 1)
		findDefinitionByType(name, type, context.project)
	}.getOrNull()
}

//NAME
//KEY
private fun linkWithoutPrefix(link: String, context: PsiElement): ParadoxLocalisationProperty? {
	return runCatching {
		val token = link
		return findLocalisation(token, context.localeConfig, context.project, hasDefault = true)
	}.getOrNull()
}
//endregion

//region Documentation Extensions
fun StringBuilder.appendIf(condition: Boolean, text: String): StringBuilder {
	if(condition) append(text)
	return this
}

fun StringBuilder.appendUnresolvedLink(label: String): StringBuilder {
	append(label) //直接显示对应的标签文本
	return this
}

fun StringBuilder.appendFilePathLink(filePath: String, context: PsiElement): StringBuilder {
	val rootPath = context.fileInfo?.rootPath
	val absPath = rootPath?.resolve(filePath)?.normalize()?.toString()
	//如果可以定位到绝对路径，则显示链接
	if(absPath != null) append("<a href=\"").append("file://").append(absPath).append("\">").append(filePath).append("</a>")
	//否则显示未解析的链接
	return appendUnresolvedLink(filePath)
}

fun StringBuilder.appendPsiLink(refText: String, label: String, plainLink: Boolean = true): StringBuilder {
	DocumentationManagerUtil.createHyperlink(this, refText, label, plainLink)
	return this
}

fun StringBuilder.appendCwtLink(name: String, link: String, context: PsiElement?): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(unresolvedEscapedString)
	//如果可以被解析为CWT规则，则显示链接（context传null时总是显示）
	if(context == null || resolveCwtLink(link, context) != null) return appendPsiLink("$cwtLinkPrefix$link", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendDefinitionLink(name: String, typeExpression: String, context: PsiElement): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(unresolvedEscapedString)
	//如果可以被解析为定义，则显示链接
	if(hasDefinition(name, null, context.project)) return appendPsiLink("$definitionLinkPrefix.$typeExpression.$name", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendLocalisationLink(name: String, context: PsiElement): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(unresolvedEscapedString)
	//如果可以被解析为本地化，则显示链接
	if(hasLocalisation(name, null, context.project)) return appendPsiLink("$localisationLinkPrefix$name", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendImgTag(url: String, local: Boolean = true): StringBuilder {
	return append("<img src=\"").appendIf(local, "file:/").append(url).append("\" />")
}

fun StringBuilder.appendImgTag(url: String, fontSize: FontSize, local: Boolean = true): StringBuilder {
	return append("<img src=\"").appendIf(local, "file:/").append(url)
		.append("\" width=\"").append(fontSize.size).append("\" height=\"").append(fontSize).append("\" />")
}

fun StringBuilder.appendFileInfo(fileInfo: ParadoxFileInfo): StringBuilder {
	return append("[").append(fileInfo.path).append("]")
}

fun StringBuilder.appendBr(): StringBuilder {
	return append("<br>")
}
//endregion

//region Inline Extensions
//@Suppress("NOTHING_TO_INLINE")
//inline fun ParadoxScriptProperty.resolveIconUrl(defaultToUnknown: Boolean = true): String {
//	return ParadoxDdsUrlResolver.resolveBySprite(this, defaultToUnknown)
//}

//@Suppress("NOTHING_TO_INLINE")
//inline fun VirtualFile.resolveIconUrl(defaultToUnknown: Boolean = true): String {
//	return ParadoxDdsUrlResolver.resolveByFile(this, defaultToUnknown)
//}

//@Suppress("NOTHING_TO_INLINE")
//inline fun PsiFile.resolveIconUrl(defaultToUnknown: Boolean = true): String {
//	return ParadoxDdsUrlResolver.resolveByFile(this, defaultToUnknown)
//}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.renderText(): String {
	return ParadoxLocalisationTextRenderer.render(this)
}

//@Suppress("NOTHING_TO_INLINE")
//inline fun ParadoxLocalisationProperty.renderTextTo(buffer: StringBuilder) {
//	ParadoxLocalisationTextRenderer.renderTo(this, buffer)
//}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationProperty.extractText(): String {
	return ParadoxLocalisationTextExtractor.extract(this)
}

//@Suppress("NOTHING_TO_INLINE")
//inline fun ParadoxLocalisationProperty.extractTextTo(buffer: StringBuilder) {
//	ParadoxLocalisationTextExtractor.extractTo(this, buffer)
//}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxScriptFile.resolveData(): List<Any> {
	return ParadoxScriptDataResolver.resolve(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxLocalisationFile.resolveData(): Map<String, String> {
	return ParadoxLocalisationDataResolver.resolve(this)
}
//endregion