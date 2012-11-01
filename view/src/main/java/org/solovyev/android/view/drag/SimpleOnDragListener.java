/*
 * Copyright (c) 2009-2011. Created by serso aka se.solovyev.
 * For more information, please, contact se.solovyev@gmail.com
 */

package org.solovyev.android.view.drag;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MotionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.view.R;
import org.solovyev.common.MutableObject;
import org.solovyev.common.interval.Interval;
import org.solovyev.common.interval.IntervalImpl;
import org.solovyev.common.math.MathUtils;
import org.solovyev.common.math.Point2d;
import org.solovyev.common.text.Mapper;
import org.solovyev.common.text.NumberIntervalMapper;

import java.util.HashMap;
import java.util.Map;

public class SimpleOnDragListener implements OnDragListener, DragPreferencesChangeListener {

	@NotNull
	public static final Point2d axis = new Point2d(0, 1);

	@NotNull
	private DragProcessor dragProcessor;

	@NotNull
	private Preferences preferences;

	public SimpleOnDragListener(@NotNull Preferences preferences) {
		this.preferences = preferences;
	}

	public SimpleOnDragListener(@NotNull DragProcessor dragProcessor, @NotNull Preferences preferences) {
		this.dragProcessor = dragProcessor;
		this.preferences = preferences;
	}

	@Override
	public boolean onDrag(@NotNull DragButton dragButton, @NotNull DragEvent event) {
		boolean result = false;

		logDragEvent(dragButton, event);

		final Point2d startPoint = event.getStartPoint();
		final MotionEvent motionEvent = event.getMotionEvent();

		// init end point
		final Point2d endPoint = new Point2d(motionEvent.getX(), motionEvent.getY());

		final float distance = MathUtils.getDistance(startPoint, endPoint);

		final MutableObject<Boolean> right = new MutableObject<Boolean>();
		final double angle = Math.toDegrees(MathUtils.getAngle(startPoint, MathUtils.sum(startPoint, axis), endPoint, right));
		Log.d(String.valueOf(dragButton.getId()), "Angle: " + angle);
		Log.d(String.valueOf(dragButton.getId()), "Is right?: " + right.getObject());

		final double duration = motionEvent.getEventTime() - motionEvent.getDownTime();

		final Preference distancePreferences = preferences.getPreferencesMap().get(PreferenceType.distance);
		final Preference anglePreferences = preferences.getPreferencesMap().get(PreferenceType.angle);

		DragDirection direction = null;
		for (Map.Entry<DragDirection, DragPreference> directionEntry : distancePreferences.getDirectionPreferences().entrySet()) {

			Log.d(String.valueOf(dragButton.getId()), "Drag direction: " + directionEntry.getKey());
			Log.d(String.valueOf(dragButton.getId()), "Trying direction interval: " + directionEntry.getValue().getInterval());

			if (directionEntry.getValue().getInterval().contains(distance)) {
				final DragPreference anglePreference = anglePreferences.getDirectionPreferences().get(directionEntry.getKey());

				Log.d(String.valueOf(dragButton.getId()), "Trying angle interval: " + anglePreference.getInterval());

				if (directionEntry.getKey() == DragDirection.left && right.getObject()) {
				} else if (directionEntry.getKey() == DragDirection.right && !right.getObject()) {
				} else {
					if (anglePreference.getInterval().contains((float) angle)) {
						direction = directionEntry.getKey();
						Log.d(String.valueOf(dragButton.getId()), "MATCH! Direction: " + direction);
						break;
					}
				}
			}

		}

		if (direction != null) {
			final Preference durationPreferences = preferences.getPreferencesMap().get(PreferenceType.duration);

			final DragPreference durationDragPreferences = durationPreferences.getDirectionPreferences().get(direction);

			Log.d(String.valueOf(dragButton.getId()), "Trying time interval: " + durationDragPreferences.getInterval());
			if (durationDragPreferences.getInterval().contains((float) duration)) {
				Log.d(String.valueOf(dragButton.getId()), "MATCH!");
				result = dragProcessor.processDragEvent(direction, dragButton, startPoint, motionEvent);
			}
		}

		return result;
	}

	@Override
	public boolean isSuppressOnClickEvent() {
		return true;
	}

	private void logDragEvent(@NotNull DragButton dragButton, @NotNull DragEvent event) {
		final Point2d startPoint = event.getStartPoint();
		final MotionEvent motionEvent = event.getMotionEvent();
		final Point2d endPoint = new Point2d(motionEvent.getX(), motionEvent.getY());

		Log.d(String.valueOf(dragButton.getId()), "Start point: " + startPoint + ", End point: " + endPoint);
		Log.d(String.valueOf(dragButton.getId()), "Distance: " + MathUtils.getDistance(startPoint, endPoint));
		final MutableObject<Boolean> right = new MutableObject<Boolean>();
		Log.d(String.valueOf(dragButton.getId()), "Angle: " + Math.toDegrees(MathUtils.getAngle(startPoint, MathUtils.sum(startPoint, axis), endPoint, right)));
		Log.d(String.valueOf(dragButton.getId()), "Is right angle? " + right);
		Log.d(String.valueOf(dragButton.getId()), "Axis: " + axis + " Vector: " + MathUtils.subtract(endPoint, startPoint));
		Log.d(String.valueOf(dragButton.getId()), "Total time: " + (motionEvent.getEventTime() - motionEvent.getDownTime()) + " ms");
	}

	@NotNull
	public DragProcessor getDragProcessor() {
		return dragProcessor;
	}

	public void setDragProcessor(@NotNull DragProcessor dragProcessor) {
		this.dragProcessor = dragProcessor;
	}

