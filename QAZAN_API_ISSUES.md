# Qazan System - Critical API Incompatibility Issues

## Problem Summary

The Qazan cooking system code was written for a different version of Minecraft/NeoForge and has **extensive API incompatibilities** with Minecraft 1.21.10. The APIs have changed significantly, and many methods/classes used don't exist or have different signatures in this version.

## Critical Compilation Errors

### 1. NBT Serialization (QazanBlockEntity.java)
**Error**: `loadAdditional` and `saveAdditional` have wrong signatures
```
error: method loadAdditional in class BlockEntity cannot be applied to given types
error: method saveAdditional in class BlockEntity cannot be applied to given types
```

**Issue**: The `loadAdditional(CompoundTag, HolderLookup.Provider)` and `saveAdditional(CompoundTag, HolderLookup.Provider)` methods don't exist in this version.

**Additionally**:
- `ItemStack.parseOptional(registries, tag)` - method doesn't exist
- `ItemStack.save(registries)` - method doesn't exist  
- `tag.getInt()` returns `Optional<Integer>` instead of `int`

### 2. BlockEntityType Registration (ElysiumHarvest.java)
**Error**: `BlockEntityType.Builder` doesn't exist
```
error: cannot find symbol
  symbol:   variable Builder
  location: class BlockEntityType
```

**Issue**: The Builder pattern for BlockEntityType registration doesn't exist in 1.21.10.

### 3. Recipe System (QazanRecipe.java)
**Error**: `RecipeBookCategory.MISC` doesn't exist
```
error: cannot find symbol
  symbol:   variable MISC
  location: class RecipeBookCategory
```

**Error**: Recipe methods don't match interface
```
error: method does not override or implement a method from a supertype
```

### 4. RecipeManager API (QazanBlockEntity.java)
**Error**: `getAllRecipesFor()` doesn't exist
```
error: cannot find symbol
      .getRecipeManager().getAllRecipesFor(...)
                         ^
```

### 5. GUI Rendering (QazanScreen.java)
**Error**: `GuiGraphics.blit()` wrong signature
```
error: no suitable method found for blit(ResourceLocation,int,int,int,int,int,int)
```

### 6. Block Lifecycle (QazanBlock.java)
**Error**: `onRemove()` method signature doesn't match
```
error: method does not override or implement a method from a supertype
error: cannot find symbol
  symbol: method onRemove(BlockState,Level,BlockPos,BlockState,boolean)
```

## Root Cause

The code was likely written for **Minecraft 1.20.x or earlier**, while your project uses **Minecraft 1.21.10**, which has breaking API changes across:
- BlockEntity NBT handling
- Recipe system interfaces
- BlockEntityType registration
- RecipeManager API
- GUI rendering
- Block lifecycle methods

## Recommended Solutions

### Option 1: Use Template/Example Code (RECOMMENDED)
Look for Minecraft 1.21.10 NeoForge examples:
1. Search GitHub for "Minecraft 1.21 NeoForge BlockEntity example"
2. Find a working custom furnace/cooking BlockEntity for 1.21.x
3. Adapt that code structure to the Qazan system

### Option 2: Downgrade Project
If you have flexibility, downgrade to Minecraft 1.20.x where these APIs are stable.

### Option 3: Manual API Migration (Complex)
Research each API change and fix them systematically:

1. **NBT Serialization**: Find the correct 1.21.10 method names
   - Check Minecraft decompiled source for `BlockEntity` class
   - Look for methods like `loadData`, `saveData`, `load`, `save`, etc.

2. **BlockEntityType**: Find correct registration pattern
   - Check NeoForge documentation for 1.21.10
   - Look at vanilla BlockEntityTypes (Furnace, Chest, etc.)

3. **Recipe System**: Update to new Recipe interface
   - Find what methods Recipe interface requires in 1.21.10
   - Update QazanRecipe accordingly

4. **RecipeManager**: Find replacement for `getAllRecipesFor()`
   - Might be `getRecipes()`, `listRecipes()`, or similar

5. **GuiGraphics.blit()**: Find correct overload
   - Check AbstractContainerScreen implementations
   - Look for correct parameter list

6. **Block.onRemove()**: Remove `@Override` or find correct method name
   - Might not need to override at all
   - Or might be named differently

## Immediate Next Steps

1. **Stop compilation attempts** - the code needs extensive rewrites

2. **Research the correct APIs**:
   ```
   Search: "Minecraft 1.21.10 NeoForge BlockEntity tutorial"
   Search: "Minecraft 1.21 custom recipe tutorial"
   Search: "NeoForge 1.21 container screen tutorial"
   ```

3. **Find working examples**:
   - Official NeoForge example mods for 1.21.x
   - Popular mods updated to 1.21.x (check their GitHub)

4. **Consider professional help**:
   - Minecraft modding Discord servers
   - NeoForge forums
   - Hire experienced 1.21.x modder

## Alternative: Simplified Approach

If you need a working mod quickly, **drastically simplify**:
1. Remove BlockEntity persistence (no NBT saving) - items lost on world reload
2. Remove custom recipes - use hardcoded logic
3. Use simpler GUI with basic texture coordinates
4. Skip heat detection - always cook when items present

This would reduce API dependencies but sacrifice functionality.

## Files Affected

All Qazan-related files need updates:
- `QazanBlockEntity.java` - Core NBT and recipe issues
- `QazanBlock.java` - Lifecycle method issues  
- `QazanRecipe.java` - Recipe interface mismatch
- `QazanScreen.java` - GUI rendering API changes
- `ElysiumHarvest.java` - Registration API changes
- `QazanMenu.java` - Likely compatible, minor issues

## Conclusion

This is **not a simple fix**. The APIs have changed fundamentally between Minecraft versions. You need either:
1. Working 1.21.10 example code to adapt from
2. Deep knowledge of the new APIs
3. Significant time to research and rewrite

**I recommend finding a working BlockEntity example for Minecraft 1.21.10 and using it as a template.**

---

## Useful Resources

- NeoForge Docs: https://docs.neoforged.net/
- Minecraft Modding Discord: https://discord.gg/neoforged
- Example Mods: Search GitHub for "neoforge 1.21" "BlockEntity"

