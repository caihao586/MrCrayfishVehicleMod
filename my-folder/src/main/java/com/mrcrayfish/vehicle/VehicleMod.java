package com.mrcrayfish.vehicle;

import com.mrcrayfish.vehicle.client.ClientHandler;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.common.CommonEvents;
import com.mrcrayfish.vehicle.common.FluidNetworkHandler;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.crafting.RecipeTypes;
import com.mrcrayfish.vehicle.crafting.WorkstationIngredient;
import com.mrcrayfish.vehicle.datagen.LootTableGen;
import com.mrcrayfish.vehicle.datagen.RecipeGen;
import com.mrcrayfish.vehicle.datagen.VehiclePropertiesGen;
import com.mrcrayfish.vehicle.entity.properties.ExtendedProperties;
import com.mrcrayfish.vehicle.entity.properties.HelicopterProperties;
import com.mrcrayfish.vehicle.entity.properties.LandProperties;
import com.mrcrayfish.vehicle.entity.properties.MotorcycleProperties;
import com.mrcrayfish.vehicle.entity.properties.PlaneProperties;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.TrailerProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.*;
import com.mrcrayfish.vehicle.network.PacketHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class VehicleMod
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    
    // 1.20.1 新版创造标签系统
    public static final CreativeModeTab CREATIVE_TAB = CreativeModeTab.builder()
        .title(Component.translatable("itemGroup." + Reference.MOD_ID))
        .icon(() -> new ItemStack(ModItems.IRON_SMALL_ENGINE.get()))
        .build();

    public VehicleMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // 注册所有内容
        ModBlocks.REGISTER.register(modEventBus);
        ModItems.REGISTER.register(modEventBus);
        ModEntities.REGISTER.register(modEventBus);
        ModBlockEntities.REGISTER.register(modEventBus); // 1.20.1 TileEntity -> BlockEntity
        ModContainers.REGISTER.register(modEventBus);
        ModParticleTypes.REGISTER.register(modEventBus);
        ModSounds.REGISTER.register(modEventBus);
        ModRecipeSerializers.REGISTER.register(modEventBus);
        ModFluids.REGISTER.register(modEventBus);
        
        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        
        // 注册事件监听器
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onGatherData);
        
        // 注册Forge事件总线监听器
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        MinecraftForge.EVENT_BUS.register(new ModCommands());
        MinecraftForge.EVENT_BUS.register(FluidNetworkHandler.instance());
        
        // 注册扩展属性
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "powered"), PoweredProperties.class, PoweredProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "land"), LandProperties.class, LandProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "motorcycle"), MotorcycleProperties.class, MotorcycleProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "plane"), PlaneProperties.class, PlaneProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "helicopter"), HelicopterProperties.class, HelicopterProperties::new);
        ExtendedProperties.register(new ResourceLocation(Reference.MOD_ID, "trailer"), TrailerProperties.class, TrailerProperties::new);
        
        // 客户端特定初始化
        modEventBus.addListener(this::onClientInit);
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        RecipeTypes.init();
        VehicleProperties.loadDefaultProperties();
        PacketHandler.init();
        HeldVehicleDataHandler.register();
        ModDataKeys.register();
        ModLootFunctions.init();
        CraftingHelper.register(new ResourceLocation(Reference.MOD_ID, "workstation_ingredient"), WorkstationIngredient.Serializer.INSTANCE);
        event.enqueueWork(() -> VehicleProperties.registerDynamicProvider(() -> new VehiclePropertiesGen(null)));
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        ClientHandler.setup();
    }
    
    private void onClientInit(FMLClientSetupEvent event)
    {
        // 1.20.1 安全的客户端初始化
        ComponentManager.registerLoader(VehicleModels.LOADER);
    }

    private void onGatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        var helper = event.getExistingFileHelper();
        
        // 1.20.1 数据生成器需要 PackOutput
        generator.addProvider(event.includeServer(), new LootTableGen(output));
        generator.addProvider(event.includeServer(), new RecipeGen(output));
        generator.addProvider(event.includeServer(), new VehiclePropertiesGen(output));
        
        // 如果需要客户端数据生成
        generator.addProvider(event.includeClient(), new BlockStateGen(output, helper));
        generator.addProvider(event.includeClient(), new ItemModelGen(output, helper));
    }
}
