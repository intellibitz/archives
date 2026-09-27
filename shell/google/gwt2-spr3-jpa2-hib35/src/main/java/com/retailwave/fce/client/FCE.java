package com.retailwave.fce.client;
/**
 * $Id: FCE.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/FCE.java $
 */

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * FCE
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class FCE implements EntryPoint {

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        HandlerManager eventBus = new HandlerManager(null);
        FCEPresenter fCEPresenter = new FCEPresenter(eventBus);
        fCEPresenter.go(RootLayoutPanel.get());
    }

}
