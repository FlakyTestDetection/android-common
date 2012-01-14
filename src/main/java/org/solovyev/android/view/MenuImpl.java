/*
 * Copyright (c) 2009-2011. Created by serso aka se.solovyev.
 * For more information, please, contact se.solovyev@gmail.com
 * or visit http://se.solovyev.org
 */

package org.solovyev.android.view;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.common.utils.CollectionsUtils;

import java.util.ArrayList;
import java.util.List;

/**
* User: serso
* Date: 12/18/11
* Time: 1:31 PM
*/
public class MenuImpl<T extends AMenuItem<D>, D> implements AMenu<T, D> {

	private final List<T> menuItems = new ArrayList<T>();

	@NotNull
	public static <T extends AMenuItem<D>, D> AMenu<T, D> newInstance(T... menuItems) {
		return new MenuImpl<T, D>(menuItems);
	}

	@NotNull
	public static <T extends AMenuItem<D>, D> AMenu<T, D> newInstance(@NotNull List<T> menuItems) {
		return new MenuImpl<T, D>(menuItems);
	}

	private MenuImpl(T... menuItems) {
		this(CollectionsUtils.asList(menuItems));
	}

	private MenuImpl(@NotNull List<T> menuItems) {
		this.menuItems.addAll(menuItems);
	}

	@Override
	@Nullable
	public T itemAt(int i) {
		if (i >= 0 && i < menuItems.size()) {
			return menuItems.get(i);
		} else {
			return null;
		}
	}

	@Override
	@NotNull
	public CharSequence[] getMenuCaptions(@NotNull final Context context) {
		final CharSequence[] result = new CharSequence[this.menuItems.size()];
		for (int i = 0; i < this.menuItems.size(); i++) {
			result[i] = this.menuItems.get(i).getCaption(context);
		}
		return result;
	}
}
