package com.github.sejoslaw.teleportme.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.StreamSupport;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    private static final String COMMAND = "tpme";
    private static final String DIM_ID = "dimensionId";
    private static final String POS_X = "x";
    private static final String POS_Y = "y";
    private static final String POS_Z = "z";

    @Shadow
    private final CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher();

    @Inject(method = "<init>()V", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        dispatcher.register(this.getCommand());

        this.dispatcher.setConsumer((commandContext, isSuccess, result) -> {
            commandContext.getSource().onCommandComplete(commandContext, isSuccess, result);
        });
    }

    private LiteralArgumentBuilder<ServerCommandSource> getCommand() {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager
                .literal(COMMAND)
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(CommandManager
                        .argument(DIM_ID, IntegerArgumentType.integer())
                        .then(CommandManager
                                .argument(POS_X, DoubleArgumentType.doubleArg())
                                .then(CommandManager
                                        .argument(POS_Y, DoubleArgumentType.doubleArg())
                                        .then(CommandManager
                                                .argument(POS_Z, DoubleArgumentType.doubleArg())
                                                .executes(this::executeCommand)))));
        return command;
    }

    private int executeCommand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int dimId = IntegerArgumentType.getInteger(ctx, DIM_ID);

        double posX = DoubleArgumentType.getDouble(ctx, POS_X);
        double posY = DoubleArgumentType.getDouble(ctx, POS_Y);
        double posZ = DoubleArgumentType.getDouble(ctx, POS_Z);

        DimensionType destinationDimension = StreamSupport
                .stream(DimensionType.getAll().spliterator(), false)
                .filter(dimType -> dimType.getRawId() == dimId)
                .findAny()
                .orElse(null);

        if (destinationDimension == null) {
            return -1;
        }

        ServerPlayerEntity serverPlayerEntity = ctx.getSource().getPlayer();
        ServerWorld destinationWorld = ctx.getSource().getMinecraftServer().getWorld(destinationDimension);

        serverPlayerEntity.teleport(destinationWorld, posX, posY, posZ, serverPlayerEntity.yaw, serverPlayerEntity.pitch);

        return 0;
    }
}
