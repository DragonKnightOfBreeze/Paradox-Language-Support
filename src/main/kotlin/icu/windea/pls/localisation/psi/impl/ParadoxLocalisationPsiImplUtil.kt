package icu.windea.pls.localisation.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.util.Iconable.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory.createCommandField
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory.createCommandScope
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory.createIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory.createPropertyKey
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory.createPropertyReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.reference.*
import icu.windea.pls.localisation.structureView.*
import javax.swing.*

//getName 确定进行重构和导航时显示的PsiElement的名字
//setName 确定进行重命名时的逻辑
//getTextOffset 确定选中一个PsiElement时，哪一部分会高亮显示
//getReference 确定选中一个PsiElement时，哪些其他的PsiElement会同时高亮显示

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationPsiImplUtil {
	//region ParadoxLocalisationPropertyList
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationPropertyList, @IconFlags flags: Int): Icon{
		return PlsIcons.localisationLocaleIcon
	}
	
	@JvmStatic
	fun getComponents(element: ParadoxLocalisationPropertyList): List<ParadoxLocalisationProperty>{
		return element.propertyList
	}
	//endregion
	
	//region ParadoxLocalisationLocale	
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationLocale, @IconFlags flags: Int): Icon {
		return PlsIcons.localisationLocaleIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationLocale): String {
		return element.localeId.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationLocale, name: String): ParadoxLocalisationLocale {
		//element.localeId.replace(createLocale(element.project, name).localeId)
		//return element
		throw IncorrectOperationException() //不允许重命名
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationLocale): PsiElement {
		return element.localeId
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationLocale): ParadoxLocalisationLocaleReference {
		val rangeInElement = element.localeId.textRangeInParent
		return ParadoxLocalisationLocaleReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxLocalisationProperty
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationProperty, @IconFlags flags: Int): Icon {
		if(element.localisationInfo != null) return PlsIcons.localisationIcon
		return PlsIcons.localisationPropertyIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationProperty): String {
		//注意：element.stub可能会导致ProcessCanceledException
		return runCatching { element.stub?.name }.getOrNull() ?: element.propertyKey.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationProperty, name: String): ParadoxLocalisationProperty {
		element.propertyKey.replace(createPropertyKey(element.project, name))
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationProperty): PsiElement {
		return element.propertyKey.propertyKeyId
	}
	
	@JvmStatic
	fun getCategory(element: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
		//注意：element.stub可能会导致ProcessCanceledException
		return runCatching { element.stub?.category }.getOrNull() ?: element.localisationInfo?.category
	}
	
	@JvmStatic
	fun getValue(element: ParadoxLocalisationProperty): String? {
		return element.propertyValue?.text?.unquote()
	}
	//endregion
	
	//region ParadoxLocalisationPropertyReference
	@JvmStatic
	fun getName(element: ParadoxLocalisationPropertyReference): String {
		return element.propertyReferenceId?.text?.trim().orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationPropertyReference, name: String): ParadoxLocalisationPropertyReference {
		element.propertyReferenceId?.replace(createPropertyReference(element.project, name).propertyReferenceId!!)
		return element
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxLocalisationPropertyReference): Int {
		return element.startOffset + 1
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationPropertyReference): ParadoxLocalisationPropertyReferenceReference? {
		val rangeInElement = element.propertyReferenceId?.textRangeInParent ?: return null
		return ParadoxLocalisationPropertyReferenceReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxLocalisationIcon	
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationIcon, @IconFlags flags: Int): Icon {
		return PlsIcons.localisationIconIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationIcon): String {
		return element.iconId?.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationIcon, name: String): ParadoxLocalisationIcon {
		element.iconId?.replace(createIcon(element.project, name).iconId!!)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationIcon): PsiElement? {
		return element.iconId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxLocalisationIcon): Int {
		return element.startOffset + 1
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationIcon): ParadoxLocalisationIconReference? {
		val rangeInElement = element.iconId?.textRangeInParent ?: return null
		return ParadoxLocalisationIconReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxLocalisationSequentialNumber
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationSequentialNumber, @IconFlags flags: Int): Icon {
		return PlsIcons.localisationSequentialNumberIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationSequentialNumber): String {
		return element.sequentialNumberId?.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationSequentialNumber, name: String): ParadoxLocalisationSequentialNumber {
		//element.sequentialNumberId?.replace(createSequentialNumber(element.project, name).sequentialNumberId!!)
		//return element
		throw IncorrectOperationException() //不允许重命名
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationSequentialNumber): PsiElement? {
		return element.sequentialNumberId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxLocalisationSequentialNumber): Int {
		return element.startOffset + 1
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationSequentialNumber): ParadoxLocalisationSequentialNumberReference? {
		val rangeInElement = element.sequentialNumberId?.textRangeInParent ?: return null
		return ParadoxLocalisationSequentialNumberReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxLocalisationColorfulText
	@JvmStatic
	fun getName(element: ParadoxLocalisationColorfulText): String {
		return element.colorId?.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationColorfulText, name: String): ParadoxLocalisationColorfulText {
		//element.colorId?.replace(createColorfulText(element.project, name).colorId!!)
		//return element
		throw IncorrectOperationException() //不允许重命名
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationColorfulText): PsiElement? {
		return element.colorId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxLocalisationColorfulText): Int {
		return element.startOffset + 1
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationColorfulText): ParadoxLocalisationColorReference? {
		val rangeInElement = element.colorId?.textRangeInParent ?: return null
		return ParadoxLocalisationColorReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxLocalisationCommandIdentifier
	@JvmStatic
	fun getPrevIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxLocalisationCommandIdentifier? {
		var separator = element.prevSibling ?: return null
		if(separator.elementType == TokenType.WHITE_SPACE) separator = separator.prevSibling ?: return null
		if(separator.elementType != COMMAND_SEPARATOR) return null
		var prev = separator.prevSibling ?: return null
		if(prev.elementType == TokenType.WHITE_SPACE) prev = prev.prevSibling ?: return null
		if(prev !is ParadoxLocalisationCommandIdentifier) return null
		return prev
	}
	
	@JvmStatic
	fun getNextIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxLocalisationCommandIdentifier? {
		var separator = element.nextSibling ?: return null
		if(separator.elementType == TokenType.WHITE_SPACE) separator = separator.nextSibling ?: return null
		if(separator.elementType != COMMAND_SEPARATOR) return null
		var next = separator.nextSibling ?: return null
		if(next.elementType == TokenType.WHITE_SPACE) next = next.nextSibling ?: return null
		if(next !is ParadoxLocalisationCommandIdentifier) return null
		return next
	}
	//endregion
	
	//region ParadoxLocalisationCommandScope
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationCommandScope, @IconFlags flags: Int): Icon {
		return PlsIcons.localisationCommandScopeIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationCommandScope): String {
		return element.text.trim()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationCommandScope, name: String): ParadoxLocalisationCommandScope {
		element.commandScopeId.replace(createCommandScope(element.project, name).commandScopeId)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationCommandScope): PsiElement {
		return element.commandScopeId
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationCommandScope): ParadoxLocalisationCommandScopeReference {
		val rangeInElement = element.commandScopeId.textRangeInParent
		return ParadoxLocalisationCommandScopeReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxLocalisationCommandField
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationCommandField, @IconFlags flags: Int): Icon {
		return PlsIcons.localisationCommandFieldIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationCommandField): String {
		return element.commandFieldId?.text?.trim().orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationCommandField, name: String): ParadoxLocalisationCommandField {
		element.commandFieldId?.replace(createCommandField(element.project, name).commandFieldId!!)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationCommandField): PsiElement? {
		return element.commandFieldId
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationCommandField): ParadoxLocalisationCommandFieldReference? {
		val rangeInElement = element.commandFieldId?.textRangeInParent ?: return null
		return ParadoxLocalisationCommandFieldReference(element, rangeInElement)
	}
	//endregion
}
