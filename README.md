# Desktop Exploration of Remote Terrain

Desktop Exploration of Remote Terrain (DERT) is a viewer for visualizing very large digital terrain models (DTM) in 3D. It was written at the NASA Ames Research Center in the Intelligent Systems Division leveraging code from science support applications implemented for a number of NASA planetary missions including Phoenix Lander and Mars Science Laboratory. DERT itself is funded by the Mars Reconnaissance Orbiter mission in collaboration with Malin Space Science Systems.

DERT employs a virtual world to display the DTM. It attempts to stay true to the data in scale and color.  In addition to visualization DERT provides:

* Measurement tools for distance, slope, area, and volume
* Positioning of artificial and solar light
* Shadows
* Layering of multiple ortho-images with adjustable transparency
* Landmarks
* Terrain profile
* Cutting plane with terrain difference map
* Through-the-lens view from a camera positioned on the terrain surface
* Terrain height exaggeration

A companion application called LayerFactory pre-processes the DTM raster files into a file structure called a "Landscape". A Landscape is a directory of multi-resolution tiled raster pyramids. Each pyramid is a directory containing a quad-tree of raster tiles. Each branch of the quad-tree contains a portion of the raster at a higher resolution and covering less area than its parent.

As the user navigates through the virtual world, near tiles are replaced with those of higher resolution while far tiles are replaced with those of less detail. Tiles are stitched together at their edges before rendering. This process allows viewing of very large DTMs.

DERT is available for Mac OS X and Linux platforms.  It is written in Java and uses several third party libraries for rendering, cartographic projection, file access, and lighting. These libraries are written in C and wrapped with the Java Native Interface.

