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

import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import org.orecruncher.lib.expression.IDynamicVariant;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("serial")
@SideOnly(Side.CLIENT)
public class ScriptVariableTable extends AbstractTableModel implements Observer {

	private static final String[] columnNames = { "Variable", "Value" };

	private final DataProxy.ScriptVariableData data;

	public ScriptVariableTable() {
		this.data = new DataProxy.ScriptVariableData();
		this.data.addObserver(this);
	}

	@Override
	public int getRowCount() {
		return this.data.getVariables().size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final IDynamicVariant<?> v = this.data.getVariables().get(rowIndex);
		if (v == null)
			return "";
		return columnIndex == 0 ? v.getName() : v.asString();
	}

	@Override
	public void update(final Observable o, final Object arg) {
		fireTableDataChanged();
	}

}
