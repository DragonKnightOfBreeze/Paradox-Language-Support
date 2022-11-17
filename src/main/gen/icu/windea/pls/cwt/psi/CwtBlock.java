// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.psi.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

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

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<PsiElement> getComponents();

}
