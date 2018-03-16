/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Eden extends JFrame implements Runnable, KeyListener, MouseListener, MouseMotionListener
{
    private enum Click {
        None(false,false),
        TimeStretch(false,false),
        Obliterate(true,true),Dirtify(true,true),
        Fire(true,true),Ice(true,true),
        Water(true,true),Dry(true,true),
        AddAir(true,true),Still(true,true),
        Log(true,true),
        Mountain(false,false),Valley(false,false),
        River(true,false)
        ;
        boolean selectSquare,drag;
        Click(boolean setDrag,boolean setSelectSquare)
        {
            drag=setDrag;
            selectSquare=setSelectSquare;
        }
    }
    public enum DisplayType {
        Loading(false,false,false,false),
        Normal(true,true,true,true),
        Help(false,false,false,false),
        Elevation(false,false,true,false),
        Humidity(false,false,false,true),
        Density(false,false,false,true),
        Heat(false,false,false,true),
        Wind(false,false,false,true),
        Animals(false,true,false,false),
        ;
        public boolean showOcc,showAnimals,showTerr,showAir;
        DisplayType(boolean dispOcc,boolean dispAn,boolean dispTerr,boolean dispAir)
        {
            showOcc=dispOcc;
            showAnimals=dispAn;
            showTerr=dispTerr;
            showAir=dispAir;
        }
        public boolean drawMap() {
            return showOcc||showAnimals||showTerr||showAir;
        }
    }
    Container con = getContentPane();
    Thread t = new Thread(this);
    boolean paused;
    int timeWarp=500;
    long timeStretch=20;
    boolean shift=false;
    Click nextClick;
    Object clickObj;
    Square selected;
    DisplayType displayType;
    LoadingScreen loading;
    boolean broke=false;
    
    long lastUpdate;
    public Eden()
    {
        loading=new LoadingScreen();
        displayType=DisplayType.Loading;
        setVisible(true);
        setSize(1500,1000);
        con.setBackground(Color.BLUE);
        con.setLayout(new FlowLayout());
        addKeyListener(this);
        paused=true;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        t.start();
        Map.onStart(loading,t);
        Species.onStart(loading);
        timeWarp=500;
        setTimeStretch();
        nextClick=Click.None;
        addMouseListener(this);
        addMouseMotionListener(this);
        displayType=DisplayType.Normal;
        lastUpdate=System.currentTimeMillis();
        paused=true;
    }
    private void setTimeStretch() {
        timeStretch=(long)(Math.pow(2,(double)timeWarp/90.0)/10);
//        System.out.println("timStreth="+timeStretch);
    }
    public void run()
    {
        try
        {
            t.sleep(20);
            while(true)
            {
                long wait=timeStretch-(System.currentTimeMillis()-lastUpdate);
                if (wait<0) wait=0;
                t.sleep(timeStretch);
                lastUpdate=System.currentTimeMillis();
                if (!paused)
                {
                    Util.onFrame();
                    Map.update();
                    Species.updateAll();
                }
                repaint();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void keyPressed(KeyEvent k)
    {
        
        if (k.getKeyCode()==KeyEvent.VK_ESCAPE)
            System.exit(0);
        else if (k.getKeyCode()==KeyEvent.VK_SHIFT)
            shift=true;
        else if (k.getKeyCode()==KeyEvent.VK_BACK_SLASH)
            Map.simpleDraw=!Map.simpleDraw;
        else if (displayType!=DisplayType.Loading)
        { 
            if (k.getKeyCode()==KeyEvent.VK_SPACE)
                paused=!paused;
            else if (k.getKeyCode()==KeyEvent.VK_E)
            {
                Map.calcAvgElev();
                displayType=DisplayType.Elevation;
            }
            else if (k.getKeyCode()==KeyEvent.VK_T)
                displayType=DisplayType.Heat;
            else if (k.getKeyCode()==KeyEvent.VK_H)
                displayType=DisplayType.Humidity;
            else if (k.getKeyCode()==KeyEvent.VK_Q)
                displayType=DisplayType.Density;
            else if (k.getKeyCode()==KeyEvent.VK_S)
                displayType=DisplayType.Wind;
            else if (k.getKeyCode()==KeyEvent.VK_B)
                displayType=DisplayType.Animals;
            else if (k.getKeyCode()==KeyEvent.VK_PERIOD)
                displayType=DisplayType.Normal;
            else if (k.getKeyCode()==KeyEvent.VK_SLASH)
            {
                paused=true;
                displayType=DisplayType.Help;
            }
            else if (k.getKeyCode()==KeyEvent.VK_F)
                nextClick=Click.Fire;
            else if (k.getKeyCode()==KeyEvent.VK_I)
                nextClick=Click.Ice;
            else if (k.getKeyCode()==KeyEvent.VK_W)
                nextClick=Click.Water;
            else if (k.getKeyCode()==KeyEvent.VK_D)
                nextClick=Click.Dry;
            else if (k.getKeyCode()==KeyEvent.VK_M)
                nextClick=Click.Mountain;
            else if (k.getKeyCode()==KeyEvent.VK_V)
                nextClick=Click.Valley;
            else if (k.getKeyCode()==KeyEvent.VK_O)
                nextClick=Click.Obliterate;
            else if (k.getKeyCode()==KeyEvent.VK_Y)
                nextClick=Click.Dirtify;
            else if (k.getKeyCode()==KeyEvent.VK_R)
                nextClick=Click.River;
            else if (k.getKeyCode()==KeyEvent.VK_A)
                nextClick=Click.AddAir;
            else if (k.getKeyCode()==KeyEvent.VK_L)
                nextClick=Click.Log;
            else if (k.getKeyCode()==KeyEvent.VK_X)
                nextClick=Click.Still;
            else if (k.getKeyCode()==KeyEvent.VK_P)//P for Print
            {
                if (selected==null)
                {
                    if (shift)
                    {
                        paused=true;
                        for (int x=0;x<Square.xAmt();x++)
                        {
                            for (int y=0;y<Square.yAmt();y++)
                            {
                                printSquare(Square.get(x,y));
                            }
                        }
                    }
                    else
                    {
                        
                    }
                }
                else
                {
                    printSquare(selected);
                }
            }
        }
        
    }
    public void printSquare(Square loc)
    {
        System.out.println(loc.toString()+":");
        System.out.println("  Air:");
        System.out.println("     Heat="+loc.air.heat());
        System.out.println("     Humidity="+loc.air.humidity());
        System.out.println("     Density="+loc.air.density());
        System.out.println("     Wind xVel="+loc.air.wind().xVel+", yVel="+loc.air.wind().yVel);
        if (loc.air.raining())
            System.out.println("     Is raining");
        System.out.println("  Terrain:");
        System.out.println("    Type="+loc.terrain().getTerrainType().toString());
        System.out.println("    Heat="+loc.terrain().heat);
        System.out.println("    Moisture="+loc.terrain().moisture);
        System.out.println("    Elevation="+loc.terrain().elevation);
        System.out.println("    Weight="+loc.terrain().mass());
        System.out.println("   "+loc.terrain().otherInfo());
        if (loc.occupant!=null)
        {
            System.out.println("  Occupant:");
            System.out.println("    Type="+loc.occupant.getType().toString());
            System.out.println("    Heat="+loc.occupant.heat);
            System.out.println("    Weight="+loc.occupant.mass());
            System.out.println("    ID="+loc.occupant.id);
            System.out.println("   "+loc.occupant.otherInfo());
            if (loc.occupant instanceof Food) 
            {
                System.out.println("    Calories= "+((Food)loc.occupant).getCalories());
                if (loc.occupant instanceof Tree.Fruit) {
                    System.out.println("    Fruit type= "+((Tree.Fruit)loc.occupant).type.name());
                }
            }
        }
        for (int n=0;n<loc.animals.size();n++)
        {
            if (loc.animals.get(n) instanceof Animal) {
                System.out.println("  Animal:");
                //TODO: more
            }
            else {
                System.out.println("  Carcass:");
            }
            System.out.println("    Species="+((Thing.Body)loc.animals.get(n)).species.toString());
            System.out.println("    Type="+loc.animals.get(n).getType().toString());
            System.out.println("    Heat="+loc.animals.get(n).heat);
            System.out.println("    ID="+loc.animals.get(n).id);
        }
    }
    public void paint(Graphics gr)
    {
        Image i=createImage(getSize().width, getSize().height);
        Graphics2D g2 = (Graphics2D)i.getGraphics();        
        if (displayType==DisplayType.Help)
            showHelpMenu(g2);
        else if (displayType==DisplayType.Loading)
        {
            loading.draw(g2);
        }
        else
        {
            g2.setColor(new Color(60,60,60));
            g2.fillRect(0,0,1500,1000);
            g2.setColor(new Color(20,140,0));
            g2.setColor(Color.BLACK);
            g2.fillRect(Map.minX-10,Map.minY-10,Map.maxX+10,Map.maxY+38);
            Map.draw(g2,displayType);
            
            //draw selection box
            if (nextClick.drag && clickObj instanceof Square)
            {
                Square from=(Square)clickObj;
                if (selected==null)
                {
                    Point p=MouseInfo.getPointerInfo().getLocation();
                    selected=Square.getNrstSquare(p.x,p.y);
                }
                g2.setColor(new Color(80,80,100,130));
                g2.setStroke(new BasicStroke(3));
                g2.draw(Util.makeRect(from.px(),from.py(),selected.px(),selected.py(),Square.SQUARE_SIZE,Square.SQUARE_SIZE));
            }
            g2.setColor(Earth.timeOfDayColor());
            g2.fillRect(17,911,78,22);
            g2.setFont(new Font("Arial",Font.BOLD,15));
            g2.setColor(Color.BLUE);
            g2.drawString("Time="+Earth.timeOfDay,19,925);
            
            g2.setColor(Color.GREEN);
            if (selected!=null)
            {
                g2.drawString(selected.toString()+":",1105,925);
            }
            else {
                g2.drawString("World: ",1105,925);
            }
            g2.setColor(Color.RED);
            g2.drawString("Temp: "+(int)((selected==null?Map.worldHeat:selected.terrain().heat)-273)+"",1175,925);
            g2.setColor(Color.BLUE);
            g2.drawString("Humidity: "+(float)(selected==null?Map.moisture:selected.air.humidity())+"",1272,925);
            if (selected!=null)
            {
                g2.setColor(new Color(0,200,0));
                g2.drawString(""+selected.terrain().getTerrainType().toString(),1105,944);
                g2.setColor(new Color(0,0,200));
                if (selected.terrain().underwater())
                {
                    g2.drawString("Depth: "+(int)selected.terrain().metersOfWaterAbove(),1255,944);
                }
                else
                    g2.drawString("Moisture: "+(int)selected.terrain().moisture,1255,944);
                g2.setColor(new Color(150,150,150));
                g2.drawString("Density: "+(float)selected.air.density(),1106,962);
                g2.setColor(Color.WHITE);
                g2.drawString("Wind speed: "+(int)(selected.air.windSpeed().xVel*1000000)+", "+(int)(selected.air.windSpeed().yVel*1000000),1106,980);
            }
            drawTimeBar(g2);
            if (paused)
            {
                g2.setFont(new Font("Arial",Font.BOLD,100));
                g2.setColor(new Color(255,255,255,200));
                g2.drawString("Paused",500,500);
                g2.setFont(new Font("Arial",Font.PLAIN,30));
                g2.drawString("Help Screen: ?",575,550);
            }
//            else
            {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial",Font.BOLD,17));
                g2.drawString("Help Screen: ?",20,995);
            }
        }
        if (broke )
        {
            int which=(int)((System.currentTimeMillis()/100000)%5);
            g2.setColor(new Color(150,0,0,200));
            g2.setFont(new Font("OCR A Std",Font.PLAIN,170));
            if (which==0)
            {
                if (System.currentTimeMillis()% 100 <50)
                    g2.drawString("HAIL SATAN",175-(System.currentTimeMillis()% 100),500+(System.currentTimeMillis()%200));
            }
            else if (which==2)
            {
                g2.drawString("NIMMIT",320,300);
                g2.drawString("FINKLEHORN",70,550);
            }
            else if (which==3)
            {
                g2.drawString("Technical",150,300);
                g2.drawString("Difficulties",5,550);
            }
            else if (which==4)
            {
                g2.setFont(new Font("OCR A Std",Font.PLAIN,120));
                g2.drawString("i accidentally",35,350);
                g2.drawString("everthing", 200, 500);
            }
            else g2.drawString("THANKS OBAMA",10,500);
        }
        g2.dispose();
        gr.drawImage(i, 0, 0, this);
    }
    public void drawTimeBar(Graphics2D g)
    {
        g.setColor(new Color(100,100,100));
        g.fillRect(100,910,1000,20);
        g.setColor(new Color(100,0,100));
        g.fillRect(97+(int)timeWarp,900,15,40);
    }
    public void showHelpMenu(Graphics2D g)
    {
    String leftCol[]={"Pause: Space","Exit: Escape",
    "Views:"," Elevation: E"," Temperature: T"," Humidity: H"," Air density: Q","Wind Speed: S"," Back to Map: >"};
    String rightCol[]={"Commands:"," Create Mountain: M"," Create Valley: V",
    " Ice: I"," Fire: F",
    " Add water: W"," Dry: D",
      " River: R"," Obliterate: O"," Dirtify: Y","Add/Remove Air: A",
      "Click to affect Square","Click and drag to affect an area","Shift to Amplify Effects"};
    g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());
        drawTimeBar(g);
        g.setFont(new Font("Arial",Font.PLAIN,30));
        g.setColor(Color.WHITE);
            g.drawString("Slide to change game speed",440,830);
            g.drawString("Faster",100,880);
            g.drawString("Slower",1000,880);
        g.drawString("",40,40);
        for (int n=0;n<leftCol.length;n++)
        {
            g.drawString(leftCol[n],280+(leftCol[n].charAt(0)==' '?30:0),150+n*35);
        }
        for (int n=0;n<rightCol.length;n++)
        {
            g.drawString(rightCol[n],830+(rightCol[n].charAt(0)==' '?30:0),225+n*35);
        }
    }
    public void keyReleased(KeyEvent k)
    {
        if (k.getKeyCode()==KeyEvent.VK_SHIFT)
            shift=false;
    }
    public void keyTyped(KeyEvent k) {}
    public void update(Graphics g)
    {
        paint(g);
    } 
    public void mouseEntered(MouseEvent mo){}
    public void mouseExited(MouseEvent mo){}
    public void mouseDragged(MouseEvent mo)
    {
        if (nextClick==Click.TimeStretch)
        {
            if ((new Rectangle(100,900,1000,100)).contains(mo.getPoint()))
            {
                timeWarp=((mo.getX()-100));
                if (timeWarp<0)
                    timeWarp=0;
                if (timeWarp>1000)
                    timeWarp=1000;
                setTimeStretch();
            }
        }
        else if (Square.getRect().contains(mo.getPoint()))
        {
            selected=Square.getSquareByPixel(mo.getX(),mo.getY());
            if (selected.isError())
                selected=null;
        }
        else selected=null;
    }
    public void mouseMoved(MouseEvent mo)
    {
        if (Square.getRect().contains(mo.getPoint()))
        {
            selected=Square.getSquareByPixel(mo.getX(),mo.getY());
            if (selected.isError())
                selected=null;
        }
        else selected=null;
    }
    public void mousePressed(MouseEvent mo)
    {
        clickObj=null;
        if ((new Rectangle(100,900,1000,100)).contains(mo.getPoint()))
        {
            nextClick=Click.TimeStretch;
            timeWarp=((mo.getX()-100));
            if (timeWarp<0)
                timeWarp=0;
            if (timeWarp>1000)
                timeWarp=1000;
        }
        else if (Square.getRect().contains(mo.getPoint())) 
        {
            Square pressed=Square.getSquareByPixel(mo.getX(),mo.getY());
            if (!pressed.isError()) {
                if (nextClick.drag)
                {
                    clickObj=pressed;
                }
                else if (nextClick==Click.Mountain)
                {
                    if (shift)
                        Map.makePeak(120,20, pressed);
                    else
                        Map.makePeak(60, 10, pressed);
                }
                else if (nextClick==Click.Valley)
                {
                    if (shift)
                        Map.makePeak(-120,20, pressed);
                    else
                        Map.makePeak(-60, 10, pressed);
                }
            }
        }
    }
    public void mouseReleased(MouseEvent mo)
    {
        if (clickObj instanceof Square)
        {
            Square from=(Square)clickObj;
            Square pressed=Square.getNrstSquare(mo.getX(),mo.getY());
            if (nextClick.selectSquare)
            {
                int dx=(from.x()<pressed.x()?1:-1);
                int dy=(from.y()<pressed.y()?1:-1);
                for (int x=from.x();x!=pressed.x()+dx;x+=dx)
                {
                    for (int y=from.y();y!=pressed.y()+dy;y+=dy)
                    {
                        if (nextClick==Click.Obliterate)
                        {
                            if (Square.get(x,y).occupant!=null)
                                Square.get(x,y).occupant.loc=Square.error;//its safer to delete it later
                            Square.get(x,y).animals.clear();
                            if (shift)
                            {
                                Square.get(x,y).setTerrain(new Grass(Square.get(x,y)));
                                ((Grass)Square.get(x,y).terrain()).putUnderground();
                                Square.get(x,y).terrain().moisture=0;
                            }
                        }
                        else if (nextClick==Click.Dirtify)
                        {
                            Square.get(x,y).setTerrain(new Terrain.Dirt(Square.get(x,y)));
                            if (shift)
                            {
                                if (Square.get(x,y).occupant!=null)
                                    Square.get(x,y).occupant.loc=Square.error;
                                Square.get(x,y).animals.clear();
                                Square.get(x,y).terrain().moisture=0;
                            }
                        }
                        else if (nextClick==Click.Log)
                        {
                            if (Square.get(x,y).occupant!=null)
                                Square.get(x,y).occupant.kill();
                            Log prev=null;
                            if (Math.abs(from.x()-pressed.x())>Math.abs(from.y()-pressed.y()))
                            {
                                if (x!=from.x())
                                {
                                    if (Square.get(x-dx,y).occupant instanceof Log)
                                        prev=(Log)Square.get(x-dx,y).occupant;
                                }
                            }
                            else
                            {
                                if (y!=from.y())
                                {
                                    if (Square.get(x,y-dy).occupant instanceof Log)
                                        prev=(Log)Square.get(x,y-dy).occupant;
                                }
                            }
                            Square.get(x,y).occupant=new Log(Square.get(x,y),prev);
                        }
                        else if (nextClick==Click.Dry)
                        {
                            Square.get(x,y).terrain().moisture=0;
                            if (shift)
                            {
                                Square.get(x,y).air.setHumidity(0);
                                Square.get(x,y).air.setRain(0);
                                Square.get(x,y).air.setClouds(0);
                            }
                        }
                        else if (nextClick==Click.Water)
                        {
                            Square.get(x,y).terrain().moisture+=Earth.MOISTURE_PER_CUBIC_METER*(shift? 300 : 5);
                        }
                        else if (nextClick==Click.Fire)
                        {
                            Square.get(x,y).air.giveHeat(shift? 2000 : 200);
                            if (shift)
                                Square.get(x,y).terrain().heat+=200;
                        }
                        else if (nextClick==Click.Ice)
                        {
                            if (shift)
                            {
                                Square.get(x,y).terrain().heat=1f;
                                Square.get(x,y).air.setHeat(1f);
                                if (Square.get(x,y).occupant!=null)
                                    Square.get(x,y).occupant.heat=1f;
                                for (int n=0;n<Square.get(x,y).animals.size();n++)
                                    Square.get(x,y).animals.get(n).heat=1f;
                            }
                            else 
                            {
                                Square.get(x,y).air.giveHeat(-200);
                                if (Square.get(x,y).air.heat()<1f)
                                    Square.get(x,y).air.setHeat(1f);
                            }
                        }
                        else if (nextClick==Click.AddAir)
                        {
                            if (shift)
                            {
                                Square.get(x,y).air.giveMass(1f*Square.get(x,y).air.volume());
                            }
                            else
                                Square.get(x,y).air.setMass(.01f*Square.get(x,y).air.volume());
                        }
                        else if (nextClick==Click.Still)
                        {
                            Square.get(x,y).air.windSpeed().xVel=0;
                            Square.get(x,y).air.windSpeed().yVel=0;
                            Square.get(x,y).air.newState().windSpeed().xVel=0;
                            Square.get(x,y).air.newState().windSpeed().yVel=0;
                        }
                    }
                }
            }
            else if (nextClick==Click.River)
            {
                Map.makeRiver(from, pressed, (shift?90:30), shift?5:2, shift?7:5);
            }
            clickObj=null;
        }
        else if (nextClick==Click.TimeStretch)
            if ((new Rectangle(100,900,1000,100)).contains(mo.getPoint()))
            {
                timeWarp=((mo.getX()-100));
                if (timeWarp<0)
                    timeWarp=0;
                if (timeWarp>1000)
                    timeWarp=1000;
                setTimeStretch();
            }
    }
    public void mouseClicked(MouseEvent mo)
    {}
    

    
    public static Eden frame;
    public static void main(String[] args)
    {
        frame = new Eden();
    }
    public static void pause() {
        if (frame!=null)
            frame.paused=true;
    }
}
