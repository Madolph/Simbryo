package simbryo.synthoscopy.microscope.lightsheet;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import simbryo.util.geom.GeometryUtils;

/*
 * Light sheet microscope with a user defined transformation matrix of the second camera   
 *  
 */

public class LightSheetMicroscopeSimulatorXWing extends
                                                LightSheetMicroscopeSimulator
{

  public LightSheetMicroscopeSimulatorXWing(ClearCLContext pContext,
                                            Matrix4f pMisalignmentCamera,
                                            int pMaxCameraResolution,
                                            long... pMainPhantomDimensions)
  {

    super(pContext, pMainPhantomDimensions);

    if (pMainPhantomDimensions.length != 3)
      throw new IllegalArgumentException("Phantom dimensions must have 3 components: (width,height,depth).");
    double gammazero = toRadians(30);
    float ax = (float) cos(gammazero);
    float ay = (float) sin(gammazero);

    Vector3f lIlluminationAxisVector0 = new Vector3f(ax, ay, 0);
    Vector3f lIlluminationAxisVector1 = new Vector3f(ax, -ay, 0);
    Vector3f lIlluminationAxisVector2 = new Vector3f(-ax, ay, 0);
    Vector3f lIlluminationAxisVector3 = new Vector3f(-ax, -ay, 0);
    Vector3f lIlluminationNormalVector01 = new Vector3f(0, 0, 1);
    Vector3f lIlluminationNormalVector23 = new Vector3f(0, 0, -1);

    addLightSheet(lIlluminationAxisVector0,
                  lIlluminationNormalVector01);

    addLightSheet(lIlluminationAxisVector1,
                  lIlluminationNormalVector01);

    addLightSheet(lIlluminationAxisVector2,
                  lIlluminationNormalVector23);

    addLightSheet(lIlluminationAxisVector3,
                  lIlluminationNormalVector23);

    int lMaxCameraImageWidth = pMaxCameraResolution;
    int lMaxCameraImageHeight = pMaxCameraResolution;

    Matrix4f lDetectionMatrix0 = new Matrix4f();
    lDetectionMatrix0.setIdentity();

    Vector3f lDetectionUpDownVector0 = new Vector3f(0, 1, 0);

    addDetectionPath(lDetectionMatrix0,
                     lDetectionUpDownVector0,
                     lMaxCameraImageWidth,
                     lMaxCameraImageHeight);

    Matrix4f lDetectionMatrix1 = GeometryUtils.rotY((float) Math.PI,
                                                    new Vector3f(0.5f,
                                                                 0.5f,
                                                                 0.5f));

    // lDetectionMatrix1 = GeometryUtils.multiply(lDetectionMatrix1,
    // GeometryUtils.scale(new Vector3f(1, 1, -1),new Vector3f(.5f, .5f, .5f)));

    lDetectionMatrix1 = GeometryUtils.multiply(pMisalignmentCamera,
                                               lDetectionMatrix1);

    Vector3f lDetectionUpDownVector1 = new Vector3f(0, 1, 0);

    addDetectionPath(lDetectionMatrix1,
                     lDetectionUpDownVector1,
                     lMaxCameraImageWidth,
                     lMaxCameraImageHeight);

    try

    {
      buildMicroscope();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

  }

}
