package icu.windea.pls.lang.expression.impl

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.expression.*

/**
 * @see CwtDataType.Icon
 */
class ParadoxIconReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport() {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataType.Icon
    }
    
    override fun matchEntire(configExpression: CwtDataExpression, element: PsiElement?): Boolean {
        return false
    }
    
    override fun matches(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): Boolean {
        val expression = configExpression.value ?: return false
        return expression.matchesPath(filePath, ignoreCase) && filePath.endsWith(".dds", true)
    }
    
    override fun extract(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): String? {
        val expression = configExpression.value ?: return null
        return filePath.removeSurroundingOrNull(expression, ".dds", ignoreCase)?.substringAfterLast('/')
    }
    
    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): String? {
        return null //信息不足
    }
    
    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): String {
        return pathReference.substringAfterLast('/')
    }
    
    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.general.unresolvedPathReference.description.icon", pathReference, configExpression)
    }
}

/**
 * @see CwtDataType.FilePath
 */
class ParadoxFilePathReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport() {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataType.FilePath
    }
    
    //filepath[./] - 匹配相对于脚本文件所在目录的路径
    
    override fun matchEntire(configExpression: CwtDataExpression, element: PsiElement?): Boolean {
        return configExpression.value == null
    }
    
    override fun matches(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): Boolean {
        var expression = configExpression.value ?: return true
        val expressionRel = expression.removePrefixOrNull("./")
        if(expressionRel != null) {
            val contextParentPath = element?.fileInfo?.path?.parent ?: return false
            expression = contextParentPath + "/" + expressionRel
        }
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
    
    override fun extract(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): String? {
        var expression = configExpression.value ?: return filePath
        val expressionRel = expression.removePrefixOrNull("./")
        if(expressionRel != null) {
            val contextParentPath = element?.fileInfo?.path?.parent ?: return null
            expression = contextParentPath + "/" + expressionRel
        }
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if(index == -1) {
            return filePath.removePrefixOrNull(expression, ignoreCase)?.trimStart('/')
        } else {
            val s1 = expression.substring(0, index)
            val s2 = expression.substring(index + 1)
            return filePath.removeSurroundingOrNull(s1, s2, ignoreCase)?.trimStart('/')
        }
    }
    
    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): String? {
        val expression = configExpression.value ?: pathReference
        val expressionRel = expression.removePrefixOrNull("./")
        if(expressionRel != null) {
            return null //信息不足
        }
        val index = configExpression.lastIndexOf(',') //","应当最多出现一次
        if(index == -1) {
            if(expression.endsWith('/')) {
                return expression + pathReference
            } else {
                return "$expression/$pathReference"
            }
        } else {
            return expression.replace(",", pathReference)
        }
    }
    
    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): String {
        val expression = configExpression.value ?: pathReference.substringAfterLast('/')
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if(index == -1) {
            return pathReference.substringAfterLast('/')
        } else {
            return expression.replace(",", pathReference).substringAfterLast('/')
        }
    }
    
    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.general.unresolvedPathReference.description.filePath", pathReference, configExpression)
    }
}

/**
 * @see CwtDataType.FileName
 */
class ParadoxFileNameReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport() {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataType.FileName
    }
    
    //filename - filePath需要是文件名
    //filename[foo/bar] - filePath需要是文件名且该文件需要位于目录foo/bar或其子目录下
    
    override fun matchEntire(configExpression: CwtDataExpression, element: PsiElement?): Boolean {
        return false
    }
    
    override fun matches(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): Boolean {
        val expression = configExpression.value ?: return true
        return expression.matchesPath(filePath, ignoreCase)
    }
    
    override fun extract(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): String {
        return filePath.substringAfterLast('/')
    }
    
    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): String? {
        return null //信息不足
    }
    
    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): String {
        return pathReference.substringAfterLast('/') + ".dds"
    }
    
    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.general.unresolvedPathReference.description.fileName", pathReference, configExpression)
    }
}