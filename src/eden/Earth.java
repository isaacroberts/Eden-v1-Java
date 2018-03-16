/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.*;

public class Earth {
    //general
    final static boolean wraparound=true;
    final public static float TIME_STEP=.5f;
    final static float gravity=98f;
    //the sun
    public static final float heatFromSun=.3f;
    public static final float HEAT_LOSS_TO_SPACE_FACTOR=.000451f;
    public static int timeOfDay=0;
    public final static int dayLength=1000;
    public static float timeOfDaySunFactor;
    public static float lattitudeHeats[];
    public static void rotateEarth()
    {
        timeOfDay++;
        if (timeOfDay>=dayLength)
            timeOfDay=0;
        timeOfDaySunFactor=(float)(1.-Math.cos(2*Math.PI*(double)timeOfDay/(double)dayLength));
    }
    public static void setupEarthAngles()
    {
        lattitudeHeats=new float[Square.yAmt()];
        
    }
    public static Color timeOfDayColor()
    {
        float grey=Util.colorBound(timeOfDaySunFactor*125);
        return new Color((int)(grey*.9f),(int)grey,(int)(grey*.75f));
    }
    public static float getHeatFromSun() {
        return heatFromSun*timeOfDaySunFactor*TIME_STEP;
    }
    public static float getHeatLossToSpaceRate() {
        return HEAT_LOSS_TO_SPACE_FACTOR*TIME_STEP;
    }
    public static void nightShade(Graphics2D g)
    {
        if (Map.simpleDraw)
        {
            if (timeOfDaySunFactor<.75)
            {
                g.setColor(new Color(0,0,50,70));
                g.fillRect(Map.minX, Map.minY, Map.maxX, Map.maxY);
            }
            else if (timeOfDaySunFactor<.85)
            {
                int nightAlpha=Util.colorBound((.85-timeOfDaySunFactor)*700);
                g.setColor(new Color(0,0,50,nightAlpha));
                g.fillRect(Map.minX, Map.minY, Map.maxX, Map.maxY);
            }
        }
        else
        {
            int nightAlpha=Util.colorBound((2-timeOfDaySunFactor)*40);
            g.setColor(new Color(0,0,50,nightAlpha));
            g.fillRect(Map.minX, Map.minY, Map.maxX, Map.maxY);
        }
    }
    //water
    public final static float freezePoint=273;
    public final static float waterDensity=1000;//kg per m^3
    public final static float MOISTURE_PER_CUBIC_METER=10000;
    public final static float waterSunMod=Terrain.TerrainType.applySunReceptivityVariance(.6f);
    public final static float iceSunMod = Terrain.TerrainType.applySunReceptivityVariance(.2f);
    public final static float WATER_SPECIFIC_HEAT=1100f/MOISTURE_PER_CUBIC_METER;
    public static float getEvap(float heat,float moisture,float surfaceArea,Air air) {
        float evap;
        float vapPres=(float)Math.pow(2.71,20-5000/heat);
        evap=(vapPres-air.humidity())/((float)Math.sqrt(heat)) * surfaceArea;
        evap*=TIME_STEP;
//        evap/=5;
        if (evap>moisture)
            evap=moisture;
        return evap;
    }
    public static Color waterColor(float depth,float heat) {
        return waterColor(depth,heat,false);
    }
    public static Color waterColor(float depth,float heat,boolean runningWater) {
        if (heat<freezePoint)
            return iceColor(depth,heat);
        else return waterColor(depth,runningWater);
    }
    public static int waterAlpha(float depth) {
        if (Map.simpleDraw)
        {
            if (depth>8.5f)
                return 255;
//            else if (depth>5)
//                return 200;
//            else if (depth>3)
//                return 150;
            else if (depth>1)
                return (int)(depth*30);
//                return 60;
//            else if (depth>.2)
//                return 15;
            else return 0;
        }
        else
            return Util.colorBound(depth*20f);
    }
//    public static Color waterColor(float depth,boolean runningWater) {
//        return new Color(0,0,200,waterAlpha(depth));
//    }
    public static Color waterColor(float depth,boolean runningWater) {
        if (runningWater)
            return new Color(10,10,150,waterAlpha(depth));
        else
            return new Color(0,0,200,waterAlpha(depth));
    }
    public static Color iceColor(float depth,float heat) {
        if (Map.simpleDraw)
        {
            return new Color(240,240,250,waterAlpha(depth));
        }
        else
        {
            int cold=(int)(freezePoint-heat +10)*5;
            if (cold>150)
                cold=150;
            return Util.makeValid(50+cold,50+cold,220+cold/5,(depth*10));
        }
    }
}
