package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by sylvain on 30/12/15.
 */
public class Quality extends Packet {

    private int quality;

    /**
     *
     * @param quality new fixed picture quality value from 0 to 100
     */
    public Quality(int quality) {
        super("quality");
        this.quality = quality;
    }


    @Override
    public void serialize(Collection<Object> elems) {
        super.serialize(elems);
        elems.add(this.quality);

    }

    @Override
    public void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        this.quality = asInt(iter.next());

    }
}
