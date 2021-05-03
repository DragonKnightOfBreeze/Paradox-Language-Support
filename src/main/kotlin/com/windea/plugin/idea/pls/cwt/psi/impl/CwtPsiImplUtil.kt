package com.windea.plugin.idea.pls.cwt.psi.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.cwt.psi.*
import javax.swing.*

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
	fun setName(element:CwtProperty,name:String): PsiElement {
		throw IncorrectOperationException(message("cannotBeRenamed"))
	}
	
	@JvmStatic
	fun checkRename(element: CwtProperty){
		throw IncorrectOperationException(message("cannotBeRenamed"))
	}
	
	@JvmStatic
	fun getPropertyName(element: CwtProperty):String{
		return element.propertyKey.name
	}
	
	@JvmStatic
	fun getPropertyValue(element:CwtProperty):String{
		return element.value?.value.orEmpty()
	}
	
	@JvmStatic
	fun getPropertyTruncatedValue(element:CwtProperty):String{
		return element.value?.truncatedValue.orEmpty()
	}
	//endregion
	
	//region CwtOption
	@JvmStatic
	fun getIcon(element: CwtOption, @Iconable.IconFlags flags: Int): Icon {
		return com.windea.plugin.idea.pls.cwtOptionIcon
	}
	
	@JvmStatic
	fun getName(element: CwtOption):String{
		return element.optionName
	}
	
	@JvmStatic
	fun setName(element:CwtOption,name:String): PsiElement {
		throw IncorrectOperationException(message("cannotBeRenamed"))
	}
	
	@JvmStatic
	fun checkRename(element: CwtOption){
		throw IncorrectOperationException(message("cannotBeRenamed"))
	}
	
	@JvmStatic
	fun getOptionName(element: CwtOption):String{
		return element.optionKey.name
	}
	
	@JvmStatic
	fun getOptionValue(element:CwtOption):String{
		return element.value?.value.orEmpty()
	}
	
	@JvmStatic
	fun getOptionTruncatedValue(element:CwtOption):String{
		return element.value?.truncatedValue.orEmpty()
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
	
	//region CwtString
	@JvmStatic
	fun getValue(element: CwtString): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun getTruncatedValue(element: CwtString): String {
		return element.value.truncate(truncateLimit)
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
		element.forEachChild {
			if(it is CwtProperty || it is CwtValue || it is CwtOption) return false
		}
		return true
	}
	
	@JvmStatic
	fun isNotEmpty(element: CwtBlock): Boolean {
		element.forEachChild {
			if(it is CwtProperty || it is CwtValue || it is CwtOption) return true
		}
		return true
	}
	
	@JvmStatic
	fun isObject(element: CwtBlock): Boolean {
		element.forEachChild {
			when(it) {
				is CwtProperty -> return true
				is CwtOption -> return true
				is CwtValue -> return false
			}
		}
		return true
	}
	
	@JvmStatic
	fun isArray(element: CwtBlock): Boolean {
		element.forEachChild {
			when(it) {
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
}