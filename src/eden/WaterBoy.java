/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.Color;
import java.util.ArrayList;

public class WaterBoy extends Thing.Body
{
    public static final float waterTakeRate=Earth.MOISTURE_PER_CUBIC_METER*200;
    public static final float maxWater=Earth.MOISTURE_PER_CUBIC_METER*1000;
    public static final float waterDispRate=Earth.MOISTURE_PER_CUBIC_METER*10;
    float water;
    boolean dispensing;
    boolean goingUp;
    public WaterBoy() {
        super();
        species=Species.error;
        water=0;
        dispensing=false;
        goingUp=false;
    }
    public WaterBoy(Square setloc)
    {
        super(setloc);
        species=Species.error;
        water=0;
        dispensing=false;
        goingUp=false;
    }
    public Thing.Type getType() {
        return Thing.Type.WaterBoy;
    }
    public WaterBoy clone() {
        WaterBoy replacement=new WaterBoy();
        replacement.makePropertiesSameAs(this);
        replacement.water=water;
        replacement.dispensing=dispensing;
        replacement.goingUp=goingUp;
        return replacement;
    }
    public Color getColor() {
        if (dispensing) return Color.yellow;
        else return getType().color;
    }
    protected void update()
    {
        if (water>maxWater)
        {
            dispensing=true;
        }
        if (dispensing)
        {
            {
                if (!loc.terrain().underwater())
                {
                    water-=waterDispRate;
                    loc.terrain().moisture+=waterDispRate;
                    if (water<0)
                    {
                        dispensing=false;
                        loc.terrain().moisture-=water;
                        water=0;
                    }
                }
                else {
                    takeWater();
                }
                if (Util.everyNth(2,id))
                {
                    setLoc(loc.transform(Dir.West));
                }
                else if (goingUp) 
                {
                    if (loc.y()==0)
                    {
                        goingUp=false;
                    }
                    else
                        setLoc(loc.transform(Dir.North));
                }
                else
                {
                    if (loc.y()==Square.yAmt()-1)
                        goingUp=true;
                    else
                    {
                        setLoc(loc.transform(Dir.South));
                    }
                }
            }
        }
        else
        {
            if (loc.terrain().underwater())
            {
                takeWater(loc,waterTakeRate);
            }
            else
            {
                Square nrstWater=Map.getNearestWater(loc,Earth.MOISTURE_PER_CUBIC_METER*5);
                if (!nrstWater.isError())
                {
                    Square move=loc.transform(loc.getDir(nrstWater));
                    setLoc(move);
                }
                else
                {
                    setLoc(loc.getRandNeighbor());
                }
            }
        }
    }
    private void takeWater()  {
        takeWater(loc,waterTakeRate);
    }
    private void takeWater(Square from) {
        takeWater(from,waterTakeRate);
    } 
    private void takeWater(Square from,float amt) 
    {
        if (from.terrain().getType()==Thing.Type.Spring)
        {
            water+=from.terrain().moisture;
            from.terrain().moisture=0;
        }
        else if (from.terrain().moisture<amt)
        {
//            water+=from.terrain().waterAbove();
//            from.terrain().moisture=from.terrain().getWaterSaturation();
            water+=from.terrain().moisture;
            from.terrain().moisture=0;
        }
        else
        {
            water+=amt;
            from.terrain().moisture-=amt;
        }
    }
    public void damage(double amt) {}
    protected void burn() {}
    public void eat(int amt) {
        damage(amt);
    }
    
    public float height() {
        return 1.5f;
    }
    public float volume() {
        return height();
    }
    public float getCalories() {
        return 2;
    }
    public boolean isRotten() {
        return false;
    }
}
