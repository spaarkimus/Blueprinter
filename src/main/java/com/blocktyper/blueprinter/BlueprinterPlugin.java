package com.blocktyper.blueprinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.blueprinter.listeners.PlaceLayoutItemListener;
import com.blocktyper.blueprinter.listeners.RequireMatsClickListener;
import com.blocktyper.v1_1_8.nbt.NBTItem;
import com.blocktyper.v1_1_8.plugin.BlockTyperPlugin;

public class BlueprinterPlugin extends BlockTyperPlugin {

	public static final String RESOURCE_NAME = "com.blocktyper.blueprinter.resources.BlueprinterMessages";
	public static final String RECIPES_KEY = "HOUSE_IN_BOTTLE_RECIPE_KEY";

	public BlueprinterPlugin() {
		super();
	}

	public void onEnable() {
		super.onEnable();
		new PlaceLayoutItemListener(this);
		new RequireMatsClickListener(this);
	}

	// begin localization
	private ResourceBundle bundle = null;

	public ResourceBundle getBundle() {
		if (bundle == null)
			bundle = ResourceBundle.getBundle(RESOURCE_NAME, locale);
		return bundle;
	}

	@Override
	public String getRecipesNbtKey() {
		return RECIPES_KEY;
	}

	@Override
	// begin localization
	public ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle(RESOURCE_NAME, locale);
	}
	// end localization

	public String getRecipeKey(ItemStack item) {
		if (item != null) {
			NBTItem nbtItem = new NBTItem(item);
			if (nbtItem.hasKey(getRecipesNbtKey())) {
				String recipeKey = nbtItem.getString(getRecipesNbtKey());
				return recipeKey;
			}
		}
		return null;
	}

	public String getLayoutKey() {
		return "LAYOUT-" + getRecipesNbtKey();
	}

	public Layout getLayout(String recipesKey) throws BuildException {
		return Layout.getLayout(recipesKey, this);
	}
	
	public Layout getLayout(ItemStack item){
		if(item == null){
			return null;
		}
		
		NBTItem nbtItem = new NBTItem(item);
		
		Layout layout = nbtItem.getObject(getLayoutKey(), Layout.class);
		return layout;
	}
	
	public ItemStack setLayout(ItemStack item, Layout layout){
		if(item == null){
			return null;
		}
		
		NBTItem nbtItem = new NBTItem(item);
		nbtItem.setObject(getLayoutKey(), layout);
		return nbtItem.getItem();
	}

	@Override
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		ItemStack result = event.getInventory().getResult();
		String recipeKey = getRecipeKey(result);

		if (recipeKey == null) {
			return;
		}

		if (Layout.hasLayout(recipeKey, this)) {
			try {
				Layout layout = getLayout(recipeKey);
				ItemMeta itemMeta = result.getItemMeta();
				List<String> lore = itemMeta.getLore();
				lore = lore == null ? new ArrayList<>() : lore;
				lore.add(layout.hashCode() + "");
				itemMeta.setLore(lore);
				result.setItemMeta(itemMeta);
				NBTItem nbtItem = new NBTItem(result);
				nbtItem.setObject(getLayoutKey(), layout);
				event.getInventory().setResult(nbtItem.getItem());
			} catch (BuildException e) {
				event.getInventory().setResult(null);
				e.printStackTrace();
			}
		}
	}

}
