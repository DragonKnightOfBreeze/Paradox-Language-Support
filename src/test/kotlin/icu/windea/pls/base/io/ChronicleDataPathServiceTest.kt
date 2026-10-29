package icu.windea.pls.base.io

import org.junit.Assert
import org.junit.Test
import kotlin.io.path.isDirectory
import kotlin.io.path.name

/**
 * @see ChronicleDataPathService
 */
class ChronicleDataPathServiceTest {
    private val service = ChronicleDataPathServiceImpl()

    @Test
    fun checkDirectories() {
        Assert.assertTrue(service.path.isDirectory())
        Assert.assertTrue(service.imagesPath.isDirectory())
        Assert.assertTrue(service.imagesTempPath.isDirectory())
        Assert.assertTrue(service.lintResultsPath.isDirectory())
        Assert.assertTrue(service.toolsPath.isDirectory())
    }

    @Test
    fun checkFiles() {
        val texconvExePath = service.texconvExePath
        Assert.assertEquals("texconv.exe", texconvExePath.fileName.toString())
        Assert.assertEquals("external-tools", texconvExePath.parent?.name)
        Assert.assertTrue(texconvExePath.parent!!.isDirectory())
    }
}
