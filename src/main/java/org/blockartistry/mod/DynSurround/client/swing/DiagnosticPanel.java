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

package org.blockartistry.mod.DynSurround.client.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class DiagnosticPanel extends JPanel {
	
	protected JFrame frame = null;
	protected JTable table = null;
	
	protected DiagnosticPanel() {
        
        this.setLayout(new BorderLayout());
		
        this.frame = new JFrame("Script Environment Variables");
        this.table = new JTable(new ScriptVariableTable());

        final JScrollPane tableContainer = new JScrollPane(table);

        this.add(tableContainer, BorderLayout.CENTER);

        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }
	
	public void close() {
		this.setVisible(false);
		this.frame.dispose();
	}
	
	private static DiagnosticPanel INSTANCE = null;
	
	public static void create() {
		INSTANCE = new DiagnosticPanel();
	}
	
	public static void refresh() {
		if(INSTANCE != null)
			INSTANCE.repaint();
	}
	
	public static void destroy() {
		if(INSTANCE != null) {
			INSTANCE.close();
			INSTANCE = null;
		}
	}
}
