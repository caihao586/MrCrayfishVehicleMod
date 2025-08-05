package com.mrcrayfish.vehicle;

import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class Reference 
{
    public static final String MOD_ID = "vehicle";
    
    // 添加常用路径常量
    public static final String ENTITY_MODEL_PATH = "textures/entity/";
    public static final String GUI_PATH = "textures/gui/";
    public static final String ITEM_MODEL_PATH = "textures/item/";
    
    // 添加创造模式标签键
    public static final String CREATIVE_TAB_KEY = "itemGroup." + MOD_ID;
    
    // 添加常用资源位置辅助方法
    public static ResourceLocation entityModel(String path) {
        return new ResourceLocation(MOD_ID, ENTITY_MODEL_PATH + path);
    }
    
    public static ResourceLocation guiTexture(String path) {
        return new ResourceLocation(MOD_ID, GUI_PATH + path);
    }
    
    public static ResourceLocation itemModel(String path) {
        return new ResourceLocation(MOD_ID, ITEM_MODEL_PATH + path);
    }
    
    public static ResourceLocation modResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}