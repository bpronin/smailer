package com.bopr.android.smailer.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ActionProvider;
import android.view.View;

/**
 * Class DebugActionProvider.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("unused") /* used in menu_main.xml */
public class DebugActionProvider extends ActionProvider {

    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public DebugActionProvider(Context context) {
        super(context);
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public boolean onPerformDefaultAction() {
        getContext().startActivity(new Intent(getContext(), DebugActivity.class));
        return true;
    }

}
