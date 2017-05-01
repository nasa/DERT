package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.landscape.factory.LayerFactory;
import gov.nasa.arc.dert.raster.ProjectionInfo;
import gov.nasa.arc.dert.raster.geotiff.GTIF;
import gov.nasa.arc.dert.raster.geotiff.GeoKey;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;

public class TestDemFactory {
	
	private int size, halfSize;
	private ByteBuffer bBuf;
	private float minValue, maxValue;
	
	public TestDemFactory(int size) {
		this.size = size;
		bBuf = ByteBuffer.allocate(size*size*4);
		bBuf.order(ByteOrder.nativeOrder());
		minValue = Float.MAX_VALUE;
		maxValue = -Float.MAX_VALUE;
		halfSize = size/2;
		for (int r=halfSize; r>-halfSize; --r) {
			for (int c=-halfSize; c<halfSize; ++c) {
				float z = getZ(c, r);
				bBuf.putFloat(z);
				if (!Float.isNaN(z)) {
					minValue = Math.min(minValue, z);
					maxValue = Math.max(maxValue, z);
				}
			}
		}
		bBuf.rewind();
		System.out.println("TestDemFactory min elev="+minValue+" max elev="+maxValue);
	}
	
	public float getZ(double c, double r) {
		double cc = 2*Math.PI*c/(double)halfSize;
		double rr = 2*Math.PI*r/(double)halfSize;
		double d = Math.sqrt(cc*cc+rr*rr);
		float z = (float)Math.sin(d)*100f;	
		return(z);
	}
	
	public boolean createDem(String filename) {
		try {
			Properties properties = new Properties();
			GTIF gtif = new GTIF(filename, properties);
			gtif.open("w");
			ProjectionInfo projInfo = ProjectionInfo.createDefault(size, size, 1);	
			projInfo.pcsCode = GeoKey.Code_UserDefined;
			projInfo.gcsCode = GeoKey.Code_GCS_WGS_84;
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_IMAGEWIDTH, size);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_IMAGELENGTH, size);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_PLANARCONFIG, GTIF.PLANARCONFIG_CONTIG);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_ROWSPERSTRIP, 1);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_BITSPERSAMPLE, 32);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_SAMPLEFORMAT, GTIF.SAMPLEFORMAT_IEEEFP);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_SAMPLESPERPIXEL, 1);
			gtif.setTIFFFieldDouble(GTIF.TIFFTAG_SMINSAMPLEVALUE, minValue);
			gtif.setTIFFFieldDouble(GTIF.TIFFTAG_SMAXSAMPLEVALUE, maxValue);
			gtif.setProjectionInfo(projInfo);
			
			byte[] bytes = new byte[size*4];
			ByteBuffer outBuf = ByteBuffer.allocateDirect(size*4);
			for (int r=0; r<size; ++r) {
				bBuf.position(r*size*4);
				bBuf.get(bytes);
				outBuf.put(bytes);
				outBuf.rewind();
				long n = gtif.writeStrip(r, outBuf, size*4);
				if (n != size*4) {
					System.err.println("TestDemFactory.writeDem invalid return from writeStrip. Bytes written = "+n);
					break;
				}
			}

			gtif.close();
			return(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return(false);
	}
	
	public boolean createLandscape(String testLoc) {
		
		System.err.println("Create "+testLoc);
		File testDir = new File(testLoc);
		if (!testDir.exists()) {
			if (!testDir.mkdir())
				return(false);
			File dertDir = new File(testLoc, "dert");
			if (!dertDir.mkdir())
				return(false);
		}
		
		System.err.println("Create DEM");
		if (!createDem(testLoc+"/testdem.tif"))
			return(false);
		
		System.err.println("Create layer");
		String[] args = new String[] {"-landscape="+testLoc, "-file="+testLoc+"/testdem.tif", "-tilesize=128", "-type=elevation"};
		LayerFactory lf = new LayerFactory(args);
		if (!lf.createLayer())
			return(false);
		
		System.err.println("Created layer successfully.\n");
		return(true);
	}
	
	public static void main(String[] arg) {
		TestDemFactory factory = new TestDemFactory(2048);
		factory.createDem("/tmp/testdem.tif");
	}

}
