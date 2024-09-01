package io.github.prismwork.emiffect;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import io.github.prismwork.emiffect.recipe.StatusEffectInfo;
import io.github.prismwork.emiffect.util.stack.StatusEffectEmiStack;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

@EmiEntrypoint
public class EMIffectPlugin implements EmiPlugin {
    public static final String MOD_ID = "emiffect";
    public static final Identifier CATEGORY_ICON = Identifier.of(MOD_ID, "textures/gui/emi/icon.png");
    public static final EmiRecipeCategory CATEGORY
            = new EmiRecipeCategory(Identifier.of(MOD_ID, "status_effect_info"), new EmiTexture(CATEGORY_ICON, 0, 0, 16, 16, 16, 16, 16, 16));

    @Override
    public void register(EmiRegistry registry) {

        for (RegistryEntry<StatusEffect> effect : Registries.STATUS_EFFECT.getIndexedEntries()) {
            StatusEffectEmiStack stack = StatusEffectEmiStack.of(effect);
            registry.addRecipe(new StatusEffectInfo(effect, stack));
            registry.addEmiStack(stack);
        }

        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Blocks.BEACON));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.POTION));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.SPLASH_POTION));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.LINGERING_POTION));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.SUSPICIOUS_STEW));

        for (Item item : Registries.ITEM) {
            FoodComponent food = item.getDefaultStack().get(DataComponentTypes.FOOD);
            if (food != null) {
                if (!food.effects().isEmpty()) {
                    registry.addWorkstation(CATEGORY, EmiStack.of(item));
                }
            }
        }
    }
}