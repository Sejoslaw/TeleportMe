package com.github.sejoslaw.teleportme;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
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
    private static final String PLAYER_NAME = "playerName";
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
                .then(Commands
                        .argument(PLAYER_NAME, EntityArgument.player())
                        .executes(this::executeCommandForPlayer))
                .then(Commands
                        .argument(DIM_ID, IntegerArgumentType.integer())
                        .then(Commands
                                .argument(POS_X, DoubleArgumentType.doubleArg())
                                .then(Commands
                                        .argument(POS_Y, DoubleArgumentType.doubleArg())
                                        .then(Commands
                                                .argument(POS_Z, DoubleArgumentType.doubleArg())
                                                .executes(this::executeCommandForPosition)))));

        event.getCommandDispatcher().register(command);
    }

    private int executeCommandForPlayer(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(ctx, PLAYER_NAME);
        ServerPlayerEntity serverPlayerEntity = ctx.getSource().asPlayer();

        serverPlayerEntity.teleport((ServerWorld) targetPlayer.world, targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, serverPlayerEntity.rotationYaw, serverPlayerEntity.rotationPitch);

        return 0;
    }

    private int executeCommandForPosition(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        int dimId = IntegerArgumentType.getInteger(ctx, DIM_ID);

        DimensionType dimensionType = StreamSupport
                .stream(DimensionType.getAll().spliterator(), false)
                .filter(dimType -> dimType.getId() == dimId)
                .findAny()
                .orElse(null);

        if (dimensionType == null) {
            return -1;
        }

        double posX = DoubleArgumentType.getDouble(ctx, POS_X);
        double posY = DoubleArgumentType.getDouble(ctx, POS_Y);
        double posZ = DoubleArgumentType.getDouble(ctx, POS_Z);

        ServerPlayerEntity serverPlayerEntity = ctx.getSource().asPlayer();
        ServerWorld destinationWorld = ctx.getSource().getServer().getWorld(dimensionType);

        serverPlayerEntity.teleport(destinationWorld, posX, posY, posZ, serverPlayerEntity.rotationYaw, serverPlayerEntity.rotationPitch);

        return 0;
    }
}
