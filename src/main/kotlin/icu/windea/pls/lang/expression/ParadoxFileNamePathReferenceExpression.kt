package icu.windea.pls.lang.expression

import icu.windea.pls.config.cwt.expression.*

/**
 * @see CwtDataType.FileName
 */
class ParadoxFileNamePathReferenceExpression : ParadoxPathReferenceExpression {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        TODO("Not yet implemented")
    }
    
    override fun matches(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean): Boolean {
        TODO("Not yet implemented")
    }
    
    override fun extract(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean): String? {
        TODO("Not yet implemented")
    }
}