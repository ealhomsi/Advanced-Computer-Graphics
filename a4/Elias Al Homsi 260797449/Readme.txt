Hello,

To whom it may concern:
Name: Elias AL Homsi
id: 260797449

Whats included:

1) This Readme file.
2) source code
3) a4 data which contains the xml and obj files
4) pictures


Description of image/xml pair:

AACheckerPlane.xml: provided with the assignmnet, checker plane
BoxRGBLights.xml: provided with the assignment, a box with 3 lights of 3 different colors on each visible face
BoxStacks.xml: provided with the assignment, a collection of 4 pillars done with stack of boxes (use of multi threading here).
Cornell.xml: provided with the assignment:  a room with a box and a sphere with lighting.
MetaBalls.xml attempt to render MetaBalls using raymarching which is very very very slow. define a simple metaball
NovelScene.xml: a bunny on the side with two pillars of a box with a sphere on top. 
Plane2.xml: provided with the assignmnet,shows a plane with two circles
Plane.xml: provided with the assignmnet, shows an empty plane (similar to the first part but with larger resolution)
Sphere.xml:  provided with the assignmnet, just a sphere.
TorusMesh.xml:  provided with the assignmnet, using torus.obj
TwoSpheresPlane.xml:  provided with the assignmnet, two spheres on a chess like background.

Extras: 

1. multi-threaded parallelization (0.5 marks)
the renderer would only output when all pixels are ready. meanwhile the screen would stay white.
can specifiy the number of threads in the xml file.
the running time of the threads is defined as the slowest thread.
partial results were turned of to make the rendering more efficient.
The number of threads have to be 2^n. (that is power of 2)

2. Generalization of slabs using slab collections (0.5 marks) "I think it is worth"
all objects like boxes should be treated as a collection of slabs
The slabs code is written in generalized way that allows you to define a generalized colleciton of slabs
to represent cubes and all kinds of shapes that can be made with intersection of planes.


3. metaballs (1.5 marks)
the feature is implemented correctly however, it takes ages to render using ray marching technique.
The rendering is so slow and it is bounded by a sphere of the closest point to the origin of the ray
and the furthest point from the ray.
The rendering kept running for 2 days and did not finish.

4. Depth of field blur (1 mark)
the feature is implemented on the level of collecting 32 different samples on an apperture size of 2.0
this would average overrall results and give them back to the user.
The depth of field is turned on using a flag in the renderer.

Thank you for your time and consideration,
Sincerely,
Elias

