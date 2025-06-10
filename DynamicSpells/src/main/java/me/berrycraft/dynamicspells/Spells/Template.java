package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.berrycraft.dynamicspells.IExecutableSpell;
import me.berrycraft.dynamicspells.Spell;

public class Template extends Spell implements IExecutableSpell {

    /*  
     *  Must include a name that maps this class to its String counterpart.
     *  Cannot match any other names in other spells
     */
    public static final String NAME = "template";

    /*  
     *  Must include a material for this item. A custom texture should
     *  be included in the texturepack to override this vanilla item
     */
    public static final Material MATERIAL = Material.ACACIA_BOAT;


    private static YamlConfiguration config;

    /*
     *  You can define various variables that are specific to a certain
     *  instance of a IExecutableSpell. Its often useful to grab variables
     *  that differ by Spell level from the config using something like
     *  config.getInt(level + ".damage", -1); 
     */
    private float var1;
    private Player var2;

    /*
     *  Always copy paste this in so that you can access the config
     */
    public static void init() {
        config = loadSpellConfig(NAME);
    }

    /*
     *  Must include a cast method that takes caster and level parameters.
     *  Implement any custom logic for spell effects or whether the spell is
     *  successful. To create a spell that has continuing affects beyond the
     *  initial cast, implement IExecutableSpell and register a spell by running
     *  SpellEngine.register(new Template(), <duration>) in this method. Return 
     *  true for a successful cast (starts cooldown on the item)
     *   
     */
    public static boolean cast(Player caster, int level) {
        new Template();
        return true;
    }

    /*
     *  Optional method from the IExecutableSpell class that is called
     *  the spell is first registered with the engine.
     */
    @Override
    public void start() {

    }

    /*
     *  Optional method from the IExecutableSpell class that is called
     *  every tick and allows the programmer to define custom behavior
     */
    @Override
    public void update(double t) {

    }

    /*
     *  Optional method from the IExecutableSpell class that is called
     *  when the spell is complete. This can happen when the spell times
     *  out or when cancel() is called
     */
    @Override
    public void finish() {

    }
    
}
