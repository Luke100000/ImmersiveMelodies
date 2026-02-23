package immersive_melodies.item;

import com.mojang.serialization.Codec;
import immersive_melodies.Common;
import immersive_melodies.Config;
import immersive_melodies.Sounds;
import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.MelodyProgressManager;
import immersive_melodies.client.sound.CancelableSoundInstance;
import immersive_melodies.network.Network;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;
import immersive_melodies.resources.ServerMelodyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InstrumentItem extends Item {
    public static final DataComponentType<Boolean> PLAYING = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "playing",
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    public static final DataComponentType<ResourceLocation> MELODY = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "melody",
            DataComponentType.<ResourceLocation>builder().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).build());

    public static final DataComponentType<Long> START_TIME = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "start_time",
            DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build());

    public static final DataComponentType<List<Integer>> TRACKS = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "enabled_tracks",
            DataComponentType.<List<Integer>>builder().persistent(Codec.list(Codec.INT)).networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())).build());

    private final Sounds.Instrument sound;
    private final long sustain;

    private final Vector3f offset;

    private final RandomSource random = RandomSource.create();

    public InstrumentItem(Properties settings, Sounds.Instrument sound, long sustain, Vector3f offset) {
        super(settings);

        this.sound = sound;
        this.sustain = sustain;
        this.offset = offset;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide) {
            Network.sendToPlayer(new MelodyListMessage(user), (ServerPlayer) user);
            Network.sendToPlayer(new OpenGuiRequest(), (ServerPlayer) user);
        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        // State
        if (isPlaying(stack)) {
            components.add(Component.translatable("immersive_melodies.playing").withStyle(ChatFormatting.GREEN));
        }

        super.appendHoverText(stack, context, components, tooltipFlag);
    }

    public boolean isPlaying(ItemStack stack) {
        return stack.getOrDefault(PLAYING, false);
    }

    public void inventoryClientTick(ItemStack stack, Level world, LivingEntity entity) {
        // check if the item is in the hand and is the primary instrument as you can't play two at once
        boolean isPrimary = false;
        for (ItemStack handItem : entity.getHandSlots()) {
            if (handItem == stack) {
                isPrimary = true;
                break;
            } else if (handItem.getItem() instanceof InstrumentItem) {
                break;
            }
        }

        // play
        if (isPlaying(stack) && isPrimary && world.isClientSide && Common.soundManager.audible(entity)) {
            MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
            progress.tick(stack);

            // sync
            MelodyProgressManager.INSTANCE.sync(world.getGameTime());

            Melody melody = progress.getMelody();

            // get enabled tracks
            List<Integer> enabledTracks = getEnabledTracks(stack);
            for (int track = 0; track < melody.getTracks().size(); track++) {
                int lastIndex = MelodyProgressManager.INSTANCE.getProgress(entity).getLastIndex(track);
                List<Note> notes = melody.getTracks().get(track).getNotes();
                for (int i = lastIndex; i < notes.size(); i++) {
                    Note note = notes.get(i);
                    if (progress.getTime() >= note.getTime()) {
                        if (enabledTracks.isEmpty() || enabledTracks.contains(track)) {
                            playNote(entity, note, progress.getTime());
                        }

                        // Mark as done
                        if (i == notes.size() - 1) {
                            MelodyProgressManager.INSTANCE.setLastIndex(entity, track, i + 1);
                        }
                    } else {
                        MelodyProgressManager.INSTANCE.setLastIndex(entity, track, i);
                        break;
                    }
                }
            }

            // Rewind
            if (progress.getTime() > melody.getLength()) {
                rewind(stack, world);
            }
        }
    }

    public CancelableSoundInstance playNote(Entity entity, Note note, long time) {
        float volume = note.getVelocity() / 255.0f * 2.0f * Config.getInstance().instrumentVolumeFactor;
        float pitch = (float) Math.pow(2, (note.getNote() - 24) / 12.0);
        long delay = note.getTime() - time;
        delay = Math.max(delay, 0);
        long length = note.getLength();
        long sustain = Math.min(this.sustain, note.getSustain());

        // humanize
        volume += Config.getInstance().humanizationVolume * (random.nextFloat() * 2 - 1);
        pitch *= 1 + Config.getInstance().humanizationPitch * (random.nextFloat() * 2 - 1);
        delay += random.nextIntBetweenInclusive(-Config.getInstance().humanizationTime, Config.getInstance().humanizationTime);
        length += random.nextIntBetweenInclusive(-Config.getInstance().humanizationTime, Config.getInstance().humanizationTime);

        int octave = 1;
        while (octave < 8 && pitch > 4.0 / 3.0) {
            pitch /= 2;
            octave++;
        }

        // adjust volume based on perceived loudness
        float factor = Config.getInstance().perceivedLoudnessAdjustmentFactor;
        float adjustedVolume = (float) (volume / Math.sqrt(pitch * Math.pow(2, octave - 4)));
        volume = volume * (1.0f - factor) + adjustedVolume * factor;

        // sound
        CancelableSoundInstance soundInstance = Common.soundManager.playSound(entity.getX(), entity.getY(), entity.getZ(),
                sound.get(octave), SoundSource.NEUTRAL,
                volume, pitch, length, sustain, delay, entity);

        // Stop game music
        if (entity instanceof Player && Config.getInstance().stopGameMusicForPlayers) {
            Common.soundManager.pauseGameMusic();
        } else if (Config.getInstance().stopGameMusicForMobs) {
            Common.soundManager.pauseGameMusic();
        }

        // particle
        if (entity instanceof LivingEntity livingEntity && !Common.soundManager.isFirstPerson(entity)) {
            double x = Math.sin(-livingEntity.yBodyRot / 180.0 * Math.PI);
            double z = Math.cos(-livingEntity.yBodyRot / 180.0 * Math.PI);
            entity.level().addParticle(ParticleTypes.NOTE,
                    entity.getX() + x * offset.z + z * offset.x, entity.getY() + entity.getBbHeight() / 2.0 + offset.y, entity.getZ() + z * offset.z - x * offset.x,
                    x * 5.0, 0.0, z * 5.0);
        }

        MelodyProgressManager.INSTANCE.setLastNote(entity, volume, pitch, length);

        return soundInstance;
    }

    public void inventoryServerTick(ItemStack stack, ServerLevel world, Entity entity) {
        // autoplay
        if (!(entity instanceof Player) && !isPlaying(stack)) {
            ResourceLocation randomMelody = ServerMelodyManager.getRandomMelody();
            play(stack, randomMelody, world, entity);
        }
    }

    public void play(ItemStack stack, ResourceLocation melody, Level world, Entity entity) {
        stack.set(MELODY, melody);
        stack.set(PLAYING, true);
        stack.set(START_TIME, world.getGameTime());

        refreshTracks(stack, entity);
    }

    public static ResourceLocation getMelody(ItemStack stack) {
        return stack.getOrDefault(MELODY, Common.locate("default"));
    }

    public void refreshTracks(ItemStack stack, Entity entity) {
        String identifier = ServerMelodyManager.getIdentifier(entity, BuiltInRegistries.ITEM.getKey(this));
        Set<Integer> enabledTracks = ServerMelodyManager.getSettings().getEnabledTracks(getMelody(stack), identifier);
        stack.set(TRACKS, new ArrayList<>(enabledTracks));
    }

    public void rewind(ItemStack stack, Level world) {
        stack.set(START_TIME, world.getGameTime());
    }

    public void play(ItemStack stack) {
        stack.set(PLAYING, true);
    }

    public void pause(ItemStack stack) {
        stack.set(PLAYING, false);
    }

    public List<Integer> getEnabledTracks(ItemStack stack) {
        return stack.getOrDefault(TRACKS, new ArrayList<>());
    }
}
