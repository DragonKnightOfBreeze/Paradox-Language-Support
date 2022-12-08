package icu.windea.pls.core.index

import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxStellarisNameFormatKeyIndex : FileBasedIndexExtension<String, String>(){
	//localisationName - valueSetName ('xxx' in 'stellaris_name_format[xxx]')
	
	private val _id = ID.create<String, String>("paradox.stellarisNameFormat.key.index")
	
	override fun getName() = _id
	
	override fun getVersion() = 1 //0.7.7
	
	private val _dataIndexer = DataIndexer<String, String, FileContent> { inputData ->
		buildMap {
			inputData.psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
				override fun visitElement(element: PsiElement) {
					if(element is ParadoxScriptStringExpressionElement) {
						doVisitStringExpressionElement(element)
					}
					if(element is ParadoxScriptExpressionContextElement) {
						super.visitElement(element)
					}
				}
				
				private fun doVisitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
					val configs = ParadoxCwtConfigHandler.resolveConfigs(element)
					val config = configs.firstOrNull() ?: return
					val configExpression = config.expression
					if(configExpression.type != CwtDataTypes.Localisation || configExpression.extraValue != CwtDataTypeAlias.StellarisNameFormat) return
					val valueSetName = configExpression.value ?: return
					put(element.value, valueSetName)
				}
			})
		}
	}
	
	override fun getIndexer() = _dataIndexer
	
	override fun getKeyDescriptor() = EnumeratorStringDescriptor.INSTANCE
	
	override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE
	
	private val _inputFilter = FileBasedIndex.InputFilter { it.fileInfo?.fileType == ParadoxFileType.ParadoxScript }
	
	override fun getInputFilter() = _inputFilter
	
	override fun dependsOnFileContent() = true
}