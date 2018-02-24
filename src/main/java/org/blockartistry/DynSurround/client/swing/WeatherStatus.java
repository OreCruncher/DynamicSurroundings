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

package org.blockartistry.DynSurround.client.swing;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("serial")
@SideOnly(Side.CLIENT)
public class WeatherStatus extends JPanel implements Observer {
	JLabel lblRainStatus = new JLabel("Rain");
	JLabel lblThunderStatus = new JLabel("Thunder");
	JLabel lblStatus = new JLabel("Status:");
	JLabel lblStatus_1 = new JLabel("Status:");
	JLabel lblStrength = new JLabel("Strength:");
	JLabel lblStrength_1 = new JLabel("Strength:");
	JLabel lblTime = new JLabel("Time:");
	JLabel lblTime_1 = new JLabel("Time:");
	JLabel lblNewLabel_1 = new JLabel("Next Event:");

	JLabel rainStatus = new JLabel("***");
	JLabel rainStrength = new JLabel("***");
	JLabel rainTime = new JLabel("***");

	JLabel thunderStatus = new JLabel("***");
	JLabel thunderStrength = new JLabel("***");
	JLabel nextThunderEvent = new JLabel("***");
	JLabel thunderTime = new JLabel("***");

	protected final DataProxy.WeatherData data;

	public WeatherStatus() {
		setLayout(null);

		setName("Weather Status");

		this.lblRainStatus.setBounds(10, 11, 88, 14);
		add(this.lblRainStatus);

		this.lblThunderStatus.setBounds(10, 118, 87, 14);
		add(this.lblThunderStatus);

		this.lblStatus.setBounds(32, 36, 66, 14);
		add(this.lblStatus);

		this.lblStatus_1.setBounds(32, 151, 66, 14);
		add(this.lblStatus_1);

		this.lblStrength.setBounds(32, 61, 66, 14);
		add(this.lblStrength);

		this.lblStrength_1.setBounds(32, 176, 66, 14);
		add(this.lblStrength_1);

		this.lblTime.setBounds(32, 86, 66, 14);
		add(this.lblTime);

		this.lblTime_1.setBounds(32, 201, 66, 14);
		add(this.lblTime_1);

		this.rainStatus.setBounds(103, 36, 66, 14);
		add(this.rainStatus);

		this.rainStrength.setBounds(103, 61, 66, 14);
		add(this.rainStrength);

		this.rainTime.setBounds(103, 86, 66, 14);
		add(this.rainTime);

		this.thunderStatus.setBounds(103, 151, 66, 14);
		add(this.thunderStatus);

		this.thunderStrength.setBounds(103, 176, 66, 14);
		add(this.thunderStrength);

		this.thunderTime.setBounds(103, 201, 66, 14);
		add(this.thunderTime);

		this.lblNewLabel_1.setBounds(32, 226, 66, 14);
		add(this.lblNewLabel_1);

		this.nextThunderEvent.setBounds(103, 226, 66, 14);
		add(this.nextThunderEvent);

		this.data = new DataProxy.WeatherData();
		this.data.addObserver(this);

	}

	@Override
	public void update(Observable o, Object arg) {

		this.rainStatus.setText(this.data.getRainStatus());
		this.rainStrength.setText("" + (int) (this.data.getRainIntensity() * 100));
		this.rainTime.setText("" + this.data.getRainTime());
		this.thunderStatus.setText(this.data.getThunderStrength() > 0.9 ? "Thundering" : "No thunder");
		this.thunderStrength.setText("" + (int) (this.data.getThunderStrength() * 100));
		this.thunderTime.setText("" + this.data.getThunderTime());
		this.nextThunderEvent.setText("" + this.data.getNextThunderEvent());

	}
}
