/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.client.swing;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("serial")
@SideOnly(Side.CLIENT)
public class DiagnosticPanel extends JPanel {

	static final Font SERVER_GUI_FONT = new Font("Monospaced", 0, 12);

	protected final JFrame frame;
	protected final JTabbedPane tabs;

	protected DiagnosticPanel() {

		setLayout(new BorderLayout());

		this.frame = new JFrame("Dynamic Surroundings Diagnostics");
		this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.tabs = new JTabbedPane();
		this.add(this.tabs);

		this.tabs.add(new ScriptVariables());
		this.tabs.add(new WeatherStatus());
		this.tabs.add(new BlockViewer());

		this.frame.getContentPane().add(this);
		this.frame.pack();
		this.frame.setAutoRequestFocus(false);
		this.frame.setVisible(true);
	}

	public void close() {
		setVisible(false);
		this.frame.dispose();
	}

	private static DiagnosticPanel INSTANCE = null;

	public static void create() {
		SwingUtilities.invokeLater(() -> INSTANCE = new DiagnosticPanel());
	}

	public static void refresh() {
		if (INSTANCE != null)
			DataProxy.update();
	}

	public static void destroy() {
		if (INSTANCE != null) {
			DataProxy.dataPools.clear();
			INSTANCE.close();
			INSTANCE = null;
		}
	}
}
