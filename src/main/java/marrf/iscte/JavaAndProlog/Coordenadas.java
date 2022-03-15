package marrf.iscte.JavaAndProlog;

public class Coordenadas {

    private String tipo;
    private float x;
    private float y;

    public Coordenadas(){

    }

    public Coordenadas(double x, double y){
        this.x = (float) x;
        this.y = (float) y;
    }

    public float getX() {
        return x;
    }

    public String getTipo() {
        return tipo;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "coordenadas{" +
                "tipo='" + tipo + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
