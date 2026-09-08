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
import immersive_melodies.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class InstrumentItem extends Item {
    public static final DataComponentType<Boolean> PLAYING = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "playing",
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    public static final DataComponentType<Identifier> MELODY = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "melody",
            DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build());

    public static final DataComponentType<Long> START_TIME = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "start_time",
            DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build());

    public static final DataComponentType<Long> PAUSED_TIME = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "paused_time",
            DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build());

    public static final DataComponentType<List<Integer>> TRACKS = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "enabled_tracks",
            DataComponentType.<List<Integer>>builder().persistent(Codec.list(Codec.INT)).networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())).build());

    private static final long MAX_LATE_NOTE_TIME = 150L;

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
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            Network.sendToPlayer(new MelodyListMessage(user), (ServerPlayer) user);
            Network.sendToPlayer(new OpenGuiRequest(), (ServerPlayer) user);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> components, TooltipFlag tooltipFlag) {
        // State
        if (isPlaying(stack)) {
            components.accept(Component.translatable("immersive_melodies.playing").withStyle(ChatFormatting.GREEN));
        }

        super.appendHoverText(stack, context, display, components, tooltipFlag);
    }

    public boolean isPlaying(ItemStack stack) {
        return stack.getOrDefault(PLAYING, false);
    }

    public void inventoryClientTick(ItemStack stack, Level world, LivingEntity entity) {
        ItemStack primaryStack = null;
        List<ItemStack> playingInstruments = new ArrayList<>();
        for (ItemStack handItem : Utils.getHandItems(entity)) {
            if (handItem.getItem() instanceof InstrumentItem instrument && instrument.isPlaying(handItem)) {
                if (primaryStack == null) {
                    primaryStack = handItem;
                }
                playingInstruments.add(handItem);
            }
        }

        if (stack != primaryStack || !world.isClientSide() || !Common.soundManager.audible(entity)) {
            return;
        }

        // Advance one shared timeline, then play each due note through every instrument
        MelodyProgress progress = MelodyProgressManager.INSTANCE.getProgress(entity);
        progress.tick(stack, world.getGameTime());

        // sync
        MelodyProgressManager.INSTANCE.sync(world.getGameTime());

        Melody melody = progress.getMelody();

        long lookAhead = Math.max(0, Config.getInstance().humanizationTime);
        for (int track = 0; track < melody.getTracks().size(); track++) {
            int lastIndex = progress.getLastIndex(track);
            List<Note> notes = melody.getTracks().get(track).getNotes();
            for (int i = lastIndex; i < notes.size(); i++) {
                Note note = notes.get(i);
                if (progress.getTime() - note.getTime() > MAX_LATE_NOTE_TIME) {
                    progress.setLastIndex(track, i + 1);
                    continue;
                }
                if (progress.getTime() + lookAhead >= note.getTime()) {
                    for (ItemStack instrumentStack : playingInstruments) {
                        InstrumentItem instrument = (InstrumentItem) instrumentStack.getItem();
                        List<Integer> enabledTracks = instrument.getEnabledTracks(instrumentStack);
                        if (enabledTracks.isEmpty() || enabledTracks.contains(track)) {
                            instrument.playNote(entity, note, progress.getTime(), instrumentStack == stack);
                        }
                    }

                    if (i == notes.size() - 1) {
                        progress.setLastIndex(track, i + 1);
                    }
                } else {
                    progress.setLastIndex(track, i);
                    break;
                }
            }
        }
    }

    public CancelableSoundInstance playNote(Entity entity, Note note, long time) {
        return playNote(entity, note, time, true);
    }

    private CancelableSoundInstance playNote(Entity entity, Note note, long time, boolean updateProgress) {
        Config config = Config.getInstance();
        float volume = note.getVelocity() / 255.0f * 2.0f * config.instrumentVolumeFactor;
        float pitch = (float) Math.pow(2, (note.getNote() - 24) / 12.0);
        long delay = Math.max(note.getTime() - time, 0);
        long length = Math.max(note.getLength(), 1);
        long sustain = Math.min(this.sustain, note.getSustain());

        // humanize
        volume = (float) Math.max(0.0f, volume * (1.0 + boundedGaussian(config.humanizationVolume)));
        pitch *= (float) Math.pow(2.0, boundedGaussian(config.humanizationPitch) / 12.0);
        delay = Math.max(0, delay + Math.round(boundedGaussian(config.humanizationTime)));
        length = Math.max(1, Math.round(length * (1.0 + boundedGaussian(config.humanizationLength))));

        int octave = 1;
        while (octave < 8 && pitch > 4.0 / 3.0) {
            pitch /= 2;
            octave++;
        }

        // adjust volume based on perceived loudness
        float factor = config.perceivedLoudnessAdjustmentFactor;
        float adjustedVolume = (float) (volume / Math.sqrt(pitch * Math.pow(2, octave - 4)));
        volume = volume * (1.0f - factor) + adjustedVolume * factor;
        volume = Math.max(0.0f, volume);

        // sound
        CancelableSoundInstance soundInstance = Common.soundManager.playSound(entity.getX(), entity.getY(), entity.getZ(),
                sound.get(octave), SoundSource.NEUTRAL,
                volume, pitch, length, sustain,
                delay, entity);

        // Suppress game music
        if (entity instanceof Player ? config.stopGameMusicForPlayers : config.stopGameMusicForMobs) {
            Common.soundManager.suppressGameMusic();
        }

        // particle
        if (entity instanceof LivingEntity livingEntity && !Common.soundManager.isFirstPerson(entity)) {
            double x = Math.sin(-livingEntity.yBodyRot / 180.0 * Math.PI);
            double z = Math.cos(-livingEntity.yBodyRot / 180.0 * Math.PI);
            entity.level().addParticle(ParticleTypes.NOTE,
                    entity.getX() + x * offset.z + z * offset.x, entity.getY() + entity.getBbHeight() / 2.0 + offset.y, entity.getZ() + z * offset.z - x * offset.x,
                    x * 5.0, 0.0, z * 5.0);
        }

        if (updateProgress) {
            MelodyProgressManager.INSTANCE.setLastNote(entity, volume, pitch, length);
        }

        return soundInstance;
    }

    public void inventoryServerTick(ItemStack stack, ServerLevel world, LivingEntity entity) {
        // autoplay
        if (!(entity instanceof Player) && !isPlaying(stack)) {
            ItemStack playingStack = null;
            for (ItemStack handItem : Utils.getHandItems(entity)) {
                if (handItem != stack && handItem.getItem() instanceof InstrumentItem instrument && instrument.isPlaying(handItem)) {
                    playingStack = handItem;
                    break;
                }
            }

            if (playingStack == null) {
                play(stack, ServerMelodyManager.getRandomMelody(), world, entity);
            } else {
                play(stack, getMelody(playingStack), playingStack.getOrDefault(START_TIME, world.getGameTime()), entity);
            }
        }
    }

    public void play(ItemStack stack, Identifier melody, Level world, Entity entity) {
        play(stack, melody, world.getGameTime(), entity);
    }

    private void play(ItemStack stack, Identifier melody, long startTime, Entity entity) {
        stack.set(MELODY, melody);
        stack.set(PLAYING, true);
        stack.set(START_TIME, startTime);
        stack.remove(PAUSED_TIME);

        refreshTracks(stack, entity);
    }

    public static Identifier getMelody(ItemStack stack) {
        return stack.getOrDefault(MELODY, Common.locate("default"));
    }

    public void refreshTracks(ItemStack stack, Entity entity) {
        String identifier = ServerMelodyManager.getIdentifier(entity, BuiltInRegistries.ITEM.getKey(this));
        Set<Integer> enabledTracks = ServerMelodyManager.getSettings().getEnabledTracks(getMelody(stack), identifier);
        stack.set(TRACKS, new ArrayList<>(enabledTracks));
    }

    public void play(ItemStack stack, Level world) {
        if (stack.has(PAUSED_TIME)) {
            long pausedTime = stack.getOrDefault(PAUSED_TIME, world.getGameTime());
            long pausedDuration = Math.max(0L, world.getGameTime() - pausedTime);
            stack.set(START_TIME, stack.getOrDefault(START_TIME, world.getGameTime()) + pausedDuration);
            stack.remove(PAUSED_TIME);
        }
        stack.set(PLAYING, true);
    }

    public void pause(ItemStack stack, Level world) {
        if (isPlaying(stack)) {
            stack.set(PAUSED_TIME, world.getGameTime());
        }
        stack.set(PLAYING, false);
    }

    public List<Integer> getEnabledTracks(ItemStack stack) {
        return stack.getOrDefault(TRACKS, new ArrayList<>());
    }

    private double boundedGaussian(double maxAbs) {
        if (maxAbs <= 0.0) {
            return 0.0;
        }

        double value = random.nextGaussian() * (maxAbs / 3.0);
        return Math.clamp(value, -maxAbs, maxAbs);
    }
}
