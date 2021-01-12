package com.dtang.solidarity.block.Machine;

import com.dtang.solidarity.init.ModBlocks;
import com.dtang.solidarity.init.ModRecipeSerializers;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

//Based on ShapedRecipe
public class RefractoryFurnaceRecipe implements IRecipe<Inventory> {
    public final static String GROUP = "refractory_smelting";
    public final static IRecipeType<RefractoryFurnaceRecipe> TYPE = ModRecipeSerializers.registerRecipeType(GROUP);
    protected final ResourceLocation id;
    protected final String group;
    protected final Ingredient primaryInput;
    protected final ItemStack primaryOutput;
    protected final ItemStack gasOutput;
    //protected final float experience;
    protected final int cookTime;

    //private static final Ingredient INGREDIENT_PAPER;

    public RefractoryFurnaceRecipe(ResourceLocation resourceLocation, String group, Ingredient primaryInput, ItemStack primaryOutput, ItemStack gasOutput, int cookTime) {
        this.id = resourceLocation;
        this.group = group;
        this.primaryInput = primaryInput;
        this.primaryOutput = primaryOutput;
        this.gasOutput = gasOutput;
        this.cookTime = cookTime;
    }

    public ItemStack getIcon() { return new ItemStack(ModBlocks.blockRefractoryFurnace.get()); }

    public IRecipeType<?> getType(){ return this.TYPE; }

    @Override
    public String getGroup() { return this.group; }

    public IRecipeSerializer<?> getSerializer() { return ModRecipeSerializers.REFRACTORY.get(); }

    //Ensure that any the input is one of the ones in recipe
    public boolean matches(Inventory inventory, World world) {
        return this.primaryInput.test(inventory.getStackInSlot(0));
    }

    public ItemStack getCraftingResult(Inventory inventory) {
        return this.primaryOutput.copy();
    }

    public ItemStack getGasResult() {
        return this.gasOutput.copy();
    }

    public boolean canFit(int source, int target) {
        return true;
    }

    public ItemStack getRecipeOutput() {
        return this.primaryOutput;
    }

    public int getCookTime() {
        return this.cookTime;
    }

    public ResourceLocation getId(){
        return this.id;
    }

    static {
        //INGREDIENT_PAPER = Ingredient.fromItems(new IItemProvider[]{Items.PAPER});
        //INGREDIENT_GUNPOWDER = Ingredient.fromItems(new IItemProvider[]{Items.GUNPOWDER});
        //INGREDIENT_FIREWORK_STAR = Ingredient.fromItems(new IItemProvider[]{Items.FIREWORK_STAR});
    }
}
