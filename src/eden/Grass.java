/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import static eden.Grass.moistureToHeightRatio;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 *
 * @author Isaac
 */
public class Grass extends Terrain implements Food
{
    static final float moistureToHeightRatio=1f;
    final static int growthFreq=Util.roundUp(12f/Earth.TIME_STEP),reproduceFreq=Util.roundUp(63f/Earth.TIME_STEP);
//    final static int growthFreq=1,reproduceFreq=5;
    int height;
    public Grass()
    {
        super();
        height=2;
    }
    public Grass(Square loc,int setWidth,int setLength) {
        super(loc);
        height=2;
        Map.setTerrain(this, loc,setWidth, setLength);
    }
    public Grass (Square setLoc)
    {
        super(setLoc);
        height=2;
    }
    public Grass (Square setLoc,int setheight)
    {
        super(setLoc);
        height=setheight;
    }
    public Grass clone()
    {
        Grass clone=new Grass(loc);
        makePropertiesSame(clone);
        return clone;
    }
    public void paint(Graphics2D g)
    {
        g.setColor(getColor());
        fillSquare(g);
    }
    public Color getColor()
    {
        if (burning())
        {
            return (new Color(200,100,10));
        }
        else if (Map.simpleDraw)
        {
            if (height>11)
                return new Color(0,100,0);
            else if (height>8)
                return new Color(32,94,6);//dirt color avg with grass at 1D:3G ratio
            else if (height>7)
                return new Color(43,91,8);//dirt color avged with grass color at 1D:2G ratio
            else if (height>5)
                return new Color(65,88,12);//dirt color avgd with grass 1:1
            else if (height>=3)
                return new Color(98,81,19);//dirt color avgd with grass 3D:1G
            else if (height>=2)
                return new Color(110,79,21);//dirt color avg ratio 255D:40G
            else if (height>=1)
                return new Color(120,77,23);//dirt color avg with grass 255D:20G
            else return Thing.Type.Dirt.color;
        }
        else
        {
            float grassWeight=height*20f/255f;
            if (grassWeight>=1)
                return Thing.Type.Grass.color;
            else if (grassWeight<=0)
                return Thing.Type.Dirt.color;
            float dirtWeight=1-grassWeight;
            return Util.makeValid(Thing.Type.Dirt.color.getRed()*dirtWeight,
                    Thing.Type.Grass.color.getGreen()*grassWeight+Thing.Type.Dirt.color.getGreen()*dirtWeight,
                    Thing.Type.Dirt.color.getBlue()*dirtWeight);
        }
    }
    public Terrain.TerrainType getTerrainType() {
        return Terrain.TerrainType.Grass;
    }
    public boolean isRotten() {
        return false;
    }
    public double getSpecificHeat() {
        return getType().specificHeat*(height>0?height:0)+moisture*Earth.WATER_SPECIFIC_HEAT+1;
    }
    public float getSurfaceArea() {
        return (height>.5f?1.5f:1);
    }
    public void damage(double damage){
        if (damage>2)
            changeHeight(-1);
    }
//        private void calcHash() {//returns a number for a Util.everyNth offset. to make sure there isnt update lines going across the screen
//            hash=(int)(Math.random()*(growthFreq*reproduceFreq));
//        }
    private int getHash()
    {
        return loc.hash;
    }
    public void putUnderground() {
        moisture+=height*moistureToHeightRatio;
        heat+=height/3;
        height=0;
        loc.replaceTerrain();
        kill();
    }
    public void kill()
    {
        loc.replaceTerrain();
        heat+=height/3;
        moisture+=height*moistureToHeightRatio;
        height=0;
        super.kill();
    }
    public void attemptGrow(int growth,float otherwiseReduction)
    {
        if (getMoisture(growth*moistureToHeightRatio))
        {
            height+=growth;
            heat-=growth/3;
        }
        else
        {
            if (otherwiseReduction<1)
                if (Util.frequency((int)(-1f/otherwiseReduction),getHash()))
                    changeHeight(-1);
            changeHeight((int)otherwiseReduction);
        }
    }
    public void changeHeight(int amt)
    {
        if (height<-amt)
        {
            if (height>0)
            {
                amt=-height;
                kill();
            }
            else {
                amt=0;
            }
        }
        else
        {
            height+=amt;
        }
        moisture-=amt*moistureToHeightRatio;
        if (moisture<0)
        {
            height-=(int)(moisture/moistureToHeightRatio);
            heat+=(moisture/moistureToHeightRatio)*3;
            moisture=0;
        }
        heat-=amt*3;
    }
    public boolean timeToGrow() {
        if (height>=15)
            return false;
        return Util.everyNth(reproduceFreq,getHash());
//            return growthCounter-(hash/reproduceFreq)==0;
    }
    public boolean timeToReproduce() {
        if (height<=10)
            return false;
        return Util.everyNth(growthFreq,getHash());
//            return reproduceCounter-(hash/growthFreq)==0;
    }
    public String otherInfo() {
        return "Height: "+height;
    }
    protected void burn()
    {
        if (Util.everyNth(2))
            changeHeight(-1);
    }
    protected void update()
    {
        if (saturated())
        {
            if (metersOfWaterAbove()>2)
            {
                if (Util.frequency(51))
                {
                    if (height>-1)
                        changeHeight(-1);
                }
            }
        }
        else if (timeToGrow())//growth
        {
            attemptGrow(1,-.25f);
        }
        if (timeToReproduce())//reproduction
        {
            Random rand=new Random();
            Square toGrow=Square.get(loc.x()+rand.nextInt(3)-1,loc.y()+rand.nextInt(3)-1);
            if (toGrow.terrain().getType()==Thing.Type.Dirt)
            {
                if (getMoisture(4))
                {
                    Grass grass=new Grass(toGrow);
                    grass.height=1;
                    toGrow.replaceTerrain(grass);
                    changeHeight(-1);
                }
            }
            //else reduceHeight(2);
        }
    }
    public void eat(int mass)
    {
        changeHeight(-mass);
    }
    public float getCalories() {
        return height/3;
    }
    public boolean isDead() {
        return height<=0;
    }
    public Thing.Type getType() {
        return Thing.Type.Grass;
    }
    public SubType getSubType()
    {
        return getTerrainType();
    }
}
