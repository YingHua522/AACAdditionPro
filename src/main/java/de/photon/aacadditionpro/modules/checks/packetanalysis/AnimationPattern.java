package de.photon.aacadditionpro.modules.checks.packetanalysis;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.ImmutableSet;
import de.photon.aacadditionpro.ServerVersion;
import de.photon.aacadditionpro.modules.ModuleType;
import de.photon.aacadditionpro.modules.PatternModule;
import de.photon.aacadditionpro.modules.RestrictedServerVersion;
import de.photon.aacadditionpro.user.DataKey;
import de.photon.aacadditionpro.user.User;
import de.photon.aacadditionpro.util.packetwrappers.client.WrapperPlayClientUseEntity;

import java.util.Set;

public class AnimationPattern extends PatternModule.PacketPattern implements RestrictedServerVersion
{

    AnimationPattern()
    {
        // THIS IS IN ORDER OF HOW THE PACKETS ARE SUPPOSED TO ARRIVE.
        super(ImmutableSet.of(PacketType.Play.Client.USE_ENTITY,
                              PacketType.Play.Client.ARM_ANIMATION));
    }

    @Override
    protected int process(User user, PacketEvent packetEvent)
    {
        if (packetEvent.getPacketType() == PacketType.Play.Client.ARM_ANIMATION) {
            user.getDataMap().setValue(DataKey.PACKET_ANALYSIS_ANIMATION_EXPECTED, false);
        } else {
            if (user.getDataMap().getBoolean(DataKey.PACKET_ANALYSIS_ANIMATION_EXPECTED)) {
                user.getDataMap().setValue(DataKey.PACKET_ANALYSIS_ANIMATION_EXPECTED, false);
                message = "PacketAnalysisData-Verbose | Player: " + user.getPlayer().getName() + " did not send animation packet after an attack.";
                return 10;
            }
        }

        if (packetEvent.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
            // Make sure an arm animation packet is sent directly after an attack as it is the next packet in the client
            // code.
            final WrapperPlayClientUseEntity useEntityWrapper = new WrapperPlayClientUseEntity(packetEvent.getPacket());
            if (useEntityWrapper.getType() == EnumWrappers.EntityUseAction.ATTACK) {
                user.getDataMap().setValue(DataKey.PACKET_ANALYSIS_ANIMATION_EXPECTED, true);
            }
            return 0;
        }
        return 0;
    }

    @Override
    public Set<ServerVersion> getSupportedVersions()
    {
        return ServerVersion.NON_188_VERSIONS;
    }

    @Override
    public String getConfigString()
    {
        return this.getModuleType().getConfigString() + ".parts.Animation";
    }

    @Override
    public ModuleType getModuleType()
    {
        return ModuleType.PACKET_ANALYSIS;
    }
}
