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

import android.app.Application;
import org.solovyev.common.listeners.JEvent;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.listeners.JEventListeners;
import org.solovyev.common.listeners.Listeners;
import org.solovyev.common.threads.DelayedExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 12/1/12
 * Time: 3:58 PM
 */

/**
 * This class aggregates several useful in any Android application interfaces and provides access to {@link Application} object from a static context.
 * NOTE: use this class only if you don't use and dependency injection library (if you use any you can directly set interfaces through it). <br/>
 * <p/>
 * Before first usage this class must be initialized by calling {@link App#init(android.app.Application)} method (for example, from {@link android.app.Application#onCreate()})
 */
public final class App {

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private static volatile Application application;

	@Nonnull
	private static volatile ServiceLocator locator;

	@Nonnull
	private static volatile DelayedExecutor uiThreadExecutor;

	@Nonnull
	private static volatile JEventListeners<JEventListener<? extends JEvent>, JEvent> eventBus;

	private static volatile boolean initialized;

	private App() {
		throw new AssertionError();
	}

    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

	public static <A extends Application & ServiceLocator> void init(@Nonnull A application) {
		init(application, new UiThreadExecutor(), Listeners.newEventBus(), application);
	}

	public static void init(@Nonnull Application application, @Nullable ServiceLocator serviceLocator) {
		init(application, new UiThreadExecutor(), Listeners.newEventBus(), serviceLocator);
	}

	public static void init(@Nonnull Application application,
							@Nonnull UiThreadExecutor uiThreadExecutor,
							@Nonnull JEventListeners<JEventListener<? extends JEvent>, JEvent> eventBus,
							@Nullable ServiceLocator serviceLocator) {
		if (!initialized) {
			App.application = application;
			App.uiThreadExecutor = uiThreadExecutor;
			App.eventBus = eventBus;
			if (serviceLocator != null) {
				App.locator = serviceLocator;
			} else {
				// empty service locator
				App.locator = new ServiceLocator() {
				};
			}

			App.initialized = true;
		} else {
			throw new IllegalStateException("Already initialized!");
		}
	}

	private static void checkInit() {
		if (!initialized) {
			throw new IllegalStateException("App should be initialized!");
		}
	}

	/**
	 * @return if App has already been initialized, false otherwise
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param <A> real type of application
	 * @return application instance which was provided in {@link App#init(android.app.Application)} method
	 */
	@Nonnull
	public static <A extends Application> A getApplication() {
		checkInit();
		return (A) application;
	}

	/**
	 * @param <L> real type of service locator
	 * @return instance of service locator user in application
	 */
	@Nonnull
	public static <L extends ServiceLocator> L getLocator() {
		checkInit();
		return (L) locator;
	}

	/**
	 * Method returns executor which runs on Main Application's thread. It's safe to do all UI work on this executor
	 *
	 * @return UI thread executor
	 */
	@Nonnull
	public static DelayedExecutor getUiThreadExecutor() {
		checkInit();
		return uiThreadExecutor;
	}

	/**
	 * @return application's event bus
	 */
	@Nonnull
	public static JEventListeners<JEventListener<? extends JEvent>, JEvent> getEventBus() {
		checkInit();
		return eventBus;
	}
}
