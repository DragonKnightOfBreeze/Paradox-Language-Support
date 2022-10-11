@file:Suppress("unused")

package icu.windea.pls

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*
import java.lang.Integer.*

//region Global Caches
val threadLocalTextEditorContainer = ThreadLocal<TextEditor?>()
//endregion

//region Misc Extensions
fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

fun getSettings() = service<ParadoxSettings>().state

fun getInternalConfig(project: Project? = null) = (project ?: getTheOnlyOpenOrDefaultProject()).service<InternalConfigProvider>().configGroup

fun getCwtConfig(project: Project) = project.service<CwtConfigProvider>().configGroups

fun preferredParadoxLocale(): ParadoxLocaleConfig? {
	val primaryLocale = getSettings().localisationPreferredLocale.orEmpty()
	if(primaryLocale.isNotEmpty() && primaryLocale != "auto") {
		val usedLocale = InternalConfigHandler.getLocale(primaryLocale)
		if(usedLocale != null) return usedLocale
	}
	//基于OS得到对应的语言区域，或者使用英文
	val userLanguage = System.getProperty("user.language")
	return InternalConfigHandler.getLocaleByCode(userLanguage) ?: InternalConfigHandler.getLocaleByCode("en")
}

/**
 * 判断指定的定义子类型表达式是否匹配一组子类型。
 * @param expression 表达式。示例：`origin`, `!origin`
 */
fun matchesDefinitionSubtypeExpression(expression: String, subtypes: List<String>): Boolean {
	return when {
		expression.startsWith('!') -> subtypes.isEmpty() || expression.drop(1) !in subtypes
		else -> subtypes.isNotEmpty() && expression in subtypes
	}
}

/**
 * 比较游戏版本。允许通配符，如："3.3.*"
 */
infix fun String.compareGameVersion(otherVersion: String): Int {
	val versionSnippets = this.split('.')
	val otherVersionSnippets = otherVersion.split('.')
	val minSnippetSize = min(versionSnippets.size, otherVersionSnippets.size)
	for(i in 0 until minSnippetSize) {
		val versionSnippet = versionSnippets[i]
		val otherVersionSnippet = otherVersionSnippets[i]
		if(versionSnippet == otherVersionSnippet || versionSnippet == "*" || otherVersion == "*") continue
		return versionSnippet.compareTo(otherVersionSnippet)
	}
	return 0
}
//endregion

//region VFS Extensions
/**
 * 当前[VirtualFile]的内容文件。（缓存且仍然存在的文件，首个子文件，生成的子文件，或者自身）
 */
var VirtualFile.contentFile
	get() = getUserData(PlsKeys.contentFileKey)?.takeIf { it.exists() }
		?: this.children.firstOrNull()
		?: ParadoxFileLocator.getGeneratedFileName(this)
		?: this
	set(value) = putUserData(PlsKeys.contentFileKey, value)
//endregion

//region PsiElement Extensions
fun PsiElement.isQuoted(): Boolean {
	return firstLeafOrSelf().text.startsWith('"') || lastLeafOrSelf().text.endsWith('"')
}

fun CwtValue.isLonely(): Boolean {
	val parent = this.parent
	return parent is ICwtBlock && parent.parent is CwtProperty
}

fun ParadoxScriptValue.isLonely(): Boolean {
	val parent = this.parent
	return parent is IParadoxScriptBlock
}

val PsiElement.localeConfig: ParadoxLocaleConfig?
	get() {
		if(this.language == ParadoxLocalisationLanguage) {
			var current = this
			while(true) {
				when {
					current is ParadoxLocalisationFile -> return current.locale?.localeConfig
					current is ParadoxLocalisationPropertyList -> return current.locale.localeConfig
					current is ParadoxLocalisationLocale -> return current.localeConfig
					current is PsiFile -> return preferredParadoxLocale() //不期望的结果
				}
				current = current.parent ?: break
			}
			return current.containingFile.localeConfig
		} else {
			return preferredParadoxLocale()
		}
	}

