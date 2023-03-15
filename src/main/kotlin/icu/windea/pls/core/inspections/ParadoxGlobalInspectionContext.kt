package icu.windea.pls.core.inspections

import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.*
import com.intellij.codeInspection.lang.*
import com.intellij.openapi.util.*

class ParadoxGlobalInspectionContext: GlobalInspectionContextExtension<ParadoxGlobalInspectionContext> {
    companion object {
        private val _id = Key.create<ParadoxGlobalInspectionContext>("ParadoxGlobalInspectionContext")
    }
    
    override fun getID(): Key<ParadoxGlobalInspectionContext> {
        return _id
    }
    
    override fun performPreRunActivities(globalTools: MutableList<Tools>, localTools: MutableList<Tools>, context: GlobalInspectionContext) {
        
    }
    
    override fun performPostRunActivities(inspections: MutableList<InspectionToolWrapper<*, *>>, context: GlobalInspectionContext) {
        
    }
    
    override fun cleanup() {
        
    }
    
}