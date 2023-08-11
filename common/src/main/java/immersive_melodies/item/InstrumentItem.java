package immersive_melodies.item;

import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class InstrumentItem extends Item {
    public InstrumentItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            NetworkHandler.sendToPlayer(new MelodyListMessage(), (ServerPlayerEntity) user);
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.SELECTOR), (ServerPlayerEntity) user);
        }

        return super.use(world, user, hand);
    }
}
