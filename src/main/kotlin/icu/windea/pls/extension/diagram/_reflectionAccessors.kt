package icu.windea.pls.extension.diagram

import com.intellij.diagram.components.*
import com.intellij.openapi.actionSystem.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.extension.diagram.components.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

var DiagramNodeItemComponentEx.left: SimpleColoredComponent by memberProperty<DiagramNodeItemComponent, _>("myLeft")
var DiagramNodeItemComponentEx.right: SimpleColoredComponent by memberProperty<DiagramNodeItemComponent, _>("myRight")

var DiagramNodeBodyComponent.itemComponent: DiagramNodeItemComponent by memberProperty<DiagramNodeBodyComponent, _>("myItemComponent")