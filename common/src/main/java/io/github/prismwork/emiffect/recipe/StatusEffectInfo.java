package io.github.prismwork.emiffect.recipe;

import com.mojang.datafixers.util.Pair;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.prismwork.emiffect.EMIffectPlugin;
import io.github.prismwork.emiffect.util.stack.StatusEffectEmiStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusEffectInfo implements EmiRecipe {
    private final List<EmiIngredient> inputs;
    private final List<OrderedText> desc;
    private final Identifier id;
    private int inputStackRow;
    private final StatusEffectEmiStack emiStack;

    public StatusEffectInfo(StatusEffect effect, StatusEffectEmiStack emiStack) {
        this.id = Registries.STATUS_EFFECT.getId(effect) != null ? Registries.STATUS_EFFECT.getId(effect) : new Identifier("emiffect", "missingno");
        List<EmiIngredient> inputs0 = new ArrayList<>();

        for (Potion potion : Registries.POTION) {
            for (StatusEffectInstance instance : potion.getEffects()) {
                if (instance.getEffectType().equals(effect)) {
                    inputs0.addAll(List.of(EmiStack.of(PotionUtil.setPotion(Items.POTION.getDefaultStack(), potion)),
                            EmiStack.of(PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), potion)),
                            EmiStack.of(PotionUtil.setPotion(Items.LINGERING_POTION.getDefaultStack(), potion)),
                            EmiStack.of(PotionUtil.setPotion(Items.TIPPED_ARROW.getDefaultStack(), potion))));
                    break;
                }
            }
        }
        for (Block block : Registries.BLOCK) {
            if (block instanceof FlowerBlock flower) {
                ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
                StatusEffect flowerEffect = flower.getEffectInStew();
                if (flowerEffect.equals(effect)) {
                    SuspiciousStewItem.addEffectToStew(stew, effect, 200);
                    inputs0.add(EmiStack.of(stew));
                    break;
                }
            }
        }
        for (Item item : Registries.ITEM) {
            FoodComponent food = item.getFoodComponent();
            if (food != null) {
                ItemStack stack = new ItemStack(item);
                for (Pair<StatusEffectInstance, Float> pair : food.getStatusEffects()) {
                    if (pair.getFirst().getEffectType().equals(effect)) {
                        inputs0.add(EmiStack.of(stack));
                        break;
                    }
                }
            }
        }
        for (StatusEffect[] effects : BeaconBlockEntity.EFFECTS_BY_LEVEL) {
            if (Arrays.asList(effects).contains(effect)) {
                inputs0.add(EmiStack.of(Blocks.BEACON));
            }
        }

        this.inputs = inputs0;
        String key1 = "effect." + id.getNamespace() + "." + id.getPath() + ".description";
        String key2 = "effect." + id.getNamespace() + "." + id.getPath() + ".desc";
        Text description = I18n.hasTranslation(key1) ? EmiPort.translatable(key1) : (I18n.hasTranslation(key2) ? EmiPort.translatable(key2) : EmiPort.translatable("emiffect.status_effect_info.missing"));
        this.desc = MinecraftClient.getInstance().textRenderer.wrapLines(description, 110);
        this.inputStackRow = inputs.isEmpty() ? 0 : 1;
        int inputColumn = 0;
        for (EmiIngredient ignored : inputs) {
            if (inputColumn >= 6) {
                this.inputStackRow += 1;
                inputColumn = 0;
            }
            inputColumn += 1;
        }
        this.emiStack = emiStack;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMIffectPlugin.CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return new Identifier("emiffect", "effects/"
                + id.getNamespace()
                + "/" + id.getPath());
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(emiStack);
    }

    @Override
    public int getDisplayWidth() {
        return 144;
    }

    @Override
    public int getDisplayHeight() {
        return 14 + Math.max(desc.size() * MinecraftClient.getInstance().textRenderer.fontHeight, 30) + 4 + (inputStackRow * 18) + 2;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int titleColor = 16777215;
        if (emiStack.getEffect() != null) {
            switch (emiStack.getEffect().getCategory()) {
                case BENEFICIAL -> titleColor = Formatting.GREEN.getColorValue();
                case NEUTRAL -> titleColor = Formatting.GOLD.getColorValue();
                case HARMFUL -> titleColor = Formatting.RED.getColorValue();
            }
        }
        OrderedText title = emiStack.getName().asOrderedText();
        int titleX = 31 + ((144 - 31 - MinecraftClient.getInstance().textRenderer.getWidth(title)) / 2);
        widgets.addText(title, titleX, 2, titleColor, true);

        final int lineHeight = MinecraftClient.getInstance().textRenderer.fontHeight;
        int descLine = 0;
        for (OrderedText text : desc) {
            widgets.addText(text, 31, 14 + lineHeight * descLine, 16777215, true);
            descLine += 1;
        }
        int descHeight = Math.max(descLine * lineHeight, 30);
        descHeight += 12;

        int inputRow = 0;
        int inputColumn = 0;
        for (EmiIngredient ingredient : inputs) {
            widgets.addSlot(ingredient, 18 + (inputColumn * 18), descHeight + 4 + (inputRow * 18));
            inputColumn += 1;
            if (inputColumn >= 6) {
                inputRow += 1;
                inputColumn = 0;
            }
        }

        SlotWidget effectSlot = new SlotWidget(emiStack, 3, (descHeight - 26) / 2).large(true);
        widgets.add(effectSlot);
    }
}