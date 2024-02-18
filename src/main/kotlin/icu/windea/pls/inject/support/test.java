package icu.windea.pls.inject.support;

import com.google.common.cache.*;
import kotlin.*;
import kotlin.jvm.internal.*;
import org.jetbrains.annotations.*;

public class test {
	private final LoadingCache a = CacheBuilder.from("spec").build(new CacheLoader() {
		@NotNull
		public Object load(@NotNull Object key) {
			Intrinsics.checkNotNullParameter(key, "key");
			String var2 = "Not yet implemented";
			throw new NotImplementedError("An operation is not implemented: " + var2);
		}
	});
}
