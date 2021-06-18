package icu.windea.pls.cwt.psi.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import javax.swing.*

@Suppress("UNUSED_PARAMETER")
object CwtPsiImplUtil {
	//region CwtProperty
	@JvmStatic
	fun getIcon(element: CwtProperty, @Iconable.IconFlags flags: Int): Icon {
		return cwtPropertyIcon
	}
	
	@JvmStatic
	fun getName(element: CwtProperty):String{
		return element.propertyName
	}
	
	@JvmStatic
	fun setName(element: CwtProperty,name:String): PsiElement {
		throw IncorrectOperationException(message("cannotBeRenamed"))
	}
	
	@JvmStatic
	fun getNameIdentifier(element: CwtProperty): PsiElement {
		return element.propertyKey
	}
	
	@JvmStatic
	fun getPropertyName(element: CwtProperty):String{
		return element.propertyKey.name
	}
	
	@JvmStatic
	fun getPropertyValue(element: CwtProperty):String{
		return element.value?.value.orEmpty()
	}
	
	@JvmStatic
	fun getPropertyTruncatedValue(element: CwtProperty):String{
		return element.value?.truncatedValue.orEmpty()
	}
	
	@JvmStatic
	fun getSeparatorType(element:CwtProperty): SeparatorType {
		//这里不能遍历element.children
		element.forEachChild { child->
			when(child.elementType){
				CwtTypes.EQUAL_SIGN -> return SeparatorType.EQUAL
				CwtTypes.NOT_EQUAL_SIGN -> return SeparatorType.NOT_EQUAL
			}
		}
		return SeparatorType.EQUAL
	}
	//endregion
	
	//region CwtOption
	@JvmStatic
	fun getIcon(element: CwtOption, @Iconable.IconFlags flags: Int): Icon {
		return cwtOptionIcon
	}
	
	@JvmStatic
	fun getName(element: CwtOption):String{
		return element.optionName
	}
	
	@JvmStatic
	fun setName(element: CwtOption,name:String): PsiElement {
		throw IncorrectOperationException(message("cannotBeRenamed"))
	}
	
	@JvmStatic
	fun getNameIdentifier(element: CwtOption): PsiElement {
		return element.optionKey
	}
	
	@JvmStatic
	fun getOptionName(element: CwtOption):String{
		return element.optionKey.name
	}
	
	@JvmStatic
	fun getOptionValue(element: CwtOption):String{
		return element.value?.value.orEmpty()
	}
	
	@JvmStatic
	fun getOptionTruncatedValue(element: CwtOption):String{
		return element.value?.truncatedValue.orEmpty()
	}
	
	@JvmStatic
	fun getSeparatorType(element:CwtOption): SeparatorType {
		//这里不能遍历element.children
		element.forEachChild { child->
			when(child.elementType){
				CwtTypes.EQUAL_SIGN -> return SeparatorType.EQUAL
				CwtTypes.NOT_EQUAL_SIGN -> return SeparatorType.NOT_EQUAL
			}
		}
		return SeparatorType.EQUAL
	}
	//endregion
	
	//region CwtPropertyKey
	@JvmStatic
	fun getName(element: CwtPropertyKey):String{
		return element.propertyKeyToken.text.unquote()
	}
	//endregion
	
	//region CwtOptionKey
	@JvmStatic
	fun getName(element: CwtOptionKey):String{
		return element.optionKeyToken.text.unquote()
	}
	//endregion
	
	//region CwtValue
	@JvmStatic
	fun getIcon(element: CwtValue, @Iconable.IconFlags flags: Int): Icon {
		return cwtValueIcon
	}
	
	@JvmStatic
	fun getValue(element: CwtValue): String {
		return element.text
	}
	
	@JvmStatic
	fun getTruncatedValue(element: CwtValue):String{
		return element.value
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
		return element.value.toIntOrNull()?:0
	}
	//endregion
	
	//region CwtFloat
	@JvmStatic
	fun getFloatValue(element: CwtFloat): Float {
		return element.value.toFloatOrNull()?:0f
	}
	//endregion
	
	//region CwtString
	@JvmStatic
	fun getValue(element: CwtString): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun getTruncatedValue(element: CwtString): String {
		return element.value.truncate(truncateLimit)
	}
	
	@JvmStatic
	fun getStringValue(element: CwtString): String {
		return element.value
	}
	//endregion
	
	//region CwtBlock
	@JvmStatic
	fun getValue(element: CwtBlock): String {
		return emptyBlockString
	}
	
	@JvmStatic
	fun getTruncatedValue(element: CwtBlock): String {
		return blockFolder
	}
	
	@JvmStatic
	fun isEmpty(element: CwtBlock): Boolean {
		for(child in element.children) {
			if(child is CwtProperty || child is CwtValue || child is CwtOption) return false
		}
		return true
	}
	
	@JvmStatic
	fun isNotEmpty(element: CwtBlock): Boolean {
		for(child in element.children) {
			if(child is CwtProperty || child is CwtValue || child is CwtOption) return true
		}
		return true
	}
	
	@JvmStatic
	fun isObject(element: CwtBlock): Boolean {
		for(child in element.children) {
			when(child) {
				is CwtProperty -> return true
				is CwtOption -> return true
				is CwtValue -> return false
			}
		}
		return true
	}
	
	@JvmStatic
	fun isArray(element: CwtBlock): Boolean {
		for(child in element.children) {
			when(child) {
				is CwtProperty -> return false
				is CwtOption -> return false
				is CwtValue -> return true
			}
		}
		return true
	}
	
	@JvmStatic
	fun getComponents(element: CwtBlock): List<PsiElement> {
		//如果存在元素为property，则认为所有合法的元素都是property
		return if(element.isObject) element.propertyList else element.valueList
	}
	//endregion
	
	//region CwtDocumentationComment
	@JvmStatic
	fun getTokenType(element:CwtDocumentationComment): IElementType{
		return CwtTypes.DOCUMENTATION_COMMENT
	}
	//endregion
	
	//region CwtOptionComment
	@JvmStatic
	fun getTokenType(element:CwtOptionComment): IElementType{
		return CwtTypes.OPTION_COMMENT
	}
	//endregion
}