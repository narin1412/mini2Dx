/**
 * Copyright (c) 2015 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.ui.theme;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mini2Dx.core.exception.MdxException;
import org.mini2Dx.core.serialization.annotation.Field;
import org.mini2Dx.ui.layout.ScreenSize;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;

/**
 *
 */
public class UiTheme {
	public static final String DEFAULT_THEME_FILE = "default-mdx-theme.json";
	public static final String DEFAULT_STYLE_ID = "default";

	@Field
	private String name;
	@Field
	private int columns;
	@Field(optional = true)
	Map<String, UiFont> fonts;
	@Field
	Map<ScreenSize, UiStyleRuleset> rules;
	
	public void validate() {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		
		while(screenSizes.hasNext()) {
			ScreenSize screenSize = screenSizes.next();
			if(getButtonStyle(screenSize, DEFAULT_STYLE_ID) == null) {
				throw new MdxException("No button style with id '" + UiTheme.DEFAULT_STYLE_ID + "' defined for theme '" + name + "'");
			}
			if(getCheckBoxStyle(screenSize, DEFAULT_STYLE_ID) == null) {
				throw new MdxException("No checkbox style with id '" + UiTheme.DEFAULT_STYLE_ID + "' defined for theme '" + name + "'");
			}
			if(getFrameStyle(screenSize, DEFAULT_STYLE_ID) == null) {
				throw new MdxException("No frame style with id '" + UiTheme.DEFAULT_STYLE_ID + "' defined for theme '" + name + "'");
			}
			if(getTextBoxStyle(screenSize, DEFAULT_STYLE_ID) == null) {
				throw new MdxException("No textbox style with id '" + UiTheme.DEFAULT_STYLE_ID + "' defined for theme '" + name + "'");
			}
			LabelStyle labelStyle = getLabelStyle(screenSize, DEFAULT_STYLE_ID);
			if(labelStyle == null) {
				throw new MdxException("No label style with id '" + UiTheme.DEFAULT_STYLE_ID + "' defined for theme '" + name + "'");
			}
			if(!labelStyle.getFont().equals(DEFAULT_STYLE_ID) && !fonts.containsKey(labelStyle.getFont())) {
				throw new MdxException("Label requires font '" + labelStyle.getFont() + "' but it is not defined in the theme '" + name + "'");
			}
		}
	}
	
	public void loadDependencies(Array<AssetDescriptor> dependencies) {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		while(screenSizes.hasNext()) {
			ScreenSize nextSize = screenSizes.next();
			if(!rules.containsKey(nextSize)) {
				continue;
			}
			UiStyleRuleset ruleset = rules.get(nextSize);
			for(ButtonStyle buttonStyle: ruleset.buttons.values()) {
				dependencies.add(new AssetDescriptor<Texture>(buttonStyle.getNormalImage(), Texture.class));
				dependencies.add(new AssetDescriptor<Texture>(buttonStyle.getHoverImage(), Texture.class));
				dependencies.add(new AssetDescriptor<Texture>(buttonStyle.getActionImage(), Texture.class));
				dependencies.add(new AssetDescriptor<Texture>(buttonStyle.getDisabledImage(), Texture.class));
			}
			for(CheckBoxStyle checkBoxStyle : ruleset.checkboxes.values()) {
				dependencies.add(new AssetDescriptor<Texture>(checkBoxStyle.getUncheckedImage(), Texture.class));
				dependencies.add(new AssetDescriptor<Texture>(checkBoxStyle.getCheckedImage(), Texture.class));
			}
			for(FrameStyle frameStyle: ruleset.frames.values()) {
				dependencies.add(new AssetDescriptor<Texture>(frameStyle.getBackgroundImage(), Texture.class));
				dependencies.add(new AssetDescriptor<Texture>(frameStyle.getScrollBarImage(), Texture.class));
			}
			for(TextBoxStyle textBoxStyle: ruleset.textboxes.values()) {
				dependencies.add(new AssetDescriptor<Texture>(textBoxStyle.getImage(), Texture.class));
			}
			for(UiFont uiFont : fonts.values()) {
				if(!uiFont.getPath().endsWith(".ttf")) {
					throw new MdxException("Non-TTF fonts are not supported by mini2Dx-ui");
				}
				//TODO: Load fonts
			}
		}
	}
	
