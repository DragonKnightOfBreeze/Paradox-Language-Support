package icu.windea.pls.script.psi

abstract class ParadoxScriptRecursiveExpressionElementWalkingVisitor : ParadoxScriptRecursiveElementWalkingVisitor() {
	final override fun visitBoolean(o: ParadoxScriptBoolean) {}
	final override fun visitInt(o: ParadoxScriptInt) {}
	final override fun visitFloat(o: ParadoxScriptFloat) {}
	final override fun visitColor(o: ParadoxScriptColor) {}
	final override fun visitInlineMath(o: ParadoxScriptInlineMath) {}
	final override fun visitParameter(o: ParadoxScriptParameter) {}
	final override fun visitParadoxInputParameter(o: ParadoxInputParameter) {}
	final override fun visitParameterCondition(o: ParadoxScriptParameterCondition) {}
	final override fun visitVariable(o: ParadoxScriptVariable) {}
	
	override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
		
	}
	
	override fun visitPropertyKey(element: ParadoxScriptPropertyKey) {
		visitExpressionElement(element)
	}
	
	override fun visitString(element: ParadoxScriptString) {
		visitExpressionElement(element)
	}
}