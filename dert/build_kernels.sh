#!/bin/tcsh

# This script gets the SPICE kernels and puts them in a directory called kernels.
# Run it from the dert project directory.

mkdir kernels
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/pck/gm_de431.tpc > kernels/gm_de431.tpc
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/pck/pck00010.tpc > kernels/pck00010.tpc
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/lsk/naif0011.tls > kernels/naif0011.tls
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/de430.bsp > kernels/de430.bsp
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/de432s.bsp > kernels/de432s.bsp
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellite/mar097.bsp > kernels/mar097.bsp
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellite/plu055.bsp > kernels/plu055.bsp
