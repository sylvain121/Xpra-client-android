package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by sylvain on 30/12/15.
 */
public class MinSpeed extends Packet {

    private int minSpeed;

    public MinSpeed(int minSpeed) {
        super("min-speed");
        this.minSpeed = minSpeed;
    }

    @Override
    public void serialize(Collection<Object> elems) {
        super.serialize(elems);
        elems.add(this.minSpeed);

    }

    @Override
    public void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        this.minSpeed = asInt(iter.next());

    }
}
