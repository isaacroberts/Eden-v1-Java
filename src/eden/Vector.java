/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

/**
 *
 * @author Isaac
 */
public class Vector {
    public float xVel,yVel;
    public Vector(float x,float y) {
        xVel=x;
        yVel=y;
    }
    public Vector(double x,double y) {
        xVel=(float)x;
        yVel=(float)y;
    }
    public Vector rotate(double radian)
    {
        Vector ret=new Vector(xVel,yVel);
        ret.xVel = (float)((xVel * Math.cos(radian)) - (yVel * Math.sin(radian)));
        ret.yVel =(float) (((yVel * Math.cos(radian)) + (xVel * Math.sin(radian))));
        return ret;
    }
    public double sqrdMagnitude() {
        return xVel*xVel + yVel*yVel;
    }
    public double magnitude() {
        return Math.sqrt((xVel*xVel + yVel*yVel));
    }
    public double angle() {
        if (xVel==0)
        {
            if (yVel==0)
                return 0;
            return Math.PI/2;
        }
        return Math.atan(yVel/xVel);
    }
    public boolean isZero() {
        return xVel==0 && yVel==0;
    }
    public Vector perpendicular()
    {
        Vector perp=new Vector(-xVel,yVel);
        return perp;
    }
    public Vector scale(double factorOf) {
        return new Vector(xVel*factorOf,yVel*factorOf);
    }
    public Vector add(Vector other) {
        return new Vector(xVel+other.xVel,yVel+other.yVel);
    }
    public Dir toDir() {
        if (xVel==0 && yVel==0)
        {
            return Dir.North;
        }
        else if (Math.abs(xVel)>Math.abs(yVel))
        {
            if (xVel>0)
                return Dir.East;
            else return Dir.West;
        }
        else
        {
            if (yVel>0)
                return Dir.South;
            else return Dir.North;
        }
    }
    public Dir xDir() {
        if (xVel>0)
            return Dir.East;
        else return Dir.West;
    }
    public Dir yDir() {
        if (yVel>0)
            return Dir.South;
        else return Dir.North;
    }
    public String toString() {
        return "xVel="+(float)xVel+",yVel="+(float)yVel;
    }
    public static Vector getVector(final Square from,final Square to)
    {
        return new Vector(Util.xVector(from.x(),to.x()),to.y()-from.y());
    }
    public static Vector getScaledVector(final Square from,final Square to,double scale)
    {
        Vector ret=getVector(from,to);
        return getScaledVector(ret,scale);
    }
    public static Vector getSquareScaledVector(final Vector orig, double scale)
    {
        double factor;
        if (Math.abs(orig.xVel)>Math.abs(orig.yVel))
        {
            factor=scale/orig.xVel;
        }
        else 
            factor=scale/orig.yVel;
        return new Vector(orig.xVel*factor,orig.yVel*factor);
    }
    public static Vector getScaledVector(final Vector orig,double scale)
    {
        Vector ret=new Vector(orig.xVel,orig.yVel);
        double factr=scale/ret.magnitude();
        ret.xVel*=factr;
        ret.yVel*=factr;
        return ret;
    }
    public static Vector shrinkVector(final Vector shrnk,double scale)
    {
        if (scale==0)
            return new Vector(0,0);
        double magn=shrnk.sqrdMagnitude();
        if (magn<=scale*scale)
            return shrnk;
        else
        {
            Vector ret=new Vector(shrnk.xVel,shrnk.yVel);
            double factr=scale/Math.sqrt(magn);
            ret.xVel*=factr;
            ret.yVel*=factr;
            return ret;
        }
    }
    public static Vector confineAngle(final Vector change,double origAngle,double within)
    {
        if (change.xVel==0 && change.yVel==0)
            return change;
        double angle=change.angle();
        if (Math.abs(origAngle-angle)<=within)
            return change;
        if (angle<origAngle)
            angle=origAngle-within;
        else
            angle=origAngle+within;
        double magn=change.magnitude();
        return new Vector(Math.cos(angle)*magn, Math.sin(angle)*magn);
    }
    public Vector clone() {
        return new Vector(xVel,yVel);
    }
}
