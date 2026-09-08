package immersive_melodies.client.animation.accessors;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;

public class BipedModelAccessor<T extends HumanoidRenderState> extends ArmsAndHeadAccessor<LivingEntity> {
    private final HumanoidModel<T> model;

    public BipedModelAccessor(HumanoidModel<T> model, LivingEntity entity) {
        super(entity, model.head, model.hat, model.leftArm, model.rightArm);
        this.model = model;
    }

    public HumanoidModel<T> getModel() {
        return model;
    }
}
