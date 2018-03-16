/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.util.*;
import java.awt.*;

public class Util {
    private static int updates=1;
    public static boolean goEast(int xFrom,int xTo)
    {
        if (Earth.wraparound)
        {
            int dist=xTo-xFrom;
            if (Math.abs(dist)*2>Square.xAmt())
                dist*=-1;
            return dist>0;
        }
        else
        {
            return xFrom<xTo;
        }
    }
    public static int xVector(int x1,int x2)
    {
        if (Earth.wraparound)
        {
            int dist=x2-x1;
            int wrapDist=Square.xAmt()-Math.abs(dist);
            if (wrapDist<Math.abs(dist))
            {
                if (x1<x2)
                    return -wrapDist;
                else return wrapDist;
            }
            else return dist;
        }
        else return x2-x1;
    }
    public static int xDist(int x1,int x2)
    {
        int dist=Math.abs(x1-x2);
        if (Earth.wraparound)
        {
            int wrapDist=Square.xAmt()-dist;
            if (wrapDist<dist)
                return wrapDist;
            else return dist;
        }
        return dist;
    }
    public static double sqrdDist(Square p1,Square p2)
    {
        return Math.pow(xDist(p1.x(),p2.x()),2) + Math.pow(p1.y()-p2.y(),2); 
    }
    public static int gridDist(Square p1,Square p2)
    {
        return xDist(p1.x(),p2.x()) + Math.abs(p1.y()-p2.y()); 
    }
    public static double distance(Square p1,Square p2)
    {
        return Math.sqrt(sqrdDist(p1,p2));
    }
    public static Rectangle makeRect(int x1,int y1,int x2,int y2)
    {  //make a rectangle thats (lowest point, highest-lowest point)
        if (x2<x1)
        {
            int temp=x1;
            x1=x2;
            x2=temp;
        }
        if (y2<y1)
        {
            int temp=y1;
            y1=y2;
            y2=temp;
        }
        return new Rectangle(x1,y1,x2-x1,y2-y1);
    }
    public static Rectangle makeRect(int x1,int y1,int x2,int y2,int addWidth,int addLength)
    {  //make a rectangle thats (lowest point, highest-lowest point)
        if (x2<x1)
        {
            int temp=x1;
            x1=x2;
            x2=temp;
        }
        if (y2<y1)
        {
            int temp=y1;
            y1=y2;
            y2=temp;
        }
        return new Rectangle(x1,y1,x2-x1 +addWidth,y2-y1 +addLength);
    }
    public static boolean broken(float f)
    {
        return (Float.isInfinite(f)||Float.isNaN(f));
    }
    public static int roundUp(double d)
    {
        if (d%1.0>0)
            d+=1;
        return (int)d;
    }
    public static int min(int a,int b)
    {
        if (a>b)
            return b;
        else return a;
    }
    public static int max(int a,int b)
    {
        if (a>b)
            return a;
        else return b;
    }
    public static float min(float a,float b)
    {
        if (a>b)
            return b;
        else return a;
    }
    public static float max(float a,float b)
    {
        if (a>b)
            return a;
        else return b;
    }
    public static int colorBound(double a)
    {
        if (a>255)
            a=255;
        else if (a<0)
            a=0;
        return (int)a;
    }
    public static int colorBound(int a)
    {
        if (a>255)
            a=255;
        else if (a<0)
            a=0;
        return a;
    }
    public static Color makeValid(int red,int grn,int blu,int alpha)
    {
        return new Color(colorBound(red),colorBound(grn),colorBound(blu),colorBound(alpha));
    }
    public static Color makeValid(float red,float grn,float blu,float alpha)
    {
        return new Color(colorBound(red),colorBound(grn),colorBound(blu),colorBound(alpha));
    }
    public static Color makeValid(float r,float g,float b) {
        return makeValid((int)r,(int)g,(int)b);
    }
    public static Color makeValid(double r,double g,double b) {
        return makeValid((int)r,(int)g,(int)b);
    }
    public static Color makeValid(int red,int grn,int blu)
    {
        return new Color(colorBound(red),colorBound(grn),colorBound(blu));
    }
    public static float factorial(float n)
    {
        if (n>=100)
        {
            float pow= n/125 + n/5 + n/25 + n/625;
            return (float)Math.pow(10,pow);
        }
        if (n<=1)
            return 1;
        return n*factorial(n-1);
    }
    public static int update() {
        return updates;
    }
    public static boolean frequency(int n)
    {
        return frequency(n,0);
    }
    public static boolean frequency(int n,int offset)
    {
        int freq=(int)(((float)n)/Earth.TIME_STEP);
        if (freq<=0)
            freq=1;
        return (updates+offset)%freq==0;
    }
    public static boolean everyNth(int n)
    {
        return updates%n==0;
    }
    public static boolean everyNth(int n,int offset)
    {
        return (updates+offset)%n==0;
    }
    public static void onFrame() {
        updates++;
    }
    public static class intDubPair {
        int integer;
        double doble;
        public intDubPair(int intt,double duble) {
            integer=intt;
            doble=duble;
        }
    }
}
