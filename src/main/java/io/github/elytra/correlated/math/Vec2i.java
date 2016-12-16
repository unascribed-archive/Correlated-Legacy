package io.github.elytra.correlated.math;

public class Vec2i {
	public int x;
	public int y;
	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2i add(int x, int y) {
		return new Vec2i(this.x+x, this.y+y);
	}
	
	public Vec2i addMut(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Vec2i other = (Vec2i) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return x+", "+y;
	}

	public Vec2i copy() {
		return new Vec2i(x, y);
	}
	
}
