package icu.windea.pls.localisation.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.util.Iconable.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.localisation.navigation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.reference.*
import javax.swing.*

//getName 确定进行重构和导航时显示的PsiElement的名字
//setName 确定进行重命名时的逻辑
//getTextOffset 确定选中一个PsiElement时，哪一部分会高亮显示
//getReference 确定选中一个PsiElement时，哪些其他的PsiElement会同时高亮显示

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationPsiImplUtil {
	//region ParadoxLocalisationPropertyList
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationPropertyList, @IconFlags flags: Int): Icon {
		return PlsIcons.LocalisationLocale
	}
	
	@JvmStatic
	fun getComponents(element: ParadoxLocalisationPropertyList): List<ParadoxLocalisationProperty> {
		return element.propertyList
	}
	//endregion
	
	//region ParadoxLocalisationLocale
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationLocale, @IconFlags flags: Int): Icon {
		return PlsIcons.LocalisationLocale
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationLocale): String {
		return element.localeId.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationLocale, name: String): ParadoxLocalisationLocale {
		val nameElement = element.localeId
		val newNameElement = ParadoxLocalisationElementFactory.createLocale(element.project, name).localeId
		nameElement.replace(newNameElement)
		return element
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
		if(element.localisationInfo != null) return PlsIcons.Localisation
		return PlsIcons.LocalisationProperty
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationProperty): String {
		//注意：element.stub可能会导致ProcessCanceledException
		runCatching { element.stub?.name }.getOrNull()?.let { return it }
		return element.propertyKey.propertyKeyId.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationProperty, name: String): ParadoxLocalisationProperty {
		val nameElement = element.propertyKey.propertyKeyId
		val newNameElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, name).propertyKeyId
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxLocalisationProperty): PsiElement {
		return element.propertyKey.propertyKeyId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxLocalisationProperty): Int{
		return element.propertyKey.textOffset
	}
	
	@JvmStatic
	fun getCategory(element: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
		//注意：element.stub可能会导致ProcessCanceledException
		runCatching { element.stub?.category }.getOrNull()?.let { return it }
		return element.localisationInfo?.category
	}
	
	@JvmStatic
	fun getValue(element: ParadoxLocalisationProperty): String? {
		return element.propertyValue?.text?.unquote()
	}
	
	@JvmStatic
	fun getPresentation(element: ParadoxLocalisationProperty): ItemPresentation {
		return ParadoxLocalisationPropertyPresentation(element)
	}
	//endregion
	
	//region ParadoxLocalisationPropertyReference
	@JvmStatic
	fun getName(element: ParadoxLocalisationPropertyReference): String {
		return element.propertyReferenceId?.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationPropertyReference, name: String): ParadoxLocalisationPropertyReference {
		val nameElement = element.propertyReferenceId ?: throw IncorrectOperationException() //不支持重命名
		val newNameElement = ParadoxLocalisationElementFactory.createPropertyReference(element.project, name).propertyReferenceId!!
		nameElement.replace(newNameElement)
		return element
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
		return PlsIcons.LocalisationIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationIcon): String? {
		//string / command / property reference
		val iconIdElement = element.iconId
		if(iconIdElement != null) return iconIdElement.text
		val iconIdReferenceElement = element.iconIdReference ?: return null
		return iconIdReferenceElement.reference?.resolve()?.castOrNull<ParadoxLocalisationProperty>()?.value
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationIcon, name: String): ParadoxLocalisationIcon {
		val nameElement = element.iconId ?: throw IncorrectOperationException() //不支持重命名
		val newNameElement = ParadoxLocalisationElementFactory.createIcon(element.project, name).iconId!!
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getFrame(element: ParadoxLocalisationIcon): Int{
		//NOTE 这里的帧数可能用propertyReference表示，对应脚本中的参数，这时帧数传0
		val iconFrameElement = element.iconFrame //默认为0（不切分）
		if(iconFrameElement != null) return iconFrameElement.text.toIntOrNull() ?: 0
		//这里的propertyReference是一个来自脚本文件的参数，不解析
		//val iconFrameReferenceElement = element.iconFrameReference ?: return 0
		//return iconFrameReferenceElement.reference?.resolve()?.value?.toIntOrDefault(0) ?: 0
		return 0
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationIcon): ParadoxLocalisationIconReference? {
		val rangeInElement = element.iconId?.textRangeInParent ?: return null
		return ParadoxLocalisationIconReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxLocalisationColorfulText
	@JvmStatic
	fun getName(element: ParadoxLocalisationColorfulText): String? {
		return element.colorId?.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationColorfulText, name: String): ParadoxLocalisationColorfulText {
		val nameElement = element.colorId ?: throw IncorrectOperationException() //不支持重命名
		val newNameElement = ParadoxLocalisationElementFactory.createColorfulText(element.project, name).colorId!!
		nameElement.replace(newNameElement)
		return element
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
		if(separator.elementType != PIPE) return null
		var prev = separator.prevSibling ?: return null
		if(prev.elementType == TokenType.WHITE_SPACE) prev = prev.prevSibling ?: return null
		if(prev !is ParadoxLocalisationCommandIdentifier) return null
		return prev
	}
	
	@JvmStatic
	fun getNextIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxLocalisationCommandIdentifier? {
		var separator = element.nextSibling ?: return null
		if(separator.elementType == TokenType.WHITE_SPACE) separator = separator.nextSibling ?: return null
		if(separator.elementType != PIPE) return null
		var next = separator.nextSibling ?: return null
		if(next.elementType == TokenType.WHITE_SPACE) next = next.nextSibling ?: return null
		if(next !is ParadoxLocalisationCommandIdentifier) return null
		return next
	}
	//endregion
	
	//region ParadoxLocalisationCommandScope
	@JvmStatic
	fun getIcon(element: ParadoxLocalisationCommandScope, @IconFlags flags: Int): Icon {
		return PlsIcons.LocalisationCommandScope
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationCommandScope): String {
		return element.commandScopeId.text.trim()
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationCommandScope, name: String): ParadoxLocalisationCommandScope {
		val nameElement = element.commandScopeId
		val newNameElement = ParadoxLocalisationElementFactory.createCommandScope(element.project, name).commandScopeId
		nameElement.replace(newNameElement)
		return element
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
		return PlsIcons.LocalisationCommandField
	}
	
	@JvmStatic
	fun getName(element: ParadoxLocalisationCommandField): String {
		return element.commandFieldId?.text?.trim().orEmpty() //不应该为null
	}
	
	@JvmStatic
	fun setName(element: ParadoxLocalisationCommandField, name: String): ParadoxLocalisationCommandField {
		val nameElement = element.commandFieldId ?: throw IncorrectOperationException() //不支持重命名
		val newNameElement = ParadoxLocalisationElementFactory.createCommandField(element.project, name).commandFieldId!!
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getReference(element: ParadoxLocalisationCommandField): ParadoxLocalisationCommandFieldReference? {
		val rangeInElement = element.commandFieldId?.textRangeInParent ?: return null
		return ParadoxLocalisationCommandFieldReference(element, rangeInElement)
	}
	//endregion
}
