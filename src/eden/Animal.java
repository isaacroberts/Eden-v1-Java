/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.*;
import java.util.*;
import java.lang.Exception;

public class Animal extends Thing.Body implements Food
{
    static enum State {
        Flee,Defend,Hide,
        Attack,Stalk,
        Seek,
        Woo,
        Sleep,Wait
    }
    static enum Target {
        Meat,Fruit,Tree,
        Peers,Mate,
        Predator,Prey,Animal,
        Rock,
        
    }
    Vector velocity;
    float mass;
    int age;
    float health;
    float exhaustion;
    State state;
    double angle;
    float width,length;
    public Animal(Species type,Square startingLoc)
    {
        super(startingLoc);
        age=0;
        species=type;
        velocity=new Vector(0,0);
        state=State.Seek;
        if (type==null)
        {
            species=new Species();
            Species.phylums.add(species);
        }
        calories=(int)(species.maxEnergy()*.9);
        health=species.maxHealth();
        exhaustion=0;
        width=species.getWidth();
        length=species.getLength();
        mass=species.birthMass();
    }
    public Animal(Species type)
    {
        super();
        age=0;
        species=type;
        velocity=new Vector(0,0);
        state=State.Seek;
        if (type==null)
        {
            species=new Species();
            Species.phylums.add(species);
        }
        calories=(int)(species.maxEnergy()*.9);
        health=species.maxHealth();
        exhaustion=0;
        width=species.getWidth();
        length=species.getLength();
        mass=species.birthMass();
    }
    public Animal clone() 
    {
        Animal an=new Animal(species);
        an.makePropertiesSameAs(this);
        an.health=health;
        an.calories=calories;
        an.exhaustion=exhaustion;
        an.width=width;
        an.length=length;
        an.mass=mass;
        an.velocity=velocity;
        an.angle=angle;
        an.state=state;
        an.age=age;
        return an;
    }
    protected void burn()
    {
        health-=(heat-getFlashPoint())/4.0f;
        heat++;
    }
    protected void update()
    {
        if (calories<=0 || exhaustion>species.maxExhaustion())
        {
            state=State.Sleep;
            health-=40;
            calories=1;
        }
        if (calories<species.maxEnergy())
            calories-=2;
        if (state==State.Seek)
        {
//           forcesAutonomous();
        }
        else if (state==State.Sleep)
        {
            if (exhaustion>5)
                exhaustion-=5;
            else
            {
                exhaustion=0;
                if (calories<10)
                    calories=10;
                if (health<species.maxHealth()/2)
                    health+=3;
                state=State.Wait;
            }
        }
        calories--;
        if (exhaustion>20)
            exhaustion--;
        else if (health<species.maxHealth())
            health++;
    }
    public float getFlashPoint() 
    {
        return getType().flashPoint;
    }
    public float getCalories()
    {
        return calories+width*length/2;
    }
    public float height() {
        return 2;
    }
    public float density() {
        return 1500;
    }
    public float volume(){
        return height()*width*length;
    }
    public float groundArea() {
        return width*length;
    }
    /*
    public void forcesAutonomous()
    {
        Util.Coord resultant=loc.clone();
        double sqRange=species.sightRange()*species.sightRange();
        for (int n=0;n<Map.thingAmt();n++)
        {
            double dist=Util.sqrdDist(loc,Map.getThing(n).loc);
            
            if (dist<sqRange)
            {
                Util.intDubPair force=species.getAttraction(Map.getThing(n).getSubType());
                if (Map.getThing(n).getSubType().isOfTypes(species.edible))
                {
                    if (dist<=10+width+length)
                    {
                        consume((Food)Map.getThing(n));
                        resultant=loc.clone();
                        break;
                    }
                }
                if (force.doble!=0)
                {
                    dist=Math.sqrt(dist);
                    double vel= -force.doble / Math.pow(dist,force.integer);
//                    System.out.println("force="+force.doble+" pow = "+force.integer+" accl= "+deltaV);
                    resultant.x+= vel*(loc.x()-Map.getThing(n).loc.x())/dist;
                    resultant.y+= vel*(loc.y()-Map.getThing(n).loc.y())/dist;
                }
            }
        }
        if (Map.onMap(resultant))
            loc=resultant;
//        changeSpeed(Vector.getVector(loc,resultant));
//        velocity=resultant;
        move();
    } */
    public void changeSpeed(Vector changeTo)
    {
        changeTo=Vector.shrinkVector(changeTo,species.maxSpeed());
//        changeTo=Vector.confineAngle(changeTo, angle, species.maxTurning());
        Vector accel=new Vector(0,0);
        accel.xVel=velocity.xVel-changeTo.xVel;
        accel.yVel=velocity.yVel-changeTo.yVel;
//        accel=Vector.shrinkVector(accel,species.getAccel(state));
//        accel=Vector.confineAngle(accel, angle,species.maxTurning());
        double magn=accel.magnitude();
        exhaustion+=species.getEnergyExpdgForAccel(magn);
        calories-=species.getEnergyExpdgForAccel(magn);
        velocity.xVel+=accel.xVel;
        velocity.yVel+=accel.yVel;
        angle=velocity.angle();
    }
    public boolean move()
    {
        Square tomove=loc.transform((int)velocity.xVel,(int)velocity.yVel);
        if (Map.onMap(tomove.x(), tomove.y()))
        {
            Thing hit=tomove.occupant;
            if (hit==null)
            {
                loc=tomove;
                calories-= species.getEnergyExpdgForVel(velocity.magnitude());
                return true;
            }
            else
            {
                velocity.xVel=0;
                velocity.yVel=0;
                double magn=Math.pow(velocity.magnitude(),2);
                damage(magn);
                hit.damage(magn*mass);
                if (hit.isOfTypes(edible))
                {
                    consume((Food)hit);
                }
                return false;
            }
        }
        else
        {
            velocity.xVel=0;
            velocity.yVel=0;
            health-=velocity.magnitude()+10;
            return false;
        }
    }
    public void consume(Food toEat)
    {
        toEat.eat(10);
        calories+=10*species.efficiency();
        if (toEat.isRotten())
            health-=10;
        mass+=10;
    }
    public boolean isDead() {
        return health<=0 || loc.isError();
    }
    public void damage(double damage)
    {
        health-=damage;
    }
    public void eat(int massEaten)
    {
        health-=massEaten*Math.random()*5;
    }
    
