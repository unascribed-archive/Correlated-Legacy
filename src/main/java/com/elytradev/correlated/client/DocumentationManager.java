package com.elytradev.correlated.client;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;

public class DocumentationManager {

	private final ImmutableMultimap<String, String> pages;
	
	private final LoadingCache<String, Future<DocumentationPage>> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.build(new CacheLoader<String, Future<DocumentationPage>>() {
				@Override
				public Future<DocumentationPage> load(String key) throws Exception {
					Language l = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
					URL u = getClass().getClassLoader().getResource("documentation/"+l.getLanguageCode()+"/"+key.replace('.', '/')+".md");
					if (u == null) {
						u = getClass().getClassLoader().getResource("documentation/en_US/"+key.replace('.', '/')+".md");
						if (u == null) {
							return Futures.immediateFuture(null);
						}
					}
					System.out.println("hello "+u);
					return Futures.immediateFuture(null);
				}
			});
	
	public DocumentationManager(List<String> pages) {
		Multimap<String, String> mmap = HashMultimap.create();
		for (String s : pages) {
			int idx = s.indexOf('.');
			if (idx == -1) continue;
			mmap.put(s.substring(0, idx), s.substring(idx+1));
		}
		this.pages = ImmutableMultimap.copyOf(mmap);
	}
	
	public Collection<String> getPages(String domain) {
		return pages.get(domain);
	}
	
	public boolean hasPage(String domain, String key) {
		return pages.containsEntry(domain, key);
	}
	
	public Future<DocumentationPage> getPage(String domain, String key) {
		try {
			return cache.get(domain+"."+key);
		} catch (ExecutionException e) {
			return Futures.immediateFailedFuture(e);
		}
	}
	
}
