/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

import java.awt.*;

public class LoadingScreen 
{
    public enum WorkingOn {
        Starting("Initializing Light"),
        Desert("Defining the Heavens and Earth"),
        Mountain("Establishing a Firmament"),
        River("Creating The Oceans"),
        Placing("Creating the Plants and the Rocks"),
        Animals("Filling the Earth with Life"),
        RunningWater("Resting"),
        ;
        String display;
        WorkingOn(String text)
        {
            display=text;
        }
    }
    public double percentage;// 0 thru 1
    public WorkingOn current;
    public LoadingScreen() {
        percentage=0;
        current=WorkingOn.Starting;
    }
    public void draw(Graphics2D g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,1500,1000);
        g.setColor(Color.WHITE);
        g.fillRect(550,500,402,102);
        g.setColor(Color.BLUE);
        g.fillRect(551,501,(int)(percentage*400.0),100);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial",Font.PLAIN,29));
        g.drawString(current.display,555,490);
    }
}
