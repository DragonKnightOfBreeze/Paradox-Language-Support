// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.windea.plugin.idea.paradox.script.reference.ParadoxScriptStringAsPropertyPsiReference;

public interface ParadoxScriptString extends ParadoxScriptStringValue {

  @Nullable
  PsiElement getQuotedStringToken();

  @Nullable
  PsiElement getStringToken();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptStringAsPropertyPsiReference getReference();

}
