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

import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.blockartistry.mod.DynSurround.client.handlers.ExpressionStateHandler;
import org.blockartistry.mod.DynSurround.client.handlers.ExpressionStateHandler.IDynamicVariable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ScriptVariableTable implements TableModel {

	private static final String[] columnNames = { "Variable", "Value" };

	private final List<IDynamicVariable> variables;

	public ScriptVariableTable() {
		this.variables = ExpressionStateHandler.getVariables();
	}

	@Override
	public int getRowCount() {
		return this.variables.size();
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
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final IDynamicVariable v = this.variables.get(rowIndex);
		if (v == null)
			return "";
		return columnIndex == 0 ? v.getName() : v.asString();
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// Do nothing
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// Do nothing?

	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// Do nothing?
	}

}
