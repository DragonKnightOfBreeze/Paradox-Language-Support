package com.windea.plugin.idea.pls.localisation.psi.impl

import com.intellij.openapi.util.Iconable.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import com.intellij.util.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.localisation.psi.*
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createColorfulText
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createCommandField
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createCommandScope
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createIcon
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createLocale
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createPropertyKey
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createPropertyReference
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationElementFactory.createSequentialNumber
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationTypes.*
import com.windea.plugin.idea.pls.localisation.reference.*
import javax.swing.*

//NOTE getName 确定进行重构和导航时显示的PsiElement的名字
//NOTE setName 确定进行重命名时的逻辑
//NOTE getTextOffset 确定选中一个PsiElement时，哪一部分会高亮显示
//NOTE getReference 确定选中一个PsiElement时，哪些其他的PsiElement会同时高亮显示

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationPsiImplUtil {
	private val cannotBeRenamedMessage = message("cannotBeRenamed")
	
	//region ParadoxLocalisationLocale
	@JvmStatic
	fun getName(element: ParadoxLocalisationLocale): String {
		return element.localeId.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationLocale, name: String): PsiElement {
		element.localeId.replace(createLocale(element.project, name).localeId)
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxLocalisationLocale) {
		throw IncorrectOperationException(cannotBeRenamedMessage)
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationLocale): PsiElement {
		return element.localeId
	}
	
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationLocale, @IconFlags flags: Int): Icon {
		return localisationLocaleIcon
	}
	//endregion
	
	//region ParadoxLocalisationProperty
	@JvmStatic
	fun getName(element: ParadoxLocalisationProperty): String {
		return element.stub?.key ?: element.propertyKey.text
	}
	
	//TODO 检查是否是项目中的localisation，这样才允许重命名
	@JvmStatic
	fun setName(element: ParadoxLocalisationProperty, name: String): PsiElement {
		element.propertyKey.replace(createPropertyKey(element.project, name))
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxLocalisationProperty) {
		
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationProperty): PsiElement {
		return element.propertyKey.propertyKeyId
	}
	
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationProperty, @IconFlags flags: Int): Icon {
		return localisationPropertyIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxLocalisationProperty): String? {
		return element.propertyValue?.text?.unquote()
	}
	//endregion
	
	//region ParadoxLocalisationPropertyReference
	@JvmStatic
	fun getTextOffset(element: ParadoxLocalisationPropertyReference): Int {
		return element.startOffset + 1
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationPropertyReference): String {
		return element.propertyReferenceId?.text?.trim().orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationPropertyReference, name: String): PsiElement {
		element.propertyReferenceId?.replace(createPropertyReference(element.project, name).propertyReferenceId!!)
		return element
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationPropertyReference): ParadoxLocalisationPropertyPsiReference? {
		val propertyReferenceId = element.propertyReferenceId ?: return null
		return ParadoxLocalisationPropertyPsiReference(element, propertyReferenceId.textRangeInParent)
	}
	//endregion
	
	//region ParadoxLocalisationIcon
	@JvmStatic
	fun getName(element: ParadoxLocalisationIcon): String {
		return element.iconId?.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationIcon, name: String): PsiElement {
		element.iconId?.replace(createIcon(element.project, name).iconId!!)
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxLocalisationIcon) {
		throw IncorrectOperationException(cannotBeRenamedMessage)
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
	fun getReference(element: ParadoxLocalisationIcon): ParadoxLocalisationIconPsiReference? {
		val iconId = element.iconId ?: return null
		return ParadoxLocalisationIconPsiReference(element, iconId.textRangeInParent)
	}
	
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationIcon, @IconFlags flags: Int): Icon {
		return localisationIconIcon
	}
	//endregion
	
	//region ParadoxLocalisationSequentialNumber
	@JvmStatic
	fun getName(element: ParadoxLocalisationSequentialNumber): String {
		return element.sequentialNumberId?.text?.toUpperCase().orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationSequentialNumber, name: String): PsiElement {
		element.sequentialNumberId?.replace(createSequentialNumber(element.project, name).sequentialNumberId!!)
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxLocalisationSequentialNumber) {
		throw IncorrectOperationException(cannotBeRenamedMessage)
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
	fun getIcon(element: ParadoxLocalisationSequentialNumber, @IconFlags flags: Int): Icon {
		return localisationSequentialNumberIcon
	}
	//endregion
	
	//region ParadoxLocalisationColorfulText
	@JvmStatic
	fun getName(element: ParadoxLocalisationColorfulText): String {
		return element.colorId?.text?.toUpperCase().orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationColorfulText, name: String): PsiElement {
		element.colorId?.replace(createColorfulText(element.project, name).colorId!!)
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxLocalisationColorfulText) {
		throw IncorrectOperationException(cannotBeRenamedMessage)
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationColorfulText): PsiElement? {
		return element.colorId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxLocalisationColorfulText): Int {
		return element.startOffset + 1
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
	fun getName(element: ParadoxLocalisationCommandScope): String {
		return element.text.trim()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationCommandScope, name: String): PsiElement {
		element.commandScopeId.replace(createCommandScope(element.project, name).commandScopeId)
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxLocalisationCommandScope) {
		throw IncorrectOperationException(cannotBeRenamedMessage)
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationCommandScope): PsiElement {
		return element.commandScopeId
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationCommandScope): ParadoxLocalisationCommandScopePsiReference {
		val commandScopeId = element.commandScopeId
		return ParadoxLocalisationCommandScopePsiReference(element, commandScopeId.textRangeInParent)
	}
	
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationCommandScope, @IconFlags flags: Int): Icon {
		return localisationCommandScopeIcon
	}
	//endregion
	
	//region ParadoxLocalisationCommandField
	@JvmStatic
	fun getName(element: ParadoxLocalisationCommandField): String {
		return element.commandFieldId?.text?.trim().orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationCommandField, name: String): PsiElement {
		element.commandFieldId?.replace(createCommandField(element.project, name).commandFieldId!!)
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxLocalisationCommandField) {
		throw IncorrectOperationException(cannotBeRenamedMessage)
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationCommandField): PsiElement? {
		return element.commandFieldId
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationCommandField): ParadoxLocalisationCommandFieldPsiReference? {
		val commandFieldId = element.commandFieldId ?: return null
		return ParadoxLocalisationCommandFieldPsiReference(element, commandFieldId.textRangeInParent)
	}
	
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationCommandField, @IconFlags flags: Int): Icon {
		return localisationCommandFieldIcon
	}
	//endregion
}
