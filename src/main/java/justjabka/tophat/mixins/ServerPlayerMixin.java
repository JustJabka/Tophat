package justjabka.tophat.mixins;

import justjabka.tophat.contents.attachment.OpenedContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Range;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "doTick", at = @At(value = "TAIL"))
    private void doTickContainer(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (player.containerMenu == player.inventoryMenu) return;
        syncToMenu(player);
    }

    @Inject(method = "doCloseContainer", at = @At(value = "TAIL"))
    private void doCloseContainer(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        OpenedContainer.get(player).clear();
    }

    @Unique
    private void syncToMenu(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == player.inventoryMenu) return;

        OpenedContainer.OpenedContainerData containerData = OpenedContainer.get(player);

        List<ItemStackWithSlot> attachedItems = containerData.get();
        if (attachedItems.isEmpty()) return;

        for (ItemStackWithSlot entry : attachedItems) {
            int slotId = entry.slot();

            Range<Integer> menuSlots = Range.of(0, menu.slots.size());
            if (!menuSlots.contains(slotId)) continue;

            Slot slot = menu.slots.get(slotId);
            ItemStack newStack = entry.stack().copy();
            slot.set(newStack);
        }

        menu.broadcastChanges();
        containerData.clear();
    }
}
