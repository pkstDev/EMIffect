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
import net.minecraft.entity.attribute.AttributeModifierCreator;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatusEffectEmiStack extends EmiStack {
    @Nullable
    private final StatusEffect effect;

    protected StatusEffectEmiStack(@Nullable StatusEffect effect) {
        this.effect = effect;
    }

    public static StatusEffectEmiStack of(@Nullable StatusEffect effect) {
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

    public @Nullable StatusEffect getEffect() {
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
    public NbtCompound getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return effect;
    }

    @Override
    public Identifier getId() {
        return Registries.STATUS_EFFECT.getId(effect);
    }

    @Override
    public List<Text> getTooltipText() {
        return List.of(getName());
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        if (effect == null) return List.of();
        List<TooltipComponent> tooltips = new ArrayList<>(getTooltipText().stream().map(EmiPort::ordered).map(TooltipComponent::of).toList());
        switch (effect.getCategory()) {
            case BENEFICIAL -> tooltips.add(TooltipComponent.of(EmiPort.ordered(
                    EmiPort.translatable("tooltip.emiffect.beneficial").formatted(Formatting.GREEN))));
            case NEUTRAL -> tooltips.add(TooltipComponent.of(EmiPort.ordered(
                    EmiPort.translatable("tooltip.emiffect.neutral").formatted(Formatting.GOLD))));
            case HARMFUL -> tooltips.add(TooltipComponent.of(EmiPort.ordered(
                    EmiPort.translatable("tooltip.emiffect.harmful").formatted(Formatting.RED))));
        }
        tooltips.add(TooltipComponent.of(EmiPort.ordered(
                EmiPort.translatable("tooltip.emiffect.color", "#" + String.format("%02x", effect.getColor())).styled(style -> style.withColor(effect.getColor())))));
        Identifier id = Registries.STATUS_EFFECT.getId(effect);
        if (!effect.getAttributeModifiers().isEmpty()) {
            tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(""))));
            tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emiffect.applied").formatted(Formatting.GRAY))));
            for (Map.Entry<EntityAttribute, AttributeModifierCreator> entry: effect.getAttributeModifiers().entrySet()) {
                EntityAttributeModifier entityAttributeModifier = entry.getValue().createAttributeModifier(0);
                double d = entityAttributeModifier.getValue();
                double e;
                if (entityAttributeModifier.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_BASE && entityAttributeModifier.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {
                    if ((entry.getKey()).equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                        e = d * 10.0;
                    } else {
                        e = d;
                    }
                } else {
                    e = d * 100.0;
                }

                if (d > 0.0) {
                    tooltips.add(TooltipComponent.of(EmiPort.ordered((EmiPort.translatable("attribute.modifier.plus." + entityAttributeModifier.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e), Text.translatable((entry.getKey()).getTranslationKey())).formatted(Formatting.BLUE).append(EmiPort.translatable("tooltip.emiffect.per_level").formatted(Formatting.BLUE))))));
                } else if (d < 0.0) {
                    e *= -1.0;
                    tooltips.add(TooltipComponent.of(EmiPort.ordered((EmiPort.translatable("attribute.modifier.take." + entityAttributeModifier.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e), Text.translatable((entry.getKey()).getTranslationKey())).formatted(Formatting.RED).append(EmiPort.translatable("tooltip.emiffect.per_level").formatted(Formatting.RED))))));
                }

            }
        }
        if (id != null)
            tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(EmiUtil.getModName(id.getNamespace()), Formatting.BLUE, Formatting.ITALIC))));
        return tooltips;
    }

    @Override
    public Text getName() {
        return effect != null ? effect.getName() : EmiPort.literal("missingno");
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = super.getItemStack();
        if (effect != null) {
            stack = PotionUtil.setCustomPotionEffects(Items.POTION.getDefaultStack(),
                    Collections.singletonList(new StatusEffectInstance(effect, 600)));
        }
        return stack;
    }
}