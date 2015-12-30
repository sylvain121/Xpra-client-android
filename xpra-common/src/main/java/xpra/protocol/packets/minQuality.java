package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by sylvain on 30/12/15.
 */
public class minQuality extends Packet {

    private int minQuality;

    /**
     *
     * @param minQuality new fixed picture minQuality value from -1 to 100
     */
    public minQuality(int minQuality) {
        super("minQuality");
        this.minQuality = minQuality;
    }


    @Override
    public void serialize(Collection<Object> elems) {
        super.serialize(elems);
        elems.add(this.minQuality);

    }

    @Override
    public void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        this.minQuality = asInt(iter.next());

    }
}
