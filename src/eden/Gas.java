/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eden;

/**
 *
 * @author Isaac
 */
public class Gas {
    
    public static final float SPECIFIC_HEAT=1f;
    Square loc;
    protected float mass;//kilograms per cubic meter
    protected float heat;//kelvin
    private float volume;
    public Gas(Square setLoc)
    {
        loc=setLoc;
        volume=10;
        mass=1*volume;
        heat=Map.worldHeat();
    }
    public Gas() {
        loc=null;
        mass=0;
        volume=1;
        heat=0;
    }
    public void loseHeat()
    {
        heat-=heat*Earth.getHeatLossToSpaceRate();
    }
    public void sendWind(Gas toWho,float airMovement)
    {
        float heatDif=heat*airMovement+toWho.heat*toWho.mass();
        heatDif/=airMovement+toWho.mass();
        heatDif-=toWho.heat;
        toWho.giveHeat(heatDif);
        toWho.setHeat(heatDif);
        if (density()>0 && airMovement<density())
        {
            giveMass(-airMovement);
            toWho.giveMass(airMovement);
        }
    }
    public void setDensity(float set) {
        set*=volume;
        mass=set;
    }
    public void setVolume(float set) {
        volume=set;
        if (volume<.001f)
            volume=.001f;
    }
    public void changeVolume(float delta) {
        volume+=delta;
        if (volume<=.001)
            volume=.001f;
    }
    public float volume() {
        return volume;
    }
    public float tallness() {
        return volume;//  h=V/SA; SA=1
    }
    public float pressure()
    {
        return heat*density();
    }
    public float specificHeat() {
        return SPECIFIC_HEAT*density();
    }
    public float density() {
        return mass/volume();
    }
    public float mass() {
        return mass;
    }
    public float weight() {
        return mass()*Earth.gravity;
    }
    public float heat()    {
        return heat;
    }
    public void setMass(float set) {
        mass=set;
    }
    public void setHeat(float set) {
        heat=set;
    }
    public void changeMass(float delta) {
        mass+=delta;
    }
    public void changeHeat(float delta) {
        heat+=delta;
    }
    public void giveMass(float amt)
    {
        mass+=amt;
    }
    public void giveHeat(float amt)
    {
        heat+=amt;
    }
}
