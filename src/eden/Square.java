
package eden;

import java.awt.*;
import java.util.*;

public class Square 
{
    /*
     * Each square is 1 sq meter
     * Squares contain: Air, Terrain, Occupants, and any animals
     * Air is all the same and always there
     * Terrain is always tehre but can change
     * occupants may or may not be there, with only one at a time
     * there can be any amount of animals
     */
    final static int SQUARE_SIZE=10;
    final static int HALF_SQUARE=SQUARE_SIZE/2;
    private int px,py;
    private int gx,gy;
    private static Square[][] grid;
    final static Square error=new Square(-1,-1,0,0);
    static ArrayList<Square> prechecked;
    Thing occupant;
    private Terrain terrain;
    Air air;
    ArrayList<Thing.Body> animals;
    int hash;
    public Square(int x,int y,int pixelXOffset,int pixelYOffset)
    {
        gx=x;
        gy=y;
        px=gx*SQUARE_SIZE+pixelXOffset;
        py=gy*SQUARE_SIZE+pixelYOffset;
        hash=(int)(Math.random()*340510170);//rand*(the product of the first several primes[to avoid modulo bias:{10%3 tends towards 1 and 0}])
        occupant=null;
        terrain=new Grass(this,5);
        air=new Air(this);
//        terrain=new Terrain.Dirt(this);
        animals=new ArrayList<Thing.Body>();
    }
    public void draw(Graphics2D g,Eden.DisplayType displayType)
    {
        if (displayType.showTerr)
        {
            if (displayType==Eden.DisplayType.Elevation)
                terrain.drawElev(g);
            else
                terrain.draw(g);
        }
        if (displayType.showOcc && occupant!=null)
        {
            occupant.draw(g);
        }
        if (displayType.showAnimals)
        {
            for (int n=0;n<animals.size();n++)
            {
                animals.get(n).draw(g);
            }
        }
        if (displayType.showAir)
        {
            if (displayType==Eden.DisplayType.Humidity)
                air.drawHumidity(g);
            else if (displayType==Eden.DisplayType.Heat)
                air.drawHeat(g);
            else if (displayType==Eden.DisplayType.Density)
                air.drawDensity(g);
            else if (displayType==Eden.DisplayType.Wind)
                air.drawWind(g);
            else
                air.draw(g);
        }
    }
    public boolean isNeighbor(Square other)
    {
        int yDif=Math.abs(other.y()-gy);
        if (yDif>1)
            return false;
        int xDif=Math.abs(other.x()-gx);
        if (xDif>1)
        {
            if (Earth.wraparound && xDif!=Square.xAmt()-1)
                return false;
        }
        return ((xDif==0)^(yDif==0));
    }
    public ArrayList<Square> getNeighbors()
    {
        if (isError())
        {
            (new Exception("Error asked for neighbors")).printStackTrace();
            System.out.println("Error was asked for his neighbors lol");
            return new ArrayList<Square>();
        }
        ArrayList<Square> nabors=new ArrayList<Square>();
        for (int x=gx-1;x<=gx+1;x++)
        {
            for (int y=gy-1;y<=gy+1;y++)
            {
                if ( (x==gx) ^ (y==gy))//removes diagonals and center
                {
                    if (Square.yInBounds(y))
                    {
                        if (Square.xInBounds(x))
                        {
                            nabors.add(getSquare(x,y));
                        }
                        else if (Earth.wraparound)//creates map wraparound
                        {
                            if (x==-1)
                                nabors.add(getSquare(Square.xAmt()-1,y));
                            else if (x==Square.xAmt())
                                nabors.add(getSquare(0,y));
                        }
                    }
                }
            }
        }
        if (nabors.size()==0)
        {
            System.out.println("SPACEY WACEY PROBLEMS");
        }
        return nabors;
    }
    public Square getHighestNeighbor(boolean considerWater) {
        ArrayList<Square> nayb=getNeighbors();
        Square highest=this;//highest neighbor includes self
        for (int n=0;n<nayb.size();n++)
        {
            if ( (!considerWater && (nayb.get(n).terrain.elevation > highest.terrain.elevation)) 
               || (considerWater && (nayb.get(n).terrain.height() > highest.terrain.height()))  )
            {
                highest=nayb.get(n);
            }
        }
        return highest;
    }
    public ArrayList<Square> getSquaresInRadius(double rad)
    {
        double sqrdRad=rad*rad;
        ArrayList<Square> toCheck=getSquareOfSquares(this,Util.roundUp(rad));
        ArrayList<Square> inRad=new ArrayList<Square>();
        for (int n=0;n<toCheck.size();n++)
        {
            if (Util.sqrdDist(this,toCheck.get(n))<sqrdRad)
            {
                inRad.add(toCheck.get(n));
            }
        }
        return inRad;
    }
    public Square getRandNeighbor() {
        ArrayList<Square> neighb=getNeighbors();
        if (neighb.size()==0)
            return Square.error;
        return neighb.get((int)(neighb.size()*Math.random()));
    }
    public int getX() {
        return gx;
    }
    public int getY() {
        return gy;
    }
    public int getPX(){
        return px;
    }
    public int getPY(){
        return py;
    }
    public Square randTransform()
    {
        Random rand=new Random();
        switch (rand.nextInt(4)) 
        {
            case 0: 
                return transform(1,0); 
            case 1: 
                return transform(-1,0); 
            case 2: 
                return transform(0,1); 
            case 3: 
                return transform(0,-1); 
            default: 
                return this;
        }
    }
    public Dir getDir(Square to)
    {
        if (Util.xDist(gx,to.gx) >Math.abs(gy-to.gy))
        {
            if (Util.goEast(gx,to.gx))
                return Dir.East;
            else return Dir.West;
        }
        else
        {
            if (gy<to.gy)
                return Dir.South;
            else 
                return Dir.North;
        }
    }
    public Square transform(int dx,int dy)
    {
        if (Earth.wraparound && dx!=0)
        {
            if (gx+dx>=Square.xAmt())
                return Square.getSquare((gx+dx)%Square.xAmt(), gy+dy);
            else if (gx+dx<0)
                return Square.getSquare(Square.xAmt()+(gx+dx)%Square.xAmt(), gy+dy);
        }
        return Square.getSquare(gx+dx,gy+dy);
    }
    public Square transform(Dir dir)
    {
        return transform(dir.x(),dir.y());
    }
    public boolean hasPath(Square to)
    {
        if (equals(to))
            return true;
        ArrayList<Square> nabors=getNeighbors();
        for (int n=0;n<nabors.size();n++)
        {
            if (!isInChecked(nabors.get(n)))
                if (nabors.get(n).hasPath(to))
                    return true;
        }
        return false;
    }
    public boolean equals(Square other)
    {
        if (other==null)
            return false;
        if (gx!=other.gx) return false;
        if (gy!=other.gy) return false;
        return true;
    }
    public void update()
    {
        if (occupant!=null)
        {
            if (occupant.isDead())
                deleteOccupant();
            else
                occupant.run();
        }
        if (terrain.isDead())
            terrain=new Terrain.Dirt(this);
        terrain.run();
        air.update();
        for (int n=0;n<animals.size();n++)
        {
            if (animals.get(n).isDead())
                animals.remove(n);
            else
                animals.get(n).run();
        }
    }
    public void endStep()
    {
        if (occupant!=null)
            occupant.endStep();
        terrain.endStep();
        air.endStep();
        for (int n=0;n<animals.size();n++)
            animals.get(n).endStep();
    }
    public String toString()
    {
        return gx+", "+gy;
    }
    public int x() {
        return gx;
    }
    public int y() {
        return gy;
    }
    public boolean isError() {
        return gx==-1 && gy==-1;
    }
//    public Square transform(Dir dir) {
//        return getSquare(gx+dir.x(),gy+dir.y());
//    }
    public int px()
    {
        return getPX();
    }
    public int py()
    {
        return getPY();
    }
    public boolean moveable()
    {
        if (isError())
            return false;
        if (!terrain.getType().walkable())
            return false;
        for (int n=0;n<animals.size();n++)
            if (!animals.get(n).species.walkable())
                return false;
        return occupant==null || occupant.getType().walkable();
    }
    public void addAnimal(Thing.Body add)
    {
        animals.add(add);
    }
    public void removeAnimal(Thing.Body remove)
    {
        animals.remove(remove);
    }
    public Terrain terrain() {
        return terrain;
    }
    public void setTerrain(Terrain set)
    {
        //delete old
        terrain=set;
        set.loc=this;
    }
    public void occupy(Thing theNinetyNinePercent)
    {
        occupant=theNinetyNinePercent;
    }
    public void replaceTerrain() {
        replaceTerrain(new Terrain.Dirt(this));
    }
    public void replaceTerrain(Terrain replace) {
//        terrain=null;//going to be useful in c++ port
        terrain=replace;
    }
    public void deleteOccupant() {
        //delete occupant
        occupant=null;//going to be usefull in c++ port
    }
    public void removeOccupant() {
        occupant=null; //doesnt delete occ
    }
    public void empty() {
        deleteOccupant();//TODO: change here & in code to account for animals
    }
    public void damageAllThings(double amt)
    {
        if (occupant!=null)
            occupant.damage(amt);
        for (int n=0;n<animals.size();n++)
            animals.get(n).damage(amt);
    }
    //static 
    public static void onStart(int minX,int minY,int maxX,int maxY)
    {
        prechecked=new ArrayList<Square>();
        int xAmt=Util.roundUp(((double)(maxX-minX))/(double)SQUARE_SIZE);
        int yAmt=Util.roundUp(((double)(maxY-minY))/(double)SQUARE_SIZE);
        grid=new Square[xAmt][yAmt];
        System.out.println("Grid size = "+grid.length+" X "+grid[0].length);
        for (int x=0;x<grid.length;x++)
        {
            for (int y=0;y<grid[x].length;y++)
            {
                grid[x][y]=new Square(x,y,minX,minY);
            }
        }
        error.terrain=new Terrain.Glass(Square.error);
        error.terrain.heat=100000;
    }
    public static ArrayList<Square> getSquareOfSquares(Square center,int radius)
    {
        ArrayList<Square> squareOf=new ArrayList<Square>();
        for (int y=Square.validizeY(center.y()-radius);y<=Square.validizeY(center.y()+radius);y++)
        {
            for (int x=Square.validizeX(center.x()-radius);x<=Square.validizeX(center.x()+radius);x++)
            {
                squareOf.add(Square.get(x,y));
            }
            if (Earth.wraparound && center.x()<radius)
            {
                for (int x=Square.wrapX(center.x()-radius);x<Square.xAmt();x++)
                {
                    squareOf.add(Square.get(x,y));
                }
            }
            if (Earth.wraparound && center.x()+radius>=Square.xAmt())
            {
                for (int x=0;x<=Square.wrapX(center.x()+radius);x++)
                {
                    squareOf.add(Square.get(x,y));
                }
            }
        }
        return squareOf;
    }
    public static ArrayList<Square> getSquareOfSquares(Square topLeft,int width,int length)
    {
        ArrayList<Square> squareOf=new ArrayList<Square>();
        for (int y=Square.validizeY(topLeft.y());y<=Square.validizeY(topLeft.y()+length);y++)
        {
            for (int x=Square.validizeX(topLeft.x());x<=Square.validizeX(topLeft.x()+width);x++)
            {
                squareOf.add(Square.get(x,y));
            }
            if (Earth.wraparound && topLeft.x()+width>=Square.xAmt())
            {
                for (int x=0;x<=Square.wrapX(topLeft.x()+width);x++)
                {
                    squareOf.add(Square.get(x,y));
                }
            }
        }
        return squareOf;
    }
    public static ArrayList<Square> getPath(Square from, Square to)
    {
        ArrayList<Square> path=new ArrayList<Square>();
        ArrayList<Square> checked=new ArrayList<Square>();
        path.add(from);
        checked.add(from);
        Square cur=from;
        Square best=null;
//        System.out.println("Find path from "+pos.gx+", "+pos.gy+" to "+to.gx+", "+to.gy);
        while (!to.equals(cur))
        {
            double leastDist=Square.xAmt()*Square.yAmt()*4;
            best=null;
//            System.out.println("At "+cur.gx+", "+cur.gy);
            for (int x=wrapX(cur.gx-1);x<=wrapX(cur.gx+1);x=incX(x,1))
            {
                for (int y=cur.gy-1;y<=cur.gy+1;y++)
                {
                    if ( (x==cur.gx) ^ (y==cur.gy))//removes diagonals and center
                    {
                        if (Square.xInBounds(x) && Square.yInBounds(y))
                        {
//                            if (!Square.getSquare(x, y).moveable() )
                            {
                                double dist=Util.sqrdDist(Square.getSquare(x,y),to);
                                if (dist<leastDist)
                                {
                                    boolean inChecked=false;
                                    for (int c=0;c<checked.size();c++)
                                    {
//                                        System.out.println("    pre-checked: "+checked.get(c).gx+","+checked.get(c).gy);
                                        if (checked.get(c).equals(Square.getSquare(x, y)))
                                        {
//                                            System.out.println("    In checked ");
                                            inChecked=true;
                                            break;
                                        }
                                    }
                                    if (!inChecked)
                                    {
//                                        System.out.println("    best");
                                        leastDist=dist;
                                        best=Square.getSquare(x, y);
                                    }
                                }
                            }
                            /*
                            else if (to.gx==x && to.gy==y)
                            {
//                                System.out.println("reached target");
                                y=cur.gy+2;
                                x=cur.gx+2;
                                best=to;
                            }
                            * */
                        }
                    }//if (not diagonal and not center)
                }//for y
            }//for x
            if (best!=null)
            {
                for (int p=0;p<path.size();p++)
                {
                    if (path.get(p).equals(best))
                    {//for clipping out loops
//                        System.out.println("  remove from "+p+" to "+(path.size()-1));
                        while (path.size()>p)
                        {
                            path.remove(path.size()-1);
                        }
                    }
                }
//                System.out.println(" add "+best.gx+", "+best.gy);
                path.add(best);
                checked.add(best);
                cur=best;
            }
            else
            {
//                System.out.println("best is null; go back");
                path.remove(path.size()-1);
                if (path.isEmpty())
                {
                    System.out.println("has no path to "+to.toString());
                    return path;
                }
                cur=path.get(path.size()-1);
            }
        }//while (cur!=to)
        return path;
    }
    public static void updateAll()
    {
        for (int y=0;y<grid[0].length;y++)
        {
            for (int x=0;x<grid.length;x++)
            {
                grid[x][y].update();
            }
        }
        for (int y=0;y<grid[0].length;y++)
        {
            for (int x=0;x<grid.length;x++)
            {
                grid[x][y].endStep();
            }
        }
    }
    public static void resetAll()
    {
        for (int x=0;x<grid.length;x++)
        {
            for (int y=0;y<grid[x].length;y++)
            {
                grid[x][y].occupy(null);
            }
        }
    }
    public static void drawAll(Graphics2D g,Eden.DisplayType drawType)
    {
        if (drawType.drawMap())
        {
            for (int x=0;x<grid.length;x++)
            {
                for (int y=0;y<grid[x].length;y++)
                {
                    grid[x][y].draw(g,drawType);
                }
            }
        }
        error.draw(g, drawType);
    }
    public static void endWorld()//big red button saying "DO NOT PRESS"
    {
        grid=null;
    }
    public static int amt() {
        return xAmt()*yAmt();
    }
    public static Square validize(int x,int y)
    {
        return Square.get(validizeX(x),validizeY(y));
    }
    public static int validizeY(int y)
    {
        if (y<0)
            return 0;
        else if (y>=yAmt())
            return yAmt()-1;
        else 
            return y;
    }
    public static int validizeX(int x)
    {
        if (x<0)
            return 0;
        else if (x>=xAmt())
            return xAmt()-1;
        else 
            return x;
    }
    public static int wrapX(int x)
    {
        if (!Earth.wraparound) return validizeX(x);
        else
        {
            if (x<0)
                return Square.xAmt()+(x%Square.xAmt());//plus a negative = minus.     (-n)%k = -(n%k)
            else if (x>=Square.xAmt())
            {
                return x%Square.xAmt();
            }
            else return x;
        }
    }
    public static int incX(int x,int delta)
    {
        if (Earth.wraparound)
            return wrapX(x+delta);
        else
            return x+delta;
    }
    public static int xAmt() {
        return grid.length;
    }
    public static int yAmt() {
        return grid[0].length;
    }
    public static boolean moveable(int x,int y) {
        if (inBounds(x,y))
        {
            if (getSquare(x,y).moveable())
                return true;
            else return false;
        }
        else return false;
    }
    public static boolean inBounds(int x,int y)
    {
        return (x>=0 && x<grid.length && y>=0 && y<grid[0].length);
    }
    public static boolean xInBounds(int x) {
        return (x>=0 && x<grid.length);
    }
    public static boolean yInBounds(int y) {
        return (y>=0 && y<grid[0].length);
    }
    public static Square get(int x,int y)
    {
        if (x>=0 && x<grid.length && y>=0 && y<grid[x].length)
            return grid[x][y];
        else return error;
    }
    public static int getXOffset() {
        return grid[0][0].px();
    }
    public static int getYOffset() {
        return grid[0][0].py();
    }
    public static Rectangle getRect() {
        return new Rectangle(getXOffset(),getYOffset(),
                grid[xAmt()-1][0].px()+Square.SQUARE_SIZE,grid[0][yAmt()-1].py()+Square.SQUARE_SIZE);
    }
    public static Square getSquareByPixel(int px,int py)
    {
        int gx=(px-getXOffset())/Square.SQUARE_SIZE;
        int gy=(py-getYOffset())/Square.SQUARE_SIZE;
        return getSquare(gx,gy);
    }
    public static Square getNrstSquare(int px,int py)
    {
        int gx=(px-getXOffset())/Square.SQUARE_SIZE;
        int gy=(py-getYOffset())/Square.SQUARE_SIZE;
        return validize(gx,gy);
    }
    public static Square getSquare(int x,int y)
    {
        if (x>=0 && x<grid.length && y>=0 && y<grid[x].length)
            return grid[x][y];
        else return error;
    }
    public static Square getRandSquare()
    {
        Random rand=new Random();
        return grid[rand.nextInt(xAmt())][rand.nextInt(yAmt())];
    }
    public static boolean isInChecked(Square in)
    {
        for (int n=0;n<prechecked.size();n++)
        {
            if (in.equals(prechecked.get(n)))
                return true;
        }
        return false;
    }
}
