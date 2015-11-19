# Desktop Exploration of Remote Terrain

Desktop Exploration of Remote Terrain (DERT) is a viewer for exploring large digital terrain models (DTM) in 3D. DTMs generally consist of a digital elevation model (DEM) and one or more co-registered ortho-images. These data sets have been collected by NASA planetary missions such as Mars Reconnaissance Orbiter, Mars Global Surveyor, and Lunar Reconnaissance Orbiter (LRO) as well as the Landsat and Shuttle Radar Topography terrestrial missions.

DERT was written at the NASA Ames Research Center in the Intelligent Systems Division leveraging code from science support applications implemented for a number of NASA missions including Phoenix Lander and Mars Science Laboratory. DERT itself is funded by the Mars Reconnaissance Orbiter mission in collaboration with Malin Space Science Systems. DERT is licensed under the NASA Open Source Agreement (NOSA).

DERT employs a virtual world to display the DTM. It attempts to stay true to the data in scale and color. In addition to visualization DERT provides:

* Measurement tools for distance, slope, area, and volume
* Positioning of artificial and solar light
* Shadows
* Layering of multiple ortho-images with adjustable transparency
* Landmarks
* Terrain profile
* Cutting plane with terrain difference map
* Through-the-lens view from a camera positioned on the terrain surface
* Terrain height exaggeration

To maintain DERT's interactivity, DTMs must be converted into a multi-resolution file structure called a "Landscape". LayerFactory, a companion application, is provided for this purpose. A Landscape is a directory of co-registered multi-resolution tiled raster pyramids. Each pyramid consists of a quad-tree of tiles representing a raster file. Each branch of the quad-tree contains a portion of the raster at a higher resolution and covering less area than its parent.

DERT creates its virtual world from a landscape. As the user navigates through the world, near tiles are replaced with those of higher resolution while far tiles are replaced with those of less detail. Tiles are stitched together at their edges before rendering.

DERT is available for Mac OS X and Linux platforms.  It is written in Java and uses several third party libraries for rendering, cartographic projection, file access, and lighting. These libraries are written in C and wrapped with the Java Native Interface.

