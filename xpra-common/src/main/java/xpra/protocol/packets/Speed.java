package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by sylvain121 on 30/12/15.
 */
public class Speed extends Packet {

    private int speed;

    /**
     *
     * @param speed new fixed speed value from -1 to 100
     */
    public Speed(int speed) {
        super("speed");
        this.speed = speed;
    }

    @Override
    public void serialize(Collection<Object> elems) {
        super.serialize(elems);
        elems.add(this.speed);

    }

    @Override
    public void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        this.speed = asInt(iter.next());

    }

}