	public void prepareAssets(AssetManager assetManager) {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		while(screenSizes.hasNext()) {
			ScreenSize nextSize = screenSizes.next();
			if(!rules.containsKey(nextSize)) {
				continue;
			}
			UiStyleRuleset ruleset = rules.get(nextSize);
			for(ButtonStyle buttonStyle: ruleset.buttons.values()) {
				buttonStyle.prepareAssets(assetManager);
			}
			for(CheckBoxStyle checkBoxStyle : ruleset.checkboxes.values()) {
				checkBoxStyle.prepareAssets(assetManager);
			}
			for(FrameStyle frameStyle: ruleset.frames.values()) {
				frameStyle.prepareAssets(assetManager);
			}
			for(TextBoxStyle textBoxStyle: ruleset.textboxes.values()) {
				textBoxStyle.prepareAssets(assetManager);
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	public UiFont getFont(String font) {
		if(fonts == null) {
			return null;
		}
		return fonts.get(font);
	}
	
	public void putFont(String name, UiFont font) {
		if(fonts == null) {
			fonts = new HashMap<String, UiFont>();
		}
		fonts.put(name, font);
	}
	
	public ButtonStyle getButtonStyle(ScreenSize screenSize, String styleId) {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		while(screenSizes.hasNext()) {
			ScreenSize nextSize = screenSizes.next();
			if(nextSize.getMinSize() > screenSize.getMinSize()) {
				continue;
			}
			if(!rules.containsKey(nextSize)) {
				continue;
			}
			UiStyleRuleset ruleset = rules.get(nextSize);
			ButtonStyle result = ruleset.getButtonStyle(styleId);
			if(result == null) {
				continue;
			}
			return result;
		}
		if(!styleId.equals(DEFAULT_STYLE_ID)) {
			return getButtonStyle(screenSize, DEFAULT_STYLE_ID);
		}
		return null;
	}
	
	public CheckBoxStyle getCheckBoxStyle(ScreenSize screenSize, String styleId) {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		while(screenSizes.hasNext()) {
			ScreenSize nextSize = screenSizes.next();
			if(nextSize.getMinSize() > screenSize.getMinSize()) {
				continue;
			}
			if(!rules.containsKey(nextSize)) {
				continue;
			}
			UiStyleRuleset ruleset = rules.get(nextSize);
			CheckBoxStyle result = ruleset.getCheckBoxStyle(styleId);
			if(result == null) {
				continue;
			}
			return result;
		}
		return null;
	}
	
	public FrameStyle getFrameStyle(ScreenSize screenSize, String styleId) {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		while(screenSizes.hasNext()) {
			ScreenSize nextSize = screenSizes.next();
			if(nextSize.getMinSize() > screenSize.getMinSize()) {
				continue;
			}
			if(!rules.containsKey(nextSize)) {
				continue;
			}
			UiStyleRuleset ruleset = rules.get(nextSize);
			FrameStyle result = ruleset.getFrameStyle(styleId);
			if(result == null) {
				continue;
			}
			return result;
		}
		return null;
	}
	
	public LabelStyle getLabelStyle(ScreenSize screenSize, String styleId) {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		while(screenSizes.hasNext()) {
			ScreenSize nextSize = screenSizes.next();
			if(nextSize.getMinSize() > screenSize.getMinSize()) {
				continue;
			}
			if(!rules.containsKey(nextSize)) {
				continue;
			}
			UiStyleRuleset ruleset = rules.get(nextSize);
			LabelStyle result = ruleset.getLabelStyle(styleId);
			if(result == null) {
				continue;
			}
			return result;
		}
		return null;
	}
	
	public TextBoxStyle getTextBoxStyle(ScreenSize screenSize, String styleId) {
		Iterator<ScreenSize> screenSizes = ScreenSize.largestToSmallest();
		while(screenSizes.hasNext()) {
			ScreenSize nextSize = screenSizes.next();
			if(nextSize.getMinSize() > screenSize.getMinSize()) {
				continue;
			}
			if(!rules.containsKey(nextSize)) {
				continue;
			}
			UiStyleRuleset ruleset = rules.get(nextSize);
			TextBoxStyle result = ruleset.getTextBoxStyle(styleId);
			if(result == null) {
				continue;
			}
			return result;
		}
		return null;
	}
	
	public void putButtonStyle(ScreenSize screenSize, String styleId, ButtonStyle style) {
		if(rules == null) {
			rules = new HashMap<ScreenSize, UiStyleRuleset>();
		}
		if(!rules.containsKey(screenSize)) {
			rules.put(screenSize, new UiStyleRuleset());
		}
		rules.get(screenSize).putButtonStyle(styleId, style);
	}
	
	public void putCheckBoxStyle(ScreenSize screenSize, String styleId, CheckBoxStyle style) {
		if(rules == null) {
			rules = new HashMap<ScreenSize, UiStyleRuleset>();
		}
		if(!rules.containsKey(screenSize)) {
			rules.put(screenSize, new UiStyleRuleset());
		}
		rules.get(screenSize).putCheckBoxStyle(styleId, style);
	}
	
	public void putFrameStyle(ScreenSize screenSize, String styleId, FrameStyle style) {
		if(rules == null) {
			rules = new HashMap<ScreenSize, UiStyleRuleset>();
		}
		if(!rules.containsKey(screenSize)) {
			rules.put(screenSize, new UiStyleRuleset());
		}
		rules.get(screenSize).putFrameStyle(styleId, style);
	}
	
	public void putLabelStyle(ScreenSize screenSize, String styleId, LabelStyle style) {
		if(rules == null) {
			rules = new HashMap<ScreenSize, UiStyleRuleset>();
		}
		if(!rules.containsKey(screenSize)) {
			rules.put(screenSize, new UiStyleRuleset());
		}
		rules.get(screenSize).putLabelStyle(styleId, style);
	}
	
	public void putTextBoxStyle(ScreenSize screenSize, String styleId, TextBoxStyle style) {
		if(rules == null) {
			rules = new HashMap<ScreenSize, UiStyleRuleset>();
		}
		if(!rules.containsKey(screenSize)) {
			rules.put(screenSize, new UiStyleRuleset());
		}
		rules.get(screenSize).putTextBoxStyle(styleId, style);
	}
}