package comp557.a1;


import java.util.Collection;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;

import mintools.parameters.DoubleParameter;

/**
 * Helper class for Double Parameter vector
 */
public class DoubleParameter3 {
  DoubleParameter xParam;
  DoubleParameter yParam;
  DoubleParameter zParam;

  public DoubleParameter3(String name, Tuple3d vector, Tuple2d xLimits, Tuple2d yLimits, Tuple2d zLimits, String prefix) {
    xParam = new DoubleParameter(name+" "+prefix+"x",vector.x, xLimits.x, xLimits.y);
    yParam = new DoubleParameter(name+" "+prefix+"y",vector.y, yLimits.x, yLimits.y);
    zParam = new DoubleParameter(name+" "+prefix+"z",vector.z, zLimits.x, zLimits.y);
  }
  
  public double x() {
    return xParam.getValue();
  }
  
  public double y() {
    return yParam.getValue();
  }
  
  public double z() {
    return zParam.getValue();
  }

  public void subscribe(Collection<DoubleParameter> list) {
    list.add(xParam);
    list.add(yParam);
    list.add(zParam);
  }
}
