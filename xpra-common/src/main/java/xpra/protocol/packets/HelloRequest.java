/**
 * 
 */
package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import xpra.client.XpraKeyboard;
import xpra.client.XpraKeyboard.KeyDesc;
import xpra.protocol.PictureEncoding;
import xpra.protocol.ProtocolConstants;

/**
 * @author Jakub Księżniak
 * 
 */
public class HelloRequest extends Packet {

	public final Map<String, Object> caps = new LinkedHashMap<String, Object>();

	public HelloRequest(int screenWidth, int screenHeight, XpraKeyboard keyboard, PictureEncoding defaultEncoding, PictureEncoding[] encodings) {
		super("hello");
		caps.put("version", ProtocolConstants.VERSION);
		// if (enc_pass != null) {
		// caps.put("challenge_response", enc_pass);
		// }
		final int[] screenDims = new int[] { screenWidth, screenHeight };
		caps.put("desktop_size", screenDims);
		caps.put("dpi", 96);
//		caps.put("dpi.x", 0);
//		caps.put("dpi.y", 0);
		caps.put("client_type", "Java");
		caps.put("screen_sizes", new int[][] { screenDims });
		caps.put("encodings", PictureEncoding.toString(encodings));
		caps.put("clipboard", false);
		caps.put("notifications", true);
		caps.put("cursors", true);
		caps.put("named_cursors", true);
		caps.put("bell", true);
		caps.put("bencode", true);
		caps.put("rencode", true);
        caps.put("initial_quality", 1);
		caps.put("chunked_compression", true);
		if(defaultEncoding == PictureEncoding.h264){
			caps.put("encoding", "h264");
            caps.put("encoding.generic", true);
            caps.put("encoding.transparency", true);
            caps.put("encoding.client_options", true);
            caps.put("encoding.csc_atoms", true);
            caps.put("encoding.video_reinit", false);
            caps.put("encoding.video_scaling", false);
            caps.put("encoding.full_csc_modes", "h264 : [\"YUV420P\"]");
			caps.put("encoding.x264.YUV420P.profile", "baseline");
		}
		if (defaultEncoding == PictureEncoding.jpeg) {
			caps.put("encoding", "jpeg");
			if (PictureEncoding.jpeg.equals(defaultEncoding)) {
				caps.put("jpeg", 40);
			}
		}
		caps.put("platform", System.getProperty("os.name").toLowerCase());
		caps.put("uuid", UUID.randomUUID().toString().replace("-", ""));
		setKeyboard(keyboard);
	}

	public void setDpi(int dpi, int xdpi, int ydpi) {
		caps.put("dpi", dpi);		
//		caps.put("dpi.x", xdpi);		
//		caps.put("dpi.y", ydpi);		
	}

	public void setKeyboard(XpraKeyboard keyboard) {
		if (keyboard != null) {
			caps.put("keyboard", true);
			caps.put("keyboard_sync", false);
			caps.put("xkbmap_layout", keyboard.getLocale().getLanguage());
			caps.put("xkbmap_variant", keyboard.getLocale().getVariant());
			caps.put("xkbmap_keycodes", buildKeycodes(keyboard.getKeycodes()));
		} else {
			caps.put("keyboard", false);
			caps.put("keyboard_sync", false);
		}
	}

	private Object buildKeycodes(List<KeyDesc> keycodes) {
		List<Object> list = new ArrayList<>();
		for(KeyDesc kd : keycodes) {
			list.add(kd.toList());
		}
		return list;
	}
//	
//	private Object buildKeycodes2(List<KeyDesc> keycodes) {
//		Map<Object, Object> out = new HashMap<Object, Object>();
//		List<Object> list = new ArrayList<>();
//		list.add("a");
//		list.add("A");
//		out.put("10", list);
//		return out;
//	}

	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(caps);
	}
}