//注意：不要更改直接调用CachedValuesManager.getCachedValue(...)的那个顶级方法（静态方法）的方法声明，IDE内部会进行检查
//如果不同的输入参数得到了相同的输出值，或者相同的输入参数得到了不同的输出值，IDE都会报错

val VirtualFile.fileInfo: ParadoxFileInfo? get() = this.getUserDataOnValid(PlsKeys.fileInfoKey) { it.isValid }

val PsiFile.fileInfo: ParadoxFileInfo? get() = this.originalFile.virtualFile?.fileInfo //需要使用原始文件

val PsiElement.fileInfo: ParadoxFileInfo? get() = this.containingFile?.fileInfo

val ParadoxDefinitionProperty.definitionInfo: ParadoxDefinitionInfo?
	get() = ParadoxDefinitionInfoHandler.get(this)

val ParadoxDefinitionProperty.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)
val ParadoxScriptPropertyKey.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)
val ParadoxScriptValue.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)
val ParadoxScriptExpressionElement.definitionElementInfo: ParadoxDefinitionElementInfo?
	get() = ParadoxDefinitionElementInfoHandler.get(this)

val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo?
	get() = ParadoxLocalisationInfoHandler.get(this)

fun ParadoxScriptProperty.getPropertyConfig(allowDefinitionSelf: Boolean = false, orFirst: Boolean = true): CwtPropertyConfig? {
	val element = this
	val definitionElementInfo = element.definitionElementInfo ?: return null
	if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return null
	//如果无法匹配value，则取第一个
	return definitionElementInfo.matchedPropertyConfig
		?: orFirst.ifTrue { definitionElementInfo.propertyConfigs.firstOrNull() }
}

fun ParadoxScriptExpressionElement.getConfig(): CwtKvConfig<*>? {
	return when(this) {
		is ParadoxScriptPropertyKey -> getPropertyConfig()
		is ParadoxScriptString -> getValueConfig()
		else -> null
	}
}

fun ParadoxScriptPropertyKey.getPropertyConfig(allowDefinitionSelf: Boolean = false, orFirst: Boolean = true): CwtPropertyConfig? {
	val element = this.parent.castOrNull<ParadoxScriptProperty>() ?: return null
	val definitionElementInfo = element.definitionElementInfo ?: return null
	if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return null
	//如果无法匹配value，则取第一个
	return definitionElementInfo.matchedPropertyConfig
		?: orFirst.ifTrue { definitionElementInfo.propertyConfigs.firstOrNull() }
}

fun ParadoxScriptValue.getValueConfig(allowDefinitionSelf: Boolean = true, orSingle: Boolean = true): CwtValueConfig? {
	val element = this
	val parent = element.parent
	when(parent) {
		//如果value是property的value
		is ParadoxScriptPropertyValue -> {
			val property = parent.parent as? ParadoxScriptProperty ?: return null
			val definitionElementInfo = property.definitionElementInfo ?: return null
			if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return null
			//如果无法匹配value，则取唯一的那个
			return definitionElementInfo.matchedPropertyConfig?.valueConfig
				?: orSingle.ifTrue { definitionElementInfo.propertyConfigs.singleOrNull()?.valueConfig }
		}
		//如果value是block中的value
		is ParadoxScriptBlock -> {
			val property = parent.parent?.parent as? ParadoxScriptProperty ?: return null
			val definitionElementInfo = property.definitionElementInfo ?: return null
			val childValueConfigs = definitionElementInfo.childValueConfigs
			if(childValueConfigs.isEmpty()) return null
			val gameType = definitionElementInfo.gameType
			val configGroup = getCwtConfig(element.project).getValue(gameType)
			//如果无法匹配value，则取唯一的那个
			return childValueConfigs.find { CwtConfigHandler.matchesValue(it.valueExpression, element, configGroup) }
				?: orSingle.ifTrue { childValueConfigs.singleOrNull() }
		}
		
		else -> return null
	}
}

val ParadoxLocalisationLocale.localeConfig: ParadoxLocaleConfig? get() = doGetLocaleConfig(name, project)

