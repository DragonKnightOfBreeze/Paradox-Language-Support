package icu.windea.pls.config.cwt.expression

import icu.windea.pls.core.*

sealed interface CwtFilePathExpressionType {
	/**
	 * 判断指定的文件路径表达式是否匹配另一个相对于游戏或模组目录根路径的路径。
	 * @param expression 表达式。示例：`some/path`, `some/path/,.ext`。
	 * @param ignoreCase 匹配时是否需要忽略大小写。
	 */
	fun matches(expression: String, filePath: String, ignoreCase: Boolean = true): Boolean
	
	/**
	 * 解析指定的文件路径表达式，得到精确路径。
	 * @param expression 表达式。示例：`some/path`, `some/path/,.ext`。
	 * @param string 作为值的字符串。
	 */
	fun resolve(expression: String?, string: String): String?
	
	/**
	 * 根据指定的文件路径表达式，从精确路径中提取出需要的作为值的字符串。
	 */
	fun extract(expression: String?, filePath: String, ignoreCase: Boolean = true): String?
}

object CwtFilePathExpressionTypes {
	/**
	 * 精确路径。
	 */
	object Exact : CwtFilePathExpressionType {
		override fun matches(expression: String, filePath: String, ignoreCase: Boolean): Boolean {
			return expression.equals(filePath, ignoreCase)
		}
		
		override fun resolve(expression: String?, string: String): String {
			return string
		}
		
		override fun extract(expression: String?, filePath: String, ignoreCase: Boolean): String {
			return filePath
		}
	}
	
	/**
	 * 示例：`exp=some/path, s=some/path/file.txt`, `exp=some/path/,.ext, s=file`。
	 * @see CwtDataTypes.FilePath
	 */
	object FilePath : CwtFilePathExpressionType {
		override fun matches(expression: String, filePath: String, ignoreCase: Boolean): Boolean {
			val index = expression.lastIndexOf(',') //","应当最多出现一次
			if(index == -1) {
				//匹配父路径
				return expression.matchesPath(filePath, ignoreCase)
			} else {
				//匹配父路径+文件名前缀+扩展名
				val parentAndFileNamePrefix = expression.substring(0, index)
				if(!filePath.startsWith(parentAndFileNamePrefix, ignoreCase)) return false
				val fileNameSuffix = expression.substring(index + 1)
				return filePath.endsWith(fileNameSuffix, ignoreCase)
			}
		}
		
		override fun resolve(expression: String?, string: String): String {
			if(expression == null) return string
			val index = expression.lastIndexOf(',') //","应当最多出现一次
			if(index == -1) {
				if(expression.endsWith('/')) {
					return expression + string
				} else {
					return "$expression/$string"
				}
			} else {
				return expression.replace(",", string)
			}
		}
		
		override fun extract(expression: String?, filePath: String, ignoreCase: Boolean): String? {
			if(expression == null) return filePath
			val index = expression.lastIndexOf(',') //","应当最多出现一次
			if(index == -1) {
				return filePath.removePrefixOrNull(expression, ignoreCase)
			} else {
				val s1 = expression.substring(0, index)
				val s2 = expression.substring(index + 1)
				return filePath.removeSurroundingOrNull(s1, s2, ignoreCase)?.trimStart('/')
			}
		}
	}
	
	/**
	 * 示例：`exp=gfx/interface/icons/resources/, s=unity`
	 * @see CwtDataTypes.Icon
	 */
	object Icon : CwtFilePathExpressionType {
		override fun matches(expression: String, filePath: String, ignoreCase: Boolean): Boolean {
			return expression.matchesPath(filePath, ignoreCase) && filePath.endsWith(".dds", true)
		}
		
		override fun resolve(expression: String?, string: String): String? {
			if(expression == null) return null
			return "$expression/$string.dds"
		}
		
		override fun extract(expression: String?, filePath: String, ignoreCase: Boolean): String? {
			if(expression == null) return null
			return filePath.removeSurroundingOrNull(expression, ".dds", ignoreCase)?.trimStart('/')
		}
	}
}