package com.elytradev.correlated;

import java.awt.Color;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

public class CSSColors {

	public static int parse(String s) {
		s = s.trim();
		if (COLOR_NAMES.containsKey(s)) {
			return COLOR_NAMES.get(s);
		} else if (s.startsWith("#")) {
			String a = "";
			// #RGBA
			if (s.length() == 5) {
				a = Character.toString(s.charAt(4));
				s = s.substring(0, 4);
			}
			// #RGB
			if (s.length() == 4) {
				String r = Character.toString(s.charAt(1));
				String g = Character.toString(s.charAt(2));
				String b = Character.toString(s.charAt(3));
				s = "#"+r+r+g+g+b+b+a+a; 
			}
			if (s.length() == 7) {
				// #RRGGBB
				return Integer.parseInt(s.substring(1), 16) | 0xFF000000;
			} else if (s.length() == 9) {
				// #RRGGBBBAA (CSS4)
				
				// Minecraft uses ARGB, so we have to reorganize a bit
				// This results in AARRGGBB
				return Integer.parseInt(s.substring(7, 9)+s.substring(1, 7), 16);
			} else {
				throw new IllegalArgumentException(s+" is not a valid CSS hex color");
			}
		} else if (s.contains("(") && s.contains(")")) {
			int start = s.indexOf('(');
			int end = s.lastIndexOf(')');
			String op = s.substring(0, start);
			List<String> params = Splitter.on(',').trimResults().splitToList(s.substring(start+1, end));
			float alpha = 1;
			float red;
			float green;
			float blue;
			switch (op) {
				case "rgba":
					if (params.size() != 4) throw new IllegalArgumentException(s+" is not a valid CSS rgba color");
					alpha = Float.parseFloat(params.remove(3));
					// fall-through
				case "rgb":
					if (params.size() != 3) throw new IllegalArgumentException(s+" is not a valid CSS rgb color");
					red = Float.parseFloat(params.get(0));
					green = Float.parseFloat(params.get(1));
					blue = Float.parseFloat(params.get(2));
					break;
				case "hsla":
					if (params.size() != 4) throw new IllegalArgumentException(s+" is not a valid CSS hsla color");
					alpha = Float.parseFloat(params.remove(3));
					// fall-through
				case "hsl":
					if (params.size() != 3) throw new IllegalArgumentException(s+" is not a valid CSS hsl color");
					float hue = Float.parseFloat(params.get(0));
					float saturation = Float.parseFloat(params.get(1));
					float lightness = Float.parseFloat(params.get(2));
					
					float brightness = lightness + (saturation * (lightness < 0.5 ? lightness : 1-lightness));
					int color = Color.HSBtoRGB(hue/360f, saturation, brightness);
					color |= (((int)(alpha*255)&0xFF)<<24);
					return color;
				default:
					throw new IllegalArgumentException(s+" is not a valid CSS color");
			}
			int color = 0;
			color |= (((int)(red*255)&0xFF)<<16);
			color |= (((int)(green*255)&0xFF)<<8);
			color |= ((int)(blue*255)&0xFF);
			color |= (((int)(alpha*255)&0xFF)<<24);
			return color;
		} else {
			throw new IllegalArgumentException(s+" is not a valid CSS color - expected name, #RGB, #RGBA, #RRGGBB, #RRGGBBAA, rgba(), rgb(), hsla(), or hsl()");
		}
	}
	
