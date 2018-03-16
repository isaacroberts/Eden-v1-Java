/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Isaac
 */
public class Tree extends Thing implements Food
{
    Fruit.FruitType fruitType;
    ArrayList<Fruit> fruits;
    int height;
    Roots roots;
    static float moisturePerHeight=3f;
    public Tree(Square loc) {
        super(loc);
        Random rand=new Random();
        fruitType=Fruit.FruitType.values()[rand.nextInt(Fruit.FruitType.values().length)];
        height=2;
        fruits=new ArrayList<Fruit>();
        if (!(loc.terrain() instanceof Roots))
        {
            roots=new Roots(loc,this);
            loc.setTerrain(roots);
        }
        else
        {
            roots=((Roots)loc.terrain());
            ((Roots)loc.terrain()).parent=this;
        }
    }
    public Tree (Fruit.FruitType setFruit)
    {
        super();
        fruitType=setFruit;
        height=2;
        fruits=new ArrayList<Fruit>();
        roots=new Roots(loc,this);
        loc.setTerrain(roots);
    }
    public Tree(Square loc,Fruit.FruitType setFruit) {
        super(loc);
        fruitType=setFruit;
        height=2;
        fruits=new ArrayList<Fruit>();
//            fruits.add(new Fruit(fruitType,loc.clone()));
        roots=new Roots(loc,this);
        loc.setTerrain(roots);
    }
    public Tree clone()
    {
        Tree clone=new Tree(fruitType);
        clone.makePropertiesSameAs(this);
        clone.height=height;
        clone.fruits=(ArrayList<Fruit>)fruits.clone();
        clone.roots=roots;
        return clone;
    }
    public float getCalories() {
        return height*5;
    }
    public float volume() {
        return height()*.785f;//pir^2, r=.5
    }
    public float height() {
        return height;
    }
    public float getSurfaceArea() {
//            System.out.println("sa of tree @ "+loc.toString()+"="+(getType().surfaceArea*height + .78f));
        return getType().surfaceArea*height + .78f;
    }
    public double getSpecificHeat() {
        return getType().specificHeat*height;
    }
    public void setHeight(int set) {
        height=set;
    }
    public float attachmentForces() {
        if (loc.terrain() instanceof Roots)
        {//dont worry about the root's children's durability, because it is almost guaranteed to be weaker
            return ((Roots)loc.terrain()).durability*Roots.ROOT_STRENGTH;
        }
        else
            return 0;

    }
    public boolean isRotten() {
        return false;
    }
    public boolean isDead() {
        return height<=0 || loc.isError();
    }
    public void changeHeight(int amt)
    {
        if (amt>0)
        {
            if (!takeMoisture(moisturePerHeight*amt))
            {
                return;
            }
        }
        else
        {
            if (height<-amt)
                amt=-height;
            loc.air.changeHumidity(-moisturePerHeight*amt);
        }
        height+=amt;
        heat-=2*amt;
    }
    public void eat(int massEaten) {
        if (height>3)
        {
            height-=massEaten/(3);
            for (int f=0;f<fruits.size();f++)
            {
                if (f>height)
                    fruits.get(f).drop();
            }
        }
        else if (height>0)
        {
            height--;
        }
    }
    public void setLoc(Square set)
    {
        Log.fellTree(this,loc.getDir(set));
    }
    public void damage(double damage) {
//            damage
        if (damage>height)
        {
            changeHeight(-1);
        }
        for (int f=0;f<fruits.size();f++)
        {
            if (f<damage)
                fruits.get(f).drop();
        }
    }
    protected void burn() {
        if (Util.frequency(3))
        {
            changeHeight(-1);
        }
    }
    protected void update()
    {
        if (isDead())
            return;
        for (int f=0;f<fruits.size();f++)
        {
            if (!fruits.get(f).onTree)
            {
                fruits.remove(f);
                f--;
            }
        }
        for (int f=0;f<fruits.size();f++)
        {
            fruits.get(f).update();
            if (fruits.get(f).heat>fruits.get(f).getFlashPoint())
            {
                fruits.get(f).drop();
                heat++;
            }
        }
        if (!burning())
        {
            if (!underwater())
            {
                if (Util.frequency(301 - (loc.air.raining()? 4:0),id))
                {
                    if (fruits.size()>8)
                        fruits.get(0).drop();
                    if (height<6)
                        changeHeight(1);
                    else if (height<15 && Util.frequency(5,id))
                        changeHeight(1);
//                        if (Util.frequency(10, id))
                    {
                        if (!roots.grow())
                            changeHeight(-3);
                    }
                    if (height>3)
                    {
                        fruits.add(new Fruit(fruitType,loc));
                    }
//                        else //if (Util.frequency(15))
//                            reduceHeight(1);
                }
            }
        }
    }
    public void reproduce() {
        Random rand=new Random();
        Tree shoot=new Tree(loc,fruitType);
        int dx=0,dy=0;
        do {
            dx+=rand.nextInt(5)-1;
            dy+=rand.nextInt(5)-1;
            loc=Square.getSquare(dx, dy);
        } while (loc.moveable());
        Map.addFeature(shoot);
    }
    public void addFruit(Fruit add) {
        fruits.add(add);
    }
    public Color getColor() 
    {
        if (Map.simpleDraw)
            return new Color(100,50,0);
        else
            return new Color(100,50,0,Util.colorBound(30*height+40));
    }
    public static boolean growable(Terrain terr)
    {
        if (terr.getType()== Type.Dirt || terr.getType()==Type.Grass)
            return true;
        else return false;
    }
    public void paint(Graphics2D g)
    {
        if (burning())
        {
            g.setColor(new Color(250,100,0));
            fillCircle(g);
        }
        else
        {
            g.setColor(getColor());
            g.fillOval(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
            if (fruits.size()>0)
            {
                fruits.get(0).paint(g);
//                g.setColor(fruits.get(0).getColor());
//                g.fillOval(loc.px()+Square.SQUARE_SIZE/4,loc.py()+Square.SQUARE_SIZE/4,Square.HALF_SQUARE,Square.HALF_SQUARE);
            }
        }
//            g.setColor(Color.WHITE);
//            g.setFont(new Font("Arial",Font.BOLD,10));
//            g.drawString(""+id, loc.px(),loc.py());
    }
    public Type getType() {
        return Type.Tree;
    }
    public SubType getSubType() {
        return Type.Tree;
    }
    
    
    public static class Fruit extends Thing implements Food {
        public enum FruitType implements SubType
        {
            Apple(Color.RED,500,10),Orange(new Color(150,75,0),300,20),Banana(Color.YELLOW,600,15),
            Nut(new Color(80,100,0),50,50),BlueBerries(Color.BLUE,400,5);
            Color color;
            int calories;
            int hardness;
            FruitType(Color setColor,int energyStored,int setHardness) {
                color=setColor;
                calories=energyStored;
                hardness=setHardness;
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
            public boolean walkable() {
                return true;
            }
        }
        int calories;
        FruitType type;
        boolean onTree;
        int rot;
        public Fruit(FruitType set,Square setLoc) {
            super(setLoc);
            type=set;
            calories=set.calories;
            onTree=true;
            rot=0;
        }
        public Fruit(FruitType set) {
            super();
            type=set;
            calories=set.calories;
            onTree=true;
            rot=0;
        }
        public Fruit clone()
        {
            Fruit clone=new Fruit(type);
            clone.makePropertiesSameAs(this);
            clone.rot=rot;
            clone.onTree=onTree;
            clone.calories=calories;
            return clone;
        }
        public void eat(int caloriesEaten)
        {
            calories-=caloriesEaten;
            if (calories<=0)
                onTree=false;
        }
        public float getCalories() {
            return calories;
        }
//        public float density() {
//            return 500;
//        }
        public float height() {
            return .125f;
        }
        public float volume() {
            return height()/4f;
        }
        public void damage(double damage)
        {
            if (onTree)
                drop();
            rot++;
        }
        public double getSpecificHeat() {
            return (float)type.hardness/25.0f * (float)getType().specificHeat;
        }
        public boolean isDead() {
            return calories<=0 || rot>12000;
        }
        public boolean isRotten() {
            return rot>300;
        }
        protected void update() {
            rot++;
            if (heat>85)
                rot++;
            if (rot>4000 && onTree)
            {
                drop();
            }
            else if (!onTree && rot>11000)
            {
                if (Tree.growable(loc.terrain()))
                {
                    Tree grown=new Tree(loc,type);
                    grown.height=1;
                    loc.occupant=grown;
                }
            }
        }
        protected void burn() {
            if (!onTree)
            {
                calories--;
                heat++;
            }
        }
        public SubType getSubType() {
            return type;
        }
        public Type getType() {
            return Type.Froot;
        }
        public Color getColor()
        {
            if (burning())
                return new Color(250,120,50);
            if (rot>20)
                return Util.makeValid(type.color.getRed()-rot/40,
                        type.color.getGreen()-rot/40,type.color.getBlue()-rot/40,420-rot/24);
            else return type.color;
        }
        public void paint(Graphics2D g)
        {
            g.setColor(getColor());
            g.fillOval(loc.px()+Square.SQUARE_SIZE/4,loc.py()+Square.SQUARE_SIZE/4,Square.HALF_SQUARE,Square.HALF_SQUARE);
        }
        protected void drawWater(Graphics2D g,float waterAbove)
        {
            g.setColor(Earth.waterColor(waterAbove,heat,loc.terrain().moved));
            g.fillOval(loc.px()+Square.SQUARE_SIZE/4,loc.py()+Square.SQUARE_SIZE/4,Square.HALF_SQUARE,Square.HALF_SQUARE);
        }
        public void drop()
        {
            if (onTree)
            {
                loc=loc.randTransform().randTransform().randTransform();
                if (loc.moveable())
                {
                    Map.moveFeature(this, loc);
//                    System.out.println("drop the "+type.toString());
                }
                else kill();
                onTree=false;
            }
        }
    }
}
