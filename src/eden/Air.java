/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;
import java.awt.*;
import java.util.ArrayList;
public class Air extends Atmosphere
{
    private Atmosphere newState;
    public Air(Square setLoc)
    {
        super(setLoc);
        newState=new Atmosphere();
        loc.air=this;
    }
    public void update()
    {
        if (heat<.5f)
        {
            heat=.5f;
        }
        if (mass<.01f)
            mass=.01f;
        if (Float.isNaN(heat))
        {
//            System.out.println("Error: atmosphere DNE at "+loc.toString());
//            System.out.println("moisture="+humidity+" density()="+density());
            heat=290;
            loc.occupant=new Thing.ErrorSlag(loc);
            loc.occupant.heat=heat;
            loc.replaceTerrain(new Terrain.Glass(loc));
            loc.terrain().heat=heat;
            return;
        }
        accumulateClouds();
        if (raining())
        {
            if (clouds>rainRate)
            {
                clouds-=rainRate;
                loc.terrain().moisture+=rainRate;
            }
            else
            {
                loc.terrain().moisture+=clouds;
                clouds=0;
                rainRate=0;
            }
        }
        else
        {
            if (clouds>rainPoint())
                setRainRate();
        }
        ArrayList<Square> nayb=loc.getNeighbors();
        Atmosphere[] sections=new Atmosphere[nayb.size()];
        for (int n=0;n<sections.length;n++)
        {
            sections[n]=new Atmosphere();
            sections[n].mass=mass()/sections.length;
            sections[n].heat=heat;
            sections[n].humidity=humidity/sections.length;
            sections[n].windSpeed=windSpeed.scale(.5f);//wind momentum
            sections[n].clouds=clouds/sections.length;
            sections[n].rainRate=rainRate;
            sections[n].setVolume(volume()/sections.length);
        }
        for (int n=0;n<nayb.size();n++)
        {
        //        windblow= (heat*density() - nayb.heat*nayb.density())*delta time/(heat of particles transferring)
            float blow=0;
            blow=(sections[n].pressure()- nayb.get(n).air.pressure());
            if (blow>0)
                blow/=sections[n].heat();
            else
                blow/=nayb.get(n).air.heat();
            blow*=Earth.TIME_STEP;
            blow/=5;    //blow=/4 has vibrating wind
            //add the wind speed in the right directions
            sections[n].windSpeed=sections[n].windSpeed.add(Vector.getScaledVector(loc, nayb.get(n), blow));
            
            //equalize heat a bit by diffusion
            Dir toNayb=loc.getDir(nayb.get(n));
            Square nextNayb=nayb.get(n).transform(toNayb);
            float deltaHeat=-heat*.5f+ nayb.get(n).air.heat*.3f+ nextNayb.air.heat*.1f;
            nextNayb=nayb.get(n).transform(toNayb.rotate(true));
            deltaHeat+=nextNayb.air.heat*.1f;
            sections[n].heat+=deltaHeat;
            
            float deltaHumid=sections[n].humidity*.1f;
            sections[n].humidity-=deltaHumid;
            nayb.get(n).air.giveHumidity(deltaHumid);
            
        }
        //calculate stuff as result of wind blow
        for (int n=0;n<nayb.size();n++)
        {
            float blow;
            if (nayb.get(n).getDir(loc).x()==0)
            {
                blow=sections[n].windSpeed.yVel;
                if (nayb.get(n).y()<loc.y())
                {
                    blow*=-1; 
                }
            }
            else
            {
                blow=sections[n].windSpeed.xVel;
                if (Util.goEast(nayb.get(n).x(),loc.x()))
                {
                    blow*=-1;
                }
            }
            if (blow>=sections[n].mass)
            {
//                System.out.println(loc.toString()+" blows so hard");
                blow=sections[n].mass()*.9f;
            }
            else if (blow<=-nayb.get(n).air.mass())
            {
//                System.out.println(loc.toString()+" sucks so hard");
                blow=nayb.get(n).air.mass()*-.9f;
            }
            //transfers density(), momentum(wind), heat and humidity of the air transferred
            if (blow>0)
                sendWind(nayb.get(n).air,blow);
            else if (blow<0)
                nayb.get(n).air.sendWind(this,-blow);
        }
        //combine sections into newState
        if (sections.length>0)
        {
            newState.combine(sections);
        }
        newState.loseHeat();
//        checkForWindAgainstWall();
        check();
    }
    public void sendWind(Air toWho,float airMovement)
    {
        float avgHeat=heat*airMovement+toWho.heat*toWho.mass();
        avgHeat/=airMovement+toWho.mass();
        toWho.heat=avgHeat;
        
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
        {
            System.out.println("Mass = "+mass);
        }
    }
    public float height() {
        return loc.terrain().height();
    }
    public void endStep()
    {
        newState.check();
        heat=newState.heat;
        mass=newState.mass;
        humidity=newState.humidity;
        rainRate=newState.rainRate;
        windSpeed=newState.windSpeed;
        clouds=newState.clouds;
        clearNewState();
    }
    private void clearNewState()
    {
//        newState=new Air();
        newState.mass=0;
        newState.heat=0;
        newState.humidity=0;
        newState.rainRate=0;
        newState.clouds=0;
        newState.windSpeed=new Vector(0,0);
    }
    public void giveHumidity(float amt)
    {
        newState.humidity+=amt;
    }
    public void giveMass(float amt)
    {
        newState.mass+=amt;
    }
    public void giveHeat(float amt)
    {
        newState.heat+=amt;
    }
    public void giveWind(Vector add)
    {
        newState.windSpeed=newState.windSpeed.add(add);
    }
    public void setWind(Vector set) {
        windSpeed=set;
        newState.windSpeed=set;
    }
    public void setMass(float set) {
        mass=set;
        newState.mass=set;
    }
    public Atmosphere newState() {
        return newState;
    }