private fun doGetLocaleConfig(id: String, project: Project): ParadoxLocaleConfig? {
	return InternalConfigHandler.getLocale(id, project)
}

val ParadoxLocalisationPropertyReference.colorConfig: ParadoxTextColorConfig?
	get() {
		//大写或小写字母，不限定位置
		val colorId = this.propertyReferenceParameter?.text?.find { it.isExactLetter() } ?: return null
		val gameType = this.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType
			?: return null
		return doGetColorConfig(colorId.toString(), gameType, project)
	}

val ParadoxLocalisationColorfulText.colorConfig: ParadoxTextColorConfig?
	get() {
		val colorId = this.name ?: return null
		val gameType = this.fileInfo?.rootInfo?.gameType //这里还是基于fileInfo获取gameType 
			?: return null
		return doGetColorConfig(colorId, gameType, project)
	}

private fun doGetColorConfig(id: String, gameType: ParadoxGameType, project: Project): ParadoxTextColorConfig? {
	return DefinitionConfigHandler.getTextColorConfig(id, gameType, project)
}

fun ParadoxScriptValue.isNullLike(): Boolean {
	return when {
		this is ParadoxScriptBlock -> this.isEmpty
		this is ParadoxScriptString -> this.textMatches("")
		this is ParadoxScriptInt -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
		this is ParadoxScriptFloat -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
		this is ParadoxScriptBoolean -> this.textMatches("no")
		else -> false
	}
}
//endregion

//region Find Extensions
/**
 * 根据名字在指定文件中递归查找封装变量（scriptedVariable）。（不一定声明在顶层）
 * @param name 变量的名字，以"@"开始。
 * @param context 需要从哪个[PsiElement]开始，在整个文件内，递归向上查找。
 */
