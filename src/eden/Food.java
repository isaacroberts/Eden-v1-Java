package eden;

public interface Food {
    public void eat(int mass);
    public boolean isRotten();
    public Thing.Type[] edible={Thing.Type.Animal,Thing.Type.Carcass,Thing.Type.Froot,
        Thing.Type.Grass,Thing.Type.Tree};
    public float getCalories();
}