// Courtesy of Riven.
// Obtained from:
//
// https://github.com/riven8192/LibBase/blob/a70af645e9b35df824b9214b0ef2749bfb2b5df0/src/craterstudio/math/CacheItem.java

package org.blockartistry.lib.math;

/*
 * Created on May 6, 2009
 */

class CacheItem {
	public CacheItem(float xpos, float ypos, float zpos) {
		this.xpos = xpos;
		this.ypos = ypos;
		this.zpos = zpos;
	}

	float position;
	float xpos, ypos, zpos;
	float travelled;
}