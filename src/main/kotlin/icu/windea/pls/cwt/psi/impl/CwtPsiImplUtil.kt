package icu.windea.pls.cwt.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.navigation.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.model.*
import javax.swing.*

@Suppress("UNUSED_PARAMETER")
object CwtPsiImplUtil {
	//region CwtRootBlock
	@JvmStatic
	fun getValue(element: CwtRootBlock): String {
		return PlsConstants.blockFolder
	}
	
	@JvmStatic
	fun isEmpty(element: CwtRootBlock): Boolean {
		element.forEachChild {
			if(it is CwtProperty || it is CwtValue) return false
		}
		return true
	}
	
	@JvmStatic
	fun isNotEmpty(element: CwtRootBlock): Boolean {
		element.forEachChild {
			if(it is CwtProperty || it is CwtValue) return true
		}
		return false
	}
	
	@JvmStatic
	fun getComponents(element: CwtRootBlock): List<PsiElement> {
		return element.findChildrenOfType { it is CwtProperty || it is CwtValue }
	}
	//endregion
	
	//region CwtOption
	@JvmStatic
	fun getIcon(element: CwtOption, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.CwtOption
	}
	
	@JvmStatic
	fun getName(element: CwtOption): String {
		return element.optionKey.value
	}
	
	@JvmStatic
	fun setName(element: CwtOption, name: String): CwtOption {
		throw IncorrectOperationException() //不允许重命名
	}
	
	@JvmStatic
	fun getNameIdentifier(element: CwtOption): PsiElement {
		return element.optionKey
	}
	
	@JvmStatic
	fun getValue(element: CwtOption): String? {
		return element.optionValue?.value
	}
	
	@JvmStatic
	fun getSeparatorType(element: CwtOption): CwtSeparatorType {
		//这里不能遍历element.children
		element.forEachChild { child ->
			when(child.elementType) {
				EQUAL_SIGN -> return CwtSeparatorType.EQUAL
				NOT_EQUAL_SIGN -> return CwtSeparatorType.NOT_EQUAL
			}
		}
		return CwtSeparatorType.EQUAL
	}
	//endregion
	
	//region CwtOptionKey
	@JvmStatic
	fun getIcon(element: CwtOptionKey, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.CwtOption
	}
	
	@JvmStatic
	fun getName(element: CwtOptionKey): String {
		return element.value
	}
	
	@JvmStatic
	fun getValue(element: CwtOptionKey): String {
		return element.findChild(OPTION_KEY_TOKEN)!!.text.unquote()
	}
	//endregion
	
	//region CwtProperty
	@JvmStatic
	fun getIcon(element: CwtProperty, @Iconable.IconFlags flags: Int): Icon {
		val type = element.configType
		return type?.icon ?: PlsIcons.CwtProperty
	}
	
	@JvmStatic
	fun getName(element: CwtProperty): String {
		return element.propertyKey.value
	}
	
	@JvmStatic
	fun setName(element: CwtProperty, name: String): CwtProperty {
		throw IncorrectOperationException() //不允许重命名
	}
	
	@JvmStatic
	fun getNameIdentifier(element: CwtProperty): PsiElement {
		return element.propertyKey
	}
	
	@JvmStatic
	fun getValue(element: CwtProperty): String? {
		return element.propertyValue?.value
	}
	
	@JvmStatic
	fun getSeparatorType(element: CwtProperty): CwtSeparatorType {
		//这里不能遍历element.children
		element.forEachChild { child ->
			when(child.elementType) {
				EQUAL_SIGN -> return CwtSeparatorType.EQUAL
				NOT_EQUAL_SIGN -> return CwtSeparatorType.NOT_EQUAL
			}
		}
		return CwtSeparatorType.EQUAL
	}
	//endregion
	
	//region CwtPropertyKey
	@JvmStatic
	fun getIcon(element: CwtPropertyKey, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.CwtProperty
	}
	
	@JvmStatic
	fun getName(element: CwtPropertyKey): String {
		return element.value
	}
	
