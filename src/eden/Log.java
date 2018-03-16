/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Isaac
 */
public class Log extends Thing
{
    //1000 durability is one meter of tree height
    public static final int DURABILITY_PER_METER=1000;
    public int durability=DURABILITY_PER_METER;
    public Log parent;
    public Log child;
    public Log() {
        super();
    }
    public Log(Square setLoc) {
        super(setLoc);
    }
    public Log(Log setParent) {
        super();
        parent=setParent;
        if (parent!=null)
            parent.child=this;
    }
    public Log(Square setLoc,Log setParent) {
        super(setLoc);
        parent=setParent;
        if (parent!=null)
            parent.child=this;
    }
    public Log clone()
    {
        Log log=new Log();
        log.makePropertiesSameAs(this);
        log.parent=parent;
        log.child=child;
        return log;
    }
    public Type getType() {
        return Type.Log;
    }
    public void draw(Graphics2D g)
    {
        super.draw(g);
//            g.setColor(Color.BLACK);
//            g.setStroke(new BasicStroke(1));
//            if (parent!=null)
//                g.drawLine(loc.px()+7,loc.py()+7,parent.loc.px()+7,parent.loc.py()+7);
//            if (child!=null)
//                g.drawLine(loc.px()+3,loc.py()+3,child.loc.px()+3,child.loc.py()+3);
    }
    public Color getColor() {
        if (burning())
            return new Color(250,100,0);
        else if (Map.simpleDraw)
            return new Color(90,40,0);
        else
            return new Color(90,40,0,Util.colorBound(percentDurability() * 255));
    }
    protected void update() {
        if (Util.everyNth(10,id))
            reduceDurability(1);
        if (parent!=null && parent.isDead())
            parent=null;
        if (child!=null && child.isDead())
            child=null;
    }
    public void reduceDurability(int amt) {
        if (durability>amt)
        {
            durability-=amt;
            heat+=(float)amt/(float)DURABILITY_PER_METER *2f;
            loc.air.changeHumidity(Tree.moisturePerHeight/(float)DURABILITY_PER_METER * (float)amt);
        }
        else
        {
            heat+=percentDurability()*2f;
            loc.air.changeHumidity(percentDurability()*Tree.moisturePerHeight);
            durability=0;
        }
    }
    protected void burn() {
        reduceDurability(10);
    }
    public float percentDurability() {
        return (float)durability/(float)DURABILITY_PER_METER;
    }
    public float volume() {
        return .785f;
    }
    public float surfaceArea() {
        return ownSurfaceArea()+parentSurfaceArea()+childSurfaceArea();
    }
    public float ownSurfaceArea() {
        return 3.14f;//pi*2r*l , r=.5, l=1
    }
    public float parentSurfaceArea() {
        if (parent==null)
            return 0.785f;//pi*r*r, r=.5
        else
            return parent.ownSurfaceArea()+parent.parentSurfaceArea();
    }
    public float childSurfaceArea() {
        if (child==null)
            return 0.785f;//pi*r*r, r=.5
        else
            return child.ownSurfaceArea()+child.childSurfaceArea();
    }
    public float attachmentForces()
    {
        return parentWeight()+childWeight();
    }
    public float parentWeight() {
        if (parent!=null)
        {
            return (parent.parentWeight()+parent.weight());
        }
        else 
            return 0;
    }
    public float childWeight() {
        if (child!=null)
        {
            return (child.childWeight()+child.weight());
        }
        else 
            return 0;
    }
    public void setLoc(Square set)
    {
        bringAncestors(set);
        bringDescendents(set);//the functions without a dir parameter dont setLoc
        super.setLoc(set);
    }
    protected void bringAncestors(Square to) 
    {
        if (parent!=null && !to.isError())
        {
            Dir dirTo=loc.getDir(to);
            parent.bringAncestors(to.transform(dirTo),dirTo);
        }//no setLoc, because setLoc calls this func
    }
    protected void bringDescendents(Square to) 
    {
        if (child!=null && !to.isError())
        {
            Dir dirTo=loc.getDir(to);
            child.bringAncestors(to.transform(dirTo),dirTo);
        }//no setLoc, because setLoc calls this func
    }
    protected void bringAncestors(Square to,Dir dirTo) 
    {
        super.setLoc(to);
        if (parent!=null && !to.isError())
        {
            parent.bringAncestors(to.transform(dirTo),dirTo);
        }
    }
    protected void bringDescendents(Square to,Dir dirTo) 
    {
        super.setLoc(to);
        if (child!=null && !to.isError())
        {
            child.bringAncestors(to.transform(dirTo),dirTo);
        }
    }
    public void damage(double amt) 
    {
        reduceDurability((int)(amt/durability));
    }
    public boolean isDead() {
        return loc.isError() || durability<=0;
    }
    public float density() {
        return getType().density*(percentDurability());
    }
    public float height() {
        return 1;
    }
    public static void fellTree(Tree tree,Dir dir)
    {
        if (dir.errorDir())
            return;
        Square cur=tree.loc;
        Log prev=null;
        for (int heit=0;heit<tree.height;heit++)
        {
            cur=cur.transform(dir);
            if (cur.isError())
                break;
            Log put=new Log(cur,prev);
            put.moved=true;
            if (prev==null)
                put.damage(100);//todo: fix force
            if (cur.occupant==null)
            {
                double damage=heit*put.weight()/6.;
                put.damage(damage);
                cur.occupant=put;
                cur.damageAllThings(damage);//damage the animals standing there
            }
            else
            {
                double damage=put.weight()/2.*heit;
                cur.damageAllThings(damage);//damages occupant & all animals
                put.damage(damage);
                if (cur.occupant.isDead())
                {
                    cur.occupant=put;
                }
                else if (!put.isDead())
                {
                    if (cur.occupant.height()<put.height())
                    {
                        cur.occupant=put;
                    }
//                        else put=null;
                }
            }
            prev=put;
        }
        tree.loc.terrain().heat+=1000;
        tree.kill();
    }
}
