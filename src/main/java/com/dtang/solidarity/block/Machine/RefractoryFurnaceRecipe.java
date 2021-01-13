package com.dtang.solidarity.block.Machine;

import com.dtang.solidarity.init.ModBlocks;
import com.dtang.solidarity.init.ModRecipeSerializers;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

//Formula for calculating smelt time
//Note: Boltzmann's constant = 1.380 * 10E-23 J/K
//Time in ticks = (mass * specific heat)*tempdiff + (latent heat needed to melt) in MJ * 4
//specific heat of beef is ~3kJ/kg/K. https://www.engineeringtoolbox.com/specific-heat-capacity-food-d_295.html
//222kg in a cow. Max 6 with looting 3, so around 45 kg per beef
//Example: 20kg beef = 3kJ*45 * 150 ~= 20MJ. Some latent heat needed, so let's say 25MJ
//specific heat of andesite is 0.7kJ/kg/K. Magma temp ~= 1300K. Density 2500kg/m3
//Example2: 1m3 Stone = 2500*0.7kJ/K * 1300 + 2500 kg * 300kJ/kg
// = 2,275,000kJ + 750,000 kJ ->3,000MJ
//Each charcoal provides 6000MJ, at 20% efficiency = 1200 MJ.



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
    protected final int temperature;
    protected final int cookTime;

    //private static final Ingredient INGREDIENT_PAPER;

    public RefractoryFurnaceRecipe(ResourceLocation resourceLocation, String group, Ingredient primaryInput, ItemStack primaryOutput, ItemStack gasOutput, int temperature, int cookTime) {
        this.id = resourceLocation;
        this.group = group;
        this.primaryInput = primaryInput;
        this.primaryOutput = primaryOutput;
        this.gasOutput = gasOutput;
        this.temperature = temperature;
        this.cookTime = cookTime;
    }

    public ItemStack getIcon() { return new ItemStack(ModBlocks.blockRefractoryFurnace.get()); }

    public IRecipeType<?> getType(){ return TYPE; }

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

    public int getCookTemperature() {
        return this.temperature;
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
