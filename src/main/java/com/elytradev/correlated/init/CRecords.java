package com.elytradev.correlated.init;

import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.item.ItemCorrelatedRecord;
import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class CRecords {

	public static final List<ItemCorrelatedRecord> RECORD_ITEMS = Lists.newArrayList();
	public static final List<String> RECORDS = Lists.newArrayList();
	
	public static void register() {
		registerRecord("danslarue.xm");
		registerRecord("jesuisbaguette.xm");
		registerRecord("papillons.xm");
		registerRecord("dreidl.mod");
		registerRecord("oak.mod");
		registerRecord("king.mod");
		registerRecord("comrades.mod");
		registerRecord("devenirmondefi.mod");
		registerRecord("ngenracer.mod");
		registerRecord("sevensixteen.mod");
		registerRecord("ombres.mod");
		registerRecord("sacrecharlemagne.mod");
		registerRecord("danone.mod");
		registerRecord("spark.mod");
		registerRecord("genesis.mod");
		registerRecord("greyatari.mod");
		registerRecord("ella.mod");
		registerRecord("framboise.mod");
		registerRecord("grecque.mod");
		registerRecord("que.mod");
		registerRecord("suddenlyisee.mod");
		registerRecord("sixsixtythreefoureightytwo.mod");
		registerRecord("pinkssideoftown.mod");
		registerRecord("thirteen.mod");
		registerRecord("irokos.mod");
	}

	private static void registerRecord(String str) {
		try {
			RECORDS.add(str);
			String basename = str.substring(0, str.indexOf('.'));
			ResourceLocation loc = new ResourceLocation("correlated", basename);
			SoundEvent snd = new SoundEvent(loc);
			snd.setRegistryName(loc);
			CSoundEvents.records.add(snd);
			ItemCorrelatedRecord item = new ItemCorrelatedRecord(basename, snd);
			item.setRegistryName("record_"+basename);
			item.setUnlocalizedName("record");
			item.setCreativeTab(Correlated.CREATIVE_TAB);
			CItems.records.add(item);
			RECORD_ITEMS.add(item);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
