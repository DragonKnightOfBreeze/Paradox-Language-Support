package icu.windea.pls.lang

import com.intellij.codeInsight.daemon.*
import com.intellij.ide.*
import com.intellij.ide.plugins.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.startup.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.dds.support.*
import icu.windea.pls.model.constants.*
import kotlinx.coroutines.*
import javax.imageio.spi.*

/**
 * 用于在特定生命周期执行特定的代码，例如，在IDE启动时初始化一些缓存数据。
 */
class PlsLifecycleListener : AppLifecycleListener, DynamicPluginListener, ProjectActivity {
    //for whole application

    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)

        //init caches for specific services
        initCaches()

        //create necessary files and directories & clean temp directories
        handlePaths()
    }

    private fun initCaches() {
        service<PlsDataProvider>().init()
        getDefaultProject().service<CwtConfigGroupService>().init()
    }

    @Suppress("UnstableApiUsage")
    private fun handlePaths() {
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            PlsPathConstants.data
            PlsPathConstants.images
            PlsPathConstants.imagesTemp
            writeAction { runCatchingCancelable { PlsPathConstants.texconvExeFile } }
        }
    }

    private val ddsImageReaderSpi by lazy { DdsImageReaderSpi() }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId.idString == PlsConstants.pluginId) {
            IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        }
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId.idString == PlsConstants.pluginId) {
            IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        }
    }

    //for each project

    override suspend fun execute(project: Project) {
        //refresh roots for libraries on project startup
        refreshRootsForLibraries(project)

        //refresh opened files on project startup (once only)
        refreshOnlyForOpenedFiles(project)

        //init caches for specific services
        initCaches(project)
    }

    private fun refreshRootsForLibraries(project: Project) {
        project.paradoxLibrary.refreshRoots()
        project.configGroupLibrary.refreshRoots()
    }

    @Suppress("KotlinConstantConditions")
    private val refreshOnProjectStartup = PlsSettingConstants.refreshOnProjectStartup
    private val refreshedProjectIdsKey = createKey<MutableSet<String>>("pls.refreshedProjectIds")
    private val refreshedProjectIds by lazy { ApplicationManager.getApplication().getOrPutUserData(refreshedProjectIdsKey) { mutableSetOf() } }

    private fun refreshOnlyForOpenedFiles(project: Project) {
        //在IDE启动后首次打开某个项目时，刷新此项目已打开的脚本文件和本地化文件

        //否则，如果插件更新后内置的规则文件也更新了，对于已打开的脚本文件和文本化文件，
        //缓存的代码检查结果、内嵌提示等信息可能未正确刷新，仍然是过时的，需要通过文件更改来触发刷新

        //TODO 1.3.37+ 也许有更好的方式来解决这个问题

        if (!refreshOnProjectStartup) return
        if (!refreshedProjectIds.add(project.locationHash)) return

        val fileEditorManager = FileEditorManager.getInstance(project) ?: return
        val daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project) ?: return
        val openFiles = fileEditorManager.openFiles
        if (openFiles.isEmpty()) return
        val psiFiles = runReadAction {
            openFiles.filter { it.fileType is ParadoxBaseFileType }.mapNotNull { it.toPsiFile(project) }
        }
        if (psiFiles.isEmpty()) return
        runInEdt {
            psiFiles.forEach { psiFile -> daemonCodeAnalyzer.restart(psiFile) }
        }
    }

    private fun initCaches(project: Project) {
        project.service<CwtConfigGroupService>().init()
    }

    object Keys : KeyRegistry() {
        val refreshedProjectIds by createKey<MutableSet<String>>(Keys)
    }
}
