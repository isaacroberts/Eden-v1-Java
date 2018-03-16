
package eden;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Isaac
 */
public abstract class Terrain extends Thing
{
    public enum TerrainType implements SubType
    {
        Dirt(.8f,1.5f,true,.2f,Thing.Type.Dirt),
        Grass(.7f,1.9f,false,.3f,Thing.Type.Grass),
        Sand(.4f,3f,true,.4f,Thing.Type.Sand),
        Glass(.2f,.2f,false,.1f,Thing.Type.Glass),
        Roots(.9f,2.1f,false,.5f,Thing.Type.Roots),
        SeededRoots(.95f,2.5f,false,.6f,Thing.Type.SeededRoots),
        Spring(.8f,1f,true,.2f,Thing.Type.Spring),
        WaterPit(.8f,1f,true,.2f,Thing.Type.WaterPit),
        Tarmac(1f,1.2f,false,.4f,Thing.Type.Tarmac),
        BlackHole(0,10000000f,false,0,Thing.Type.BlackHole),
        ;
        float sunMod;//between 1 and 0, where 0 is total reflection and 1 is total absorption 
        Thing.Type thingType;
        float waterSaturation;
        boolean erodable;
        float friction;//diffuclty to push thing sideways is thing.weight()*friction
        TerrainType(float sunReceptivity,float waterSat,boolean erodes,float frictionFactor,Thing.Type sameAs)
        {
            sunMod=applySunReceptivityVariance(sunReceptivity);
            waterSaturation=waterSat*Earth.MOISTURE_PER_CUBIC_METER;
            erodable=erodes;
            thingType=sameAs;
            friction=frictionFactor;
        }
        public static float applySunReceptivityVariance(float receptivityFactor)
        {
            float sunRecepVariance=.7f;
            return receptivityFactor*sunRecepVariance +(1-sunRecepVariance)/2f;
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
            return thingType.walkable;
        }
        public boolean equals(TerrainType other) {
            return ordinal()==other.ordinal();
        }
        public Terrain getInstance(Square loc)
        {
            if (equals(Grass))
                return new Grass(loc);
            else if (equals(Dirt))
                return new Dirt(loc);
            else if (equals(SeededRoots))
                return new Roots.SeededRoots(loc);
            else if (equals(Spring))
                return new Spring(loc);
            else if (equals(WaterPit))
                return new WaterPit(loc);
            else 
                return new GenericLand(loc,this);
        }
        public static TerrainType getTerrainType(Type thingType)
        {
            for (int n=0;n<values().length;n++)
            {
                if (values()[n].thingType.equals(thingType))
                    return values()[n];
            }
            return Dirt;
        }
    }
    float moisture=100;
    float elevation=100;
    public Terrain() {
        super();
        elevation=100;
        loc=Square.error;
        moisture=100;
        heat=Map.worldHeat;
    }
    public Terrain(Square setLoc)
    {
        super();
        loc=setLoc;
        if (loc.terrain()!=null)
        {
            heat=loc.terrain().heat;
            elevation=loc.terrain().elevation;
            moisture=loc.terrain().moisture;
        }
        loc.setTerrain(this);
    }
    public Terrain(Square setLoc,int width,int length)
    {
        super(setLoc);
        Map.setTerrain(this, loc, width, length);
    }
    public abstract TerrainType getTerrainType();
    public Thing.Type getType() {
        return getTerrainType().thingType;
    }
    public SubType getSubType() {
        return getTerrainType();
    }
    public float getSunMod() {
        
        if (underwater())
        {
            if (heat<Earth.freezePoint)
                return Earth.iceSunMod;
            else
                return Earth.waterSunMod;
        }
        else
            return getTerrainType().sunMod;
    }
    public double getSpecificHeat() {
        return getType().specificHeat+moisture*Earth.WATER_SPECIFIC_HEAT;
    }
    public boolean erodable() {
        return getTerrainType().erodable;
    }
    public float frictionFactor() {
        return getTerrainType().friction;
    }
    public float friction() {
        return frictionFactor()*weight();
    }
    public float attachmentForces() {
        if (erodable())
            return 0;
        else
            return density();
    }
    public abstract Terrain clone();
    public void makePropertiesSame(Terrain change)
    {
        super.makePropertiesSame(change);
        change.elevation=elevation;
        change.moisture=moisture;
    }
    public void makePropertiesSameAs(Terrain change)
    {
        super.makePropertiesSameAs(change);
        elevation=change.elevation;
        moisture=change.moisture;
    }
    public boolean isTerrain()
    {
        return true;
    }
    public void draw(Graphics2D g)
    {
        paint(g);
        drawWater(g);
    }
    public void paint(Graphics2D g)
    {
        g.setColor(getColor());
        fillSquare(g);
    }
    public void damage(double amt){}
    public void drawWater(Graphics2D g)
    {
        if (saturated()) {
            g.setColor(Earth.waterColor(metersOfWaterAbove(),heat,drawAsRunning()));
            fillSquare(g);
        }
    }
    public boolean drawAsRunning() {
//        if (!moved) return false;
//        return (Util.update()/5+loc.x()*loc.y())%10==0;
        return moved || nextMoved;
    }
    public void drawElev(Graphics2D g)
    {
        if (Util.broken(elevation))
        {
            g.setColor(new Color(0,255,0));
        }
        else
        {
            if (Map.simpleDraw && underwater())
            {
                int blue=Util.colorBound((height()-Map.avgElev)/10+125);
                g.setColor(new Color(20,20,blue));
            }
            else
            {
                int grey=Util.colorBound((elevation-Map.avgElev)/10+125);
                g.setColor(new Color(grey,grey,grey));
            }
            
        }
        fillSquare(g);
        if (getType()==Type.Spring || getType()==Type.WaterPit)
        {
            g.setColor(getType().color);
            g.fillOval(loc.px()+Square.HALF_SQUARE/2,loc.py()+Square.HALF_SQUARE/2,Square.HALF_SQUARE,Square.HALF_SQUARE);
        }
        
    }
    public void setLoc(Square set) {
        if (loc!=null)
            loc.replaceTerrain();
        loc=set;
        set.replaceTerrain(this);
    }
    protected void giveMoisture(float amt) 
    {
        loc.air.addMoisture(amt);
    }
    public static final Thing.Type[] dontCoverGround={Thing.Type.Froot,Thing.Type.Carcass,Thing.Type.Animal};
    public boolean groundExposed() {
        if (saturated()) return false;
        if (loc.occupant!=null)
        {
            if (!loc.occupant.isOfTypes(dontCoverGround))
                return false;
        }
        for (int n=0;n<loc.animals.size();n++)
        {
            if (loc.animals.get(n) instanceof Animal)
            {
                if (((Animal)loc.animals.get(n)).groundArea()<.6f)
                {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean saturated() {
        return moisture>getWaterSaturation();
    }
    public boolean underwater() {
        return moisture>getWaterSaturation();
    }
    public float getWaterSaturation() {
        return getTerrainType().waterSaturation;
    }
    public float waterAbove() {
        if (moisture>getWaterSaturation())
        {
            return moisture-getWaterSaturation();
        }
        else return 0;
    }
    public float metersOfWaterAbove() {
        return waterAbove()/Earth.MOISTURE_PER_CUBIC_METER;
    }
    public float waterLevel() {
        return metersOfWaterAbove()+elevation;
    }
    public float height() {
        return waterLevel();
    }
    public float volume() {
        return height();
    }
    public void changeElevation(float amt)
    {
        if (Util.broken(amt))
            return;
        elevation+=amt;
//        if (elevation>990)
//            elevation=990;
//        else if (elevation<-450)
//            elevation=-450;
    }
    protected boolean getMoisture(float amt) {
        if (moisture>amt)
        {
            moisture-=amt;
            return true;
        }
        else
        {
            amt-=moisture;
            moisture=0;
            return takeMoisture(amt);
        }
    }
    public void fill(float metersHigh)
    {
        metersHigh-=elevation;
        float waterNeeded=metersHigh*Earth.MOISTURE_PER_CUBIC_METER;
        waterNeeded+=getWaterSaturation();
        if (moisture<waterNeeded)
            moisture=waterNeeded;
    }
    protected boolean takeMoisture(float amt) {
        return loc.air.addMoisture(-amt);
    }
    protected void handlePhysics() {
        calcHeat();
        if (loc.air.raining())
        {
            moisture++;
        }
        if (moisture<0)
            moisture=0;
        evaporate();
        runoff();
        windDamage();
    }
    public void calcHeat() 
    {
        super.calcHeat();
        heat+=Earth.getHeatFromSun()*getSunMod()*(loc.air.raining()?.5f:1);
    }
    public static double erosionRate=Math.pow(10,4);
    protected void windDamage() //erosion
    {
        if (erodable() && groundExposed())
        {
            for (byte isX=0;isX<=1;isX++)
            {
                float blow=(isX==0?loc.air.wind().xVel:loc.air.wind().yVel);
                blow= (float)Math.abs(blow*erosionRate/weight());
                Dir dir=(isX==0?loc.air.wind().xDir():loc.air.wind().yDir());
                Square toBlowTo=loc.transform(dir);
                if (!toBlowTo.isError())
                {
                    changeElevation(blow);
                    toBlowTo.terrain().changeElevation(blow);
                    if (blow>=1)
                    {
                        Terrain set=(Terrain)(getType().getInstance(toBlowTo));
                        toBlowTo.terrain().makePropertiesSame(set);
                        if (set.getType()==Type.Roots)
                            ((Roots)set).parent=this;
                        toBlowTo.replaceTerrain(set);
                    }
                }
            }
        }
    }
    public boolean getPushed(Dir dir,double force) {
        return false;
    }
    public static float waterFlowRate=.3f;//must be ≤1 and >0
    public static float waterErosionRate=.1f;
    boolean nextMoved=false;
    protected void runoff() 
    {
        if (saturated() && heat>Earth.freezePoint)
        {
            Dir dir=Dir.West;
            float initialLevel=metersOfWaterAbove();
            boolean noFlow=true;
            boolean updateWaterDisplay=Util.everyNth(2);
            for (int rotate=0;rotate<4;rotate++)
            {
                dir=dir.rotate(true);
//                Dir dir=Dir.getRandDir();
                Square neighb=loc.transform(dir);
                if (!neighb.isError())
                {
                    float levelDiff=(initialLevel+loc.terrain().elevation-neighb.terrain().waterLevel());
                    float flow=levelDiff*waterFlowRate;
//                    flow/=4;
                    if (flow>initialLevel/4)
                        flow=initialLevel/4;
                    float moistureFlow=flow*Earth.MOISTURE_PER_CUBIC_METER;
                    if (moistureFlow>waterAbove())
                        moistureFlow=waterAbove();
                    if (moistureFlow>0)
                    {
                        if (moistureFlow>Earth.MOISTURE_PER_CUBIC_METER/2)
                        {
                            if (updateWaterDisplay)
                            {
                                noFlow=false;
    //                            if (Util.everyNth(2))
                                {
                                    if (moved)
                                    {
                                        neighb.terrain().setWaterMovementDisplay(true);
                                        moved=false;
    //                                    setWaterMovementDisplay(false);
                                    }
                                    else if (Math.random()<.02)
                                        setWaterMovementDisplay(true);
                                }
                            }
                            neighb.terrain().heat=(neighb.terrain().heat*neighb.terrain().moisture + heat*moistureFlow)/(neighb.terrain().moisture+moistureFlow);
                            if (neighb.occupant!=null)
                            {
                                neighb.occupant.getPushed(dir,flow*20*neighb.occupant.surfaceArea());
                            }
                            for (int n=0;n<neighb.animals.size();n++) {
                                neighb.animals.get(n).getPushed(dir, flow*20*neighb.animals.get(n).surfaceArea());
                            }
                            if (waterAbove()-moistureFlow<=3*Earth.MOISTURE_PER_CUBIC_METER)
                            {
                                if (getTerrainType()!=TerrainType.Spring)
                                    changeElevation(-waterErosionRate);
                                if (neighb.terrain().getTerrainType()!=TerrainType.WaterPit)
                                    neighb.terrain().changeElevation(waterErosionRate);
                            }
                        }
                        neighb.terrain().moisture+=moistureFlow;
                        moisture-=moistureFlow;
                    }
                }
            }//to for(rotate)
            if (noFlow && updateWaterDisplay)
                setWaterMovementDisplay(false);
        }
    }
    public void setWaterMovementDisplay(boolean set) {
        nextMoved=set;
    }
    protected void evaporate() {
        if (loc.air.raining())
        {
            takeMoisture(Earth.TIME_STEP);
            moisture+=Earth.TIME_STEP;
        }
        float evap=Earth.getEvap(heat, moisture,getSurfaceArea(),loc.air);
        moisture-=evap;
        heat-=evap*Earth.WATER_SPECIFIC_HEAT;
        giveMoisture(evap);
    }
    public void endStep() 
    {
        if (Util.everyNth(2))
        {
            moved=nextMoved;
            nextMoved=false;
        }
    }
    public static class Dirt extends Terrain
    {
        public Dirt() {
            super();
        }
        public Dirt(Square setLoc)
        {
            super(setLoc);
        }
        public Dirt clone()
        {
            Dirt clone=new Dirt(loc);
            makePropertiesSame(clone);
            return clone;
        }
        public Type getType()
        {
            return Type.Dirt;
        }
        public SubType getSubType()
        {
            return getTerrainType();
        }
        public Color getColor() 
        {
            if (burning())
                return new Color(Util.colorBound(130+(heat-getType().flashPoint)/5),75,25);
            else
                return getType().color;
        }
        public static Color getColor(float heat)
        {
            if (heat>Type.Dirt.flashPoint)
                return new Color(Util.colorBound(130+(heat-Type.Dirt.flashPoint)/5),75,25);
            else
                return Type.Dirt.color;
        }
        public TerrainType getTerrainType() {
            return TerrainType.Dirt;
        }
        public boolean isDead()
        {
            return loc.isError();
        }
        public void damage(double amt)
        {}
        protected void burn() {
            
        }
        protected void update()
        {
        }
    }
    public static class Sand extends Terrain
    {
        public Sand() {
            super();
        }
        public Sand(Square loc)
        {
            super(loc);
        }
        public Sand(Square loc,int width,int length)
        {
            super(loc);
            Map.setTerrain(this, loc, width, length);
        }
        public Sand clone()
        {
            Sand s=new Sand();
            makePropertiesSame(s);
            return s;
        }
        public Type getType()
        {
            return Thing.Type.Sand;
        }
        public SubType getSubType()
        {
            return getTerrainType();
        }
        public Color getColor()
        {
            return new Color(200,200,0);
        }
        public TerrainType getTerrainType() {
            return TerrainType.Sand;
        }
        public boolean isDead()
        {
            return loc.isError();
        }
        public void damage(double amt){}
        protected void burn()
        {
            loc.replaceTerrain(new Glass(loc));
            loc.terrain().heat=heat;
        }
        protected void update() 
        {
        }
    }
    public static class Spring extends Terrain
    {
        public static float flowRate=Earth.MOISTURE_PER_CUBIC_METER*30;
        public static float springTemp=278;
        public Spring()
        {
            super();
//            Map.makePeak(10, 15, loc);
            elevation+=10;
        }
        public Spring(Square setloc)
        {
            super(setloc);
//            Map.makePeak(10, 15, loc);
            elevation+=10;
        }
        public Spring clone() {
            Spring s=new Spring();
            makePropertiesSame(s);
            return s;
        }
        public TerrainType getTerrainType() {
            return TerrainType.Spring;
        }
        public boolean isDead() {
            return loc.isError();
        }
        protected void runoff() {
            super.runoff();
            float amtBefore=(float)getSpecificHeat();
//            if (heat>Earth.freezePoint)
                moisture+=flowRate;
            float amtAfter=(float)getSpecificHeat();
            heat=(heat*amtBefore+springTemp*(amtAfter-amtBefore))/(amtAfter);
        }
        public void damage(double amt){}
        protected void burn() {
            
        }
        protected void update()
        {
             
        }
        public void drawWater(Graphics2D g)
        {
//            if (saturated()) {
//                g.setColor(new Color(50,50,250));
//                fillSquare(g);
//                if (heat<Earth.freezePoint)
//                {
//                    g.setColor(Earth.iceColor(metersOfWaterAbove(),heat));
//                    fillSquare(g);
//                }
//            }
        }
    }
    public static class WaterPit extends Terrain
    {
        public static float pitHeat=300;
        public WaterPit()
        {
            super();
//            Map.makePeak(-30, 15, loc);
            elevation-=10;
        }
        public WaterPit(Square setloc)
        {
            super(setloc);
//            Map.makePeak(-30, 15, loc);
            elevation-=10;
        }
        public WaterPit clone() {
            WaterPit s=new WaterPit();
            makePropertiesSame(s);
            return s;
        }
        public TerrainType getTerrainType() {
            return TerrainType.WaterPit;
        }
        public boolean isDead() {
            return loc.isError();
        }
        protected void burn() {
            
        }
        public void damage(double amt){}
        protected void update()
        {
//            if (loc.occupant!=null)
//            {
//                loc.deleteOccupant();
//            }
        }
        protected void runoff() 
        {
            moisture*=.9;
            super.runoff();
        }
        public void calcHeat()
        {
            super.calcHeat();
            
        }
        public void drawWater(Graphics2D g) {}
    }
    public static class Glass extends Terrain
    {
        public Glass() {
            super();
        }
        public Glass(Square setLoc) {
            super(setLoc);
        }
        public Glass clone() {
            Glass clone=new Glass();
            makePropertiesSame(clone);
            return clone;
        }
        public Thing.Type getType() {
            return Thing.Type.Glass;
        }
        public SubType getSubType()
        {
            return getTerrainType();
        }
        public TerrainType getTerrainType() {
            return TerrainType.Glass;
        }
        public boolean isDead() {
            return loc.isError();
        }
        public void damage(double amt) {
            if (amt>10)
            {
                loc.replaceTerrain(new Sand(loc));
                loc.terrain().heat=heat;
            }
        }
        protected void burn() 
        {
            heat-=100;
            if (heat<=1f)
                heat=1f;
            loc.replaceTerrain(new BlackHole(loc));
        }
        protected void update() {}
        
    }
    public static class BlackHole extends Terrain 
    {
        public BlackHole(Square setLoc)
        {
            super(setLoc);
        }
        public BlackHole() {
            super();
        }
        public BlackHole clone() {
            BlackHole hole=new BlackHole();
            makePropertiesSame(hole);
            return hole;
        }
        public TerrainType getTerrainType() 
        {
            return TerrainType.BlackHole;
        }
        protected void burn() {}
        public void damage(double amt) {}
        protected void update() 
        {
            if (loc.air.mass>.5f)
                loc.air.mass-=.1f;
            if (loc.air.humidity>1)
                loc.air.humidity--;
            moisture=0;
            loc.air.setWind(new Vector(0,0));
            if (loc.occupant!=null)
                loc.deleteOccupant();
            loc.animals.clear();
            ArrayList<Square> nayb=loc.getSquaresInRadius(2);
            for (int n=0;n<nayb.size();n++)
            {
                if (loc.terrain().getType()!=Thing.Type.BlackHole)
                {
                    Vector wind=Vector.getScaledVector(nayb.get(n),loc,.001f);
                    nayb.get(n).air.giveWind(wind);
                }
            }
        }
    }
    public static class GenericLand extends Terrain 
    {
        TerrainType type;
        public GenericLand(TerrainType setType) {
            super();
            type=setType;
        }
        public GenericLand(Square setLoc,TerrainType setType) {
            super(setLoc);
            type=setType;
        }
        public Terrain clone() {
            Terrain clone=type.getInstance(loc);
            makePropertiesSame(clone);
            return clone;
        }
        public TerrainType getTerrainType() {
            return type;
        }
        public Type getType() {
            return getTerrainType().thingType;
        }
        public boolean isDead() {
            return loc.isError();
        }
        public void damage(double amt){}
        protected void update()
        {
            
        }
        protected void burn() {
            
        }
    }
}
