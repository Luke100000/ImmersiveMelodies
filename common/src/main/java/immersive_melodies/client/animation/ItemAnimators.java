package immersive_melodies.client.animation;

import immersive_melodies.Common;
import immersive_melodies.client.animation.animators.*;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ItemAnimators {
    private static final Map<Identifier, Animator> ANIMATORS = new HashMap<>();
    private static final Animator DEFAULT = new FluteAnimator();

    public static void register(Identifier id, Animator animator) {
        ANIMATORS.put(id, animator);
    }

    public static Animator get(Identifier id) {
        return ANIMATORS.getOrDefault(id, DEFAULT);
    }

    static {
        register(Common.locate("bagpipe"), new BagpipeAnimator());
        register(Common.locate("didgeridoo"), new DidgeridooAnimator());
        register(Common.locate("flute"), new FluteAnimator());
        register(Common.locate("lute"), new LuteAnimator());
        register(Common.locate("piano"), new PianoAnimator());
        register(Common.locate("triangle"), new TriangleAnimator());
        register(Common.locate("trumpet"), new TrumpetAnimator());
    }
}
