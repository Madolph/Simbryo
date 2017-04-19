package simbryo.synthoscopy.microscope.lightsheet;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import simbryo.util.geom.GeometryUtils;

/*
 * Light sheet microscope with a user defined transformation matrix of the second camera   
 *  
 */

public class LightSheetMicroscopeSimulatorOrthoNonregistered extends
                                                             LightSheetMicroscopeSimulator
{

  public LightSheetMicroscopeSimulatorOrthoNonregistered(ClearCLContext pContext,
                                                         Matrix4f pDetectionMatrixCamera2,
                                                         int pMaxCameraResolution,
                                                         long... pMainPhantomDimensions)
  {
    super(pContext, pMainPhantomDimensions);

    if (pMainPhantomDimensions.length != 3)
      throw new IllegalArgumentException("Phantom dimensions must have 3 components: (width,height,depth).");

    // light sheet 1
    Vector3f lIlluminationAxisVector1 = new Vector3f(1, 0, 0);
    Vector3f lIlluminationNormalVector1 = new Vector3f(0, 0, 1);

    addLightSheet(lIlluminationAxisVector1,
                  lIlluminationNormalVector1);

    // light sheet 1
    Vector3f lIlluminationAxisVector2 = new Vector3f(-1, 0, 0);
    Vector3f lIlluminationNormalVector2 = new Vector3f(0, 0, -1);

    addLightSheet(lIlluminationAxisVector2,
                  lIlluminationNormalVector2);

    int lMaxCameraImageWidth = pMaxCameraResolution;
    int lMaxCameraImageHeight = pMaxCameraResolution;

    // camera 1
    Matrix4f lDetectionMatrix1 = new Matrix4f();
    lDetectionMatrix1.setIdentity();

    Vector3f lDetectionUpDownVector1 = new Vector3f(0, 1, 0);

    addDetectionPath(lDetectionMatrix1,
                     lDetectionUpDownVector1,
                     lMaxCameraImageWidth,
                     lMaxCameraImageHeight);

    // camera 2

    Vector3f lDetectionUpDownVector2 = new Vector3f(0, 1, 0);

    Matrix4f lMat =
                  GeometryUtils.rotY((float) Math.PI,
                                     new Vector3f(0.5f, 0.5f, 0.5f));

    // addDetectionPath(pDetectionMatrixCamera2, lDetectionUpDownVector2,
    // lMaxCameraImageWidth, lMaxCameraImageHeight);
    addDetectionPath(lMat,
                     lDetectionUpDownVector2,
                     lMaxCameraImageWidth,
                     lMaxCameraImageHeight);

    buildMicroscope();

  }

}
