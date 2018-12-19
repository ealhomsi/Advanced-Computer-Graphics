package comp557.a4;

public class RenderingTask extends Thread {
	public int row;
	public int col;
	public int gridSize;
    public double rtime;
    public Scene scene;
    
    public RenderingTask(int row, int col, int gridSize, Scene scene) { 
        this.row = row;
        this.col = col;
        this.gridSize = gridSize;
        this.scene = scene;
    }

    @Override
    public void run() {
        try {
            long rstart = System.nanoTime();
            scene.render(row, col, gridSize, true);
            rtime = (double) ((System.nanoTime() - rstart) / 1E9);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load simulation input file.", e);
        }
    }
}