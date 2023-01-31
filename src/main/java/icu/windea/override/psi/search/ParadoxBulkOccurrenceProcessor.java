package icu.windea.override.psi.search;

import com.intellij.psi.*;
import com.intellij.util.text.*;
import org.jetbrains.annotations.*;

//com.intellij.psi.impl.search.BulkOccurrenceProcessor

@FunctionalInterface
interface ParadoxBulkOccurrenceProcessor {
  boolean execute(@NotNull PsiElement scope, int @NotNull [] offsetsInScope, @NotNull StringSearcher searcher);
}