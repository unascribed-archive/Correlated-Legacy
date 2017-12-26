package com.elytradev.correlated.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class DirectoryResourcePack implements IResourcePack {

	private String domain;
	private File directory;
	
	private DirectoryResourcePack(String domain, File directory) {
		this.domain = domain;
		this.directory = directory;
	}
	
	public static DirectoryResourcePack createWithFixedDomain(String domain, File directory) {
		return new DirectoryResourcePack(domain, directory);
	}
	
	public static DirectoryResourcePack create(File directory) {
		return new DirectoryResourcePack(null, directory);
	}
	
	private File getFile(ResourceLocation loc) throws IOException {
		File f;
		if (domain != null) {
			if (!domain.equals(loc.getResourceDomain())) return null;
			f = new File(directory, loc.getResourcePath());
		} else {
			f = new File(new File(directory, loc.getResourceDomain()), loc.getResourcePath());
		}
		if (!f.getCanonicalPath().startsWith(directory.getCanonicalPath()))
			throw new IllegalArgumentException("Attempt to walk out of directory "+directory+" - bad path: "+f);
		return f;
	}
	
	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException {
		File f = getFile(location);
		return f == null || !f.exists() ? null : new FileInputStream(f);
	}

	@Override
	public boolean resourceExists(ResourceLocation location) {
		try {
			File f = getFile(location);
			return f != null && f.exists();
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Set<String> getResourceDomains() {
		if (domain != null) return ImmutableSet.of(domain);
		File[] children = directory.listFiles();
		Set<String> out = Sets.newHashSetWithExpectedSize(children.length);
		for (File f : children) {
			if (f.isDirectory()) out.add(f.getName().toLowerCase(Locale.ROOT));
		}
		return Collections.unmodifiableSet(out);
	}

	@Override
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
		return null;
	}

	@Override
	public BufferedImage getPackImage() throws IOException {
		return null;
	}

	@Override
	public String getPackName() {
		return "Correlated Internal Resource Pack";
	}

}
