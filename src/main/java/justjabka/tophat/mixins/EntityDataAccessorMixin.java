package justjabka.tophat.mixins;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EntityDataAccessor.class)
public class EntityDataAccessorMixin {
    @Shadow @Final private Entity entity;
    @Shadow @Final private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "setData", at = @At("HEAD"), cancellable = true)
    public void setData(CompoundTag tag, CallbackInfo ci) {
        if (!(entity instanceof Player)) return;

        UUID uuid = this.entity.getUUID();
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.entity.problemPath(), LOGGER)) {
            this.entity.load(TagValueInput.create(reporter, this.entity.registryAccess(), tag));
            this.entity.setUUID(uuid);
        }

        ci.cancel();
    }
}