	@JvmStatic
	fun getValue(element: CwtPropertyKey): String {
		return element.findChild(PROPERTY_KEY_TOKEN)!!.text.unquote()
	}
	
	@JvmStatic
	fun setValue(element: CwtPropertyKey, value: String) : CwtPropertyKey {
		val newElement = CwtElementFactory.createPropertyKey(element.project, value)
		element.replace(newElement)
		return element
	}
	//endregion
	
	//region CwtValue
	@JvmStatic
	fun getIcon(element: CwtValue, @Iconable.IconFlags flags: Int): Icon {
		val type = element.configType
		return type?.icon ?: PlsIcons.CwtValue
	}
	
	@JvmStatic
	fun getName(element: CwtValue): String {
		return getValue(element)
	}
	
	@JvmStatic
	fun getValue(element: CwtValue): String {
		return element.text
	}
	
	@JvmStatic
	fun setValue(element: CwtValue, value: String) : CwtValue {
		val newElement = CwtElementFactory.createValue(element.project, value)
		element.replace(newElement)
		return element
	}
	//endregion
	
	//region CwtBoolean
	@JvmStatic
	fun getBooleanValue(element: CwtBoolean): Boolean {
		return element.value.toBooleanYesNo()
	}
	//endregion
	
	//region CwtInt
	@JvmStatic
	fun getIntValue(element: CwtInt): Int {
		return element.value.toIntOrNull() ?: 0
	}
	//endregion
	
	//region CwtFloat
	@JvmStatic
	fun getFloatValue(element: CwtFloat): Float {
		return element.value.toFloatOrNull() ?: 0f
	}
	//endregion
	
	//region CwtString
	@JvmStatic
	fun getName(element: CwtString): String {
		return element.value
	}
	
	@JvmStatic
	fun setName(element: CwtString, name: String): CwtString {
		throw IncorrectOperationException() //不允许重命名
	}
	
	@JvmStatic
	fun getNameIdentifier(element: CwtString): PsiElement {
		return element
	}
	
	@JvmStatic
	fun getValue(element: CwtString): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun setValue(element: CwtString, value: String) : CwtString {
		val newElement = CwtElementFactory.createString(element.project, value)
		element.replace(newElement)
		return element
	}
	
	@JvmStatic
	fun getStringValue(element: CwtString): String {
		return element.value
	}
	//endregion
	
	//region CwtBlock
	@JvmStatic
	fun getIcon(element: CwtBlock, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.CwtBlock
	}
	
	@JvmStatic
	fun getName(element: CwtBlock): String {
		return element.value
	}
	
	@JvmStatic
	fun getValue(element: CwtBlock): String {
		return PlsConstants.blockFolder
	}
	
	@JvmStatic
	fun isEmpty(element: CwtBlock): Boolean {
		element.forEachChild {
			if(it is CwtOption || it is CwtProperty || it is CwtValue) return false
		}
		return true
	}
	
	@JvmStatic
	fun isNotEmpty(element: CwtBlock): Boolean {
		element.forEachChild {
			if(it is CwtOption || it is CwtProperty || it is CwtValue) return true
		}
		return false
	}
	
	@JvmStatic
	fun getComponents(element: CwtBlock): List<PsiElement> {
		return element.findChildrenOfType { it is CwtProperty || it is CwtValue }
	}
	//endregion
	
	//region CwtDocumentationComment
	@JvmStatic
	fun getTokenType(element: CwtDocumentationComment): IElementType {
		return DOCUMENTATION_COMMENT
	}
	//endregion
	
	//region CwtOptionComment
	@JvmStatic
	fun getTokenType(element: CwtOptionComment): IElementType {
		return OPTION_COMMENT
	}
	//endregion
	
	@JvmStatic
	fun getPresentation(element: PsiElement): ItemPresentation {
		return CwtItemPresentation(element)
	}
	
	@JvmStatic
	fun getUseScope(element: PsiElement): SearchScope {
		return GlobalSearchScope.allScope(element.project)
	}
}
