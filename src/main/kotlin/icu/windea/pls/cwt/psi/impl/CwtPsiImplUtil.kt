package icu.windea.pls.cwt.psi.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*
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
		return true
	}
	
	@JvmStatic
	fun getComponents(element: CwtRootBlock): List<PsiElement> {
		return element.filterChildOfType { it is CwtProperty || it is CwtValue }
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
	fun getValue(element: CwtOptionKey): String {
		return element.findRequiredChild(OPTION_KEY_TOKEN).text.unquote()
	}
	//endregion
	
	//region CwtProperty
	@JvmStatic
	fun getIcon(element: CwtProperty, @Iconable.IconFlags flags: Int): Icon {
		//0.7.4 对CWT别名规则（dataType=alias/single_alias）使用特殊的别名图标，以便区分内联前后的CWT规则
		val type = CwtConfigType.resolve(element)
		return when {
			type == CwtConfigType.Alias -> PlsIcons.Alias
			type == CwtConfigType.SingleAlias -> PlsIcons.Alias
			else -> PlsIcons.CwtProperty
		}
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
	fun getValue(element: CwtPropertyKey): String {
		return element.findRequiredChild(PROPERTY_KEY_TOKEN).text.unquote()
	}
	//endregion
	
	//region CwtValue
	@JvmStatic
	fun getIcon(element: CwtValue, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.CwtValue
	}
	
	@JvmStatic
	fun getValue(element: CwtValue): String {
		return element.text
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
	fun getValue(element: CwtBlock): String {
		return PlsConstants.blockFolder
	}
	
	@JvmStatic
	fun isEmpty(element: CwtBlock): Boolean {
		element.forEachChild {
			if(it is CwtProperty || it is CwtValue) return false
		}
		return true
	}
	
	@JvmStatic
	fun isNotEmpty(element: CwtBlock): Boolean {
		element.forEachChild {
			if(it is CwtProperty || it is CwtValue) return true
		}
		return true
	}
	
	@JvmStatic
	fun getComponents(element: CwtBlock): List<PsiElement> {
		return element.filterChildOfType { it is CwtProperty || it is CwtValue }
	}
	//endregion
	
	//region CwtDocumentationComment
	@JvmStatic
	fun getTokenType(element: CwtDocumentationComment): IElementType {
		return CwtElementTypes.DOCUMENTATION_COMMENT
	}
	//endregion
	
	//region CwtOptionComment
	@JvmStatic
	fun getTokenType(element: CwtOptionComment): IElementType {
		return CwtElementTypes.OPTION_COMMENT
	}
	//endregion
}