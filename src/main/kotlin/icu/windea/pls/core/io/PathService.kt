package icu.windea.pls.core.io

import com.intellij.util.io.createParentDirectories
import com.intellij.util.io.delete
import icu.windea.pls.core.toClasspathUrl
import org.apache.commons.io.file.PathUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

object PathService {
    fun ensureDirectoryCreated(path: Path) {
        if (path.isDirectory()) return
        if (path.exists()) {
            path.delete()
        }
        path.createDirectories()
    }

    fun ensureFileCreatedFromClasspath(path: Path, url: String) {
        if (path.isRegularFile()) return
        if (path.exists()) {
            if (path.isDirectory()) PathUtils.cleanDirectory(path)
            PathUtils.delete(path)
        } else {
            path.createParentDirectories()
        }
        url.toClasspathUrl().openStream().use { Files.copy(it, path, StandardCopyOption.REPLACE_EXISTING) }
    }
}
