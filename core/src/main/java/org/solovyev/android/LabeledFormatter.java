/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.common.text.Formatter;

/**
 * User: serso
 * Date: 8/10/12
 * Time: 1:01 PM
 */
public final class LabeledFormatter<T extends Labeled> implements Formatter<T> {

    @Nonnull
    private Context context;

    public LabeledFormatter(@Nonnull Context context) {
        this.context = context;
    }

    @Override
    public String formatValue(@Nullable T value) throws IllegalArgumentException {
        assert value != null;
        return context.getString(value.getCaptionResId());
    }
}
