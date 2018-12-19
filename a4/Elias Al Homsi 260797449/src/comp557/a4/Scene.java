package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Color3f;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {

	/** List of surfaces in the scene */
	public List<Intersectable> surfaceList = new ArrayList<Intersectable>();

	/** All scene lights */
	public Map<String, Light> lights = new HashMap<String, Light>();

	/** Contains information about how to render the scene */
	public Render render;

	/** The ambient light color */
	public Color3f ambient = new Color3f();

	/**
	 * Default constructor.
	 */
	public Scene() {
		this.render = new Render();
	}

	/**
	 * renders the scene
	 */
	public void render(int row, int col, int gridSize, boolean showPanel) {
		Camera cam = render.camera;
		int w = cam.imageSize.width;
		int h = cam.imageSize.height;
		int startJ = col * (w / gridSize);
		int startI = row * (h / gridSize);
		int ww = w / gridSize;
		int hh = h / gridSize;

		// super sampling uniform grid with jitter
		// calculate number of intersections always use higher number of samples
		int areas = 0;
		int samples = render.samples;
		FastPoissonDisk fdp = null;
		if(render.depthOfField) {
			fdp = new FastPoissonDisk();
		} 
		
		double root = Math.sqrt(samples);
		areas = (int) Math.ceil(root); // get more samples

		// for every pixel
		for (int i = startI; i < hh + startI && !render.isDone(); i++) {
			for (int j = startJ; j < ww + startJ && !render.isDone(); j++) {

				int rAcc = 0;
				int gAcc = 0;
				int bAcc = 0;

				double center = (1.0d / areas) / 2.0d;
				double[] offset = new double[2];

				// for each row
				for (int r = 0; r < areas; r++) {
					if (render.jitter)
						center = Math.random() * (1.0d / areas);

					offset[0] = (double) r / areas + center;

					// for each column
					for (int c = 0; c < areas; c++) {
						if (render.jitter)
							center = Math.random() * (1.0d / areas);

						offset[1] = (double) c / areas + center;

						// build depth of field
						if (render.depthOfField) {
							Color3f avgcolor = new Color3f();
							for (int depthSample = 0; depthSample < 32; depthSample++) {
								// shoot a ray at row i and column j and try to intersect
								Point2d factors = fdp.get(depthSample, 32);
								
								//find u and v
								Vector3d u = cam.up;
								u.normalize();
								Vector3d v = new Vector3d();
								Vector3d dir = new Vector3d();
								dir.set(cam.to);
								dir.sub(cam.from);
								v.cross(u, dir);
								v.normalize();
								
								//the aperture size
								double apertureSize = 2.0;
								
								//find the displacement
								u.scale(factors.x);
								v.scale(factors.y);
								double xdisplacement = u.dot(new Vector3d(1.0, 0, 0)) + v.dot(new Vector3d(1.0, 0, 0));
								double ydisplacement = u.dot(new Vector3d(0, 1.0, 0)) + v.dot(new Vector3d(0, 1.0, 0));
								double zdisplacement = u.dot(new Vector3d(0, 0, 1.0)) + v.dot(new Vector3d(0, 0, 1.0));
								xdisplacement *= apertureSize;
								ydisplacement *= apertureSize;
								zdisplacement *= apertureSize;
								Point3d newEyePoint = new Point3d(cam.from.x + xdisplacement, cam.from.y + ydisplacement, cam.from.z + zdisplacement);

								
								//generate new ray
								Ray ray = new Ray();
								generateRay(j, i, offset, new Camera("depthtest", newEyePoint, cam.to, cam.up, cam.fovy, cam.imageSize), ray);

								Color3f color = new Color3f();
								color.set(render.bgcolor);

								IntersectResult closest = new IntersectResult();
								for (Intersectable in : surfaceList) {
									IntersectResult result = new IntersectResult();
									in.intersect(ray, result);
									if (result.t != Double.POSITIVE_INFINITY
											&& (closest.t == Double.POSITIVE_INFINITY || result.t < closest.t))
										closest = result;
								}

								// shading
								if (closest.t != Double.POSITIVE_INFINITY) {
									// Ambient shading for rgb
									color.x = closest.material.diffuse.x * ambient.x;
									color.y = closest.material.diffuse.y * ambient.y;
									color.z = closest.material.diffuse.z * ambient.z;

									for (Light light : lights.values()) {
										boolean inShadowForAllShapes = false;

										for (Intersectable in : surfaceList) {
											IntersectResult shadowResult = new IntersectResult();
											Ray shadowRay = new Ray();
											if (inShadow(closest, light, in, shadowResult, shadowRay)) {
												inShadowForAllShapes = true;
												break;
											}
										}

										if (!inShadowForAllShapes) {
											Vector3d lightDirection = new Vector3d(light.from);
											lightDirection.sub(closest.p);
											lightDirection.normalize();

											// Diffuse Lambertian shading for rgb
											color.x += closest.material.diffuse.x * light.color.x * light.power
													* Math.max(0, closest.n.dot(lightDirection));
											color.y += closest.material.diffuse.y * light.color.y * light.power
													* Math.max(0, closest.n.dot(lightDirection));
											color.z += closest.material.diffuse.z * light.color.z * light.power
													* Math.max(0, closest.n.dot(lightDirection));

											// BlinnPhong shading for rgb
											Vector3d half = new Vector3d(ray.viewDirection);
											half.negate();
											half.add(lightDirection);
											half.normalize();

											double factor = Math.pow(Math.max(0, half.dot(closest.n)),
													closest.material.shinyness);

											color.x += closest.material.specular.x * light.power * factor;
											color.y += closest.material.specular.y * light.power * factor;
											color.z += closest.material.specular.z * light.power * factor;
										}
									}
								}

								//add to avg color
								avgcolor.x += color.x;
								avgcolor.y += color.y;
								avgcolor.z += color.z;
							}

							rAcc += 255 * avgcolor.x/32;
							gAcc += 255 * avgcolor.y/32;
							bAcc += 255 * avgcolor.z/32;
						} else {
							// shoot a ray at row i and column j and try to intersect
							Ray ray = new Ray();
							generateRay(j, i, offset, render.camera, ray);

							Color3f color = new Color3f();
							color.set(render.bgcolor);

							IntersectResult closest = new IntersectResult();
							for (Intersectable in : surfaceList) {
								IntersectResult result = new IntersectResult();
								in.intersect(ray, result);
								if (result.t != Double.POSITIVE_INFINITY
										&& (closest.t == Double.POSITIVE_INFINITY || result.t < closest.t))
									closest = result;
							}

							// shading
							if (closest.t != Double.POSITIVE_INFINITY) {
								// Ambient shading for rgb
								color.x = closest.material.diffuse.x * ambient.x;
								color.y = closest.material.diffuse.y * ambient.y;
								color.z = closest.material.diffuse.z * ambient.z;

								for (Light light : lights.values()) {
									boolean inShadowForAllShapes = false;

									for (Intersectable in : surfaceList) {
										IntersectResult shadowResult = new IntersectResult();
										Ray shadowRay = new Ray();
										if (inShadow(closest, light, in, shadowResult, shadowRay)) {
											inShadowForAllShapes = true;
											break;
										}
									}

									if (!inShadowForAllShapes) {
										Vector3d lightDirection = new Vector3d(light.from);
										lightDirection.sub(closest.p);
										lightDirection.normalize();

										// Diffuse Lambertian shading for rgb
										color.x += closest.material.diffuse.x * light.color.x * light.power
												* Math.max(0, closest.n.dot(lightDirection));
										color.y += closest.material.diffuse.y * light.color.y * light.power
												* Math.max(0, closest.n.dot(lightDirection));
										color.z += closest.material.diffuse.z * light.color.z * light.power
												* Math.max(0, closest.n.dot(lightDirection));

										// BlinnPhong shading for rgb
										Vector3d half = new Vector3d(ray.viewDirection);
										half.negate();
										half.add(lightDirection);
										half.normalize();

										double factor = Math.pow(Math.max(0, half.dot(closest.n)),
												closest.material.shinyness);

										color.x += closest.material.specular.x * light.power * factor;
										color.y += closest.material.specular.y * light.power * factor;
										color.z += closest.material.specular.z * light.power * factor;
									}
								}
							}

							rAcc += 255 * color.x;
							gAcc += 255 * color.y;
							bAcc += 255 * color.z;
						}
					}
				}

				// calculate the average
				int r = (int) (rAcc / Math.pow(areas, 2));
				int g = (int) (gAcc / Math.pow(areas, 2));
				int b = (int) (bAcc / Math.pow(areas, 2));
				int a = 255;

				// clamp issues fixed
				r = Math.min(r, 255);
				g = Math.min(g, 255);
				b = Math.min(b, 255);

				int argb = (a << 24 | r << 16 | g << 8 | b);

				render.setPixel(j, i, argb);
			}
		}
	}

	/**
	 * Generate a depth ray through pixel (i,j).
	 * 
	 * @param i      The pixel row.
	 * @param j      The pixel column.
	 * @param offset The offset from the lower left corner of the pixel, in the
	 *               range [0.0,1.0] for each coordinate.
	 * @param cam    The camera.
	 * @param ray    Contains the generated ray.
	 */
	public static void generateDepthRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		// calculate u and v "check slides raytracing1"
		double w = cam.imageSize.width;
		double h = cam.imageSize.height;
		double ratio = w / h;

		double d = cam.from.distance(cam.to);

		double t = Math.tan(Math.toRadians(cam.fovy) / 2.0d) * d;
		double r = ratio * t;
		double l = -r;
		double b = -t;

		double u = l + (r - l) * (i + offset[0]) / w;
		double v = t - (t - b) * (j + offset[1]) / h;

		Vector3d zAxis = new Vector3d(cam.from);
		zAxis.sub(cam.to);
		zAxis.normalize();

		Vector3d xAxis = new Vector3d();
		xAxis.cross(cam.up, zAxis);
		xAxis.normalize();

		Vector3d yAxis = new Vector3d();
		yAxis.cross(zAxis, xAxis);
		yAxis.normalize();

		// s = e + Uu + Vv − Dw
		// d = s - e
		// d = Uu + Vv − Dw
		Vector3d dir = new Vector3d();
		xAxis.scale(u);
		yAxis.scale(v);
		zAxis.scale(d);

		dir.add(xAxis);
		dir.add(yAxis);
		dir.sub(zAxis);
		dir.normalize();
		ray.viewDirection.set(dir);
	}

	/**
	 * Generate a ray through pixel (i,j).
	 * 
	 * @param i      The pixel row.
	 * @param j      The pixel column.
	 * @param offset The offset from the lower left corner of the pixel, in the
	 *               range [0.0,1.0] for each coordinate.
	 * @param cam    The camera.
	 * @param ray    Contains the generated ray.
	 */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		// set eye point
		ray.eyePoint.set(cam.from);

		// calculate u and v "check slides raytracing1"
		double w = cam.imageSize.width;
		double h = cam.imageSize.height;
		double ratio = w / h;

		double d = cam.from.distance(cam.to);

		double t = Math.tan(Math.toRadians(cam.fovy) / 2.0d) * d;
		double r = ratio * t;
		double l = -r;
		double b = -t;

		double u = l + (r - l) * (i + offset[0]) / w;
		double v = t - (t - b) * (j + offset[1]) / h;

		Vector3d zAxis = new Vector3d(cam.from);
		zAxis.sub(cam.to);
		zAxis.normalize();

		Vector3d xAxis = new Vector3d();
		xAxis.cross(cam.up, zAxis);
		xAxis.normalize();

		Vector3d yAxis = new Vector3d();
		yAxis.cross(zAxis, xAxis);
		yAxis.normalize();

		// s = e + Uu + Vv − Dw
		// d = s - e
		// d = Uu + Vv − Dw
		Vector3d dir = new Vector3d();
		xAxis.scale(u);
		yAxis.scale(v);
		zAxis.scale(d);

		dir.add(xAxis);
		dir.add(yAxis);
		dir.sub(zAxis);
		dir.normalize();
		ray.viewDirection.set(dir);
	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result       Intersection result from raytracing.
	 * @param light        The light to check for visibility.
	 * @param root         The Intersectable node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay    Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise.
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final Intersectable root,
			IntersectResult shadowResult, Ray shadowRay) {
		// set up the shadow ray
		Vector3d l = new Vector3d();
		l.sub(light.from, result.p);
		double lightDistance = l.length();
		l.normalize();

		// prevent self shadowing
		Vector3d epsilon = new Vector3d(l);
		epsilon.scale(1e-6);

		Point3d origin = new Point3d(result.p);
		origin.add(epsilon);

		shadowRay.eyePoint = origin;
		shadowRay.viewDirection = new Vector3d(l);

		root.intersect(shadowRay, shadowResult);
		if (shadowResult.t < 1e-6)
			return false;

		if (shadowResult.t != Double.POSITIVE_INFINITY && shadowResult.t <= lightDistance)
			return true;
		return false;
	}
}
