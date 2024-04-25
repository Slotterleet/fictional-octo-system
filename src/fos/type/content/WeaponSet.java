package fos.type.content;

import arc.math.Mathf;
import arc.struct.Seq;
import fos.content.FOSUnitTypes;
import fos.gen.LumoniPlayerUnitc;
import mindustry.core.UI;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.*;
import mindustry.world.meta.*;

//YES, this looks very cursed lmao
/**
 * FOR MODDERS: applying this "status effect" will have no effect whatsoever
 */
public class WeaponSet extends StatusEffect {
    public static Seq<WeaponSet> sets = new Seq<>();

    public int id;
    /** Weapons this set contains. */
    public Seq<Weapon> weapons;
    /** Abilities this set contains, if any. */
    public Seq<Ability> abilities = new Seq<>();
    /** Items required to produce the set. */
    public ItemStack[] reqs;
    /** Production time of this set, in ticks. */
    public float produceTime = 300f;
    /** Whether replace it with a custom sprite. */
    public boolean customIcon = false;
    /** Custom research cost. */
    public ItemStack[] researchCost;


    public WeaponSet(String name, Weapon... weapons) {
        super(name);
        id = sets.size;
        sets.add(this);
        permanent = false;
        this.weapons = new Seq<>(weapons);
    }

    public WeaponSet(String name, Seq<Ability> abilities, Weapon... weapons) {
        this(name, weapons);
        this.abilities = abilities;
    }

    public WeaponSet(String name, Seq<Ability> abilities) {
        this(name, abilities, new Weapon());
    }

    @Override
    public void init() {
        super.init();
        weapons.each(Weapon::init);
    }

    @Override
    public void load() {
        super.load();
        weapons.each(Weapon::load);

        if (weapons.any() && !weapons.first().name.isEmpty() && !customIcon)
            fullIcon = uiIcon = weapons.first().region;
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.buildCost, StatValues.items(reqs));
        stats.add(Stat.weapons, StatValues.weapons(FOSUnitTypes.lord, weapons));
        // stolen from UnitType. TODO: this sucks.
/*
        if (abilities.any()) {
            var unique = new ObjectSet<String>();

            for(Ability a : abilities){
                if(a.display && unique.add(a.localized())){
                    stats.add(Stat.abilities, a.localized());
                }
            }
        }
*/
    }

    @Override
    public void applied(Unit unit, float time, boolean extend) {
        unit.unapply(this);
    }

    @Override
    public ItemStack[] researchRequirements() {
        if (researchCost != null) return researchCost;
        ItemStack[] out = new ItemStack[reqs.length];
        for(int i = 0; i < out.length; i++){
            int quantity = Mathf.round(60 + Mathf.pow(reqs[i].amount, 1.11f) * 20, 10);

            out[i] = new ItemStack(reqs[i].item, UI.roundAmount(quantity));
        }

        return out;
    }

    public void applyToUnit(LumoniPlayerUnitc lpc) {
        lpc.isEditedWeapons(true);
        lpc.weaponSet(this);

        lpc.mounts(getMounts());
        lpc.abilities(abilities.toArray(Ability.class));
    }

    /**
     * Applies weapons to unit.
     */
    public WeaponMount[] getMounts() {
        WeaponMount[] mounts = new WeaponMount[weapons.size];

        for (int i = 0; i < mounts.length; i++)
            mounts[i] = getMount(i);

        return mounts;
    }

    public WeaponMount getMount(int index) {
        Weapon weapon = weapons.get(index);
        return weapon.mountType.get(weapon);
    }

    /**
     * Sets the module's requirements.
     * @param reqs Items needed for production.
     * @return the weapon set for chaining.
     */
    public WeaponSet reqs(ItemStack[] reqs) {
        this.reqs = reqs;

        return this;
    }

    /**
     * Sets the module's production time.
     * @param ticks Production time, in ticks.
     * @return the weapon set for chaining.
     */
    public WeaponSet produceTime(float ticks) {
        this.produceTime = ticks;

        return this;
    }
}

