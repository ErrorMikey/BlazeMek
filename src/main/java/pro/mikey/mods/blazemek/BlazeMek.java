package pro.mikey.mods.blazemek;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("blazemek")
public class BlazeMek {
    public static final Logger LOGGER = LogManager.getLogger();

    Lazy<Block> underBlock = Lazy.of(() -> {
        var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("mob_grinding_utils:dreadful_dirt"));
        if (block == null) {
            return Blocks.AIR;
        }

        return block;
    });

    public BlazeMek() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntitySpawned(LivingSpawnEvent.CheckSpawn event) {
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        Mob entity = event.getEntity();
        if (!serverLevel.dimension().equals(Level.NETHER)) return;
        if (entity instanceof Blaze || entity instanceof WitherSkeleton) return;

        if (serverLevel.getRandom().nextInt(100) > 25) return;

        BlockPos belowLocation = entity.blockPosition().below();
        var blockUnderEntity = serverLevel.getBlockState(belowLocation);
        if (!blockUnderEntity.isAir() && blockUnderEntity.getBlock() == underBlock.get()) {
            event.setResult(Event.Result.DENY);
            var shouldSpawnBlaze = serverLevel.getRandom().nextInt(100) > 30;

            var entityToSpawn = (shouldSpawnBlaze ? EntityType.BLAZE.create(serverLevel) : EntityType.WITHER_SKELETON.create(serverLevel));
            if (entityToSpawn == null) {
                LOGGER.warn("Failed to create Blaze or Wither Skeleton...");
                return;
            }

            entityToSpawn.setPos(new Vec3(belowLocation.getX() + 0.25,belowLocation.getY() + 1.25,belowLocation.getZ() + 0.25));
            serverLevel.addFreshEntity(entityToSpawn);
        }
    }
}
