package icu.windea.pls.lang.util.dataFlow

import icu.windea.pls.csv.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

typealias ParadoxMemberSequence = DataFlowSequence<ParadoxScriptMemberElement, ParadoxDataFlowOptions.Member>

typealias ParadoxPropertySequence = DataFlowSequence<ParadoxScriptProperty, ParadoxDataFlowOptions.Member>

typealias ParadoxValueSequence = DataFlowSequence<ParadoxScriptValue, ParadoxDataFlowOptions.Member>

typealias ParadoxLocalisationSequence = DataFlowSequence<ParadoxLocalisationProperty, ParadoxDataFlowOptions.Localisation>

typealias ParadoxRowSequence = DataFlowSequence<ParadoxCsvRow, ParadoxDataFlowOptions.Base>

typealias ParadoxColumnSequence = DataFlowSequence<ParadoxCsvColumn, ParadoxDataFlowOptions.Base>
