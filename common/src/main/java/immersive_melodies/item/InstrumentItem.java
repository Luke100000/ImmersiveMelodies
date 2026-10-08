package immersive_melodies.item;

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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class InstrumentItem extends Item {
    public static final String TAG_PLAYING = "playing";
    public static final String TAG_MELODY = "melody";
    public static final String TAG_START_TIME = "start_time";
    public static final String TAG_PAUSED_TIME = "paused_time";
    public static final String TAG_TRACKS = "enabled_tracks";

    private static final long MAX_LATE_NOTE_TIME = 150L;

    private final Sounds.Instrument sound;
    private final long sustain;

    private final Vector3f offset;
    private final Random random = new Random();

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
            Network.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.SELECTOR), (ServerPlayer) user);
        }

        return InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        // State
        if (isPlaying(stack)) {
            tooltip.add(Component.translatable("immersive_melodies.playing").withStyle(ChatFormatting.GREEN));
        }

        super.appendHoverText(stack, world, tooltip, context);
    }

    public boolean isPlaying(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(TAG_PLAYING);
    }

    public void inventoryClientTick(ItemStack stack, Level world, Entity entity) {
        ItemStack primaryStack = null;
        List<ItemStack> playingInstruments = new ArrayList<>();
        for (ItemStack handItem : entity.getHandSlots()) {
            if (handItem.getItem() instanceof InstrumentItem instrument && instrument.isPlaying(handItem)) {
                if (primaryStack == null) {
                    primaryStack = handItem;
                }
                playingInstruments.add(handItem);
            }
        }

        if (stack != primaryStack || !world.isClientSide || !Common.soundManager.audible(entity)) {
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
                        Set<Integer> enabledTracks = instrument.getEnabledTracks(instrumentStack);
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

    private double boundedGaussian(double maxAbs) {
        if (maxAbs <= 0.0) {
            return 0.0;
        }

        double value = random.nextGaussian() * (maxAbs / 3.0);
        return Math.max(-maxAbs, Math.min(maxAbs, value));
    }

    public void inventoryServerTick(ItemStack stack, ServerLevel world, Entity entity) {
        // autoplay
        if (!(entity instanceof Player) && !isPlaying(stack)) {
            ItemStack playingStack = null;
            for (ItemStack handItem : entity.getHandSlots()) {
                if (handItem != stack && handItem.getItem() instanceof InstrumentItem instrument && instrument.isPlaying(handItem)) {
                    playingStack = handItem;
                    break;
                }
            }

            if (playingStack == null) {
                play(stack, ServerMelodyManager.getRandomMelody(), world, entity);
            } else {
                play(stack, getMelody(playingStack), playingStack.getOrCreateTag().getLong(TAG_START_TIME), entity);
            }
        }
    }

    /**
     * Starts a melody at the current world game tick.
     */
    public void play(ItemStack stack, ResourceLocation melody, Level world, Entity entity) {
        play(stack, melody, world.getGameTime(), entity);
    }

    /**
     * Starts a melody with the given start tick so performers can play in sync.
     *
     * @param startTime The world game tick when playback began.
     * @param entity    The performer used to look up track settings.
     */
    public void play(ItemStack stack, ResourceLocation melody, long startTime, Entity entity) {
        stack.getOrCreateTag().putString(TAG_MELODY, melody.toString());
        stack.getOrCreateTag().putBoolean(TAG_PLAYING, true);
        stack.getOrCreateTag().putLong(TAG_START_TIME, startTime);
        stack.getOrCreateTag().remove(TAG_PAUSED_TIME);

        refreshTracks(stack, entity);
    }

    public ResourceLocation getMelody(ItemStack stack) {
        return new ResourceLocation(stack.getOrCreateTag().getString(TAG_MELODY));
    }

    /**
     * Returns the melody and start tick of a playing instrument.
     * Returns empty if the stack is not a playing instrument or its playback data is missing or invalid.
     */
    public static Optional<Playback> getPlayback(ItemStack stack) {
        if (!(stack.getItem() instanceof InstrumentItem instrument)) {
            return Optional.empty();
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_MELODY, Tag.TAG_STRING)
            || !tag.contains(TAG_START_TIME, Tag.TAG_LONG) || !instrument.isPlaying(stack)) {
            return Optional.empty();
        }

        ResourceLocation melody = ResourceLocation.tryParse(tag.getString(TAG_MELODY));
        return melody == null ? Optional.empty() : Optional.of(new Playback(melody, tag.getLong(TAG_START_TIME)));
    }

    /**
     * A playing melody and its start tick in world game time.
     */
    public record Playback(ResourceLocation melody, long startTime) {
    }

    public void refreshTracks(ItemStack stack, Entity entity) {
        String identifier = ServerMelodyManager.getIdentifier(entity, BuiltInRegistries.ITEM.getKey(this));
        Set<Integer> enabledTracks = ServerMelodyManager.getSettings().getEnabledTracks(getMelody(stack), identifier);
        stack.getOrCreateTag().putIntArray(TAG_TRACKS, enabledTracks.stream().mapToInt(i -> i).toArray());
    }

    /**
     * Resumes playback at its paused position. Call on the server thread.
     */
    public void play(ItemStack stack, Level world) {
        if (stack.getOrCreateTag().contains(TAG_PAUSED_TIME)) {
            long pausedTime = stack.getOrCreateTag().getLong(TAG_PAUSED_TIME);
            long pausedDuration = Math.max(0L, world.getGameTime() - pausedTime);
            stack.getOrCreateTag().putLong(TAG_START_TIME, stack.getOrCreateTag().getLong(TAG_START_TIME) + pausedDuration);
            stack.getOrCreateTag().remove(TAG_PAUSED_TIME);
        }
        stack.getOrCreateTag().putBoolean(TAG_PLAYING, true);
    }

    /**
     * Pauses playback. Call on the server thread.
     * Mobs holding the instrument start playing again on their next instrument tick.
     */
    public void pause(ItemStack stack, Level world) {
        if (isPlaying(stack)) {
            stack.getOrCreateTag().putLong(TAG_PAUSED_TIME, world.getGameTime());
        }
        stack.getOrCreateTag().putBoolean(TAG_PLAYING, false);
    }

    public Set<Integer> getEnabledTracks(ItemStack stack) {
        if (!stack.getOrCreateTag().contains(TAG_TRACKS)) {
            return Set.of();
        }
        int[] array = stack.getOrCreateTag().getIntArray(TAG_TRACKS);
        return Arrays.stream(array).boxed().collect(Collectors.toSet());
    }
}
