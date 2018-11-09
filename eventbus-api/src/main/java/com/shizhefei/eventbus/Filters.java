package com.shizhefei.eventbus;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by luckyjayce on 2018/1/16.
 */
public class Filters {

    public static IEvent.Filter deliver(final IEvent... registers) {
        if (registers == null) {
            return acceptAll();
        }
        if (registers.length == 1) {
            final WeakReference<IEvent> reference = new WeakReference<>(registers[0]);
            return new IEvent.Filter() {
                @Override
                public boolean accept(IEvent event) {
                    return reference.get() != event;
                }
            };
        } else {
            final Set<IEvent> iEvents = Collections.newSetFromMap(new WeakHashMap<IEvent, Boolean>(registers.length));
            Collections.addAll(iEvents, registers);
            return new IEvent.Filter() {
                @Override
                public boolean accept(IEvent event) {
                    return !iEvents.contains(event);
                }
            };
        }
    }

    public static IEvent.Filter contains(final IEvent... registers) {
        if (registers == null) {
            return NONE;
        }
        if (registers.length == 1) {
            final WeakReference<IEvent> reference = new WeakReference<>(registers[0]);
            return new IEvent.Filter() {
                @Override
                public boolean accept(IEvent event) {
                    return reference.get() == event;
                }
            };
        } else {
            final Set<IEvent> iEvents = Collections.newSetFromMap(new WeakHashMap<IEvent, Boolean>(registers.length));
            Collections.addAll(iEvents, registers);
            return new IEvent.Filter() {
                @Override
                public boolean accept(IEvent event) {
                    return iEvents.contains(event);
                }
            };
        }
    }

    public static IEvent.Filter acceptAll() {
        return ALL;
    }

    private static final IEvent.Filter ALL = new IEvent.Filter() {
        @Override
        public boolean accept(IEvent event) {
            return true;
        }
    };

    private static final IEvent.Filter NONE = new IEvent.Filter() {
        @Override
        public boolean accept(IEvent event) {
            return true;
        }
    };
}
