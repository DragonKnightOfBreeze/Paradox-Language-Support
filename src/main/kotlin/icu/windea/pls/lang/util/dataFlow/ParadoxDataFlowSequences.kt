package icu.windea.pls.lang.util.dataFlow

import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

typealias ParadoxMemberSequence = DataFlowSequence<ParadoxScriptMemberElement, ParadoxDataFlowOptions.Member>

typealias ParadoxPropertySequence = DataFlowSequence<ParadoxScriptProperty, ParadoxDataFlowOptions.Member>

typealias ParadoxValueSequence = DataFlowSequence<ParadoxScriptValue, ParadoxDataFlowOptions.Member>

typealias ParadoxLocalisationSequence = DataFlowSequence<ParadoxLocalisationProperty, ParadoxDataFlowOptions.Localisation>

typealias ParadoxRowSequence = DataFlowSequence<ParadoxCsvRow, ParadoxDataFlowOptions.Base>

typealias ParadoxColumnSequence = DataFlowSequence<ParadoxCsvColumn, ParadoxDataFlowOptions.Base>
