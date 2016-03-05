/*******************************************************************************
 * Copyright (c) 2015 David Green.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the EULA
 * which accompanies this distribution.
 *******************************************************************************/
package greensopinion.finance.services.application;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.inject.Inject;

import greensopinion.finance.services.bridge.ConsoleBridge;
import javafx.application.Application.Parameters;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

class WebApplicationRegion extends Region {

	private static final String JS_MEMBER_NAME_APP_SERVICE_LOCATOR = "appServiceLocator";
	private static final String JS_MEMBER_NAME_CONSOLE_BRIDGE = "consoleBridge";

	private WebView webView;
	private WebEngine webEngine;
	private final ServiceLocator serviceLocator;
	private final Parameters parameters;
	private final ConsoleBridge consoleBridge;

	@Inject
	WebApplicationRegion(ServiceLocator serviceLocator, Parameters parameters, ConsoleBridge consoleBridge) {
		this.serviceLocator = checkNotNull(serviceLocator);
		this.parameters = checkNotNull(parameters);
		this.consoleBridge = checkNotNull(consoleBridge);
	}

	public void initialize() {
		checkState(webView == null);
		webView = new WebView();
		webView.setContextMenuEnabled(false);
		webEngine = webView.getEngine();
		installConsoleBridge();
		installServiceLocator(serviceLocator);
		webEngine.load(Constants.webViewLocation(parameters));

		getChildren().add(webView);
	}

	private void installServiceLocator(ServiceLocator serviceLocator) {
		JSObject windowObject = getJsWindow();
		windowObject.setMember(JS_MEMBER_NAME_APP_SERVICE_LOCATOR, serviceLocator);
	}

	private void installConsoleBridge() {
		JSObject windowObject = getJsWindow();
		windowObject.setMember(JS_MEMBER_NAME_CONSOLE_BRIDGE, consoleBridge);
		webEngine.executeScript("console.log = function(message) {\nconsoleBridge.log(message);\n};");
	}

	private JSObject getJsWindow() {
		return (JSObject) webEngine.executeScript("window");
	}

	@Override
	protected void layoutChildren() {
		double w = getWidth();
		double h = getHeight();
		layoutInArea(webView, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
	}

	@Override
	protected double computePrefWidth(double height) {
		return Constants.DEFAULT_WIDTH;
	}

	@Override
	protected double computePrefHeight(double width) {
		return Constants.DEFAULT_HEIGHT;
	}
}
