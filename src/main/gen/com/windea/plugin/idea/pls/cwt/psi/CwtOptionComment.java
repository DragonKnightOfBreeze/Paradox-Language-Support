// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CwtOptionComment extends PsiElement {

  @NotNull
  List<CwtOption> getOptionList();

  @NotNull
  List<CwtValue> getValueList();

}
