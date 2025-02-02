package redstonedubstep.mods.vanishmod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.misc.FieldHolder;
import redstonedubstep.mods.vanishmod.misc.SoundSuppressionHelper;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;
	@Shadow
	@Final
	private MinecraftServer server;

	//Filter any packets that we wish to not send to players that cannot see vanished players, mainly consisting of player info and sound packets.
	//We don't filter player info removal packets, because this mod uses them to remove players after their status has changed to be vanished,
	//and it can be done safely because not suppressing these packets does not break this mod (in: a player removal packet sent too much wouldn't break this mod as much as a player addition packet)
	//We need to filter the item entity packets because otherwise all other clients think that they picked up an item (and thus show a pickup animation for the local player), while in reality a vanished player did
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
		if (packet instanceof ClientboundPlayerInfoPacket infoPacket && infoPacket.getAction() != Action.REMOVE_PLAYER) {
			List<PlayerUpdate> filteredPacketEntries = infoPacket.getEntries().stream().filter(p -> !VanishUtil.isVanished(server.getPlayerList().getPlayer(p.getProfile().getId()), player)).toList();

			if (filteredPacketEntries.isEmpty())
				callbackInfo.cancel();
			else if (!filteredPacketEntries.equals(infoPacket.getEntries()))
				infoPacket.entries = filteredPacketEntries;
		}
		else if (packet instanceof ClientboundTakeItemEntityPacket pickupPacket && VanishUtil.isVanished(player.level.getEntity(pickupPacket.getPlayerId()), player))
			callbackInfo.cancel();
		else if (VanishConfig.CONFIG.hidePlayersFromWorld.get()) {
			if (packet instanceof ClientboundSoundPacket soundPacket && SoundSuppressionHelper.shouldSuppressSoundEventFor(SoundSuppressionHelper.getPlayerForPacket(soundPacket), player.level, soundPacket.getX(), soundPacket.getY(), soundPacket.getZ(), player))
				callbackInfo.cancel();
			else if (packet instanceof ClientboundSoundEntityPacket soundPacket && SoundSuppressionHelper.shouldSuppressSoundEventFor(SoundSuppressionHelper.getPlayerForPacket(soundPacket), player.level, player.level.getEntity(soundPacket.getId()), player))
				callbackInfo.cancel();
			else if (packet instanceof ClientboundLevelEventPacket soundPacket && SoundSuppressionHelper.shouldSuppressSoundEventFor(SoundSuppressionHelper.getPlayerForPacket(soundPacket), player.level, Vec3.atCenterOf(soundPacket.getPos()), player))
				callbackInfo.cancel();
			else if (packet instanceof ClientboundBlockEventPacket eventPacket && SoundSuppressionHelper.shouldSuppressSoundEventFor(null, player.level, Vec3.atCenterOf(eventPacket.getPos()), player))
				callbackInfo.cancel();
			else if (packet instanceof ClientboundLevelParticlesPacket particlesPacket && SoundSuppressionHelper.shouldSuppressParticlesFor(null, player.level, particlesPacket.getX(), particlesPacket.getY(), particlesPacket.getZ(), player))
				callbackInfo.cancel();
		}
	}

	//Prevent join, leave, death and advancement messages of vanished players from being broadcast
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"), cancellable = true)
	private void vanishmod$onSendPacket(Packet<?> packet, PacketSendListener listener, CallbackInfo callbackInfo) {
		if (packet instanceof ClientboundSystemChatPacket chatPacket && chatPacket.content() instanceof MutableComponent component && component.getContents() instanceof TranslatableContents content) {
			if (content.getKey().startsWith("multiplayer.player.joined") && VanishUtil.isVanished(FieldHolder.joiningPlayer, player)) {
				FieldHolder.joiningPlayer = null;
				callbackInfo.cancel();
			}
			else if (content.getKey().startsWith("multiplayer.player.left") || content.getKey().startsWith("death.") || content.getKey().startsWith("chat.type.advancement")) {
				if (content.getArgs()[0] instanceof Component playerName) {
					for (ServerPlayer sender : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
						if (sender.getDisplayName().getString().equals(playerName.getString()) && VanishUtil.isVanished(sender, player))
							callbackInfo.cancel();
					}
				}
			}
		}
	}
}
