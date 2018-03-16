/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import com.sun.tools.javac.code.Flags;
import java.awt.*;
import java.util.*;

public abstract class Thing 
{
    public enum Type implements SubType
    {
        //Terrain extensions
        Grass(true,false,true,.001,327,2,1249, new Color(0,100,0)),
        Dirt(true,false,true,.004,410,1,2002, new Color(130,75,25)),
        Sand(true,false,true,.006,1922,1,1576, new Color(200,200,0)),
        Glass(true,false,true,.003,5810,1,1700, new Color(100,120,200)),
        Roots(true,false,true,.002,338,1.1f,1000,Grass.color),
        SeededRoots(true,false,true,.01,1000,1,1500,new Color(75,75,75)),
        Spring(true,true,false,.003,300,1,2000,new Color(50,50,250)),
        WaterPit(true,true,false,.003,300,1,2000,new Color(80,20,0)),
        Tarmac(true,false,true,.005,1000,1,2200,Color.BLACK),
        BlackHole(true,false,false,1f,1000000,1f,2000000f,Color.BLACK),
        
        
        //Thing extesions
        Rock(false,false,false,.0006,644,4,2000,Color.gray),
        Froot(false,true,true,.003,330,0.3f,500,Color.RED),
        Tree(false,true,false,.01,338,1.59f,753,new Color(100,50,0)), 
        Log(false,false,false,.009,368,2,700,new Color(90,40,0)),
        Gardener(false,true,true,.01,100000,4.5f,2000,Color.gray),
        Fan(false,true,true,.002,400,3,300,new Color(170,170,250)),
        ErrorSlag(false,false,false,1.,5000,5,4500,new Color(255,0,0,200)),
        //Body extensions
        Carcass(false,true,true,.008,316,3,950,new Color(170,30, 0)),
        Animal(false,false,false,.01,344,2,750,Color.gray),
        WaterBoy(false,true,true,.01,350,3,1050,Color.cyan),
        //Generic Things
        Sail(false,false,true,.002,300,200,10,new Color(230,230,230)),
        Bastion(false,true,false,.01,500,.2f,10000,new Color(10,10,10)),
        
