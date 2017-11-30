package com.elytradev.correlated.client.anim;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.collect.ImmutableMap;

public final class FaceState {
	private static final MapJoiner COMMA_EQUALS_JOINER = Joiner.on(',').withKeyValueSeparator('=');
	
	public final ImmutableMap<String, String> blockstate;
	public final Face face;
	
	public FaceState(ImmutableMap<String, String> blockstate, Face face) {
		if (blockstate == null) throw new IllegalArgumentException("blockstate cannot be null");
		if (face == null) throw new IllegalArgumentException("face cannot be null");
		if (face.isPsuedoface()) throw new IllegalArgumentException("face cannot be a psuedoface");
		this.blockstate = blockstate;
		this.face = face;
	}

	@Override
	public String toString() {
		return "["+COMMA_EQUALS_JOINER.join(blockstate)+"]#"+face.name().toLowerCase(Locale.ROOT);
	}
	
	@Override
	public int hashCode() {
		return face.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof FaceState)) return false;
		return isEquivalent((FaceState)obj);
	}

	public boolean isEquivalent(@Nonnull FaceState that) {
		if (this == that) {
			return true;
		}
		for (Map.Entry<String, String> en : this.blockstate.entrySet()) {
			if ("*".equals(en.getValue())) continue;
			String otherValue = that.blockstate.get(en.getKey());
			if ("*".equals(otherValue)) continue;
			if (!Objects.equal(en.getValue(), otherValue)) return false;
		}
		if (face != that.face) {
			return false;
		}
		return true;
	}
	
	
	
}
 