package io.github.pkstdev.emiffect;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import io.github.pkstdev.emiffect.recipe.StatusEffectInfo;
import io.github.pkstdev.emiffect.util.stack.StatusEffectEmiStack;
import io.shcm.shsupercm.fabric.fletchingtable.api.Entrypoint;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entrypoint("emi") // For automatically registering the entrypoint
public class EMIffectPlugin implements EmiPlugin {
    public static final String MOD_ID = "emiffect";
    public static final Logger LOGGER = LoggerFactory.getLogger("Emiffect");
    public static final Identifier CATEGORY_ICON = new Identifier(MOD_ID, "textures/gui/emi/icon.png");
    public static final EmiRecipeCategory CATEGORY
            = new EmiRecipeCategory(new Identifier(MOD_ID, "status_effect_info"), new EmiTexture(CATEGORY_ICON, 0, 0, 16, 16, 16, 16, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        for (StatusEffect effect : Registry.STATUS_EFFECT) {
            StatusEffectEmiStack stack = StatusEffectEmiStack.of(effect);
            registry.addEmiStack(stack);
            registry.addRecipe(new StatusEffectInfo(effect, stack));
        }
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Blocks.BEACON));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.POTION));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.SPLASH_POTION));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.LINGERING_POTION));
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.SUSPICIOUS_STEW));
        for (Item item : Registry.ITEM) {
            FoodComponent food = item.getFoodComponent();
            if (food != null) {
                if (!food.getStatusEffects().isEmpty()) {
                    registry.addWorkstation(CATEGORY, EmiStack.of(item));
                }
            }
        }
    }
}
