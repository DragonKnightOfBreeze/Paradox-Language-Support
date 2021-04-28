// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;

public interface CwtBlock extends CwtValue, PsiListLikeElement {

  @NotNull
  List<CwtDocumentationComment> getDocumentationCommentList();

  @NotNull
  List<CwtOption> getOptionList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  boolean isEmpty();

  boolean isNotEmpty();

  boolean isObject();

  boolean isArray();

  @NotNull
  List<PsiElement> getComponents();

}