	@Override
	public void onDragPreferencesChange(@NotNull Preferences preferences) {
		this.preferences = preferences;
	}

	public interface DragProcessor {

		boolean processDragEvent(@NotNull DragDirection dragDirection, @NotNull DragButton dragButton, @NotNull Point2d startPoint2d, @NotNull MotionEvent motionEvent);
	}

	// todo serso: currently we do not use direction
	public static String getPreferenceId(@NotNull PreferenceType preferenceType, @NotNull DragDirection direction) {
		return "org.solovyev.android.calculator.DragButtonCalibrationActivity" + "_" + preferenceType.name() /*+ "_" + direction.name()*/;
	}

	@NotNull
	public static Preferences getDefaultPreferences(@NotNull Context context) {
		return getPreferences0(null, context);
	}

	@NotNull
	public static Preferences getPreferences(@NotNull final SharedPreferences preferences, @NotNull Context context) {
		return getPreferences0(preferences, context);
	}

	@NotNull
	private static Preferences getPreferences0(@Nullable final SharedPreferences preferences, @NotNull Context context) {

		final Mapper<Interval<Float>> mapper = new NumberIntervalMapper<Float>(Float.class);

		final Preferences result = new Preferences();

		for (PreferenceType preferenceType : PreferenceType.values()) {
			for (DragDirection dragDirection : DragDirection.values()) {

				final String preferenceId = getPreferenceId(preferenceType, dragDirection);

				final String defaultValue;
				switch (preferenceType) {
					case angle:
						defaultValue = context.getResources().getString(R.string.p_drag_angle);
						break;
					case distance:
						defaultValue = context.getResources().getString(R.string.p_drag_distance);
						break;
					case duration:
						defaultValue = context.getResources().getString(R.string.p_drag_duration);
						break;
					default:
						defaultValue = null;
						Log.e(SimpleOnDragListener.class.getName(), "New preference type added: default preferences should be defined. Preference id: " + preferenceId);
				}

				final String value = preferences == null ? defaultValue : preferences.getString(preferenceId, defaultValue);

				if (value != null) {
					final Interval<Float> intervalPref = transformInterval(preferenceType, dragDirection, mapper.parseValue(value));

					Log.d(SimpleOnDragListener.class.getName(), "Preference loaded for " + dragDirection +". Id: " + preferenceId + ", value: " + intervalPref.toString());

					final DragPreference directionPreference = new DragPreference(dragDirection, intervalPref);

					Preference preference = result.getPreferencesMap().get(preferenceType);
					if (preference == null) {
						preference = new Preference(preferenceType);
						result.getPreferencesMap().put(preferenceType, preference);
					}

					preference.getDirectionPreferences().put(dragDirection, directionPreference);
				}
			}
		}

		return result;
	}

	@NotNull
	public static Interval<Float> transformInterval(@NotNull PreferenceType preferenceType,
                                                    @NotNull DragDirection dragDirection,
                                                    @NotNull Interval<Float> interval) {

        if (preferenceType == PreferenceType.angle) {
			final Float leftLimit = interval.getLeftLimit();
			final Float rightLimit = interval.getRightLimit();

            if (leftLimit != null && rightLimit != null) {
                final Float newLeftLimit;
                final Float newRightLimit;

                if (dragDirection == DragDirection.up) {
                    newLeftLimit = 180f - rightLimit;
                    newRightLimit = 180f - leftLimit;
                } else if (dragDirection == DragDirection.left) {
                    newLeftLimit = 90f - rightLimit;
                    newRightLimit = 90f + rightLimit;
                } else if (dragDirection == DragDirection.right) {
                    newLeftLimit = 90f - rightLimit;
                    newRightLimit = 90f + rightLimit;
                } else {
                    newLeftLimit = leftLimit;
                    newRightLimit = rightLimit;
                }

                return IntervalImpl.newClosed(newLeftLimit, newRightLimit);
            }
        }

        return interval;
	}


	public static enum PreferenceType {
		angle,
		distance,
		duration
	}

	public static class DragPreference {

		@NotNull
		private DragDirection direction;

		@NotNull
		private Interval<Float> interval;


		public DragPreference(@NotNull DragDirection direction, @NotNull Interval<Float> interval) {
			this.direction = direction;
			this.interval = interval;
		}

		@NotNull
		public DragDirection getDirection() {
			return direction;
		}

		public void setDirection(@NotNull DragDirection direction) {
			this.direction = direction;
		}

		@NotNull
		public Interval<Float> getInterval() {
			return interval;
		}

		public void setInterval(@NotNull Interval<Float> interval) {
			this.interval = interval;
		}
	}

	public static class Preference {

		@NotNull
		private PreferenceType preferenceType;

		@NotNull
		private Map<DragDirection, DragPreference> directionPreferences = new HashMap<DragDirection, DragPreference>();


		public Preference(@NotNull PreferenceType preferenceType) {
			this.preferenceType = preferenceType;
		}

		@NotNull
		public PreferenceType getPreferenceType() {
			return preferenceType;
		}

		public void setPreferenceType(@NotNull PreferenceType preferenceType) {
			this.preferenceType = preferenceType;
		}

		@NotNull
		public Map<DragDirection, DragPreference> getDirectionPreferences() {
			return directionPreferences;
		}

		public void setDirectionPreferences(@NotNull Map<DragDirection, DragPreference> directionPreferences) {
			this.directionPreferences = directionPreferences;
		}
	}

	public static class Preferences {

		private final Map<PreferenceType, Preference> preferencesMap = new HashMap<PreferenceType, Preference>();

		public Map<PreferenceType, Preference> getPreferencesMap() {
			return preferencesMap;
		}
	}
}