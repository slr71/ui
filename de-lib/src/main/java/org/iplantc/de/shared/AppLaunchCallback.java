package org.iplantc.de.shared;

import org.iplantc.de.client.models.WindowType;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author aramsey
 */
public abstract class AppLaunchCallback<T> implements DECallback<T> {

    @Override
    public List<WindowType> getWindowTypes() {
        return Lists.newArrayList(WindowType.APP_WIZARD);
    }
}
