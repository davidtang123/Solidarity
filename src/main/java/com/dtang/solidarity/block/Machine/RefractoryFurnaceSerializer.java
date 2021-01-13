package com.dtang.solidarity.block.Machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistryEntry;

//Based off vanilla CookingRecipeSerializer code
public class RefractoryFurnaceSerializer
    <T extends RefractoryFurnaceRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
    private final int default_cooktime;
    private final RefractoryFurnaceSerializer.IFactory<T> factory;

    public RefractoryFurnaceSerializer(RefractoryFurnaceSerializer.IFactory<T> factory, int default_cooktime) {
        this.default_cooktime = default_cooktime;
        this.factory = factory;
    }

    /* Example JSON:
{
  "type": "refractory_smelting",
  "ingredient": [
    {"item": "minecraft:cobblestone"},
    {"item": "minecraft:stone_brick"},
    {"item": "minecraft:stone_button"},
    {"item": "minecraft:stone_pressure_plate"},
    {"item": "minecraft:chiseled_stone_bricks"},
    {"item": "minecraft:mossy_cobblestone"},
    {"item": "minecraft:mossy_stone_bricks"}
  ],
  "result": "minecraft:stone",
  "cookingtime": 1000
}
     */

    public T read(ResourceLocation id, JsonObject json) {
        String group = JSONUtils.getString(json, "group", "");
        JsonElement jsonelement = JSONUtils.isJsonArray(json, "ingredient") ? JSONUtils.getJsonArray(json, "ingredient") : JSONUtils.getJsonObject(json, "ingredient");
        Ingredient primaryInput = Ingredient.deserialize(jsonelement);
        if (!json.has("result")) {
            //Expects a result
            throw new JsonSyntaxException("Missing result, expected to find a string or object");
        } else {
            ItemStack primaryOutput;
            if (json.get("result").isJsonObject()) {
                //ShapedRecipe deserialize should cover item deserialization I think
                primaryOutput = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            } else {
                String s1 = JSONUtils.getString(json, "result");
                int ct = JSONUtils.getInt(json, "count", 1);
                ResourceLocation resourcelocation = new ResourceLocation(s1);
                primaryOutput = new ItemStack((IItemProvider)Registry.ITEM.getValue(resourcelocation).orElseThrow(() -> {
                    return new IllegalStateException("Item: " + s1 + " does not exist");
                }), ct);

            }

            ItemStack gasOutput = ItemStack.EMPTY;
            //Optional input gas, should be a gas tank
            if (json.has("gas")) {
                if (json.get("gas").isJsonObject()) {
                    //ShapedRecipe deserialize should cover item deserialization I think
                    gasOutput = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "gas"));
                } else {
                    String s1 = JSONUtils.getString(json, "gas");
                    ResourceLocation resourcelocation = new ResourceLocation(s1);
                    gasOutput = new ItemStack((IItemProvider) Registry.ITEM.getValue(resourcelocation).orElseThrow(() -> {
                        return new IllegalStateException("Item: " + s1 + " does not exist");
                    }));
                }
            }

            //Defaults to 100 degrees celsius, requires at least burning potential of stick
            int temp = JSONUtils.getInt(json, "temperature", 100);

            //Use of technology does not produce experience, aka magic
            //float f = JSONUtils.getFloat(json, "experience", 0.0F);
            int cooktime = JSONUtils.getInt(json, "cookingtime", this.default_cooktime);
            return this.factory.create(id, group, primaryInput, primaryOutput, gasOutput, temp, cooktime);
        }
    }

    public T read(ResourceLocation id, PacketBuffer buffer) {
        String group = buffer.readString(32767);
        Ingredient primaryInput = Ingredient.read(buffer);
        ItemStack primaryOutput = buffer.readItemStack();
        ItemStack gasOutput = buffer.readItemStack();
        //float f = buffer.readFloat();
        int temp = buffer.readVarInt();//temperature, not temporary
        int cooktime = buffer.readVarInt();
        return this.factory.create(id, group, primaryInput, primaryOutput, gasOutput, temp, cooktime);
    }

    public void write(PacketBuffer buffer, T recipe) {
        buffer.writeString(recipe.group);
        recipe.primaryInput.write(buffer);
        buffer.writeItemStack(recipe.primaryOutput);
        buffer.writeItemStack(recipe.gasOutput);
        //buffer.writeFloat(recipe.experience);
        buffer.writeVarInt(recipe.temperature);
        buffer.writeVarInt(recipe.cookTime);
    }

    public interface IFactory<T extends RefractoryFurnaceRecipe> {
        T create(ResourceLocation id, String group, Ingredient primaryInput, ItemStack primaryOutput, ItemStack gasOutput, int temperature, int cookTime);
    }
}
