package icu.windea.pls.core

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import java.util.LinkedList

typealias ParadoxDefinitionPath = ParadoxElementPath<ParadoxDefinitionProperty, ParadoxScriptFile>

/**
 * 定义或定义属性相对于所属文件或定义的路径。保留大小写。
 *
 * 示例：
 * * （空字符串） - 对应所属文件或定义本身。
 * * `foo` - 对应所属文件或定义中名为"foo"的属性。
 * * `foo/bar` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性
 * * `foo/"bar"` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性（在脚本中用引号括起）
 * * `#` - 对应所属定义的值（非代码块）
 * * `-` - 对应所属文件或定义的值（代码块）中的（任意索引位置的）值（代码块或非代码块）
 * * `foo/-` 对应文所属文件或定义中名为"foo"的属性的值（代码块）中的（任意索引位置的）值（代码块或非代码块）
 */
@Suppress("unused")
class ParadoxElementPath<E : PsiElement, R : PsiElement> private constructor(
	val originalSubPaths: List<String>, //注意：这里传入的子路径需要保留可能的括起的双引号
	val pointer: SmartPsiElementPointer<out E>,
	val rootPointer: SmartPsiElementPointer<out R>?,
) : Iterable<String> {
	companion object Resolver {
		/**
		 * 解析指定定义相对于所属文件的属性路径。
		 */
		fun resolveFromFile(element: ParadoxDefinitionProperty, maxDepth: Int = -1): ParadoxDefinitionPath? {
			if(element is ParadoxScriptFile) {
				return resolveEmptyPath(element)
			}
			var current: PsiElement = element
			var depth = 0
			val originalSubPaths = LinkedList<String>()
			while(current !is ParadoxScriptFile) {
				when {
					current is PsiFile -> return resolveEmptyPath(element) //不应当出现
					current is ParadoxScriptProperty -> {
						originalSubPaths.addFirst(current.propertyKey.text) //这里需要使用原始文本
						depth++
					}
					current is ParadoxScriptValue && current.isLonelyValue() -> {
						originalSubPaths.addFirst("-")
						depth++
					}
				}
				//如果发现深度超出指定的最大深度，则直接返回null
				if(maxDepth != -1 && maxDepth < depth) return null
				current = current.parent ?: break
			}
			val file = current as ParadoxScriptFile
			val pointer = element.createPointer(file)
			val rootPointer = file.createPointer(file)
			return ParadoxElementPath(originalSubPaths, pointer, rootPointer)
		}
		
		/**
		 * 解析指定元素相对于所属定义的属性路径。
		 */
		fun <E : PsiElement> resolveFromDefinition(element: E): ParadoxElementPath<E, ParadoxDefinitionProperty>? {
			var current: PsiElement = element
			var depth = 0
			val subPaths = LinkedList<String>()
			var definition: ParadoxDefinitionProperty? = null
			while(current !is ParadoxScriptFile) {
				when {
					current is PsiFile -> return resolveEmptyPath(element) //不应当出现
					current is ParadoxScriptProperty -> {
						val definitionInfo = current.definitionInfo
						if(definitionInfo != null) {
							if(element is ParadoxScriptValue) { //处理指定的元素直接是所属定义的值（非代码块）的情况
								subPaths.addFirst("#")
							}
							definition = current
							break
						}
						subPaths.addFirst(current.propertyKey.text) //这里需要使用原始文本
						depth++
					}
					current is ParadoxScriptValue && current.isLonelyValue() -> {
						subPaths.addFirst("-")
						depth++
					}
				}
				current = current.parent ?: break
			}
			if(definition == null) return null //如果未找到所属的definition，则直接返回null
			val file = definition.containingFile
			val pointer = element.createPointer(file)
			val rootPointer = definition.createPointer(file)
			return ParadoxElementPath(subPaths, pointer, rootPointer)
		}
		
		fun <E : PsiElement, R : PsiElement> resolveEmptyPath(element: E): ParadoxElementPath<E, R> {
			val pointer = element.createPointer()
			return ParadoxElementPath(emptyList(), pointer, null)
		}
	}
	
	val originalPath = originalSubPaths.joinToString("/")
	val subPaths = originalSubPaths.map { it.unquote() }
	val path = subPaths.joinToString("/")
	val length = originalSubPaths.size
	
	fun isEmpty(): Boolean {
		return length == 0
	}
	
	override fun iterator(): Iterator<String> {
		return originalSubPaths.iterator()
	}
	
	override fun toString(): String {
		return originalPath
	}
}