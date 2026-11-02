package icu.windea.pls.core.util.io

import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.core.toClasspathUrl
import org.apache.commons.io.file.PathUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.walk

object PathService {
    fun ensureDirectoryCreated(path: Path): Boolean {
        if (path.isDirectory()) return false
        if (path.exists()) {
            PathUtils.delete(path)
        }
        path.createDirectories()
        return true
    }

    fun ensureParentDirectoryCreated(path: Path): Boolean {
        val parent = path.parent ?: return false
        parent.createDirectories()
        return true
    }

    fun ensureFileCreatedFromClasspath(path: Path, url: String) {
        if (path.isRegularFile() && path.fileSize() > 0) return
        if (path.exists()) {
            PathUtils.delete(path)
        } else {
            PathUtils.createParentDirectories(path)
        }
        url.toClasspathUrl().openStream().use { Files.copy(it, path, StandardCopyOption.REPLACE_EXISTING) }
    }

    fun findExecutable(targetDirectory: Path, executableName: String): Path? {
        if (!targetDirectory.exists()) return null
        if (!targetDirectory.isDirectory()) return null
        return targetDirectory.walk().find { it.isRegularFile() && it.name.equals(executableName, ignoreCase = true) }
    }

    fun makeExecutable(path: Path) {
        if (SystemInfo.isWindows) return
        runCatching {
            val permissions = Files.getPosixFilePermissions(path) + setOf(
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_EXECUTE,
            )
            Files.setPosixFilePermissions(path, permissions)
        }
    }
}