    public void draw(Graphics2D g)
    {
        g.setColor(getColor());
        fillCircle(g);
        
        // { animal drawin only
//        g.rotate(angle+Math.PI/2,loc.x()+width/2,loc.y()+length/2);
//        g.setColor(species.getColor());
//        g.fillOval((int)loc.x()-width/2,(int)loc.y()-length/2,width,length);
//        g.fillOval((int)loc.x()-width/3,(int)(loc.y()-length*2.0/3.0),width,length/3);//head
//        g.rotate(-angle-Math.PI/2,loc.x(),loc.y());
        // }
        int drawWidth=Util.roundUp(width*Square.SQUARE_SIZE);
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        g.drawRect((int)loc.px(),(int)loc.py()-4,Square.SQUARE_SIZE,2);
        g.drawRect((int)loc.px(),(int)loc.py()-2,Square.SQUARE_SIZE,2);
        g.drawRect((int)loc.px(),(int)loc.py(),Square.SQUARE_SIZE,2);
        g.setColor(Color.GREEN);
        g.drawRect((int)loc.px(),(int)loc.py()-4,(int)(Square.SQUARE_SIZE*exhaustion/species.maxExhaustion()),2);
        g.setColor(Color.BLUE);
        g.fillRect((int)loc.px(),(int)loc.py()-2,(int)(Square.SQUARE_SIZE*calories/species.maxEnergy()),2);
        g.setColor(Color.RED);
        g.fillRect((int)loc.px(),(int)loc.py(),(int)(Square.SQUARE_SIZE*health/species.maxHealth()),2);
        if (state==State.Sleep)
        {
            g.setColor(Color.BLACK);
            g.drawString("Z",loc.px()-5,loc.py()-10);
        }
    }
    public Square getLoc() {
        return loc;
    }
    public Color getColor() {
        return species.getColor();
    }
    public float getHealth() {
        return health;
    }
    public boolean isRotten() {
        return false;
    }
    public Thing.Type getType() {
        return Thing.Type.Animal;
    }
    public SubType getSubType() {
        return species;
    }
    static final double energyPerAccelMassEff=2;
    static final double energyPerVelMassEff=1;
}
