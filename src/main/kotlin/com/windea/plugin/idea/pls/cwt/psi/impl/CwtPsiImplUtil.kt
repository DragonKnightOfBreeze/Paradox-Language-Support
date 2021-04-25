package com.windea.plugin.idea.pls.cwt.psi.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.cwt.psi.*
import javax.swing.*

object CwtPsiImplUtil {
	//region CwtBlock
	@JvmStatic
	fun getIcon(element: CwtProperty, @Iconable.IconFlags flags: Int): Icon {
		return cwtPropertyIcon
	}
	
	@JvmStatic
	fun getName(element: CwtProperty):String{
		return element.propertyKey
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
	fun getPropertyKey(element: CwtProperty):String{
		return element.key.name
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
	
	//region CwtKey
	@JvmStatic
	fun getName(element: CwtKey):String{
		return element.keyToken.text.unquote()
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
	fun isObject(element: CwtBlock): Boolean {
		element.forEachChild {
			when(it) {
				is CwtProperty -> return true
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