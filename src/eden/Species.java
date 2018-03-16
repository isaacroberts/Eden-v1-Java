/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.*;
import java.util.*;

public class Species implements SubType
{
    public final static Species error=new Species(false);
    
    ArrayList<Animal> animals;
    private float width,length;
    private float height;
    private float birthMass;
    private float avgMass;
    private Color color;
    private float mass;
    private float maxSpeed;
    private float maxAccel;
    private double maxTurning;//in radians
    private boolean canFly;
    private int maxEnergy;
    private int maxHealth;
    private int maxExhaustion;
    private double efficiency;
    private double[] energyExpendage;
    SubType[] edible;
    SubType[] predator;
    SubType[] prey;
    private double sightRange;
    public Species(boolean thisVariableMakesThisAnError) {
        edible=new SubType[0];
        predator=new SubType[0];
        prey=new SubType[0];
        animals=new ArrayList<Animal>();
    }
    public Species()
    {
        width=.4f;
        length=1;
        color=Color.GRAY;
        canFly=false;
        sightRange=300;
        maxSpeed=1;
        maxTurning=.1;
        maxAccel=.1f;
        maxEnergy=30000;
        maxExhaustion=6000;
        efficiency=.9;
        energyExpendage=new double[Animal.State.values().length];
        for (int n=0;n<energyExpendage.length;n++)
        {
            energyExpendage[n]=1;
        }
        maxHealth=10000;
        
        animals=new ArrayList<Animal>();
        for (int n=0;n<16;n++)
        {
            Square start;
            do {
                start=Square.getRandSquare();
            }
            while (start.moveable());
            animals.add(new Animal(this,start));
        }
        edible=new SubType[] {Thing.Type.Froot,Thing.Type.Carcass,Thing.Type.Tree};
        prey=new SubType[0];
        predator=new SubType[0];
    }
    public void update()
    {
        for (int n=0;n<animals.size();n++)
        {
            if (animals.get(n).isDead())
            {
                Thing.Carcass body=new Thing.Carcass(animals.get(n));
                body.heat=animals.get(n).heat;
                Map.addFeature(body);
                animals.remove(n);
                n--;
            }
            else
            {
                animals.get(n).update();
            }
        }
    }
    public void reproduce(Animal father,Animal mother)
    {
        
    }
    public void draw(Graphics2D g)
    {
        for (int n=0;n<animals.size();n++)
        {
            animals.get(n).draw(g);
        }
    }
    public Util.intDubPair getAttraction(SubType type)
    {
        if (type.isOfTypes(prey)) {
            return new Util.intDubPair(2,200000);
        }
        else if (type.isOfTypes(edible)) {
            return new Util.intDubPair(2,100000);
        }
        else if (type.isOfTypes(predator)) {
            return new Util.intDubPair(2,-10000);
        }
        else if (!type.walkable()) {
            return new Util.intDubPair(2,-10000);
        }
        else return new Util.intDubPair(0,0);
    }
    public float height() {
        return height;
    }
    public double sightRange() {
        return sightRange;
    }
    public float birthMass() {
        return birthMass;
    }
    public float avgMass() {
        return avgMass;
    }
    public float flySpeed() {
        return (canFly? maxSpeed:0);
    }
    public float maxSpeed() {
        return maxSpeed;
    }
    public float getAccel(Animal.State state)
    {
        return (float)(maxAccel*energyExpendage(state));
    }
    public double maxTurning() {
        return maxTurning;
    }
    public int maxHealth() {
        return maxHealth;
    }
    public int maxEnergy() {
        return maxEnergy;
    }
    public int maxExhaustion() {
        return maxExhaustion;
    }
    public double mass() {
        return mass;
    }
    public double efficiency() {
        return efficiency;
    }
    public double energyExpendage(Animal.State state)
    {//between 0 and 1
        return energyExpendage[state.ordinal()];
    }
    public double getEnergyExpdgForAccel(double accel)
    {
        return Animal.energyPerVelMassEff*accel*mass()*efficiency();
    }
    public double getEnergyExpdgForVel(double vel)
    {
        return Animal.energyPerVelMassEff*vel*mass()*efficiency();
    }
    public float getLength() {
        return length;
    }
    public float getWidth() {
        return width;
    }
    public Color getColor() {
        return color;
    }
    public boolean walkable() {
        return false;
    }
    public boolean isOfTypes(SubType[] types)
    {
        for (int t=0;t<types.length;t++)
        {
            if (types[t].equals(this))
                return true;
        }
        return false;
    }
    //static
    static ArrayList<Species> phylums;
    public static void onStart(LoadingScreen loading) {
        loading.current=LoadingScreen.WorkingOn.Animals;
        loading.percentage=0;
        phylums=new ArrayList<Species>();
        phylums.add(new Species());
//        loading.percentage=.97f;
//        Species.error.height=10;
//        Species.error.avgMass=5;
//        Species.error.width=5;
//        Species.error.length=5;
//        Species.error.mass=5;
        loading.percentage=1;
    }
    public static void updateAll()
    {
        for (int n=0;n<phylums.size();n++)
        {
            phylums.get(n).update();
        }
    }
    public static void drawAll(Graphics2D g)
    {
        for (int n=0;n<phylums.size();n++)
        {
            phylums.get(n).draw(g);
        }
    }
    public static Species get(int n)
    {
        if (n>=phylums.size())
            return null;
        return phylums.get(n);
    }
    public static int amt() {
        return phylums.size();
    }
}
