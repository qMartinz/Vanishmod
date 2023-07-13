package redstonedubstep.mods.vanishmod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class VanishConfig {
	public static final ForgeConfigSpec SERVER_SPEC;
	public static final Config CONFIG;

	static {
		final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);

		SERVER_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	public static class Config {
		public BooleanValue hidePlayersFromWorld;
		public BooleanValue hidePlayersFromPlayerLists;
		public BooleanValue disableCommandTargeting;
		public BooleanValue hidePlayerNameInChat;
		public BooleanValue sendFakeJoinLeaveMessages;
		public BooleanValue fixModCompatibility;
		public BooleanValue vanishedPlayersSeeEachOther;
		public BooleanValue seeVanishedTeamPlayers;
		public BooleanValue indirectSoundSuppression;
		public BooleanValue indirectParticleSuppression;
		public IntValue vanishCommandPermissionLevel;
		public IntValue seeVanishedPermissionLevel;
		public ConfigValue<String> onVanishMessage;
		public ConfigValue<String> onUnvanishMessage;
		public ConfigValue<String> onVanishQuery;
		public ConfigValue<String> onUnvanishQuery;

		Config(ForgeConfigSpec.Builder builder) {
			hidePlayersFromWorld = builder
					.comment(" --- Vanishmod Config File --- ", "Should vanished players be physically hidden from the world? This includes sound and particle suppression")
					.define("hidePlayersFromWorld", false);
			hidePlayersFromPlayerLists = builder
					.comment("Should vanished players be hidden from player lists such as the /list command and the list in the Multiplayer screen?")
					.define("hidePlayersFromPlayerLists", true);
			disableCommandTargeting = builder
					.comment("Should vanished players not be targetable by other players via command selectors (so players that cannot see vanished players cannot target them with e.g. /msg or /give)?")
					.define("disableCommandTargeting", true);
			hidePlayerNameInChat = builder
					.comment("When unvanished players receive a chat message from a vanished player, should the name of the player that sent the message be replaced with \"???\" (in gray color and obfuscated)?")
					.define("hidePlayerNameInChat", true);
			sendFakeJoinLeaveMessages = builder
					.comment("Should players see a fake join/leave message in their chat when another player (un-)vanishes?")
					.define("sendFakeJoinLeaveMessages", false);
			fixModCompatibility = builder
					.comment("Should there be a (potential) fix for other mods uncovering the presence of vanished players? This may severely increase CPU usage and is thus not recommended")
					.define("fixModCompatibility", false);
			vanishedPlayersSeeEachOther = builder
					.comment("Should vanished players be able to see each other?")
					.define("vanishedPlayersSeeEachOther", false);
			seeVanishedTeamPlayers = builder
					.comment("Should players be able to see vanished players in the same vanilla team, if the \"seeFriendlyInvisibles\" option is enabled for that team?")
					.define("seeVanishedTeamPlayers", false);
			indirectSoundSuppression = builder
					.comment("Should this mod try to detect and suppress sounds that are indirectly caused by vanished players (e.g. pressing a button or hitting an entity)? This detection might accidentally suppress a few sounds unrelated to vanished players, disable this detection if too many sound bugs occur")
					.define("indirectSoundSuppression", false);
			indirectParticleSuppression = builder
					.comment("Should this mod try to detect and suppress particles that are indirectly caused by vanished players (e.g. eating or block breaking particles)? This detection might accidentally suppress particles unrelated to vanished players, disable this detection if too many visual bugs occur")
					.define("indirectParticleSuppression", false);

			vanishCommandPermissionLevel = builder
					.comment("What op permission level should be the requirement for being able to execute /vanish? (A value of 2 or lower allows command blocks to execute /vanish)")
					.defineInRange("vanishCommandPermissionLevel", 2, 0, 4);
			seeVanishedPermissionLevel = builder
					.comment("What op permission level should be the requirement for being able to see vanished players, no matter if the player with that permission level is vanished or not? A value of -1 disables this feature.")
					.defineInRange("seeVanishedPermissionLevel", -1, -1, 4);

			onVanishMessage = builder
					.comment("What message should a player receive when they vanish? (%s will get replaced with the name of the vanishing player)")
					.define("onVanishMessage", "%s vanished");
			onUnvanishMessage = builder
					.comment("What message should a player receive when they unvanish? (%s will get replaced with the name of the unvanishing player)")
					.define("onUnvanishMessage", "%s unvanished");
			onVanishQuery = builder
					.comment("What message should a player receive if they query the vanished status of a vanished player? (%s will get replaced with the name of the player that the status is queried of)")
					.define("onVanishQuery", "%s is currently vanished.");
			onUnvanishQuery = builder
					.comment("What message should a player receive if they query the vanished status of a visible player? (%s will get replaced with the name of the player that the status is queried of)")
					.define("onUnvanishQuery", "%s is currently not vanished.");
		}
	}
}
