#!/bin/sh

# This script gets the SPICE kernels and puts them in a directory called kernels.
# Run it from the DERT installation directory.

# Edit dert.properties to add or remove kernel files.

# Gravitational constant x mass (GM) values
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/pck/gm_de431.tpc > dert.app/Contents/Java/kernels/gm_de431.tpc

# Generic orientation data
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/pck/pck00010.tpc > dert.app/Contents/Java/kernels/kernels/pck00010.tpc

# Leapseconds kernel
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/lsk/naif0011.tls > dert.app/Contents/Java/kernels/kernels/naif0011.tls

# Generic Planet Ephemeris
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/de430.bsp > dert.app/Contents/Java/kernels/kernels/de430.bsp

# Update to de430 for New Horizons
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/de432s.bsp > dert.app/Contents/Java/kernels/kernels/de432s.bsp

# Mars Planet Ephemeris
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellite/mar097.bsp > dert.app/Contents/Java/kernels/kernels/mar097.bsp

# Pluto Planet Ephemeris
curl http://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellite/plu055.bsp > dert.app/Contents/Java/kernels/kernels/plu055.bsp
