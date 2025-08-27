package icu.windea.pls.ep.expression

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configExpression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.constants.*

/**
 * @see CwtDataTypes.Icon
 */
class ParadoxIconReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataTypes.Icon
    }

    //icon[] -  filePath需要是不带扩展名的文件名（其扩展名必须是合法的图片的扩展名）
    //icon[foo/bar] - filePath需要是不带扩展名的文件名（其扩展名必须是合法的图片的扩展名），且该文件需要位于目录foo/bar中

    override fun matches(configExpression: CwtDataExpression, element: PsiElement?, filePath: String): Boolean {
        val filePathWithoutExtension = getFilePathWithoutExtension(filePath) ?: return false
        val expression = configExpression.value ?: return true
        return expression.matchesPath(filePathWithoutExtension, trim = true)
    }

    override fun extract(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): String? {
        val filePathWithoutExtension = getFilePathWithoutExtension(filePath) ?: return null
        val expression = configExpression.value ?: return filePathWithoutExtension
        return filePathWithoutExtension.removePrefixOrNull(expression, ignoreCase)?.trimFast('/')
    }

    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): Set<String>? {
        return null //信息不足
    }

    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): Set<String> {
        val fileNameWithoutExtension = pathReference.substringAfterLast('/')
        return PlsConstants.imageFileExtensions.mapTo(mutableSetOf()) { "$fileNameWithoutExtension.$it" }
    }

    private fun getFilePathWithoutExtension(filePath: String): String? {
        val i = filePath.lastIndexOf('.')
        if (i == -1) return null
        val extension = filePath.substring(i + 1).lowercase()
        if (extension !in PlsConstants.imageFileExtensions) return null
        return filePath.substring(0, i)
    }

    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.unresolvedPathReference.desc.icon", pathReference, configExpression)
    }
}

/**
 * @see CwtDataTypes.FilePath
 */
class ParadoxFilePathReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataTypes.FilePath
    }

    //filepath - 匹配任意路径
    //filepath[./] - 匹配相对于脚本文件所在目录的路径

    override fun matches(configExpression: CwtDataExpression, element: PsiElement?, filePath: String): Boolean {
        var expression = configExpression.value ?: return true
        val expressionRel = expression.removePrefixOrNull("./")
        if (expressionRel != null) {
            val contextParentPath = element?.fileInfo?.path?.parent ?: return false
            expression = "$contextParentPath/$expressionRel"
        }
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if (index == -1) {
            //匹配父路径
            return expression.matchesPath(filePath, trim = true)
        } else {
            //匹配父路径+文件名前缀+扩展名
            val parentAndFileNamePrefix = expression.substring(0, index)
            if (!filePath.startsWith(parentAndFileNamePrefix)) return false
            val fileNameSuffix = expression.substring(index + 1)
            return filePath.endsWith(fileNameSuffix)
        }
    }

    override fun extract(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): String? {
        var expression = configExpression.value ?: return filePath
        val expressionRel = expression.removePrefixOrNull("./")
        if (expressionRel != null) {
            val contextParentPath = element?.fileInfo?.path?.parent ?: return null
            expression = "$contextParentPath/$expressionRel"
        }
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if (index == -1) {
            return filePath.removePrefixOrNull(expression, ignoreCase)?.trimFast('/')
        } else {
            //optimized
            val l1 = index
            if (!filePath.regionMatches(0, expression, 0, l1, ignoreCase)) return null
            val l2 = expression.length - index - 1
            if (!filePath.regionMatches(filePath.length - l2, expression, index + 1, l2, ignoreCase)) return null
            return filePath.substring(l1, filePath.length - l2).trimFast('/')
            //val s1 = expression.substring(0, index)
            //val s2 = expression.substring(index + 1)
            //return filePath.removeSurroundingOrNull(s1, s2, ignoreCase)?.trimFast('/')
        }
    }

    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): Set<String>? {
        val expression = configExpression.value ?: return pathReference.singleton.set()
        val expressionRel = expression.removePrefixOrNull("./")
        if (expressionRel != null) {
            return null //信息不足
        }
        val index = configExpression.expressionString.lastIndexOf(',') //","应当最多出现一次
        if (index == -1) {
            if (expression.endsWith('/')) {
                return "$expression$pathReference".singleton.set()
            } else {
                return "$expression/$pathReference".singleton.set()
            }
        } else {
            return expression.replace(",", pathReference).singleton.set()
        }
    }

    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): Set<String> {
        val expression = configExpression.value ?: return pathReference.substringAfterLast('/').singleton.set()
        val index = expression.lastIndexOf(',') //","应当最多出现一次
        if (index == -1) {
            return pathReference.substringAfterLast('/').singleton.set()
        } else {
            return expression.replace(",", pathReference).substringAfterLast('/').singleton.set()
        }
    }

    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.unresolvedPathReference.desc.filePath", pathReference, configExpression)
    }
}

/**
 * @see CwtDataTypes.FileName
 */
class ParadoxFileNameReferenceExpressionSupport : ParadoxPathReferenceExpressionSupport {
    override fun supports(configExpression: CwtDataExpression): Boolean {
        return configExpression.type == CwtDataTypes.FileName
    }

    //filename - filePath需要是文件名
    //filename[foo/bar] - filePath需要是文件名，且该文件需要位于目录foo/bar中

    override fun matches(configExpression: CwtDataExpression, element: PsiElement?, filePath: String): Boolean {
        val expression = configExpression.value ?: return true
        return expression.matchesPath(filePath, trim = true)
    }

    override fun extract(configExpression: CwtDataExpression, element: PsiElement?, filePath: String, ignoreCase: Boolean): String {
        return filePath.substringAfterLast('/')
    }

    override fun resolvePath(configExpression: CwtDataExpression, pathReference: String): Set<String>? {
        return null //信息不足
    }

    override fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): Set<String> {
        return pathReference.substringAfterLast('/').singleton.set()
    }

    override fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String {
        return PlsBundle.message("inspection.script.unresolvedPathReference.desc.fileName", pathReference, configExpression)
    }
}
