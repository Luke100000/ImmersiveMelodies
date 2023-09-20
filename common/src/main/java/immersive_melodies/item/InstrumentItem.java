package immersive_melodies.item;

import immersive_melodies.Common;
import immersive_melodies.Sounds;
import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.MelodyProgressManager;
import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InstrumentItem extends Item {
    private final Sounds.Instrument sound;
    private final long sustain;

    private final Vec3f offset;

    public InstrumentItem(Settings settings, Sounds.Instrument sound, long sustain, Vec3f offset) {
        super(settings);

        this.sound = sound;
        this.sustain = sustain;
        this.offset = offset;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            NetworkHandler.sendToPlayer(new MelodyListMessage(user), (ServerPlayerEntity) user);
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.SELECTOR), (ServerPlayerEntity) user);
        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // State
        if (isPlaying(stack)) {
            tooltip.add(new TranslatableText("immersive_melodies.playing").formatted(Formatting.GREEN));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    public boolean isPlaying(ItemStack stack) {
        return stack.getOrCreateNbt().getBoolean("playing");
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        // autoplay
        if (!world.isClient && !(entity instanceof PlayerEntity) && !isPlaying(stack)) {
            Identifier randomMelody = ServerMelodyManager.getRandomMelody();
            play(stack, randomMelody, world);
        }

        // check if the item is in the hand, and is the primary instrument as you cant play two at once
        boolean isPrimary = false;
        for (ItemStack handItem : entity.getItemsHand()) {
            if (handItem == stack) {
                isPrimary = true;
                break;
            } else if (handItem.getItem() instanceof InstrumentItem) {
                break;
            }
        }

        // play
        if (isPlaying(stack) && isPrimary && world.isClient && Common.soundManager.audible(entity)) {
            MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.tick(stack);

            // sync
            MelodyProgressManager.INSTANCE.sync(world.getTime());

            Melody melody = progress.getMelody();
            for (int i = MelodyProgressManager.INSTANCE.getProgress(entity).getLastIndex(); i < melody.getPrimaryTrack().getNotes().size(); i++) {
                Note note = melody.getPrimaryTrack().getNotes().get(i);
                if (progress.getTime() >= note.getTime()) {
                    float volume = note.getVelocity() / 255.0f * 2.0f;
                    float pitch = (float) Math.pow(2, (note.getNote() - 24) / 12.0);
                    int octave = 1;
                    while (octave < 8 && pitch > 4.0 / 3.0) {
                        pitch /= 2;
                        octave++;
                    }
                    long length = note.getLength();
                    long sustain = Math.min(this.sustain, note.getSustain());

                    // sound
                    Common.soundManager.playSound(entity.getX(), entity.getY(), entity.getZ(),
                            sound.get(octave), SoundCategory.RECORDS,
                            volume, pitch, length, sustain,
                            note.getTime() - progress.getTime(), entity);

                    // particle
                    if (entity instanceof LivingEntity livingEntity && !Common.soundManager.isFirstPerson(entity)) {
                        double x = Math.sin(-livingEntity.bodyYaw / 180.0 * Math.PI);
                        double z = Math.cos(-livingEntity.bodyYaw / 180.0 * Math.PI);
                        world.addParticle(ParticleTypes.NOTE,
                                entity.getX() + x * offset.getZ() + z * offset.getX(), entity.getY() + entity.getHeight() / 2.0 + offset.getY(), entity.getZ() + z * offset.getZ() - x * offset.getX(),
                                x * 5.0, 0.0, z * 5.0);
                    }

                    MelodyProgressManager.INSTANCE.setLastNote(entity, volume, pitch, length);

                    if (i == melody.getPrimaryTrack().getNotes().size() - 1) {
                        if (entity instanceof PlayerEntity) {
                            MelodyProgressManager.INSTANCE.setLastIndex(entity, i + 1);
                        } else {
                            // other entities loop
                            rewind(stack, world);
                        }
                    }
                } else {
                    MelodyProgressManager.INSTANCE.setLastIndex(entity, i);
                    break;
                }
            }
        }
    }

    public void play(ItemStack stack, Identifier melody, World world) {
        stack.getOrCreateNbt().putString("melody", melody.toString());
        stack.getOrCreateNbt().putBoolean("playing", true);
        stack.getOrCreateNbt().putLong("start_time", world.getTime());
    }

    public void rewind(ItemStack stack, World world) {
        stack.getOrCreateNbt().putLong("start_time", world.getTime());
    }

    public void play(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("playing", true);
    }

    public void pause(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean("playing", false);
    }
}
