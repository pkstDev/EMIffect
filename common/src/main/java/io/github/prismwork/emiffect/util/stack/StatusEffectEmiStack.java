package io.github.prismwork.emiffect.util.stack;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatusEffectEmiStack extends EmiStack {
    @Nullable
    private final RegistryEntry<StatusEffect> effect;

    protected StatusEffectEmiStack(@Nullable RegistryEntry<StatusEffect> effect) {
        this.effect = effect;
    }

    public static StatusEffectEmiStack of(@Nullable RegistryEntry<StatusEffect> effect) {
        return new StatusEffectEmiStack(effect);
    }

    @Override
    public EmiStack copy() {
        return StatusEffectEmiStack.of(this.effect);
    }

    @Override
    public boolean isEmpty() {
        return effect == null;
    }

    public @Nullable RegistryEntry<StatusEffect> getEffect() {
        return effect;
    }

    @Override
    public void render(DrawContext draw, int x, int y, float delta, int flags) {
        StatusEffectSpriteManager sprites = MinecraftClient.getInstance().getStatusEffectSpriteManager();
        if (effect != null) {
            Sprite sprite = sprites.getSprite(effect);
            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, sprite.getAtlasId());
            draw.drawSprite(x - 1, y - 1, 0, 18, 18, sprite);
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Override
    public ComponentChanges getComponentChanges() {
        return ComponentChanges.EMPTY;
    }

    @Override
    public Object getKey() {
        return effect;
    }

    @Override
    public Identifier getId() {
        return Registries.STATUS_EFFECT.getId(effect.value());
    }

    @Override
    public List<Text> getTooltipText() {
        return List.of(getName());
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        if (effect == null) return List.of();
        List<TooltipComponent> tooltips = new ArrayList<>(getTooltipText().stream().map(EmiPort::ordered).map(TooltipComponent::of).toList());
        switch (effect.value().getCategory()) {
            case BENEFICIAL -> tooltips.add(TooltipComponent.of(EmiPort.ordered(
                    EmiPort.translatable("tooltip.emiffect.beneficial").formatted(Formatting.GREEN))));
            case NEUTRAL -> tooltips.add(TooltipComponent.of(EmiPort.ordered(
                    EmiPort.translatable("tooltip.emiffect.neutral").formatted(Formatting.GOLD))));
            case HARMFUL -> tooltips.add(TooltipComponent.of(EmiPort.ordered(
                    EmiPort.translatable("tooltip.emiffect.harmful").formatted(Formatting.RED))));
        }
        tooltips.add(TooltipComponent.of(EmiPort.ordered(
                EmiPort.translatable("tooltip.emiffect.color", "#" + String.format("%02x", effect.value().getColor())).styled(style -> style.withColor(effect.value().getColor())))));
        Identifier id = Registries.STATUS_EFFECT.getId(effect.value());
        AtomicBoolean blankLine = new AtomicBoolean(false);
        effect.value().forEachAttributeModifier(0, ((entityAttribute, entityAttributeModifier) -> {
            if (!blankLine.get()) {
                tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(""))));
                tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emiffect.applied").formatted(Formatting.GRAY))));
                blankLine.set(true);
            }
            double d = entityAttributeModifier.value();
            double e;
            if (entityAttributeModifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE && entityAttributeModifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                if ((entityAttribute.getKeyOrValue()).equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE.getKeyOrValue())) {
                    e = d * 10.0;
                } else {
                    e = d;
                }
            } else {
                e = d * 100.0;
            }

            if (d > 0.0) {
                tooltips.add(TooltipComponent.of(EmiPort.ordered((EmiPort.translatable("attribute.modifier.plus." + entityAttributeModifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e), Text.translatable((entityAttribute.value()).getTranslationKey())).formatted(Formatting.BLUE).append(EmiPort.translatable("tooltip.emiffect.per_level").formatted(Formatting.BLUE))))));
            } else if (d < 0.0) {
                e *= -1.0;
                tooltips.add(TooltipComponent.of(EmiPort.ordered((EmiPort.translatable("attribute.modifier.take." + entityAttributeModifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e), Text.translatable((entityAttribute.value()).getTranslationKey())).formatted(Formatting.RED).append(EmiPort.translatable("tooltip.emiffect.per_level").formatted(Formatting.RED))))));
            }

        }));
        if (id != null)
            tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(EmiUtil.getModName(id.getNamespace()), Formatting.BLUE, Formatting.ITALIC))));
        return tooltips;
    }

    @Override
    public Text getName() {
        return effect != null ? effect.value().getName() : EmiPort.literal("missingno");
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = super.getItemStack();
        if (effect != null) {
            stack = Items.POTION.getDefaultStack();
            stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), Collections.singletonList(new StatusEffectInstance(effect, 600))));
        }
        return stack;
    }
}