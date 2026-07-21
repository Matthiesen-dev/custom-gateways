package dev.matthiesen.custom_gateways.common.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class LinkCrossDimensionPortalsCriterion extends SimpleCriterionTrigger<LinkCrossDimensionPortalsCriterion.Conditions> {

    @Override
    public @NotNull Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, Conditions::requirementsMet);
    }

    public record Conditions(Optional<ContextAwarePredicate> player) implements SimpleInstance {
        public static Codec<Conditions> CODEC = ContextAwarePredicate.CODEC.optionalFieldOf("player")
                .xmap(Conditions::new, Conditions::player).codec();

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return player;
        }

        public boolean requirementsMet() {
            return true;
        }
    }
}

