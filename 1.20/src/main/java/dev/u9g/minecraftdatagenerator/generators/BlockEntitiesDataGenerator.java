package dev.u9g.minecraftdatagenerator.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import dev.u9g.minecraftdatagenerator.util.DGU;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

public class BlockEntitiesDataGenerator implements IDataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(BlockEntitiesDataGenerator.class);

    @Override
    public String getDataName() {
        return "blockEntities";
    }

    @Override
    public JsonArray generateDataJson() {
        JsonArray resultBlocksArray = new JsonArray();
        Registry<BlockEntityType> blockRegistry = DGU.getWorld().getRegistryManager().get(RegistryKeys.BLOCK_ENTITY_TYPE);
        blockRegistry.forEach(blockEntityType -> {
            JsonObject blockObject = generateBlockEntity(blockEntityType);
            if (blockObject != null)
                resultBlocksArray.add(blockObject);
        });

        return resultBlocksArray;
    }

    public static JsonObject generateBlockEntity(BlockEntityType type) {
        BlockEntity blockEntity;
        Set<Block> blocks;
        try {
            Field privateField = BlockEntityType.class.getDeclaredField("blocks");
            privateField.setAccessible(true);
            blocks = (Set<Block>) privateField.get(type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        blockEntity = type.instantiate(new BlockPos(0, 0, 0), blocks.iterator().next().getDefaultState());


        JsonObject blockDesc = new JsonObject();
        blockDesc.addProperty("id", blockEntity.getType().getRegistryEntry().getKey().get().getValue().toString());
        try {
            if (type == BlockEntityType.LECTERN) {
                ((LecternBlockEntity) blockEntity).setBook(Items.WRITTEN_BOOK.getDefaultStack());
            } else if (type == BlockEntityType.JUKEBOX) {
                Field privateField = JukeboxBlockEntity.class.getDeclaredField("inventory");
                privateField.setAccessible(true);
                DefaultedList<ItemStack> list = (DefaultedList<ItemStack>) privateField.get(blockEntity);
                list.set(0, Items.MUSIC_DISC_PIGSTEP.getDefaultStack());
            } else if (type == BlockEntityType.MOB_SPAWNER) {
                ((MobSpawnerBlockEntity) blockEntity).setEntityType(EntityType.ZOMBIE, Random.create());
            } else if (type == BlockEntityType.ENCHANTING_TABLE) {
                ((EnchantingTableBlockEntity) blockEntity).setCustomName(Text.of("Custom Name"));
            } else if (type == BlockEntityType.BEACON) {
                ((BeaconBlockEntity) blockEntity).setCustomName(Text.of("Custom Name"));
                Field privateField = BeaconBlockEntity.class.getDeclaredField("primary");
                privateField.setAccessible(true);
                privateField.set(blockEntity, StatusEffects.ABSORPTION);

                Field privateField2 = BeaconBlockEntity.class.getDeclaredField("secondary");
                privateField2.setAccessible(true);
                privateField2.set(blockEntity, StatusEffects.ABSORPTION);
            } else if (type == BlockEntityType.SKULL) {
                ((SkullBlockEntity) blockEntity).setOwner(new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), "Steve"));
                Field privateField = SkullBlockEntity.class.getDeclaredField("noteBlockSound");
                privateField.setAccessible(true);
                privateField.set(blockEntity, Identifier.of("minecraft", "entity.zombie.ambient"));
            } else if (type == BlockEntityType.BANNER) {
                ((BannerBlockEntity) blockEntity).setCustomName(Text.of("Custom Name"));
                Field privateField = BannerBlockEntity.class.getDeclaredField("patternListNbt");
                privateField.setAccessible(true);
                privateField.set(blockEntity, new NbtList());

            } else if (type == BlockEntityType.END_GATEWAY) {
                Field privateField = EndGatewayBlockEntity.class.getDeclaredField("exitPortalPos");
                privateField.setAccessible(true);
                privateField.set(blockEntity, new BlockPos(0, 0, 0));

                Field privateField2 = EndGatewayBlockEntity.class.getDeclaredField("exactTeleport");
                privateField2.setAccessible(true);
                privateField2.set(blockEntity, true);
            } else if (type == BlockEntityType.COMMAND_BLOCK) {
                Field privateField = CommandBlockExecutor.class.getDeclaredField("lastOutput");
                privateField.setAccessible(true);
                privateField.set(((CommandBlockBlockEntity) blockEntity).getCommandExecutor(), Text.of("Custom data"));

                Field privateField2 = CommandBlockExecutor.class.getDeclaredField("lastExecution");
                privateField2.setAccessible(true);
                privateField2.set(((CommandBlockBlockEntity) blockEntity).getCommandExecutor(), 10L);
            } else if (type == BlockEntityType.CONDUIT) {
                Field privateField = ConduitBlockEntity.class.getDeclaredField("targetEntity");
                privateField.setAccessible(true);
                privateField.set(blockEntity, new LivingEntity(EntityType.ZOMBIE, null) {
                    @Override
                    public Iterable<ItemStack> getArmorItems() {
                        return null;
                    }

                    @Override
                    public ItemStack getEquippedStack(EquipmentSlot slot) {
                        return null;
                    }

                    @Override
                    public void equipStack(EquipmentSlot slot, ItemStack stack) {

                    }

                    @Override
                    public Arm getMainArm() {
                        return null;
                    }
                });
            } else if (type == BlockEntityType.BEEHIVE) {
                Field privateField = BeehiveBlockEntity.class.getDeclaredField("flowerPos");
                privateField.setAccessible(true);
                privateField.set(blockEntity, new BlockPos(0, 0, 0));
            } else if (type == BlockEntityType.DECORATED_POT) {
                Field privateField = DecoratedPotBlockEntity.class.getDeclaredField("sherds");
                privateField.setAccessible(true);
                privateField.set(blockEntity, new DecoratedPotBlockEntity.Sherds(Items.COMMAND_BLOCK, null, null, null));
            }
//            progress: skull


            NbtCompound nbt = blockEntity.createNbt();
            JsonObject defaultData = new JsonObject();
            for (String key : nbt.getKeys()) {
                defaultData.addProperty(key, nbt.get(key).asString());
            }

            blockDesc.add("default_data", defaultData);

        } catch (Exception e) {
            logger.error("Can't create default nbt for block entity " + blockEntity.getType().getRegistryEntry().getKey().get().getValue());
//            e.printStackTrace();
            return null;
        }

        return blockDesc;
    }

}
