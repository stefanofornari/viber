/*
 * Copyright 2026 the original author or authors from the Viber project
 * (https://stefanofornari.github.io/llm-toolify).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ste.ai.viber.util;

import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class Safe {
    public static final void ifNotNull(final Object o, final Runnable r) {
        if (o != null) {
            r.run();
        }
    }

    public static final void ifNull(final Object o, final Runnable t) {
        if (o == null) {
            t.run();
        }
    }

    public static final void ifNull(final Object o, final Runnable t, final Runnable f) {
        if (o == null) {
            t.run(); // true
        } else {
            f.run(); // false
        }
    }

    public static final <T> void safe(final T o, Consumer<T> c) {
    if (o != null) {
        c.accept(o);
    }
}

    public static final void requireNonNull(final Object o, final String name) {
        if (o == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    public static final void requireNonEmpty(final String s, final String name) {
        if (StringUtils.isEmpty(s)) {
            throw new IllegalArgumentException(name + " must be non empty");
        }
    }

    public static final void requireNonBlank(final String s, final String name) {
        if (StringUtils.isBlank(s)) {
            throw new IllegalArgumentException(name + " must be non blank");
        }
    }
}