fun findScriptedVariableInFile(name: String, context: PsiElement): ParadoxScriptVariable? {
	//在整个脚本文件中递归向上向前查找，返回查找到的第一个
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
 * 根据名字在指定文件中递归查找所有的封装变量（scriptedVariable）。（不一定声明在顶层）
 * @param name 变量的名字，以"@"开始。
 * @param context 需要从哪个[PsiElement]开始，在整个文件内，向上查找。
 */
fun findScriptedVariablesInFile(name: String, context: PsiElement): Set<ParadoxScriptVariable> {
	//在整个脚本文件中递归向上向前查找，按查找到的顺序排序
	var result: MutableSet<ParadoxScriptVariable>? = null
	var current = context
	while(current !is PsiFile) {
		var prevSibling = current.prevSibling
		while(prevSibling != null) {
			if(prevSibling is ParadoxScriptVariable && prevSibling.name == name) {
				if(result == null) result = mutableSetOf()
				result.add(prevSibling)
				break
			}
			prevSibling = prevSibling.prevSibling
		}
		current = current.parent ?: break
	}
	if(result == null) return emptySet()
	return result
}

/**
 * 在当前文件中递归查找所有的封装变量（scriptedVariable）。（不一定声明在顶层）
 * @param distinct 是否需要对相同名字的变量进行去重。默认为`false`。
 */
fun findAllScriptVariablesInFile(context: PsiElement, distinct: Boolean = false): Set<ParadoxScriptVariable> {
	//在整个脚本文件中递归向上查找，返回查找到的所有结果，按查找到的顺序排序
	var result: MutableSet<ParadoxScriptVariable>? = null
	val namesToDistinct = if(distinct) mutableSetOf<String>() else null
	var current = context
	while(current !is PsiFile) {
		var prevSibling = current.prevSibling
		while(prevSibling != null) {
			if(prevSibling is ParadoxScriptVariable) {
				if(namesToDistinct == null || namesToDistinct.add(prevSibling.name)) {
					if(result == null) result = mutableSetOf()
					result.add(prevSibling)
				}
			}
			prevSibling = prevSibling.prevSibling
		}
		current = current.parent ?: break
	}
	if(result == null) return emptySet()
	return result
}

/**
 * 基于封装变量名字索引，根据名字查找封装变量（scriptedVariable）。
 * @param name 变量的名字，以"@"开始。
 */
fun findScriptedVariable(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxScriptVariable> = nopSelector()
): ParadoxScriptVariable? {
	return ParadoxScriptedVariableNameIndex.findOne(name, project, scope, !getSettings().preferOverridden, selector)
}

/**
 * 基于封装变量名字索引，根据名字查找所有的封装变量（scriptedVariable）。
 * @param name 变量的名字，以"@"开始。
 */
fun findScriptedVariables(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxScriptVariable> = nopSelector()
): Set<ParadoxScriptVariable> {
	return ParadoxScriptedVariableNameIndex.findAll(name, project, scope, selector)
}

/**
 * 基于封装变量名字索引，查找所有的封装变量（scriptedVariable）。
 * @param distinct 是否需要对相同名字的变量进行去重。默认为`false`。
 */
fun findAllScriptedVariables(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	distinct: Boolean = false,
	selector: ChainedParadoxSelector<ParadoxScriptVariable> = nopSelector()
): Set<ParadoxScriptVariable> {
	return ParadoxScriptedVariableNameIndex.findAll(project, scope, distinct, selector)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinition(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	preferFirst: Boolean = !getSettings().preferOverridden,
	selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
): ParadoxDefinitionProperty? {
	return ParadoxDefinitionNameIndex.findOne(name, typeExpression, project, scope, preferFirst, selector)
}

/**
 * 基于定义名字索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinitions(
	name: String,
	typeExpression: String?,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
): Set<ParadoxDefinitionProperty> {
	return ParadoxDefinitionNameIndex.findAll(name, typeExpression, project, scope, selector)
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
	distinct: Boolean = false,
	selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
): Set<ParadoxDefinitionProperty> {
	return ParadoxDefinitionNameIndex.findAll(typeExpression, project, scope, distinct, selector)
}

/**
 * 基于定义类型索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinitionByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	preferFirst: Boolean = !getSettings().preferOverridden,
	selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
): ParadoxDefinitionProperty? {
	return ParadoxDefinitionTypeIndex.findOne(name, typeExpression, project, scope, preferFirst, selector)
}

/**
 * 基于定义类型索引，根据名字、类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 */
fun findDefinitionsByType(
	name: String,
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
): Set<ParadoxDefinitionProperty> {
	return ParadoxDefinitionTypeIndex.findAll(name, typeExpression, project, scope, selector)
}

/**
 * 基于定义类型索引，根据类型表达式查找所有的脚本文件的定义（definition）。
 * @param typeExpression 参见[ParadoxDefinitionTypeExpression]。
 * @param distinct 是否需要对同一基本类型而相同名字的定义进行去重。默认为`false`。
 */
fun findAllDefinitionsByType(
	typeExpression: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	distinct: Boolean = false,
	selector: ChainedParadoxSelector<ParadoxDefinitionProperty> = nopSelector()
): Set<ParadoxDefinitionProperty> {
	return ParadoxDefinitionTypeIndex.findAll(typeExpression, project, scope, distinct, selector)
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
 * 基于本地化名字索引，根据名字查找本地化（localisation）。
 */
fun findLocalisation(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	preferFirst: Boolean = !getSettings().preferOverridden,
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): ParadoxLocalisationProperty? {
	return ParadoxLocalisationNameIndex.Localisation.findOne(name, project, scope, preferFirst, selector)
}

/**
 * 基于本地化名字索引，根据名字、语言区域查找所有的本地化（localisation）。
 * @see preferredParadoxLocale
 */
fun findLocalisations(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): Set<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.Localisation.findAll(name, project, scope, selector)
}

/**
 * 基于本地化名字索引，根据关键字和推断的语言区域遍历所有的本地化（localisation）。对本地化的键去重。
 * @see preferredParadoxLocale
 */
inline fun processLocalisationVariants(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector(),
	crossinline processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean
): Boolean {
	val maxSize = getSettings().maxCompleteSize
	return ParadoxLocalisationNameIndex.Localisation.processVariants(keyword, project, scope, maxSize, selector, processor)
}

/**
 * 基于同步本地化名字索引，根据名字查找同步本地化（localisation_synced）。
 */
fun findSyncedLocalisation(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	preferFirst: Boolean = !getSettings().preferOverridden,
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): ParadoxLocalisationProperty? {
	return ParadoxLocalisationNameIndex.SyncedLocalisation.findOne(name, project, scope, preferFirst, selector)
}

/**
 * 基于同步本地化名字索引，根据名字、语言区域查找所有的同步本地化（localisation_synced）。
 * @see preferredParadoxLocale
 */
fun findSyncedLocalisations(
	name: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector()
): Set<ParadoxLocalisationProperty> {
	return ParadoxLocalisationNameIndex.SyncedLocalisation.findAll(name, project, scope, selector)
}

/**
 * 基于同步本地化名字索引，根据关键字和推断的语言区域遍历所有的同步本地化（synced_localisation）。对本地化的键去重。
 * @see preferredParadoxLocale
 */
inline fun processSyncedLocalisationVariants(
	keyword: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	selector: ChainedParadoxSelector<ParadoxLocalisationProperty> = nopSelector(),
	crossinline processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean
): Boolean {
	val maxSize = getSettings().maxCompleteSize
	return ParadoxLocalisationNameIndex.Localisation.processVariants(keyword, project, scope, maxSize, selector, processor)
}


/**
 * 基于文件索引，根据相对于游戏或模组目录的文件路径查找匹配的文件（非目录）。
 * @param expressionType 使用何种文件路径表达式类型。默认使用精确路径。
 * @param ignoreCase 匹配路径时是否忽略大小写。 默认为`true`。
 * @param selector 用于指定如何选择需要查找的文件，尤其时当存在覆盖与重载的情况时。
 */
fun findFileByFilePath(
	filePath: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	expressionType: CwtFilePathExpressionType = CwtFilePathExpressionTypes.Exact,
	ignoreCase: Boolean = true,
	selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
): VirtualFile? {
	return ParadoxFilePathIndex.findOne(filePath, scope, expressionType, ignoreCase, selector)
}

/**
 * 基于文件索引，根据相对于游戏或模组目录的文件路径查找所有匹配的文件（非目录）。
 * @param expressionType 使用何种文件路径表达式类型。默认使用精确路径。
 * @param ignoreCase 匹配路径时是否忽略大小写。默认为`true`。
 * @param distinct 是否需要对相同路径的文件进行去重。默认为`false`。
 * @param selector 用于指定如何选择需要查找的文件，尤其时当存在覆盖与重载的情况时。
 */
fun findFilesByFilePath(
	filePath: String,
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	expressionType: CwtFilePathExpressionType = CwtFilePathExpressionTypes.Exact,
	ignoreCase: Boolean = true,
	distinct: Boolean = false,
	selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
): Set<VirtualFile> {
	return ParadoxFilePathIndex.findAll(filePath, scope, expressionType, ignoreCase, distinct, selector)
}

/**
 * 基于文件索引，根据相查找所有匹配的（位于游戏或模组根目录或其子目录中的）文件（非目录）。
 * @param ignoreCase 匹配路径时是否忽略大小写。默认为`true`。
 * @param distinct 是否需要对相同路径的文件进行去重。默认为`false`。
 * @param selector 用于指定如何选择需要查找的文件，尤其时当存在覆盖与重载的情况时。
 */
fun findAllFilesByFilePath(
	project: Project,
	scope: GlobalSearchScope = GlobalSearchScope.allScope(project),
	ignoreCase: Boolean = true,
	distinct: Boolean = false,
	selector: ChainedParadoxSelector<VirtualFile> = nopSelector()
): Set<VirtualFile> {
	return ParadoxFilePathIndex.findAll(project, scope, ignoreCase, distinct, selector)
}
//endregion

//region Psi Link Extensions

//com.jetbrains.python.documentation.PyDocumentationLink

private const val cwtLinkPrefix = "#cwt/"
private const val definitionLinkPrefix = "#definition/"
private const val localisationLinkPrefix = "#localisation/"
private const val filePathLinkPrefix = "#path/"

fun resolveScope(link: String, context: PsiElement): PsiElement? {
	return when {
		link.startsWith(cwtLinkPrefix) -> resolveCwtLink(link.drop(cwtLinkPrefix.length), context)
		link.startsWith(definitionLinkPrefix) -> resolveDefinitionLink(link.drop(definitionLinkPrefix.length), context)
		link.startsWith(localisationLinkPrefix) -> resolveLocalisationLink(link.drop(localisationLinkPrefix.length), context)
		link.startsWith(filePathLinkPrefix) -> resolveFilePathLink(link.drop(filePathLinkPrefix.length), context)
		else -> null
	}
}

//#cwt/stellaris/types/civic_or_origin/civic
private fun resolveCwtLink(linkWithoutPrefix: String, context: PsiElement): CwtProperty? {
	return runCatching {
		val project = context.project
		val tokens = linkWithoutPrefix.split('/')
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

//#definition/civic_or_origin.origin/origin_default
private fun resolveDefinitionLink(linkWithoutPrefix: String, context: PsiElement): ParadoxDefinitionProperty? {
	return runCatching {
		val lastDotIndex = linkWithoutPrefix.lastIndexOf('/')
		val type = linkWithoutPrefix.substring(0, lastDotIndex)
		val name = linkWithoutPrefix.substring(lastDotIndex + 1)
		val selector = definitionSelector().gameTypeFrom(context).preferRootFrom(context)
		findDefinitionByType(name, type, context.project, selector = selector)
	}.getOrNull()
}

//#localisation/KEY
private fun resolveLocalisationLink(linkWithoutPrefix: String, context: PsiElement): ParadoxLocalisationProperty? {
	return runCatching {
		val token = linkWithoutPrefix
		val selector = localisationSelector().gameTypeFrom(context).preferRootFrom(context).preferLocale(context.localeConfig)
		return findLocalisation(token, context.project, selector = selector)
	}.getOrNull()
}

private fun resolveFilePathLink(linkWithoutPrefix: String, context: PsiElement): PsiFile? {
	return runCatching {
		val filePath = linkWithoutPrefix
		val project = context.project
		val fileInfo = context.fileInfo ?: return@runCatching null
		val selector = fileSelector().gameType(fileInfo.rootInfo.gameType).preferRoot(fileInfo.rootFile)
		findFileByFilePath(filePath, project, selector = selector)?.toPsiFile<PsiFile>(project)
	}.getOrNull()
}
//endregion

//region Documentation Extensions
fun StringBuilder.appendIf(condition: Boolean, text: String): StringBuilder {
	if(condition) append(text)
	return this
}

fun StringBuilder.appendExternalLinkIcon(): StringBuilder {
	append("<icon src='ide/external_link_arrow.svg'/>")
	return this
}

fun StringBuilder.appendUnresolvedLink(label: String): StringBuilder {
	append(label.escapeXml()) //直接显示对应的标签文本
	return this
}

fun StringBuilder.appendLink(refText: String, label: String): StringBuilder {
	append("<a href=\"").append(refText).append("\">").append(label).append("</a>")
	return this
}

fun StringBuilder.appendPsiLink(refText: String, label: String, plainLink: Boolean = true): StringBuilder {
	DocumentationManagerUtil.createHyperlink(this, refText, label, plainLink)
	return this
}

fun StringBuilder.appendCwtLink(name: String, link: String, context: PsiElement?): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(unresolvedString)
	//如果可以被解析为CWT规则，则显示链接（context传null时总是显示）
	val isResolved = context == null || resolveCwtLink(link, context) != null
	if(isResolved) return appendPsiLink("$cwtLinkPrefix$link".escapeXml(), name.escapeXml())
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendDefinitionLink(name: String, typeExpression: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(unresolvedString)
	//如果可以被解析为定义，则显示链接
	val isResolved = resolved == true || (resolved == null && findDefinition(name, null, context.project, preferFirst = true, selector = definitionSelector().gameTypeFrom(context)) != null)
	if(isResolved) return appendPsiLink("$definitionLinkPrefix$typeExpression/$name", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendLocalisationLink(name: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
	//如果name为空字符串，需要特殊处理
	if(name.isEmpty()) return append(unresolvedString)
	//如果可以被解析为本地化，则显示链接
	val isResolved = resolved == true || (resolved == null && findLocalisation(name, context.project, preferFirst = true, selector = localisationSelector().gameTypeFrom(context)) != null)
	if(isResolved) return appendPsiLink("$localisationLinkPrefix$name", name)
	//否则显示未解析的链接
	return appendUnresolvedLink(name)
}

fun StringBuilder.appendFilePathLink(filePath: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
	//如果可以定位到绝对路径，则显示链接
	val isResolved = resolved == true || (resolved == null && findFileByFilePath(filePath, context.project, selector = fileSelector().gameTypeFrom(context)) != null)
	if(isResolved) return appendPsiLink("$filePathLinkPrefix$filePath", filePath)
	//否则显示未解析的链接
	return appendUnresolvedLink(filePath)
}

/**
 * @param local 输入的[url]是否是本地绝对路径。
 */
fun StringBuilder.appendImgTag(url: String, local: Boolean = true): StringBuilder {
	append("<img src=\"")
	if(local) append(url.toFileUrl()) else append(url)
	append("\" />")
	return this
}

fun StringBuilder.appendImgTag(url: String, width: Int, height: Int, local: Boolean = true): StringBuilder {
	append("<img src=\"")
	if(local) append(url.toFileUrl()) else append(url)
	//这里不能使用style="..."
	append("\" width=\"").append(width).append("\" height=\"").append(height)
	append(" vspace=\"0\" hspace=\"0\"")
	append("\"/>")
	return this
}

fun StringBuilder.appendFileInfoHeader(fileInfo: ParadoxFileInfo?): StringBuilder {
	if(fileInfo != null) {
		append("<span>")
		//描述符信息（模组名、版本等）
		append("[").append(fileInfo.rootInfo.gameType.description).append(" ").append(fileInfo.rootType.description)
		val descriptorInfo = fileInfo.descriptorInfo
		if(descriptorInfo != null) {
			if(fileInfo.rootType == ParadoxRootType.Mod) {
				append(": ").append(descriptorInfo.name.escapeXml())
			}
			val version = descriptorInfo.version
			if(version != null) append("@").append(version)
		}
		append("]")
		grayed {
			val remoteFileId = descriptorInfo?.remoteFileId
			//remoteFileId（暂不显示）
			//if(remoteFileId != null) {
			//	append(" ").append(PlsDocBundle.message("name.core.remoteFileId")).append(": ").append(remoteFileId).append(" )
			//}
			//相关链接
			val rootUri = fileInfo.rootPath.toUri().toString() //通过这种方式获取需要的url
			append(" ")
			appendLink(rootUri, PlsDocBundle.message("name.core.localLinkLabel"))
			if(remoteFileId != null) {
				append(" | ")
				appendLink(getSteamWorkshopLinkOnSteam(remoteFileId), PlsDocBundle.message("name.core.steamLinkLabel"))
				appendExternalLinkIcon() // 使用翻译插件翻译文档注释后，这里会出现不必要的换行 - 已被修复
				append(" | ")
				appendLink(getSteamWorkshopLink(remoteFileId), PlsDocBundle.message("name.core.steamWebsiteLinkLabel")) //自带外部链接图标
			}
		}
		append("</span>")
		appendBr()
		//文件信息（相对于游戏或模组根目录的路径）
		append("[").append(fileInfo.path).append("]")
		appendBr()
	}
	return this
}

fun StringBuilder.appendBr(): StringBuilder {
	return append("<br>")
}
//endregion