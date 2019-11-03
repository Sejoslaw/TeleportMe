package com.github.sejoslaw.teleportme;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.stream.StreamSupport;

@Mod(TeleportMe.MODID)
public class TeleportMe {
    public static final String MODID = "teleportme";

    private static final String COMMAND = "tpme";
    private static final String DIM_ID = "dimensionId";
    private static final String POS_X = "x";
    private static final String POS_Y = "y";
    private static final String POS_Z = "z";

    public TeleportMe() {
        System.out.println("Registering Teleport Me...");
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
    }

    public void serverStarting(FMLServerStartingEvent event) {
        LiteralArgumentBuilder<CommandSource> command = Commands
                .literal(COMMAND)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands
                        .argument(DIM_ID, IntegerArgumentType.integer())
                        .then(Commands
                                .argument(POS_X, DoubleArgumentType.doubleArg())
                                .then(Commands
                                        .argument(POS_Y, DoubleArgumentType.doubleArg())
                                        .then(Commands
                                                .argument(POS_Z, DoubleArgumentType.doubleArg())
                                                .executes(this::executeCommand)))));

        event.getCommandDispatcher().register(command);
    }

    private int executeCommand(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        int dimId = IntegerArgumentType.getInteger(ctx, DIM_ID);

        double posX = DoubleArgumentType.getDouble(ctx, POS_X);
        double posY = DoubleArgumentType.getDouble(ctx, POS_Y);
        double posZ = DoubleArgumentType.getDouble(ctx, POS_Z);

        DimensionType destinationDimension = StreamSupport
                .stream(DimensionType.getAll().spliterator(), false)
                .filter(dimType -> dimType.getId() == dimId)
                .findAny()
                .orElse(null);

        if (destinationDimension == null) {
            return -1;
        }

        ServerPlayerEntity serverPlayerEntity = ctx.getSource().asPlayer();
        ServerWorld destinationWorld = ctx.getSource().getServer().getWorld(destinationDimension);

        serverPlayerEntity.teleport(destinationWorld, posX, posY, posZ, serverPlayerEntity.rotationYaw, serverPlayerEntity.rotationPitch);

        return 0;
    }
}
