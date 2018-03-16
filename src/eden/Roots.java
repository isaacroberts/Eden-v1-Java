/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Isaac
 */
public class Roots extends Terrain implements Food
{
    public static float ROOT_STRENGTH=1500;
    Thing parent;//tree or root
    int durability;
    public Roots() {
        super();
        parent=Map.getNearest(Type.Tree,Square.get(0,0));
        if (parent==null)
            parent=Map.getNearest(Type.Roots, Square.get(0,0));
        durability=1;
    }
    public Roots(Thing creator)
    {
        super(creator.loc);
        parent=creator;
        durify();
    }
    public Roots(Square loc,Thing creator)
    {
        super(loc);
        parent=creator;
        durify();
    }
    public Roots (Square loc,int width,int length)
    {
        super(loc);
        Map.setTerrain(this, loc, width, length);
        parent=Map.getNearest(Type.Tree,Square.get(0,0));
        if (parent==null)
            parent=Map.getNearest(Type.Roots, Square.get(0,0));
        durability=1;
    }
    public Roots(Square loc)
    {
        super(loc);
        parent=Map.getNearest(Type.Tree,Square.get(0,0));
        if (parent==null)
            parent=Map.getNearest(Type.Roots, Square.get(0,0));
        durability=1;
    }
    protected void durify()
    {
        durability=1;
//            if (parent instanceof Tree)
//            {
//                durability=((Tree)parent).height;
//                System.out.println("set dur" +durability);
//            }
//            else if (parent instanceof Roots)
//                durability=(int)(((Roots)parent).durability*.8);
//            else
//                durability=0;
    }
    public float attachmentForces() {
        ArrayList<Square> neighbs=loc.getNeighbors();
        float forces=super.attachmentForces();
        for (int n=0;n<neighbs.size();n++)
        {
            if (neighbs.get(n).terrain() instanceof Roots)
            {
                forces+=((Roots)neighbs.get(n).terrain()).durability*ROOT_STRENGTH;
            }
        }
        return forces;
    }
    public Roots clone()
    {
        Roots r=new Roots();
        r.durability=durability;
        makePropertiesSame(r);
        return r;
    }
    public Type getType()
    {
        return Thing.Type.Roots;
    }
    public SubType getSubType()
    {
        return getTerrainType();
    }
    public TerrainType getTerrainType() {
        return TerrainType.Roots;
    }
    public Color getColor()
    {
        if (heat>Type.Grass.flashPoint)
            return Dirt.getColor(heat);
        else
            return Type.Grass.color;
    }
    public void eat(int amt) {
        durability-=amt;
    }
    public float getCalories() {
        return durability/2;
    }
    public boolean isRotten() {
        return (parent==null || parent.isDead());
    }
    public void paint(Graphics2D g)
    {
        g.setColor(getColor());
        fillSquare(g);
        if (burning())
        {
            g.setColor(new Color(160,80,0));
            g.setStroke(new BasicStroke(4));
        }
        else
        {
            if (Map.simpleDraw)
                g.setColor(new Color(50,20,0));
            else
                g.setColor(new Color(50,20,0,Util.colorBound(durability*3)));
            g.setStroke(new BasicStroke(1));
        }
        g.drawLine(loc.px(),loc.py(),loc.px()+Square.SQUARE_SIZE,loc.py()+Square.SQUARE_SIZE);
        g.drawLine(loc.px()+Square.SQUARE_SIZE,loc.py(),loc.px(),loc.py()+Square.SQUARE_SIZE);

    }
    public boolean isDead()
    {
        return durability<=0 || loc.isError();
    }
    public void damage(double amt)
    {
        changeDurability((float)(amt/ROOT_STRENGTH));
    }
    public int distanceDownRootChain()
    {
        if (parent instanceof Roots)
            return ((Roots)parent).distanceDownRootChain()+1;
        else return 1;
    }
    public void changeDurability(float amt)
    {
        if (durability>-amt)
        {
            durability+=amt;
            heat-=amt;
        }
        else
        {
            heat+=durability;
            durability=0;
        }
    }
    public String otherInfo() {
        return "Durability: "+durability;
    }
    protected void burn() 
    {
        changeDurability(-1);
    }
    public float moistureFlowRate=10;
    protected void update()
    {
        if (parent==null)
        {
            changeDurability(-.5f);
            return;
        }
        else if (parent.isDead())
        {
            changeDurability(-.5f);
        }
        else
        {
            if (parent.loc.terrain().moisture < moisture)
            {
                if (moisture>=moistureFlowRate)
                {
                    parent.loc.terrain().moisture+=moistureFlowRate;
                    moisture-=moistureFlowRate;
                }
                else
                {
                    parent.loc.terrain().moisture+=moisture;
                    moisture=0;
                }
            }
            if (Util.frequency(durability,id))
            {
                if (Util.frequency(distanceDownRootChain()+6,id))
                {
                    changeDurability(1);
                    if (durability>=250)
                    {
                        loc.replaceTerrain(new SeededRoots(this));
                    }
                }
            }   //lol easter egg
        }
    }
    public boolean grow()
    {
        Random rand=new Random();
        if (rand.nextInt(5)==0)
            return false;
        int x=loc.x()+rand.nextInt(3)-1;
        int y=loc.y();
        if (x==loc.x())
            y+=rand.nextInt(2)*2-1;
        return grow(Square.get(x,y));
    }
    public boolean grow(Square to)
    {
        if (to.terrain()!=null && to.terrain() instanceof Roots)
        {
            return ((Roots)to.terrain()).grow();
        }
        else if (Tree.growable(to.terrain()))
        {
            to.setTerrain(new Roots(to,this));
            return true;
        }
        else
        {
            return false;
        }
    }
    public static class SeededRoots extends Roots
    {
        public SeededRoots() {
            super();
        }
        public SeededRoots(Roots replace)
        {
            super(replace.loc);
            parent=replace.parent;
            durability=replace.durability+10;
            heat=replace.heat;
            moisture=replace.moisture;
//            System.out.println("supersize me");
        }
        public SeededRoots(Square setLoc) {
            super(setLoc);
            if (loc.occupant==null || !(loc.occupant instanceof Tree))
            {
                parent=new Tree(setLoc);
            }
            else
                parent=setLoc.occupant;
            durify();
            durability+=10;
        }
        public SeededRoots(Square setLoc,Thing setPar,int dur)
        {
            super(setLoc,setPar);
            durability=dur;
        }
        public SeededRoots clone() {
            SeededRoots clone=new SeededRoots();
            makePropertiesSame(clone);
            clone.parent=parent;
            return clone;
        }
        public Thing.Type getType() {
            return Thing.Type.SeededRoots;
        }
        public SubType getSubType()
        {
            return getTerrainType();
        }
        public TerrainType getTerrainType() {
            return TerrainType.SeededRoots;
        }
        public boolean isDead() {
            return loc.isError();
        }
        public void damage(double amt) 
        {
            durability-=amt/2;
        }
        protected void burn() {
            if (Util.frequency(11))
            {
                durability--;
                heat++;
            }
        }
        protected void update() {
            if (parent==null || parent.isDead())
            {
                durability-=5;
                parent=new Tree(loc);
            }
            else
            {
                if (moisture>=.1)
                    if (parent.loc.terrain().moisture < moisture)
                    {
                        parent.loc.terrain().moisture+=.1;
                        moisture-=.1;
                    }
                if (durability==0)
                    kill();
                else if (Util.frequency(durability))
                    if (Util.frequency(distanceDownRootChain()+12,id))
                        durability++;
            }
        }
        public void paint(Graphics2D g)
        {
            g.setColor(getColor());
            fillSquare(g);
            if (burning())
                g.setColor(new Color(250,125,0));
            else
                g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawLine(loc.px(),loc.py(),loc.px()+Square.SQUARE_SIZE,loc.py()+Square.SQUARE_SIZE);
            g.drawLine(loc.px()+Square.SQUARE_SIZE,loc.py(),loc.px(),loc.py()+Square.SQUARE_SIZE);
        }
    }
}
