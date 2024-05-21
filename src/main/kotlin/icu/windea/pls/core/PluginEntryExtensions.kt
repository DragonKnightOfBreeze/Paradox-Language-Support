package icu.windea.pls.core

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.io.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*

fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

//from official documentation: Never acquire service instances prematurely or store them in fields for later use.

fun getSettings() = service<ParadoxSettings>().state

fun getProfilesSettings() = service<ParadoxProfilesSettings>().state

fun getConfigGroup(gameType: ParadoxGameType?) = getDefaultProject().service<CwtConfigGroupService>().getConfigGroup(gameType)

fun getConfigGroup(project: Project, gameType: ParadoxGameType?) = project.service<CwtConfigGroupService>().getConfigGroup(gameType)

val Paths get() = service<ParadoxPathProvider>()

val Urls get() = service<ParadoxUrlProvider>()