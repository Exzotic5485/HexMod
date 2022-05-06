package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.client.be.BlockEntityAkashicBookshelfRenderer;
import at.petrak.hexcasting.client.be.BlockEntitySlateRenderer;
import at.petrak.hexcasting.client.entity.WallScrollRenderer;
import at.petrak.hexcasting.client.particles.ConjureParticle;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.ItemFocus;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.items.ItemSlate;
import at.petrak.hexcasting.common.items.ItemWand;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexParticles;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;

public class RegisterClientStuff {
    public static void init() {
        registerDataHolderOverrides(HexItems.FOCUS);
        registerDataHolderOverrides(HexItems.SPELLBOOK);

        registerPackagedSpellOverrides(HexItems.CYPHER);
        registerPackagedSpellOverrides(HexItems.TRINKET);
        registerPackagedSpellOverrides(HexItems.ARTIFACT);

        IClientXplatAbstractions x = IClientXplatAbstractions.INSTANCE;
        x.registerItemProperty(HexItems.BATTERY, ItemManaBattery.MANA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (ManaHolderItem) stack.getItem();
                return item.getManaFullness(stack);
            });
        x.registerItemProperty(HexItems.BATTERY, ItemManaBattery.MAX_MANA_PREDICATE,
            (stack, level, holder, holderID) -> {
                var item = (ItemManaBattery) stack.getItem();
                var max = item.getMaxMana(stack);
                return (float) Math.sqrt((float) max / HexConfig.common().chargedCrystalManaAmount() / 10);
            });

        x.registerItemProperty(HexItems.SCROLL, ItemScroll.ANCIENT_PREDICATE,
            (stack, level, holder, holderID) -> stack.getOrCreateTag().contains(ItemScroll.TAG_OP_ID) ? 1f : 0f);

        x.registerItemProperty(HexItems.SLATE, ItemSlate.WRITTEN_PRED,
            (stack, level, holder, holderID) -> ItemSlate.hasPattern(stack) ? 1f : 0f);

        registerWandOverrides(HexItems.WAND_OAK);
        registerWandOverrides(HexItems.WAND_BIRCH);
        registerWandOverrides(HexItems.WAND_SPRUCE);
        registerWandOverrides(HexItems.WAND_JUNGLE);
        registerWandOverrides(HexItems.WAND_DARK_OAK);
        registerWandOverrides(HexItems.WAND_ACACIA);
        registerWandOverrides(HexItems.WAND_AKASHIC);

        HexTooltips.init();

        x.setRenderLayer(HexBlocks.CONJURED_LIGHT, RenderType.cutout());
        x.setRenderLayer(HexBlocks.CONJURED_BLOCK, RenderType.cutout());
        x.setRenderLayer(HexBlocks.AKASHIC_DOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.AKASHIC_TRAPDOOR, RenderType.cutout());
        x.setRenderLayer(HexBlocks.SCONCE, RenderType.cutout());

        x.setRenderLayer(HexBlocks.AKASHIC_LEAVES1, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.AKASHIC_LEAVES2, RenderType.cutoutMipped());
        x.setRenderLayer(HexBlocks.AKASHIC_LEAVES3, RenderType.cutoutMipped());

        x.setRenderLayer(HexBlocks.AKASHIC_RECORD, RenderType.translucent());

        x.registerEntityRenderer(HexEntities.WALL_SCROLL, WallScrollRenderer::new);

