/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.*;
import java.util.*;

public class Map {
    static final boolean organicWaterways=true;
    
    static int minX,minY;
    static int maxX,maxY;
    static float worldHeat;//in Kelvin
    public static float moisture;
    static boolean raining;
    public static float avgElev=100;
    
//    static ArrayList<Float> heatGraph=new ArrayList<Float>();
//    static ArrayList<Vector> desertSizeGraph=new ArrayList<Vector>();
    static boolean simpleDraw=false;
    public static void onStart(final LoadingScreen screen,Thread mainThread)
    {
        minX=20;
        minY=40;
        maxX=1460;
        maxY=900;
        worldHeat=270+(float)(Math.random()*40);
        moisture=5* (maxY-minY)*(maxX-minX)/(Square.SQUARE_SIZE*Square.SQUARE_SIZE);
        Square.onStart(minX,minY,maxX,maxY);
        System.out.println("Square amt="+Square.amt());
        int width=(maxX-minX)/Square.SQUARE_SIZE, length=(maxY-minY)/Square.SQUARE_SIZE;
//        Tree treeOfLife=new Tree(
//                Square.getSquare(width/2,length/2),Tree.Fruit.FruitType.Apple);
//        treeOfLife.height=400;
//        treeOfLife.addFruit(new Tree.Fruit(Tree.Fruit.FruitType.Apple,treeOfLife.loc));
        
        screen.current=LoadingScreen.WorkingOn.Desert;
        
        screen.percentage=0;
        int amt=(int)(Math.random()*(double)Square.amt()/3000.);
        for (int n=0;n<amt;n++)
        {
            screen.percentage=(float)(n)/(float)(amt);
            genTerrain(Square.getRandSquare(),new Terrain.Sand(),5,.1f);
        }
        
        Random rand=new Random();
        screen.current=LoadingScreen.WorkingOn.Placing;
        
        Thing.Type[] growable={Thing.Type.Grass,Thing.Type.Dirt};
        int squareAmt=Square.amt();
        int rockAmt=rand.nextInt((squareAmt/143));
        int treeAmt=rand.nextInt((squareAmt/32))+rockAmt;
        int meatAmt=rand.nextInt((squareAmt/85));
//        int sailAmt=rand.nextInt(squareAmt/286)+meatAmt;
//        int fanAmt=rand.nextInt(squareAmt/200);
        amt=meatAmt+treeAmt;
        for (int n=0;n<amt;n++)
        {
            screen.percentage=(double)n/(double)amt;
            Square loc=Square.getRandSquare();
            if (n<rockAmt)
                new Thing.Rock(loc,rand.nextInt(4),rand.nextInt(4));
            else if (n<treeAmt)
            {
                if (loc.terrain().isOfTypes(growable))
                {
                    Tree.Fruit.FruitType fruitType=Tree.Fruit.FruitType.values()[rand.nextInt(Tree.Fruit.FruitType.values().length)];
                    Tree add=new Tree(loc,fruitType);
                    int height=rand.nextInt(10);
                    add.setHeight(rand.nextInt(10));
                    add.roots.durability=10;
                    for (int h=0;h<height;h+=2)
                    {
                        add.roots.grow();
                        add.roots.durability+=10;
                    }
                }
            }
            else// if (n<meatAmt)
            {
                new Thing.Carcass(loc,null);
            }
        }
        new WaterBoy(Square.getRandSquare());
        new WaterBoy(Square.getRandSquare());
        new WaterBoy(Square.getRandSquare());
        
        screen.current=LoadingScreen.WorkingOn.Mountain;
        int peakAmt=(int)(((width*length)/20) * (Math.random()+.5));
//        peakAmt=0;
        for (int n=0;n<peakAmt;n++)
        {
            screen.percentage=(double)n/(double)peakAmt;
            float radius=(2f * (float)Math.random())+3.5f;
            radius*=radius;
            radius*=(float)Square.xAmt()/100f;
            float peak=(float)(Math.pow(Math.random()-0.5 ,3)*300);
            peak*=Math.pow(radius,.5);
            Square center=Square.getRandSquare();
            makePeak(peak,(int)radius,center);
        }
        if (organicWaterways)
        {
            screen.current=LoadingScreen.WorkingOn.River;
            screen.percentage=0;
            placeRiverEnds(3,true);
            screen.percentage=.3;
            placeRiverEnds(3,false);
            screen.percentage=.6;
        }
        else
        {
            screen.current=LoadingScreen.WorkingOn.River;
            screen.percentage=0;
            makeRiver(null,null,50,4,0);
            screen.percentage=.3;
            makeRiver(null,null,50,4,200);
            screen.percentage=.6;
            float waterLevel=0;
            float minLevel=Square.get(0,0).terrain().elevation;
            for (int x=0;x<Square.xAmt();x++)
            {
                for (int y=0;y<Square.yAmt();y++)
                {
                    waterLevel+=Square.get(x,y).terrain().elevation;
                    if (Square.get(x,y).terrain().elevation<minLevel)
                        minLevel=Square.get(x,y).terrain().elevation;
                }
            }
            waterLevel/=Square.amt();
            waterLevel-=50;
            for (int x=0;x<Square.xAmt();x++)
            {
                for (int y=0;y<Square.yAmt();y++)
                {
                    if (Square.get(x,y).terrain().elevation<waterLevel)
                    {
                        Square.get(x,y).terrain().moisture=(waterLevel-Square.get(x,y).terrain().elevation)*Earth.MOISTURE_PER_CUBIC_METER
                                + Square.get(x,y).terrain().getWaterSaturation();
                    }
                }
            }
        }
    
        float sunReceptivity=0;
        for (int x=0;x<Square.xAmt();x++)
        {
            for (int y=0;y<Square.yAmt();y++)
            {
                sunReceptivity+=Square.get(x,y).terrain().getSunMod();
            }
        }
        sunReceptivity/=Square.amt();
        System.out.println("avg sunReceptivity="+sunReceptivity);
        System.out.println("avg final temperature= "+(sunReceptivity*Earth.heatFromSun/Earth.HEAT_LOSS_TO_SPACE_FACTOR *.665));
        if (organicWaterways)
        {
            screen.current=LoadingScreen.WorkingOn.RunningWater;
            screen.percentage=0;
            Terrain.Spring.flowRate*=5;
            Terrain.erosionRate*=100;
            float origWaterFlowRate=Terrain.waterFlowRate;
            Terrain.waterFlowRate=.9f;
            amt=200;
            int settleTime=25;
            amt=settleTime=0;
            for (int n=0;n<amt;n++)
            {
                screen.percentage=((double)n)/((double)amt+settleTime);
                runPhysics();
            }
            Terrain.Spring.flowRate/=5;
            Terrain.erosionRate/=100;
            Terrain.waterFlowRate=origWaterFlowRate;
            for (int n=0;n<settleTime;n++)
            {
                screen.percentage=((double)n+amt)/((double)amt+settleTime);
                runPhysics();
            }
        }
    }
    public static void makePeak(float height,int radius,Square center)
    {
        if (radius==0)
            radius=1;
        float slope=height/(radius*radius);
        ArrayList<Square> area=Square.getSquareOfSquares(center, radius);
        for (int n=0;n<area.size();n++)
        {
            changeHeight(height,slope,center,area.get(n));
        }
    }
    private static void changeHeight(float height,float slope,Square center,Square change)
    {
        float elevChange=(float)(height-slope*Util.sqrdDist(center,change));
        if ((elevChange>0) == (height>0))
        {
            change.terrain().changeElevation(elevChange);
        }
    }
    private static class PlaceVal {
        Square place;
        float val;
        public PlaceVal(Square loc,float num) {
            place=loc;
            val=num;
        }
    }
    public static void testGenTerrain(LoadingScreen loading)
    {
        loading.current=LoadingScreen.WorkingOn.Desert;
        for (float chain=0;chain<=2;chain+=.1)
        {
            for (float rad=1;rad<=10;rad+=.1)
            {
                loading.percentage=(chain+((rad-1)*.1f/10f))/2f;
                genTerrain(Square.get(70,40),new Terrain.Glass(),rad,chain);
                int amt=0;
                for (int x=0;x<Square.xAmt();x++)
                {
                    for (int y=0;y<Square.yAmt();y++)
                    {
                        if (Square.get(x, y).terrain().getType()==Thing.Type.Glass)
                        {
                            amt++;
                            Square.get(x ,y).setTerrain(new Terrain.Dirt(Square.get(x,y)));
                        }
                    }
                }
//                int half=Square.amt()/2;
//                if (Math.abs(amt-half)<400)
//                    desertSizeGraph.add(new Vector(chain,rad));
            }
        }
    }
    public static void genTerrain(Square startPoint,Terrain set,float radius,float chaining)
    {   //chain is a diminishing probability.   size is always greater pi*(1-log(chain)/log(.9))^2 * rad^2, and always greater than pi*rad^2
        
         ArrayList<PlaceVal> centers=new ArrayList<PlaceVal>();
         centers.add(new PlaceVal(startPoint,chaining));
         while (centers.size()>0)
         {
             ArrayList<Square> within=centers.get(0).place.getSquaresInRadius(radius);
             for (int n=0;n<within.size();n++)
             {
                 if (!within.get(n).terrain().sameType(set))
                 {
                     if (Math.random()<centers.get(0).val)
                     {
                         centers.add(new PlaceVal(within.get(n),centers.get(0).val*.9f));
                     }
                 }
                 within.get(n).setTerrain(set.clone());
             }
             centers.remove(0);
         }
    }
    public static void placeRiverEnds(int amt,boolean spring/*spring or pit*/)
    {
        ArrayList<Square> peaks=new ArrayList<Square>();
        for (int sample=0;sample<amt;sample++)
        {
            Square curLoc=Square.getRandSquare();
            boolean peaked=false;
            while (!peaked)//find the lowest surrounding square
            {
                ArrayList<Square> nayb=curLoc.getSquaresInRadius(3);
                Square highest=curLoc;//if !spring, its actually lowest
                peaked=true;
                for (int n=0;n<nayb.size();n++)
                {
                    if   ( (!spring && nayb.get(n).terrain().elevation<highest.terrain().elevation)
                       ||  (spring && nayb.get(n).terrain().elevation>highest.terrain().elevation) )
                    {       //not a simplyfied because if nayb.elev==highest.elev, it will infinite move back and forth
                        peaked=false;
                        highest=nayb.get(n);
                    }
                }
                curLoc=highest;
            }
            peaks.add(curLoc);
        }
        for (int n=0;n<amt;n++)
        {
            int ix=(int)(Math.random()*(double)peaks.size());
            if (spring)
                peaks.get(ix).setTerrain(new Terrain.Spring(peaks.get(ix)));
            else
                peaks.get(ix).setTerrain(new Terrain.WaterPit(peaks.get(ix)));
            peaks.remove(ix);
        }
    }
    public static void makeRiver(Square from,Square to,float depth,float width,double wonkiness)
    {       //  wonk≥0 . around 20 it ceases to matter. under ~4 it looks unnatural. 
                 //as wonk increases, the observable difference in the river from ∆wonk decreases
        if (from==null)
            from=Square.getRandSquare();
        if (to==null)
            to=Square.getRandSquare();
        while (from==to)
        {
            from=Square.getRandSquare();
            to=Square.getRandSquare();
        }
        Square cur=from;
        Vector momentum=new Vector(0,0);
        ArrayList<Square> river=new ArrayList<Square>();
        river.add(from);
        while (!cur.equals(to))
        {
            Vector flow;
            flow=Vector.getScaledVector(cur, to,1);
            flow=flow.add(momentum.scale(5));
            if (wonkiness>0)
            {
                Vector wonk=new Vector((Math.random()-0.5)*wonkiness,(Math.random()-0.5)*wonkiness);
                flow=flow.add(wonk);
            }
            flow=Vector.getScaledVector(flow, 1);
            momentum=momentum.scale(.8).add(flow.scale(.2));
            Square tomove=cur.transform(flow.toDir());
            if (!tomove.isError())
            {
                cur=tomove;
                for (int m=0;m<river.size();m++)
                {
                    if (river.get(m).equals(cur))
                    {
                        while (river.size()>m)
                        {
                            river.remove(river.size()-1);
                        }
                        break;
                    }
                }
                river.add(cur);
            }
        }
        float curHeight=from.terrain().elevation-depth/5f;
        float dElev=depth*.8f/(float)river.size();
        width-=1;
        width/=2;
        for (int n=0;n<river.size();n++)
        {
            curHeight-=dElev;
            if (river.get(n).terrain().elevation>=curHeight)
            {
                river.get(n).terrain().elevation=curHeight;
                river.get(n).terrain().fill(curHeight+dElev*2);
            }
            else
            {
                river.get(n).terrain().elevation-=dElev/2;
                river.get(n).terrain().fill(curHeight+dElev*2);
            }
            if (width>0)
            {
                float slope=dElev/width;
                for (Dir way=Dir.North;way!=Dir.West;way=way.rotate(true))
                {
                    float shoreHeight=river.get(n).terrain().elevation;
                    cur=river.get(n);
                    for (int shore=1;shore<width;shore++)
                    {
                        shoreHeight+=slope;
                        cur=cur.transform(way);
                        if (cur.terrain().elevation>shoreHeight)
                        {
                            cur.terrain().elevation=shoreHeight;
                            cur.terrain().fill(shoreHeight+slope*2);
                        }
                        else
                            cur.terrain().fill(shoreHeight+slope*2);
                    }
                }
            }
        }
        new Terrain.Spring(from);
        new Terrain.WaterPit(to);
        if (width>0)
        {
            Dir frontFlow=river.get(0).getDir(river.get(1));
            Dir endFlow=river.get(river.size()-2).getDir(river.get(river.size()-1));
            Square frontCur=from;
            Square backCur=to;
            for (byte way=0;way<1;way++)
            {
                for (int n=0;n<width;n++)
                {
                    frontCur=frontCur.transform(frontFlow.rotate(way==0));
                    new Terrain.Spring(frontCur);
                    backCur=backCur.transform(endFlow.rotate(way==0));
                    new Terrain.WaterPit(backCur);
                }
            }
        }
    }
    public static void calcAvgElev()
    {
        avgElev=0;
        for (int x=0;x<Square.xAmt();x++)
        {
            for (int y=0;y<Square.yAmt();y++)
            {
                if (!Util.broken(Square.get(x,y).terrain().elevation))
                    avgElev+=Square.get(x,y).terrain().elevation;
            }
        }
        avgElev/=Square.amt();
    }
    public static void runPhysics() {
        Earth.rotateEarth();
        for (int x=0;x<Square.xAmt();x++)
        {
            for (int y=0;y<Square.yAmt();y++)
            {
                Square.get(x,y).terrain().handlePhysics();
                Square.get(x,y).air.update();
                if (Square.get(x,y).occupant!=null)
                {
//                    System.out.println(Square.get(x,y).occupant.toString());
                    if (Square.get(x,y).occupant.isDead())
                        Square.get(x,y).deleteOccupant();
                    else
                        Square.get(x,y).occupant.handlePhysics();
                    //TODO: fix occupant going null after handlePhysics() call
                }
            }
        }
        for (int x=0;x<Square.xAmt();x++)
        {
            for (int y=0;y<Square.yAmt();y++)
            {
                Square.get(x,y).air.endStep();
                Square.get(x,y).terrain().endStep();
                if (Square.get(x,y).occupant!=null)
                {
                    Square.get(x,y).occupant.endStep();
                }
            }
        }
    }
    public static void update()
    {
        Earth.rotateEarth();
        Square.updateAll();
        //update display numbers
//        if (Util.everyNth(10))
        {
            float heat=0;
            float totalSpecHeat=0;
            moisture=0;
            for (int x=0;x<Square.xAmt();x++)
            {
                for (int y=0;y<Square.yAmt();y++)
                {
                    heat+=Square.get(x,y).terrain().heat * Square.get(x,y).terrain().getType().specificHeat;
                    totalSpecHeat+=Square.get(x,y).terrain().getType().specificHeat;
                    if (Square.get(x,y).occupant!=null)
                    {
                        heat+=Square.get(x,y).occupant.heat * Square.get(x,y).occupant.getType().specificHeat;
                        totalSpecHeat+=Square.get(x,y).occupant.getType().specificHeat;
                    }
                    moisture+=Square.get(x,y).air.humidity();
                }
            }
            worldHeat=(heat/totalSpecHeat);
            moisture/=Square.amt();
//            if (Util.everyNth(10))
//                heatGraph.add(new Float(worldHeat));
        }
        
    }
    public static boolean raining() {
        return raining;
    }
    public static float worldHeat() {
        return worldHeat- (raining? 20:0);
    }
    public static float humidity()
    {
        try {
            return moisture/Square.amt();
        }
        catch (Exception e) {
            return 1;
        }
    }
    public static void addMoisture() {
        moisture++;
    }
    public static boolean takeMoisture() {
        if (moisture>=1)
        {
            moisture--;
            return true;
        }
        else return false;
    }
    public static void addMoisture(float amt) {
        moisture+=amt;
    }
    public static boolean takeMoisture(float amt) {
        if (moisture>=amt)
        {
            moisture-=amt;
            return true;
        }
        else 
        {
            moisture=0;
            return false;
        }
    }
    public static void moveAnimal(Animal move, Square to)
    {
        move.loc.removeAnimal(move);
        move.loc=to;
        to.addAnimal(move);
    }
    public static void removeFeature(Thing remove)
    {
        remove.loc.empty();
        remove.loc=Square.error;
    }
    public static void moveFeature(Thing move,Square to)
    {
        move.loc.empty();
        if (to.moveable())
        {
            to.occupy(move);
            move.loc=to;
        }
    }
    public static void addFeature(Thing add)
    {
        if (add.isTerrain())
        {
            add.loc.setTerrain((Terrain)add);
        }
        else if (add.loc.moveable())
        {
            add.loc.occupy(add);
        }
    }
    public static void placeFeatures(Thing copyFrom,Square from,int width,int length)
    {
        ArrayList<Square> area=Square.getSquareOfSquares(from, width, length);
        for (int n=0;n<area.size();n++)
        {
            if (!from.equals(area.get(n)))
            {
                Thing tempClone=copyFrom.clone();
                tempClone.loc=area.get(n);
                area.get(n).occupant=tempClone;
            }
        }
    }
    public static void setTerrain(Terrain copyFrom,Square from,int width,int length)
    {
        ArrayList<Square> area=Square.getSquareOfSquares(from, width, length);
        for (int n=0;n<area.size();n++)
        {
            if (!from.equals(area.get(n)))
            {
                Terrain tempClone=copyFrom.clone();
                tempClone.loc=area.get(n);
                area.get(n).setTerrain(tempClone);
            }
        }
    }
    public static ArrayList<Square> getSquaresWithin(Square point,double range)
    {
        ArrayList<Square> within=Square.getSquareOfSquares(point, Util.roundUp(range));
        range*=range;
        for (int n=0;n<within.size();n++)
        {
            if (Util.sqrdDist(point, within.get(n))>range)
            {
                within.remove(n);
                n--;
            }
        }
        return within;
    }
    public static Thing getNearest(Thing.Type what,Square from)
    {
        double leastDist=999999999;
        Thing nrst=null;
        for (int x=0;x<Square.xAmt();x++)
        {
            for (int y=0;y<Square.yAmt();y++)
            {
                if (Square.getSquare(x, y).occupant!=null)
                    if (Square.getSquare(x, y).occupant.getType()==what)
                    {
                        double dist=Util.sqrdDist(from,Square.getSquare(x, y));
                        if (dist<leastDist)
                        {
                            leastDist=dist;
                            nrst=Square.getSquare(x,y).occupant;
                        }
                        
                    }
                if (Square.getSquare(x, y).terrain().getType()==what)
                {
                    double dist=Util.sqrdDist(from,Square.getSquare(x, y));
                    if (dist<leastDist)
                    {
                        leastDist=dist;
                        nrst=Square.getSquare(x,y).terrain();
                    }
                }
            }
        }
        return nrst;
    }
    public static Square getNearestWater(Square from) 
    {
        return getNearestWater(from,1);
    }
    public static Square getNearestWater(Square from,float depth)
    {
        double leastDist=999999999;
        Square nrst=Square.error;
        for (int x=0;x<Square.xAmt();x++)
        {
            for (int y=0;y<Square.yAmt();y++)
            {
                if (Square.getSquare(x, y).terrain().waterAbove()>=depth)
                {
                    double dist=Util.sqrdDist(from,Square.getSquare(x, y));
                    if (dist<leastDist)
                    {
                        leastDist=dist;
                        nrst=Square.getSquare(x,y);
                    }
                }
            }
        }
        return nrst;
    }
    public static Thing getNearest(final Thing.Type[] anyOfThese,Square from)
    {
        double leastDist=999999999;
        Thing nrst=null;
        for (int x=0;x<Square.xAmt();x++)
        {
            for (int y=0;y<Square.yAmt();y++)
            {
                boolean okType=false;
                double dist=-1;
                if (Square.get(x,y).occupant!=null)
                {
                    for (int t=0;t<anyOfThese.length;t++)
                    {
                        if (Square.getSquare(x, y).occupant.getType()==anyOfThese[t])
                        {
                            okType=true;
                            break;
                        }
                    }
                    if (okType)
                    {
                        dist=Util.sqrdDist(from,Square.getSquare(x, y));
                        if (dist<leastDist)
                        {
                            leastDist=dist;
                            nrst=Square.getSquare(x,y).occupant;
                        }
                    }
                }
                okType=false;
                for (int t=0;t<anyOfThese.length;t++)
                {
                    if (Square.getSquare(x, y).terrain().getType()==anyOfThese[t])
                    {
                        okType=true;
                        break;
                    }
                }
                if (okType)
                {
                    if (dist==-1)//if it wasnt calculated the first time
                        dist=Util.sqrdDist(from,Square.getSquare(x, y));
                    if (dist<leastDist)
                    {
                        leastDist=dist;
                        nrst=Square.getSquare(x,y).terrain();
                    }
                }
            }//for y
        }//for x
        return nrst;
    }//getNearest
    public static boolean hasClearPath(Thing from,Thing to)
    {
        return hasClearPath(from.loc,to.loc);
    }
    public static boolean hasClearPath(Square from,Square to)
    {
        if (from.equals(to))
            return true;
        if (from.x()==to.x())
        {
            if (from.y()>to.y())
            {
                for (int y=to.y();y<=from.y();y++)
                {
                    if (!Square.getSquare(from.x(), y).moveable())
                        return false;
                }
            }
            else if (from.y()<to.y())
            {
                for (int y=from.y();y<=to.y();y++)
                {
                    if (!Square.getSquare(from.x(), y).moveable())
                        return false;
                }
            }
            //else theyre in the same spot so return true
            return true;
        }
        else
        {
            float slope;
            slope=(from.y()-to.y())/(from.x()-to.x());
            int innerX=(from.x() < to.x() ? from.x(): to.x());
            int outerX=(from.x() < to.x() ? to.x(): from.x());
            double y=from.y();
            for (int x=innerX;x<=outerX;x++)
            {
                if (!Square.get(x,(int)y).moveable())
                    return false;
                if (!Square.get(x,Util.roundUp(y)).moveable())
                    return false;
                y+=slope;
            }
            return true;
        }
    }
    public static void draw(Graphics2D g,Eden.DisplayType displayType)
    {
        Square.drawAll(g,displayType);
        if (displayType==Eden.DisplayType.Normal)
            Earth.nightShade(g);
//        drawGraphs(g);
    }
    public static void drawGraphs(Graphics2D g)
    {
        /*
        g.setFont(new Font("Arial",Font.PLAIN,20));
        for (int y=0;y<=10;y+=2)
        {
            g.setColor(Color.YELLOW);
            g.drawString((y)+"",95,800-y*70);
        }
        for (int x=0;x<40;x+=1)
        {
            g.setColor(Color.YELLOW);
            g.drawString(((float)x/20f)+"",100+x*25,900);
        }
        int graphSpillover=desertSizeGraph.size()-1100;
        if (graphSpillover<0) graphSpillover=0;
        g.setColor(Color.YELLOW);
        for (int n=0;n<desertSizeGraph.size();n++)
        {
            g.setColor(new Color((n%51)*5,(n%6)*50,255));
            g.fillOval(100+(int)(desertSizeGraph.get(n).xVel*500f)+(n%10),800-(int)(desertSizeGraph.get(n).yVel*70f) + (n%10) ,3,3);
        }
        */
    }
    public static boolean pixelOnMap(int x,int y)
    {
        if (x<minX) return false;
        if (x>maxX) return false;
        if (y<minY) return false;
        if (y>maxY) return false;
        return true;
    }
    public static boolean onMap(double x,double y)
    {
        return onMap((int)x,(int)y);
    }
}
