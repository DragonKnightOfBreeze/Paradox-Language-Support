package icu.windea.pls.core.search

import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 值集中的值的查询器。
 */
class ParadoxValueSetValuesSearcher : QueryExecutor<PsiElement, ParadoxValueSetValuesSearch.SearchParameters> {
	override fun execute(queryParameters: ParadoxValueSetValuesSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
		val project = queryParameters.project
		val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		return ParadoxValueSetValueIndex.processAllElements(queryParameters.valueSetName, project, scope) {
			if(queryParameters.name == null || matches(it, queryParameters.name)) {
				consumer.process(it)
			}
			true
		}
	}
	
	private fun matches(it: ParadoxScriptString, valueName: String): Boolean {
		return getName(it) == valueName
	}
	
	private fun getName(it: ParadoxScriptString): String? {
		return it.stub?.valueSetValueInfo?.name?.takeIfNotEmpty()
	}
	
	//override fun execute(queryParameters: ParadoxValueSetValuesSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
	//	val project = queryParameters.project
	//	val scope = queryParameters.scope
	//	when(scope) {
	//		is GlobalSearchScope -> {
	//			FileTypeIndex.processFiles(ParadoxScriptFileType, Processor { virtualFile ->
	//				val psiFile = virtualFile.toPsiFile<ParadoxScriptFile>(project) ?: return@Processor true
	//				processFile(psiFile, queryParameters, consumer)
	//			}, scope)
	//		}
	//		is LocalSearchScope -> {
	//			val virtualFiles = scope.virtualFiles
	//			for(virtualFile in virtualFiles) {
	//				val psiFile = virtualFile.toPsiFile<ParadoxScriptFile>(project) ?: continue
	//				processFile(psiFile, queryParameters, consumer)
	//			}
	//		}
	//	}
	//	return true
	//}
	
	//private fun processFile(file: ParadoxScriptFile, queryParameters: ParadoxValueSetValuesSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
	//	//必须进行索引，否则性能过低
	//	//for((valueSetName, valueSetValues) in file.valueSetValueMap) {
	//	//	if(queryParameters.valueSetName != valueSetName) continue
	//	//	for(valueSetValue in valueSetValues) {
	//	//		val element = valueSetValue.element ?: continue
	//	//		//排除带参数的情况
	//	//		if(element.isParameterAwareExpression()) return true
	//	//		//去除后面的作用域信息
	//	//		val name = element.value.substringBefore('@')
	//	//		if(queryParameters.name != null && queryParameters.name != name) continue
	//	//		val result = consumer.process(element)
	//	//		if(!result) break
	//	//	}
	//	//}
	//	//return true
	//	
	//	//psiFile.accept(object : ParadoxScriptRecursiveExpressionElementWalkingVisitor() {
	//	//	override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
	//	//		ProgressManager.checkCanceled()
	//	//		val config = element.getConfig() ?: return
	//	//		val dataType = config.expression.type
	//	//		if(dataType != CwtDataTypes.Value && dataType != CwtDataTypes.ValueSet) return
	//	//		val valueSetName = config.expression.value
	//	//		if(queryParameters.valueSetName != valueSetName) return
	//	//		//排除带参数的情况
	//	//		if(element.isParameterAwareExpression()) return
	//	//		//去除后面的作用域信息
	//	//		val name = element.value.substringBefore('@')
	//	//		if(queryParameters.name != null && queryParameters.name != name) return
	//	//		val result = consumer.process(element)
	//	//		if(!result) stopWalking()
	//	//		//不需要继续向下遍历
	//	//	}
	//	//})
	//}
}

