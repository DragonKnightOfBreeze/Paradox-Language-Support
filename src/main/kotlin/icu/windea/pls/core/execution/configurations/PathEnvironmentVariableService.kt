package icu.windea.pls.core.execution.configurations

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.util.text.StringUtil
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isExecutable

/** @see PathEnvironmentVariableUtil */
object PathEnvironmentVariableService {
    private val ourOnPathCache: MutableMap<String, Boolean> = Collections.synchronizedMap(HashMap())

    fun isOnPath(name: String): Boolean {
        // NOTE 3.0.1 [compatibility] target method was introduced in IDEA-253 and missing in IDEA-252, so there is a workaround
        // com.intellij.execution.configurations.PathEnvironmentVariableUtil.isOnPath

        require(name.indexOf('\\') < 0 && name.indexOf('/') < 0) { name }
        var result = ourOnPathCache.get(name)
        if (result == null) {
            result = false
            val path = PathEnvironmentVariableUtil.getPathVariableValue()
            if (path != null) {
                for (dir in StringUtil.tokenize(path, File.pathSeparator)) {
                    if (Path.of(dir, name).isExecutable()) {
                        result = true
                        break
                    }
                }
            }
            ourOnPathCache.put(name, result!!)
        }
        return result
    }
}
