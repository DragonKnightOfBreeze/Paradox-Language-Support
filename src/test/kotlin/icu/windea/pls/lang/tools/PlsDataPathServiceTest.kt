package icu.windea.pls.lang.tools

import org.junit.Assert
import org.junit.Test
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

class PlsDataPathServiceTest {
    private val service = PlsDataPathServiceImpl()

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
        Assert.assertTrue(service.texconvExePath.isRegularFile())
    }
}
