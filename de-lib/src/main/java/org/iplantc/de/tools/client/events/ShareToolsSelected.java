package org.iplantc.de.tools.client.events;

/**
 * @author sriram
 */

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class ShareToolsSelected extends GwtEvent<ShareToolsSelected.ShareToolsSelectedHandler> {

    public static interface ShareToolsSelectedHandler extends EventHandler {
        void onShareToolsSelected(ShareToolsSelected event);
    }

    public static interface HasShareToolselectedHandlers {
        HandlerRegistration addShareToolselectedHandler(ShareToolsSelectedHandler handler);
    }

    public static final Type<ShareToolsSelectedHandler> TYPE = new Type<>();

    public ShareToolsSelected() {

    }

    @Override
    public Type<ShareToolsSelectedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ShareToolsSelectedHandler handler) {
        handler.onShareToolsSelected(this);
    }

}
