package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.raster.ProjectionInfo;
import gov.nasa.arc.dert.raster.geotiff.GTIF;
import gov.nasa.arc.dert.raster.geotiff.GeoKey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;

public class TestDemFactory {
	
	private int size = 2048;
	private ByteBuffer bBuf;
	private float minValue, maxValue;
	
	public TestDemFactory() {
		bBuf = ByteBuffer.allocate(size*size*4);
		bBuf.order(ByteOrder.nativeOrder());
		minValue = Float.MAX_VALUE;
		maxValue = -Float.MAX_VALUE;
		int halfSize = size/2;
		for (int r=halfSize; r>-halfSize; --r) {
			for (int c=-halfSize; c<halfSize; ++c) {
				double cc = 2*Math.PI*c/(double)halfSize;
				double rr = 2*Math.PI*r/(double)halfSize;
				double d = Math.sqrt(cc*cc+rr*rr);
				float z = (float)Math.sin(d)*100f;
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
	
	public void createDem() {
		try {
			Properties properties = new Properties();
			GTIF gtif = new GTIF("/tmp/testdem.tif", properties);
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] arg) {
		TestDemFactory factory = new TestDemFactory();
		factory.createDem();
	}

}