        addScryingLensStuff();
    }

    private static void addScryingLensStuff() {
        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction, lensHand) -> state.getBlock() instanceof BlockAbstractImpetus,
            (lines, state, pos, observer, world, direction, lensHand) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAbstractImpetus beai) {
                    beai.applyScryingLensOverlay(lines, state, pos, observer, world, direction, lensHand);
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(HexBlocks.AKASHIC_BOOKSHELF,
            (lines, state, pos, observer, world, direction, lensHand) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile) {
                    var recordPos = tile.getRecordPos();
                    var pattern = tile.getPattern();
                    if (recordPos != null && pattern != null) {
                        lines.add(new Pair<>(new ItemStack(HexBlocks.AKASHIC_RECORD), new TranslatableComponent(
                            "hexcasting.tooltip.lens.akashic.bookshelf.location",
                            recordPos.toShortString()
                        )));
                        if (world.getBlockEntity(recordPos) instanceof BlockEntityAkashicRecord record) {
                            lines.add(new Pair<>(new ItemStack(Items.BOOK), record.getDisplayAt(pattern)));
                        }
                    }
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(HexBlocks.AKASHIC_RECORD,
            (lines, state, pos, observer, world, direction, lensHand) -> {
                if (world.getBlockEntity(pos) instanceof BlockEntityAkashicRecord tile) {
                    int count = tile.getCount();

                    lines.add(new Pair<>(new ItemStack(HexBlocks.AKASHIC_BOOKSHELF), new TranslatableComponent(
                        "hexcasting.tooltip.lens.akashic.record.count" + (count == 1 ? ".single" : ""),
                        count
                    )));
                }
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.COMPARATOR,
            (lines, state, pos, observer, world, direction, lensHand) -> {
                int comparatorValue = ScryingLensOverlayRegistry.getComparatorValue(true);
                lines.add(new Pair<>(
                    new ItemStack(Items.REDSTONE),
                    new TextComponent(comparatorValue == -1 ? "" : String.valueOf(comparatorValue))
                        .withStyle(ChatFormatting.RED)));
                lines.add(new Pair<>(
                    new ItemStack(Items.REDSTONE_TORCH),
                    new TextComponent(
                        state.getValue(ComparatorBlock.MODE) == ComparatorMode.COMPARE ? ">" : "-")
                        .withStyle(ChatFormatting.RED)));
            });

        ScryingLensOverlayRegistry.addDisplayer(Blocks.REPEATER,
            (lines, state, pos, observer, world, direction, lensHand) -> lines.add(new Pair<>(
                new ItemStack(Items.CLOCK),
                new TextComponent(String.valueOf(state.getValue(RepeaterBlock.DELAY)))
                    .withStyle(ChatFormatting.YELLOW))));

        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction, lensHand) -> state.isSignalSource() && !state.is(
                Blocks.COMPARATOR),
            (lines, state, pos, observer, world, direction, lensHand) -> {
                int signalStrength = 0;
                if (state.getBlock() instanceof RedStoneWireBlock) {
                    signalStrength = state.getValue(RedStoneWireBlock.POWER);
                } else {
                    for (Direction dir : Direction.values()) {
                        signalStrength = Math.max(signalStrength, state.getSignal(world, pos, dir));
                    }
                }

                lines.add(0, new Pair<>(
                    new ItemStack(Items.REDSTONE),
                    new TextComponent(String.valueOf(signalStrength))
                        .withStyle(ChatFormatting.RED)));
            });

        ScryingLensOverlayRegistry.addPredicateDisplayer(
            (state, pos, observer, world, direction, lensHand) -> state.hasAnalogOutputSignal(),
            (lines, state, pos, observer, world, direction, lensHand) -> {
                int comparatorValue = ScryingLensOverlayRegistry.getComparatorValue(false);
                lines.add(
                    new Pair<>(
                        new ItemStack(Items.COMPARATOR),
                        new TextComponent(comparatorValue == -1 ? "" : String.valueOf(comparatorValue))
                            .withStyle(ChatFormatting.RED)));
            });
    }

    private static void registerDataHolderOverrides(DataHolderItem item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty((Item) item, ItemFocus.DATATYPE_PRED,
            (stack, level, holder, holderID) -> {
                var datum = item.readDatumTag(stack);
                if (datum != null) {
                    var typename = datum.getAllKeys().iterator().next();
                    return switch (typename) {
                        case SpellDatum.TAG_ENTITY -> 1f;
                        case SpellDatum.TAG_DOUBLE -> 2f;
                        case SpellDatum.TAG_VEC3 -> 3f;
                        case SpellDatum.TAG_WIDGET -> 4f;
                        case SpellDatum.TAG_LIST -> 5f;
                        case SpellDatum.TAG_PATTERN -> 6f;
                        default -> 0f; // uh oh
                    };
                }
                return 0f;
            });
        IClientXplatAbstractions.INSTANCE.registerItemProperty((Item) item, ItemFocus.SEALED_PRED,
            (stack, level, holder, holderID) -> item.canWrite(stack, SpellDatum.make(Widget.NULL)) ? 0f : 1f);
    }

    private static void registerPackagedSpellOverrides(ItemPackagedHex item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(item, ItemPackagedHex.HAS_PATTERNS_PRED,
            (stack, level, holder, holderID) ->
                item.getPatterns(stack) != null ? 1f : 0f
        );
    }

    private static void registerWandOverrides(ItemWand item) {
        IClientXplatAbstractions.INSTANCE.registerItemProperty(item, ItemWand.FUNNY_LEVEL_PREDICATE,
            (stack, level, holder, holderID) -> {
                var name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                if (name.contains("old")) {
                    return 1f;
                } else if (name.contains("wand of the forest")) {
                    return 2f;
                } else {
                    return 0f;
                }
            });
    }

    public static void registerParticles() {
        // rip particle man
        IClientXplatAbstractions.INSTANCE.registerParticleType(HexParticles.LIGHT_PARTICLE.get(),
            ConjureParticle.Provider::new);
        IClientXplatAbstractions.INSTANCE.registerParticleType(HexParticles.CONJURE_PARTICLE.get(),
            ConjureParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerBlockEntityRenderer(HexBlockEntities.SLATE_TILE.get(), BlockEntitySlateRenderer::new);
        evt.registerBlockEntityRenderer(HexBlockEntities.AKASHIC_BOOKSHELF_TILE.get(),
            BlockEntityAkashicBookshelfRenderer::new);
    }
}
