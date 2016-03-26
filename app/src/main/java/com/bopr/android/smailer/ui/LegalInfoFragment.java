package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.bopr.android.smailer.R;

/**
 * Legal info fragment.
 */
public class LegalInfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_legal_info, container, false);

        WebView webView = (WebView) view.findViewById(R.id.web_view);
        webView.loadUrl("file:///android_asset/legal.html");

//        URL resource = Transport.class.getResource("/META-INF/LICENSE.txt");
        return view;
    }

}