	private static final ImmutableMap<String, Integer> COLOR_NAMES = ImmutableMap.<String, Integer>builder()
			.put("aliceblue", 0xf0f8ff)
			.put("antiquewhite", 0xfaebd7)
			.put("aqua", 0x00ffff)
			.put("aquamarine", 0x7fffd4)
			.put("azure", 0xf0ffff)
			.put("beige", 0xf5f5dc)
			.put("bisque", 0xffe4c4)
			.put("black", 0x000000)
			.put("blanchedalmond", 0xffebcd)
			.put("blue", 0x0000ff)
			.put("blueviolet", 0x8a2be2)
			.put("brown", 0xa52a2a)
			.put("burlywood", 0xdeb887)
			.put("cadetblue", 0x5f9ea0)
			.put("chartreuse", 0x7fff00)
			.put("chocolate", 0xd2691e)
			.put("coral", 0xff7f50)
			.put("cornflowerblue", 0x6495ed)
			.put("cornsilk", 0xfff8dc)
			.put("crimson", 0xdc143c)
			.put("cyan", 0x00ffff)
			.put("darkblue", 0x00008b)
			.put("darkcyan", 0x008b8b)
			.put("darkgoldenrod", 0xb8860b)
			.put("darkgray", 0xa9a9a9)
			.put("darkgreen", 0x006400)
			.put("darkgrey", 0xa9a9a9)
			.put("darkkhaki", 0xbdb76b)
			.put("darkmagenta", 0x8b008b)
			.put("darkolivegreen", 0x556b2f)
			.put("darkorange", 0xff8c00)
			.put("darkorchid", 0x9932cc)
			.put("darkred", 0x8b0000)
			.put("darksalmon", 0xe9967a)
			.put("darkseagreen", 0x8fbc8f)
			.put("darkslateblue", 0x483d8b)
			.put("darkslategray", 0x2f4f4f)
			.put("darkslategrey", 0x2f4f4f)
			.put("darkturquoise", 0x00ced1)
			.put("darkviolet", 0x9400d3)
			.put("deeppink", 0xff1493)
			.put("deepskyblue", 0x00bfff)
			.put("dimgray", 0x696969)
			.put("dimgrey", 0x696969)
			.put("dodgerblue", 0x1e90ff)
			.put("firebrick", 0xb22222)
			.put("floralwhite", 0xfffaf0)
			.put("forestgreen", 0x228b22)
			.put("fuchsia", 0xff00ff)
			.put("gainsboro", 0xdcdcdc)
			.put("ghostwhite", 0xf8f8ff)
			.put("gold", 0xffd700)
			.put("goldenrod", 0xdaa520)
			.put("gray", 0x808080)
			.put("green", 0x008000)
			.put("greenyellow", 0xadff2f)
			.put("grey", 0x808080)
			.put("honeydew", 0xf0fff0)
			.put("hotpink", 0xff69b4)
			.put("indianred", 0xcd5c5c)
			.put("indigo", 0x4b0082)
			.put("ivory", 0xfffff0)
			.put("khaki", 0xf0e68c)
			.put("lavender", 0xe6e6fa)
			.put("lavenderblush", 0xfff0f5)
			.put("lawngreen", 0x7cfc00)
			.put("lemonchiffon", 0xfffacd)
			.put("lightblue", 0xadd8e6)
			.put("lightcoral", 0xf08080)
			.put("lightcyan", 0xe0ffff)
			.put("lightgoldenrodyellow", 0xfafad2)
			.put("lightgray", 0xd3d3d3)
			.put("lightgreen", 0x90ee90)
			.put("lightgrey", 0xd3d3d3)
			.put("lightpink", 0xffb6c1)
			.put("lightsalmon", 0xffa07a)
			.put("lightseagreen", 0x20b2aa)
			.put("lightskyblue", 0x87cefa)
			.put("lightslategray", 0x778899)
			.put("lightslategrey", 0x778899)
			.put("lightsteelblue", 0xb0c4de)
			.put("lightyellow", 0xffffe0)
			.put("lime", 0x00ff00)
			.put("limegreen", 0x32cd32)
			.put("linen", 0xfaf0e6)
			.put("magenta", 0xff00ff)
			.put("maroon", 0x800000)
			.put("mediumaquamarine", 0x66cdaa)
			.put("mediumblue", 0x0000cd)
			.put("mediumorchid", 0xba55d3)
			.put("mediumpurple", 0x9370db)
			.put("mediumseagreen", 0x3cb371)
			.put("mediumslateblue", 0x7b68ee)
			.put("mediumspringgreen", 0x00fa9a)
			.put("mediumturquoise", 0x48d1cc)
			.put("mediumvioletred", 0xc71585)
			.put("midnightblue", 0x191970)
			.put("mintcream", 0xf5fffa)
			.put("mistyrose", 0xffe4e1)
			.put("moccasin", 0xffe4b5)
			.put("navajowhite", 0xffdead)
			.put("navy", 0x000080)
			.put("oldlace", 0xfdf5e6)
			.put("olive", 0x808000)
			.put("olivedrab", 0x6b8e23)
			.put("orange", 0xffa500)
			.put("orangered", 0xff4500)
			.put("orchid", 0xda70d6)
			.put("palegoldenrod", 0xeee8aa)
			.put("palegreen", 0x98fb98)
			.put("paleturquoise", 0xafeeee)
			.put("palevioletred", 0xdb7093)
			.put("papayawhip", 0xffefd5)
			.put("peachpuff", 0xffdab9)
			.put("peru", 0xcd853f)
			.put("pink", 0xffc0cb)
			.put("plum", 0xdda0dd)
			.put("powderblue", 0xb0e0e6)
			.put("purple", 0x800080)
			.put("rebeccapurple", 0x663399)
			.put("red", 0xff0000)
			.put("rosybrown", 0xbc8f8f)
			.put("royalblue", 0x4169e1)
			.put("saddlebrown", 0x8b4513)
			.put("salmon", 0xfa8072)
			.put("sandybrown", 0xf4a460)
			.put("seagreen", 0x2e8b57)
			.put("seashell", 0xfff5ee)
			.put("sienna", 0xa0522d)
			.put("silver", 0xc0c0c0)
			.put("skyblue", 0x87ceeb)
			.put("slateblue", 0x6a5acd)
			.put("slategray", 0x708090)
			.put("slategrey", 0x708090)
			.put("snow", 0xfffafa)
			.put("springgreen", 0x00ff7f)
			.put("steelblue", 0x4682b4)
			.put("tan", 0xd2b48c)
			.put("teal", 0x008080)
			.put("thistle", 0xd8bfd8)
			.put("tomato", 0xff6347)
			.put("turquoise", 0x40e0d0)
			.put("violet", 0xee82ee)
			.put("wheat", 0xf5deb3)
			.put("white", 0xffffff)
			.put("whitesmoke", 0xf5f5f5)
			.put("yellow", 0xffff00)
			.put("yellowgreen", 0x9acd32)
			.build();

}
