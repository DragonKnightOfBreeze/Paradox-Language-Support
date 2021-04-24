// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.windea.plugin.idea.pls.script.reference.ParadoxScriptStringPropertyPsiReference;

public interface ParadoxScriptString extends ParadoxScriptStringValue {

  @Nullable
  PsiElement getQuotedStringToken();

  @Nullable
  PsiElement getStringToken();

  @NotNull
  String getValue();

  @NotNull
  PsiElement setValue(@NotNull String name);

  @NotNull
  ParadoxScriptStringPropertyPsiReference getReference();

  //WARNING: getReferneceName(...) is skipped
  //matching getReferneceName(ParadoxScriptString, ...)
  //methods are not found in ParadoxScriptPsiImplUtil

}
