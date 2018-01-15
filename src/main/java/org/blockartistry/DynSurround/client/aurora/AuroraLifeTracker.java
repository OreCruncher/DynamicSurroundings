package org.blockartistry.DynSurround.client.aurora;

import org.blockartistry.lib.math.MathStuff;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AuroraLifeTracker {

	protected final int maxAge;
	protected final int ageDelta;
	protected int timer;
	protected boolean isAlive = true;
	protected boolean isFading = false;

	public AuroraLifeTracker(final int maxAge, final int ageDelta) {
		this.maxAge = maxAge;
		this.ageDelta = ageDelta;
	}

	public boolean isAlive() {
		return this.isAlive;
	}

	public boolean isFading() {
		return this.isFading;
	}

	public void setFading(final boolean f) {
		this.isFading = f;
	}
	
	public void kill() {
		this.isAlive = false;
		this.timer = 0;
	}

	public float ageRatio() {
		return (float) this.timer / (float) this.maxAge;
	}

	public void update() {

		if (!this.isAlive)
			return;

		if (this.isFading) {
			this.timer -= this.ageDelta;
		} else {
			this.timer += this.ageDelta;
		}

		this.timer = MathStuff.clamp(this.timer, 0, this.maxAge);

		if (this.timer == 0 && this.isFading)
			this.isAlive = false;
	}
}
