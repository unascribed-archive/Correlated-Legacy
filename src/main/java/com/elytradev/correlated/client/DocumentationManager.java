package com.elytradev.correlated.client;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Futures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;

public class DocumentationManager {

	private final ImmutableMultimap<String, String> pages;
	
	private final ExecutorService loader = Executors.newCachedThreadPool();
	
	private final LoadingCache<String, Future<DocumentationPage>> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.removalListener(new RemovalListener<String, Future<DocumentationPage>>() {

				@Override
				public void onRemoval(RemovalNotification<String, Future<DocumentationPage>> notification) {
					Future<DocumentationPage> future = notification.getValue();
					if (future.isDone()) {
						try {
							future.get().destroy();
						} catch (Exception e) {}
					} else {
						future.cancel(true);
					}
				}
			})
			.build(new CacheLoader<String, Future<DocumentationPage>>() {
				@Override
				public Future<DocumentationPage> load(String key) {
					return loader.submit(() -> {
						Language l = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
						URL u = getClass().getClassLoader().getResource("documentation/"+l.getLanguageCode()+"/"+key.replace('.', '/')+".md");
						if (u == null) {
							u = getClass().getClassLoader().getResource("documentation/en_US/"+key.replace('.', '/')+".md");
							if (u == null) {
								return null;
							}
						}
						ByteSource bs = Resources.asByteSource(u);
						CharSource cs = bs.asCharSource(Charsets.UTF_8);
						String s = cs.read();
						Parser parser = Parser.builder()
							.extensions(Lists.newArrayList(
									TablesExtension.create(),
									StrikethroughExtension.create()
								))
							.build();
						Node node = parser.parse(s);
						return new DocumentationPage(key, node);
					});
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
			if (hasPage(domain, key)) {
				return cache.get(domain+"."+key);
			} else if (hasPage("global", key)) {
				return cache.get("global."+key);
			} else {
				return Futures.immediateFuture(null);
			}
		} catch (ExecutionException e) {
			return Futures.immediateFailedFuture(e);
		}
	}
	
	public void invalidateCache() {
		cache.invalidateAll();
	}
	
}
