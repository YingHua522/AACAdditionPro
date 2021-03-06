package de.photon.aacadditionpro.modules.checks.packetanalysis;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ImmutableSet;
import de.photon.aacadditionpro.modules.IncompatiblePluginModule;
import de.photon.aacadditionpro.modules.ModuleType;
import de.photon.aacadditionpro.modules.PatternModule;
import de.photon.aacadditionpro.user.DataKey;
import de.photon.aacadditionpro.user.User;
import de.photon.aacadditionpro.util.VerboseSender;
import de.photon.aacadditionpro.util.packetwrappers.client.WrapperPlayClientPositionLook;
import org.bukkit.Location;

import java.util.Set;

/**
 * This {@link de.photon.aacadditionpro.modules.PatternModule.PacketPattern} detects spoofed positions.
 */
class PositionSpoofPattern extends PatternModule.PacketPattern implements IncompatiblePluginModule
{
    PositionSpoofPattern()
    {
        // Response to PacketType.Play.Client.POSITION
        super(ImmutableSet.of(PacketType.Play.Client.POSITION_LOOK));
    }

    @Override
    protected int process(User user, PacketEvent packetEvent)
    {
        final WrapperPlayClientPositionLook clientPositionLookWrapper = new WrapperPlayClientPositionLook(packetEvent.getPacket());

        // Only check if the player has been teleported recently
        if (user.hasTeleportedRecently(1000) &&
            // World changes and respawns are exempted
            !user.hasRespawnedRecently(2500) &&
            !user.hasChangedWorldsRecently(2500) &&
            // Lag occurrences after login.
            !user.hasLoggedInRecently(10000))
        {
            // The position packet might not be exactly the same position.
            // Squared values of 10, 5 and 3
            final double allowedDistanceSquared = user.getPlayer().isFlying() ?
                                                  100 :
                                                  (user.getPlayer().isSprinting() ? 25 : 9);

            final Location forcedLocation = (Location) user.getDataMap().getValue(DataKey.PACKET_ANALYSIS_LAST_POSITION_FORCE_LOCATION);

            if (forcedLocation.getWorld().getUID().equals(user.getPlayer().getWorld().getUID())) {
                final double distanceSquared = forcedLocation.distanceSquared(clientPositionLookWrapper.getLocation(user.getPlayer().getWorld()));

                if (distanceSquared > allowedDistanceSquared) {
                    VerboseSender.getInstance().sendVerboseMessage("PacketAnalysisData-Verbose | Player: " + user.getPlayer().getName() + " tried to spoof position packets. | DS: " + distanceSquared + " ADS: " + allowedDistanceSquared);
                    return 10;
                }
            } else {
                VerboseSender.getInstance().sendVerboseMessage("PacketAnalysisData-Verbose | Player: " + user.getPlayer().getName() + " diff world.");
            }
        }
        return 0;
    }

    @Override
    public Set<String> getIncompatiblePlugins()
    {
        return ImmutableSet.of("ProtocolSupport");
    }

    @Override
    public String getConfigString()
    {
        return this.getModuleType().getConfigString() + ".parts.PositionSpoof";
    }

    @Override
    public ModuleType getModuleType()
    {
        return ModuleType.PACKET_ANALYSIS;
    }
}
