/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;


public class Atmosphere extends Gas
{
    protected float humidity;
    protected float clouds;
    protected float rainRate;
    protected Vector windSpeed;
    public Atmosphere(Square setLoc) {
        super(setLoc);
//        setVolume(10);
        humidity=Map.humidity();
        rainRate=0;
        clouds=0;
        windSpeed=new Vector(0,0);
    }
    public Atmosphere() {
        super();
        humidity=0;
        rainRate=0;
        clouds=0;
        windSpeed=new Vector(0,0);
    }
    public boolean raining() {
        return rainRate >0;
    }
    public float rainPoint()
    {
        float point=0;
        point-=loc.terrain().elevation/10000f;
        point+=(loc.terrain().heat-270)/5000f;
        if (loc.terrain().underwater()) point-=.02;
        return point;
    }
    protected void accumulateClouds() 
    {
        float rate=0;
//        rate+=.000001f*humidity;
//        if (loc.terrain().underwater()) rate+=.0000005f;
//        if (clouds>0)
//            rate*=humidity/clouds;
//        else
//            rate+=.1;
        if (rate>0)
        {
            if (humidity>rate)
            {
                humidity-=rate;
                clouds+=rate;
            }
            else
            {
                clouds+=humidity;
                humidity=0;
            }
        }
    }
    protected void setRainRate()
    {
        rainRate=clouds*.01f;
    }
    public void update() {
    }
    public void sendWind(Atmosphere toWho,float airMovement)
    {
        float heatDif=heat*airMovement+toWho.heat*toWho.mass();
        heatDif/=airMovement+toWho.mass();
        heatDif-=toWho.heat;
        toWho.giveHeat(heatDif);
        
        Vector difference=new Vector(0,0);
        difference.xVel=windSpeed.xVel*(airMovement)+toWho.windSpeed.xVel*toWho.mass();
        difference.yVel=windSpeed.yVel*(airMovement)+toWho.windSpeed.yVel*toWho.mass();
        difference.xVel/=airMovement+toWho.mass();
        difference.yVel/=airMovement+toWho.mass();
        difference.xVel-=toWho.windSpeed.xVel;
        difference.yVel-=toWho.windSpeed.yVel;
//        difference=difference.scale(.5);
        toWho.giveWind(difference);
        
        if (mass()>0)
        {
            if ( airMovement>mass())
            {
                airMovement=mass()-.01f;
            }
            float deltaHumid=humidity* airMovement/mass;
            giveHumidity(-deltaHumid);
            toWho.giveHumidity(deltaHumid);

            giveMass(-airMovement);
            toWho.giveMass(airMovement);
        }
        else
            System.out.println("Mass = "+mass);
    }
    static int amtAlarms=0;
    public void check()
    {
        if (heat<0)
            heat=0;
        if (mass<.01f)
        {
            mass=.01f;
            amtAlarms++;
            if (amtAlarms==1)
            {
                Eden.pause();
            }
        }
        if (humidity<0)
        {
            humidity=0;
        }
//        if (heat>100000000.)
//        {
//            
//        }
//        if (Earth.TIME_STEP/(density()*heat)>4)
//        {
//            System.out.println("∆t/(dens*Temp)="+(Earth.TIME_STEP/(density()*heat))+" -Courant and Levy will have their revenge!");
//        }
    }
    public void checkForWindAgainstWall()
    {
        if (loc.transform(Dir.getDir(0,windSpeed.yVel)).isError())
        {
            windSpeed.yVel=0;
        }
    }
    public void combine(Atmosphere[] combine)
    {
        prepareForAveraging(combine.length);
        for (int n=0;n<combine.length;n++)
        {
            mass+=combine[n].mass();
            heat+=combine[n].heat;
            humidity+=combine[n].humidity;
            windSpeed=windSpeed.add(combine[n].windSpeed);
            clouds+=combine[n].clouds;
            rainRate+=combine[n].rainRate;
        }
        heat/=combine.length;
        windSpeed=windSpeed.scale(1f/combine.length);
        rainRate/=combine.length;
    }
    private void prepareForAveraging(int avgerAmt)
    {
        heat*=avgerAmt;
        rainRate*=avgerAmt;
        windSpeed=windSpeed.scale(avgerAmt);
    }
    public float humidity()    {
        return humidity;
    }
    public Vector wind()     {
        return windSpeed;
    }
    public Vector windSpeed()     {
        return windSpeed;
    }
    public void changeHumidity(float delta) {
        humidity+=delta;
    }
    public void setHumidity(float set) {
        humidity=set;
    }
    public void setRain(float set) {
        rainRate=set;
    }
    public void setClouds(float set) {
        clouds=set;
    }
    public void setWind(Vector set) {
        windSpeed=set;
    }
    public void giveHumidity(float amt)
    {
        humidity+=amt;
    }
    public void giveMass(float amt)
    {
        mass+=amt;
    }
    public void giveHeat(float amt)
    {
        heat+=amt;
    }
    public void giveWind(Vector add)
    {
        windSpeed=windSpeed.add(add);
    }
    
    
}