    public void draw(Graphics2D g)
    {
        if (raining())
        {
            g.setColor(new Color(0,0,150,Util.colorBound(rainRate*2000)));
            g.fillRect(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
        }
    }
    public void drawHumidity(Graphics2D g)
    {
        if (Util.broken(humidity()))
            g.setColor(new Color(0,255,0));
        else
            g.setColor(Util.makeValid(0,0,humidity()*5f));
        g.fillRect(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
        if (clouds>0)
        {
            g.setColor(Util.makeValid(255,255,255,clouds/rainPoint()*255));
            g.fillOval(loc.px()+Square.HALF_SQUARE/2,loc.py()+Square.HALF_SQUARE/2,Square.HALF_SQUARE,Square.HALF_SQUARE);
        }
        if (raining())
        {
            g.setColor(new Color(0,0,0,Util.colorBound(rainRate*100)));
//            g.setStroke(new BasicStroke(2));
            g.fillOval(loc.px()+Square.HALF_SQUARE,loc.py()+Square.HALF_SQUARE,Square.SQUARE_SIZE/4,Square.SQUARE_SIZE/4);
        }
    }
    public void drawHeat(Graphics2D g)
    {
//        float totalHeat=0;
//        float totalMass=.001f;
//        for (int n=0;n<sky.size();n++)
//        {
//            totalHeat+=sky.get(n).heat()*sky.get(n).mass();
//            totalMass+=sky.get(n).mass();
//        }
//        float drawHeat=totalHeat/totalMass;
        g.setColor(heatColor(heat));
        g.fillRect(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
//        g.setColor(Color.BLACK);
//        g.setFont(new Font("Arial",Font.PLAIN,Square.SQUARE_SIZE-2));
//        g.drawString((int)(heat-273)+"",loc.px(),loc.py()+Square.SQUARE_SIZE);
    }
    public static Color heatColor(float heat)
    {
        if (Util.broken(heat))
            return (new Color(0,255,0));
        else
            return (Util.makeValid((heat-270)*3,0,125-(heat-Map.worldHeat)*10));
    }
    public static Color densColor(float dens) 
    {
        if (Util.broken(dens))
            return (new Color(0,255,0));
        else if (dens>=0)
        {
            int turq=Util.colorBound(dens*100f);
            return (new Color(0,turq,turq));
        }
        else
            return (new Color(255,0,0));
    }
    public void drawDensity(Graphics2D g)
    {
//        float totalMass=0;
//        float totalVolume=.001f;
//        for (int n=0;n<sky.size();n++)
//        {
//            totalMass+=sky.get(n).mass();
//            totalVolume+=sky.get(n).tallness();
//        }
        g.setColor(densColor(density()));
        g.fillRect(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
        
    }
    public void drawWind(Graphics2D g)
    {
        float magn=(float)(wind().magnitude()*1000000f);
        int midX=loc.px()+Square.HALF_SQUARE,midY=loc.py()+Square.HALF_SQUARE;
        if (Util.broken(magn))
        {
            g.setColor(new Color(0,255,0));
            g.fillRect(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
        }
        else
        {
            g.setColor(Util.makeValid(magn/2,0,magn-Math.pow(magn,2)/1000));
            g.fillRect(loc.px(),loc.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE);
            if (!wind().isZero())
            {
                Vector arrow=Vector.getScaledVector(wind(),Square.HALF_SQUARE);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawLine(midX,midY,midX+(int)arrow.xVel,midY+(int)arrow.yVel);
            }
        }
        g.setColor(Color.BLACK);
        g.fillOval(midX-1,midY-1,3,3);
    }
    public boolean addMoisture(float amt)
    {
        if (Util.broken(amt))
        {
//            System.out.println("tried to add NaN moisture : "+amt);
//            (new Exception("Adding NaN Moisture")).printStackTrace();
            return false;
        }
        else if (humidity> -amt)
        {
            giveHumidity(amt);
            return true;
        }
        else 
        {
            giveHumidity(-humidity);
            return false;
        }
    }
}