        ;
        public boolean terrain;
        public boolean round,walkable;
        public double specificHeat;
        public int flashPoint;
        public float surfaceArea;
        public float density;
        public Color color;
        Type(boolean land,boolean ovalur,boolean moveThru,
                double specificHeatPerDensity,int setFlashPoint,
                float setSurfaceArea,float setDens,
                Color staticColor
            ) 
        {
            terrain=land;
            round=ovalur;
            walkable=moveThru;
            specificHeat=specificHeatPerDensity;//must be >0
            flashPoint=setFlashPoint;//in Kelvin
            surfaceArea=setSurfaceArea;//in square meters
            density=setDens;
            color=staticColor;
        }
        public boolean isOfTypes(SubType[] types)
        {
            for (int t=0;t<types.length;t++)
            {
                if (types[t]==this)
                    return true;
            }
            return false;
        }
        public boolean walkable() {
            return walkable;
        }
        public Thing getInstance(Square loc)
        {
            if (terrain)
            {
                return Terrain.TerrainType.getTerrainType(this).getInstance(loc);
            }
            else if (equals(Rock))
                return new Rock(loc);
            else if (equals(Tree))
                return new Tree(loc);
            else if (equals(Carcass))
                return new Carcass(loc,null);
            else if (equals(Froot))
                return new Tree.Fruit(eden.Tree.Fruit.FruitType.Nut,loc);
            else if (equals(Animal))
                return new Animal(Species.get(0),loc);
            else if (equals(Gardener))
                return new Gardener(loc);
            else if (equals(ErrorSlag))
                return new ErrorSlag(loc);
            else 
                return new Rock(loc);
            
        }
        public boolean equals(Type other) {
            return ordinal()==other.ordinal();
        }
    }
    protected Square loc;
    int id;
    static int curID;
    float heat=290;//in Kelvin
    boolean moved=false;
    public Thing()
    {
        assignID();
        heat=Map.worldHeat();
    }
    public Thing(Square setLoc)
    {
        assignID();
        loc=setLoc;
        if (loc.terrain()!=null)
            heat=loc.terrain().heat;
        else
            heat=Map.worldHeat();
        if (isTerrain()) 
            loc.setTerrain((Terrain)this);
        else if (getType()!=Type.Froot)
            loc.occupy(this);
    }
    private void assignID() {
        id=curID;
        if (curID==Integer.MAX_VALUE)
            curID=Integer.MIN_VALUE;
        else if (curID==-1)
        {
            System.out.println("Assigning repeated ID to "+toString());
            (new Exception("Out of ID's")).printStackTrace();
            System.exit(0);
        }
        else
            curID++;
    }
    public boolean underwater() {
        return loc.terrain().metersOfWaterAbove()>=height();
    }
    public void draw(Graphics2D g)
    {
        float waterAbove=loc.terrain().metersOfWaterAbove()-height();
        if (waterAbove<=0)
            paint(g);
        else
        {
            if (waterAbove<26)
                paint(g);
            drawWater(g,waterAbove);
        }
    }
    public void setLoc(Square set) {
        if (set.equals(loc))
            return;
        moved=true;
        if (loc!=null)
            loc.removeOccupant();
        set.occupant=this;
        loc=set;
    }
    public void setMoved(boolean set) {
        moved=set;
    }
    protected void drawWater(Graphics2D g,float waterAbove)
    {
        if (waterAbove<26)
        {
            
        }
        g.setColor(Earth.waterColor(waterAbove,loc.terrain().heat,loc.terrain().drawAsRunning()));
        fillSquare(g);
    }
    public void paint(Graphics2D g) 
    {
        g.setColor(getColor());
        if (getType().round)
            g.fillOval(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
        else
            fillSquare(g);
    }
    public void fillCircle(Graphics2D g)
    {
        g.fillOval(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
    }
    public void fillSquare(Graphics2D g)
    {
        g.fillRect(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
    }
    public abstract float height();//in meters
    public float getSurfaceArea() {
        return getType().surfaceArea;
    } 
    public double getSpecificHeat() {
        return getType().specificHeat*density();
    }
    public float getFlashPoint() {
        return getType().flashPoint;
    }
    public boolean burning() {
        return heat>=getFlashPoint();
    }
    public boolean linePassesThru(float slope, float offset,float xInnerBound,float xOuterBound)
    {
        if (loc.x() > xOuterBound)
            return false;
        else if (loc.x() < xInnerBound)
            return false;
        else
            return linePassesThru(slope,offset);
    }
    public boolean linePassesThru(float slope, float offset)
    {
        float rightY=((loc.x()+Square.SQUARE_SIZE)*slope) + offset;
        float leftY=((loc.x())*slope) + offset;
        if (rightY >= loc.y() && rightY <loc.y()+Square.SQUARE_SIZE)
        {
            return true;
        }
        if (leftY  >= loc.y() && leftY  <loc.y()+Square.SQUARE_SIZE)
        {
            return true;
        }
        if ((rightY < loc.y()) ^ (leftY < loc.y())) 
        {
            return true;
        }
        return false;
    }
    public boolean onSideOfAxis(boolean xAxis,boolean closerToZero,float coord)
    {
        if (xAxis)
        {
            if (closerToZero)
            {
                return (loc.x()+Square.SQUARE_SIZE < coord);
            }
            else
                return (loc.x() > coord);
        }
        else
        {
            if (closerToZero)
            {
                return (loc.y()+Square.SQUARE_SIZE < coord);
            }
            else
                return (loc.y() > coord);
        }
    }
    protected void giveMoisture(float amt) {
        loc.terrain().moisture+=amt;
    }
    protected boolean takeMoisture(float amt) {
        if (loc.terrain().moisture>=amt)
        {
            loc.terrain().moisture-=amt;
            return true;
        }
        else
        {
            amt-=loc.terrain().moisture;
            loc.terrain().moisture=0;
            return loc.air.addMoisture(-amt);
        }
    }
    public void run() {
        update();
        handlePhysics();
        if (burning())
            burn();
    }
    public String otherInfo() {
        return "";
    }
    protected abstract void update();
    public void endStep()
    {
        moved=false;
    }
    protected abstract void burn();
    protected void handlePhysics() {
        calcHeat();
        windDamage();
    }
    protected void windDamage() {
//        System.out.println("mass="+mass()+"   wind="+loc.air.wind().sqrdMagnitude());
        double windForce=Math.pow(10,4)*surfaceArea();
        if (Math.abs(loc.air.wind().xVel)>Math.abs(loc.air.wind().yVel))
            windForce*=loc.air.wind().xVel;
        else 
            windForce*=loc.air.wind().yVel;
        if (Double.isInfinite(windForce) || Double.isNaN(windForce))
            return;
        getPushed(loc.air.wind().toDir(),windForce);
    }
    
    public boolean getPushed(Dir dir,double force)
    {
        if (moved)
            return false;
        if (force<0)
            force=-force;
        if (force<1)
            return false;
        if (isDead())
        {
            setLoc(Square.error);
            return true;
        }
        Square toMove=loc.transform(dir);
        if (toMove.isError())//dont allow pushing off map
            return false;
        if (dir.equals(Dir.Up) || dir.equals(Dir.Down))
        {
            (new Exception("A thing got pushed "+dir.name())).printStackTrace();
            return false;
        }
        float slope=loc.terrain().elevation-toMove.terrain().elevation;
        force-=attachmentForces();
        force-=friction();
        force-=weight()*slope;
        if (force>0)
        {
            if (toMove.occupant==null)
            {
                setLoc(toMove);
                return true;
            }
            else
            {
                damage(force/4);
                if (!isDead())
                {
                    if (!toMove.occupant.getPushed(dir,force))
                    {
                        toMove.occupant.damage(force/4);
                        return false;
                    }
                    else
                    {
                        damage(force*.1f);
                        setLoc(toMove);
                        return true;
                    }
                }
                else return false;
            }
        }
        else return false;
    }
    public void calcHeat()
    {
        if (Float.isInfinite(heat))
        {
//            System.out.println(getSubType().toString()+" "+loc.toString()+" infinite heat");
//            (new Exception("Infinite heat")).printStackTrace();
            ErrorSlag obs=new ErrorSlag(loc);
            obs.heat=1;
            loc.occupant=obs;
            loc.replaceTerrain(new Terrain.Glass(loc));
            loc.terrain().heat=1;
        }
        float heatDif=loc.air.heat()-heat;
        float deltaHeat=(heatDif)/(float)(getSpecificHeat()+loc.air.specificHeat()) *getSurfaceArea()*Earth.TIME_STEP;
        if (deltaHeat>heatDif/4.2)
            deltaHeat=heatDif/4.2f;
        heat+=deltaHeat;
        loc.air.changeHeat(-deltaHeat);
//        heat+=(loc.terrain().heat-heat)/(10*getSpecificHeat());
        if (heat<1)
            heat=1;
    }
    public float density() { //in kilogram per cubic meter
        return getType().density;
    }
    public float surfaceArea() {
        return getType().surfaceArea;
    }
    public float attachmentForces() {
        return 0;
    }
    public float friction() {
        return weight()*loc.terrain().frictionFactor();
    }
    public abstract float volume();
    public float mass()
    {
        return density()*volume();
    }
    public float weight() {
        return mass()*Earth.gravity;
    }
    public abstract void damage(double damage);
    public void kill() {
        loc=Square.error;
    }
    public boolean isDead() {
        return loc.isError();
    }
    public boolean isTerrain() {
        return false;
    }
    public abstract Thing clone();
    public void makePropertiesSame(Thing other) {
        other.heat=heat;
        other.loc=loc;
        other.moved=moved;
    }
    public void makePropertiesSameAs(final Thing other) {
        heat=other.heat;
        loc=other.loc;
        moved=other.moved;
    }
    public boolean sameType(Thing other) {
        return (getType().ordinal()==other.getType().ordinal());
    }
    public boolean isOfTypes(Type[] types)
    {
        for (int t=0;t<types.length;t++)
        {
            if (types[t]==getType())
                return true;
        }
        return false;
    }
    public Color getColor() {
        return getType().color;
    }
    public abstract Type getType();
    public SubType getSubType() {
        return getType();
    }
    
    public static class Rock extends Thing 
    {
        float strength=1;
        public Rock() {
            super();
        }
        public Rock(Square setLoc,int setWidth,int setLength) {
            super(setLoc);
            Map.placeFeatures(this,loc,setWidth,setLength);
        }
        public Rock(Square setLoc)
        {
            super(setLoc);
        }
        public Rock clone()
        {
            Rock lock=new Rock();
            lock.makePropertiesSameAs(this);
            return lock;
        }
        public Color getColor()
        {
            if (heat>getFlashPoint()-100)
                return new Color(250,75,0);
            else
                return Color.gray;
        }
        public Type getType() {
            return Type.Rock;
        }
        public SubType getSubType() {
            return Type.Rock;
        }
        public float height() {
            return 2;
        }
        public float density() {
            return getType().density*strength;
        }
        public float volume() {
            return height();
        }
        protected void update() {
        }
        protected void burn() {
            loc.terrain().heat+=10000;
            kill();
        }
        public void damage(double damage) {
            strength-=damage/10;
            if (strength<0)
            {
                loc.terrain().elevation+=(damage/10+strength);
                strength=0;
            }
            else
                loc.terrain().elevation+=damage/10;
        }
        public boolean isDead() {
            return loc.isError() || strength<=0;
        }
    }
    public static class Fan extends Thing
    {
        public Fan() {
            super();
        }
        public Fan(Square setLoc) {
            super(setLoc);
        }
        public Type getType() {
            return Type.Fan;
        }
        public Fan clone() {
            Fan fan=new Fan();
            makePropertiesSame(fan);
            return fan;
        }
//        public boolean getPushed(Dir dir, double force)
//        {
//            return false;
//        }
        public void damage(double amt){}
        protected void burn() {}
        protected void update() {
            loc.air.newState().wind().xVel+=.1f;
//            loc.transform(Dir.East).air.newState().wind().xVel+=.1f;
//            loc.transform(Dir.West).air.newState().wind().xVel+=-.1f;
        }
        public float height() {
            return .5f;
        }
        public float volume() {
            return height();
        }
    }
    public static abstract class Body extends Thing implements Food
    {
        Species species;
        float calories;
        public Body(Square setLoc)
        {
            super();
            loc=setLoc;
            loc.addAnimal(this);
        }
        public Body()
        {
            super();
            loc=Square.error;
        }
        public boolean getPushed(Dir dir,double force)
        {
            if (moved)
                return false;
            if (force<0)
                force=-force;
            if (force<1)
                return false;
            if (isDead())
            {
                setLoc(Square.error);
                return true;
            }
            Square toMove=loc.transform(dir);
            if (toMove.isError())//dont allow pushing off map
                return false;
            if (dir.equals(Dir.Up) || dir.equals(Dir.Down))
            {
                (new Exception("A thing got pushed "+dir.name())).printStackTrace();
                return false;
            }
            float slope=loc.terrain().elevation-toMove.terrain().elevation;
            force-=attachmentForces();
            force-=friction();
            force-=weight()*slope;
            if (force>0)
            {
                setLoc(toMove);
                return true;
            }
            else return false;
        }
        public void setLoc(Square set) {
            if (set.equals(loc) || moved)
                return;
            loc.animals.remove(this);
            loc=set;
            for (int n=0;n<loc.animals.size();n++)
            {
                if (loc.animals.get(n).id==id)
                    return;
            }
            loc.addAnimal(this);
            moved=true;
        }
    }
    public static class Carcass extends Body implements Food
    {
        int rot;
        float height;
        public Carcass(Square loc,Species bodyType) {
            super(loc);
            species=bodyType;
            rot=0;
            if (species==null)
            {
                species=Species.error;
                height=5;
                calories=100;
            }
            else
            {
                calories=species.maxEnergy()/2;
                height=species.height()/2;
            }
        }
        public Carcass(Species bodyType) {
            super();
            species=bodyType;
            rot=0;
            if (species==null)
            {
                species=Species.error;
                height=5;
                calories=100;
            }
            else
            {
                calories=species.maxEnergy()/2;
                height=species.height()/2;
            }
        }
        public Carcass(Animal body) {
            super(body.getLoc());
            species=body.species;
            calories=body.calories;
            rot=body.age/6;
        }
        public Carcass clone()
        {
            Carcass clone=new Carcass(species);
            clone.makePropertiesSameAs(this);
            clone.calories=calories;
            clone.rot=rot;
            return clone;
        }
        protected void burn() {
            calories--;
            heat+=2;
        }
        protected void update()
        {
            if ((heat>85 || Math.random()/Earth.TIME_STEP<=.5) && heat>50)
            {
                rot++;
            }
            if (rot>800)
                calories-=2;
        }
        
        public float height() {
            return height;
        }
        public float volume() {
            return height();
        }
        public void damage(double damage)
        {
            rot+=damage;
        }
        public boolean isDead() {
            return calories<=0 || loc.isError();
        }
        public boolean isRotten() {
            return rot>600;
        }
        public void eat(int calEaten)
        {
            calories-=calEaten;
        }
        public float getCalories()
        {
            return calories;
        }
        public Color getColor() 
        {
            if (burning())
                return new Color(70,70,250);
//            if (rot<600)
            else
                return Util.makeValid((170-rot/4),30, 0);
//            else
//                return Color.BLACK;
        }
        public Type getType() {
            return Type.Carcass;
        }
        public SubType getSubType() {
            return Type.Carcass;
        }
    }
    public static class Gardener extends Thing
    {
        public Gardener(Square setLoc)
        {
            super(setLoc);
        }
        public Gardener()
        {
            super();
        }
        public Type getType() {
            return Type.Gardener;
        }
        public SubType getSubType() {
            return getType();
        }
        public Color getColor() {
            return Color.GRAY;
        }
        public boolean isDead() {
            return loc.isError();
        }
        public float height() {
            return 2;
        }
        public float volume() {
            return height();
        }
        public Gardener clone() {
            Gardener gard=new Gardener();
            makePropertiesSame(gard);
            return gard;
        }
        public void damage(double amt) {}
        protected void update() 
        {
            if (Util.frequency(4,id))
            {
                ArrayList<Square> neighbors=loc.getNeighbors();
                for (Square nayb : neighbors)
                {
                    if (nayb.terrain().getType()!=Type.Grass)
                    {
                        Grass grass=new Grass(nayb);
                        grass.height=1;
                        grass.heat=heat;
                    }
                }
            }
        }
        public void burn() {
            kill();
        }
    }
    public static class Generic extends Thing
    {
        Type type;
        Generic(Type setType) {
            super();
            type=setType;
        }
        Generic(Square setLoc,Type setType) 
        {
            super(setLoc);
            type=setType;
        }
        public Type getType() {
            return type;
        }
        public void damage(double amt){}
        protected void burn() {
            heat+=.1f;
            if (Util.everyNth(10,id))
            {
                loc.terrain().heat+=3;
                kill();
            }
        }
        public Generic clone() {
            Generic gen=new Generic(type);
            makePropertiesSame(gen);
            return gen;
        }
        protected void update(){}
        public float height() {
            return 1;
        }
        public float volume() {
            return height();
        }
    }
    public static class ErrorSlag extends Thing
    {
        public ErrorSlag() {
            super();
            loc=Square.error;
        }
        public ErrorSlag(Square loc)
        {
            super(loc);
            if (Eden.frame!=null)
                Eden.frame.broke=true;
        }
        public Type getType() {
            return Type.ErrorSlag;
        }
        public SubType getSubType() {
            return Type.ErrorSlag; 
        }
        public ErrorSlag clone() {
            ErrorSlag slag=new ErrorSlag();
            slag.makePropertiesSameAs(this);
            return slag;
        }
        public float height() {
            return loc.terrain().waterLevel()+1;//so its always above water
        }
        public float volume() {
            return height();
        }
        public void paint(Graphics2D g)
        {
            g.setColor(getColor());
            fillSquare(g);
        }
        public boolean getPushed(Dir dir,double force) {
            return false;
        }
        protected void update() 
        {
        }
        public void burn() {
            
        }
        public void damage(double amt){}
        public boolean isDead() {
            return loc.isError();
        }
    }
}

