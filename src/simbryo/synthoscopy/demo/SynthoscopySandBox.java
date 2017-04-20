package simbryo.synthoscopy.demo;

import java.io.File;
import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.io.RawWriter;
import clearcl.viewer.ClearCLImageViewer;

import org.junit.Test;

import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrtho;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorXWing;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;
import simbryo.util.geom.GeometryUtils;

/**
 * Light sheet illumination demo
 *
 * @author royer
 */
public class SynthoscopySandBox
{

  /**
   * Demo
   * 
   * @throws IOException
   *           NA
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void demo() throws IOException, InterruptedException
  {

    try
    {

      int lNumberOfDetectionArms = 1;
      int lNumberOfIlluminationArms = 2;

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      boolean lWriteFile = false;

      RawWriter lRawWriter = new RawWriter();
      lRawWriter.setOverwrite(true);
      File lDesktopFolder = new File(System.getProperty("user.home")
                                     + "/Tmp/simbryo_data");
      lDesktopFolder.mkdirs();

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {

        Drosophila lDrosophila = Drosophila.getDeveloppedEmbryo(11);

        DrosophilaHistoneFluorescence lDrosophilaFluorescencePhantom =
                                                                     new DrosophilaHistoneFluorescence(lContext,
                                                                                                       lDrosophila,
                                                                                                       lPhantomWidth,
                                                                                                       lPhantomHeight,
                                                                                                       lPhantomDepth);
        lDrosophilaFluorescencePhantom.render(true);

        // @SuppressWarnings("unused")

        /*
         * ClearCLImageViewer lFluoPhantomViewer =
         * lDrosophilaFluorescencePhantom.openViewer();/
         **/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 2,
                                                                                                 lPhantomHeight / 2,
                                                                                                 lPhantomDepth / 2);

        lDrosophilaScatteringPhantom.render(true);

        // @SuppressWarnings("unused")
        /*
         * ClearCLImageViewer lScatterPhantomViewer =
         * lDrosophilaScatteringPhantom.openViewer();/
         **/

        LightSheetMicroscopeSimulatorOrtho lSimulator =
                                                      new LightSheetMicroscopeSimulatorOrtho(lContext,
                                                                                             lNumberOfDetectionArms,
                                                                                             lNumberOfIlluminationArms,
                                                                                             lMaxCameraResolution,
                                                                                             lPhantomWidth,
                                                                                             lPhantomHeight,
                                                                                             lPhantomDepth);

        lSimulator.setPhantomParameter(PhantomParameter.Fluorescence,
                                       lDrosophilaFluorescencePhantom.getImage());
        lSimulator.setPhantomParameter(PhantomParameter.Scattering,
                                       lDrosophilaScatteringPhantom.getImage());

        lSimulator.openViewerForControls();

        ClearCLImageViewer lCameraImageViewer =
                                              lSimulator.openViewerForCameraImage(0);
        for (int i = 1; i < lNumberOfDetectionArms; i++)
          lCameraImageViewer = lSimulator.openViewerForCameraImage(i);

        // for (int i = 0; i < lNumberOfIlluminationArms; i++)
        // lSimulator.openViewerForLightMap(i);

        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      0,
                                      1f);
        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      1,
                                      0.2f);

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      0,
                                      50f);
        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      1,
                                      0f);

        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      0,
                                      0f);
        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      1,
                                      20f);

        int i = 0;

        for (float z =
                     -0.0f; z < 0.3
                            && lCameraImageViewer.isShowing(); z +=
                                                                 0.001)
        {

          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        0,
                                        z);
          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        1,
                                        z);

          lSimulator.setNumberParameter(DetectionParameter.Z, 0, z);

          // lDrosophila.simulationSteps(10, 1);
          // lDrosophilaFluorescencePhantom.clear(false);
          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);

          if (lWriteFile)
          {
            File lRawFile = new File(lDesktopFolder,
                                     String.format("output%d.raw",
                                                   i++)); // lDrosophila.getTimeStepIndex()

            System.out.println("Writting: " + lRawFile);
            lRawWriter.write(lSimulator.getCameraImage(0), lRawFile);
          }
        }

        lSimulator.close();
        lDrosophilaScatteringPhantom.close();
        lDrosophilaFluorescencePhantom.close();

      }

      lRawWriter.close();
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  public void simulate_single_view(final int lightsheet,
                                   final int camera,
                                   final int n_planes,
                                   final Matrix4f pMisalignmentCamera,
                                   final String outdir) throws IOException,
                                                        InterruptedException
  {

    try
    {

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      final float z_max = 0.25f;

      boolean lWriteFile = (outdir != null);

      System.out.format("lightsheet =  %d, camera = %d, outdir = %s",
                        lightsheet,
                        camera,
                        outdir);

      RawWriter lRawWriter = new RawWriter();
      lRawWriter.setOverwrite(true);
      File lDesktopFolder = null;
      if (lWriteFile)
      {
        lDesktopFolder = new File(outdir);
        lDesktopFolder.mkdirs();
      }

      // ElapsedTime.sStandardOutput = true;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try (ClearCL lClearCL = new ClearCL(lBestBackend);
          ClearCLDevice lFastestGPUDevice =
                                          lClearCL.getFastestGPUDeviceForImages();
          ClearCLContext lContext = lFastestGPUDevice.createContext())
      {

        Drosophila lDrosophila = Drosophila.getDeveloppedEmbryo(11);

        DrosophilaHistoneFluorescence lDrosophilaFluorescencePhantom =
                                                                     new DrosophilaHistoneFluorescence(lContext,
                                                                                                       lDrosophila,
                                                                                                       lPhantomWidth,
                                                                                                       lPhantomHeight,
                                                                                                       lPhantomDepth);
        lDrosophilaFluorescencePhantom.render(true);

        // @SuppressWarnings("unused")

        /*
         * ClearCLImageViewer lFluoPhantomViewer =
         * lDrosophilaFluorescencePhantom.openViewer();/
         **/

        DrosophilaScatteringPhantom lDrosophilaScatteringPhantom =
                                                                 new DrosophilaScatteringPhantom(lContext,
                                                                                                 lDrosophila,
                                                                                                 lDrosophilaFluorescencePhantom,
                                                                                                 lPhantomWidth / 2,
                                                                                                 lPhantomHeight / 2,
                                                                                                 lPhantomDepth / 2);

        lDrosophilaScatteringPhantom.render(true);

        // @SuppressWarnings("unused")
        /*
         * ClearCLImageViewer lScatterPhantomViewer =
         * lDrosophilaScatteringPhantom.openViewer();/
         **/

        LightSheetMicroscopeSimulatorXWing lSimulator =
                                                      new LightSheetMicroscopeSimulatorXWing(lContext,
                                                                                             pMisalignmentCamera,
                                                                                             lMaxCameraResolution,
                                                                                             lPhantomWidth,
                                                                                             lPhantomHeight,
                                                                                             lPhantomDepth);

        lSimulator.setPhantomParameter(PhantomParameter.Fluorescence,
                                       lDrosophilaFluorescencePhantom.getImage());
        lSimulator.setPhantomParameter(PhantomParameter.Scattering,
                                       lDrosophilaScatteringPhantom.getImage());

        lSimulator.openViewerForControls();

        ClearCLImageViewer lCameraImageViewer =
                                              lSimulator.openViewerForCameraImage(0);

        for (int i = 0; i < 4; i++)
        {
          lSimulator.setNumberParameter(IlluminationParameter.Height,
                                        i,
                                        1.2f);
          lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                        i,
                                        50.f);

        }

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      lightsheet,
                                      50f);

        for (int counter = 0; counter < n_planes; counter++)
        {

          final float z = -z_max
                          + counter * 2.f * z_max / (n_planes - 1.f);

          System.out.println(z + " " + counter);
          for (int i = 0; i < 4; i++)
            lSimulator.setNumberParameter(IlluminationParameter.Z,
                                          i,
                                          z);

          lSimulator.setNumberParameter(DetectionParameter.Z, 0, z);
          lSimulator.setNumberParameter(DetectionParameter.Z, 1, -z);

          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);

          if (lWriteFile)
          {
            File lRawFile = new File(lDesktopFolder,
                                     String.format("plane_%01d_%01d_%01d_%01d_%04d.raw",
                                                   lMaxCameraResolution,
                                                   lMaxCameraResolution,
                                                   camera,
                                                   lightsheet,
                                                   counter)); // lDrosophila.getTimeStepIndex()
            lRawWriter.setOverwrite(true);

            // System.out.println("Writing: " + lRawFile);
            lRawWriter.write(lSimulator.getCameraImage(camera),
                             lRawFile);
          }
        }

        lSimulator.close();
        lDrosophilaScatteringPhantom.close();
        lDrosophilaFluorescencePhantom.close();

      }

      lRawWriter.close();
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  @Test
  public void demo_simul_all() throws IOException,
                               InterruptedException
  {

    final float euler_x = 0.0f;
    final float euler_y = 1.57f;
    final float euler_z = 0.0f;

    final float dx = 1.f / 1023;
    final float translate_x = -0 * dx;
    final float translate_y = 0 * 14 * dx;
    final float translate_z = 0 * 2 * dx;

    // build euler matrix
    final Vector3f lCenter = new Vector3f(0.5f, 0.5f, 0.5f);

    Matrix4f lMatrix = GeometryUtils.rotX(euler_x, lCenter);
    lMatrix.mul(GeometryUtils.rotY(euler_y, lCenter), lMatrix);
    lMatrix.mul(GeometryUtils.rotZ(euler_z, lCenter), lMatrix);

    // Stackgenerator lStackgenerator = new Stackgenerator(lMatrix);
    Stackgenerator lStackgenerator = new Stackgenerator();

    for (int camera = 0; camera < 2; camera++)
      lStackgenerator.generate_stack(camera,
                                     0,
                                     64,
                                     System.getProperty("user.home")
                                         + String.format("/Temp/xscope/%d",
                                                         camera));

    // for(int camera=0; camera<2;camera++)
    // for(int sheet=0; sheet<4;sheet++)
    // lStackgenerator.generate_view(camera, sheet, 128,
    // System.getProperty("user.home")
    // + "/Tmp/xscope");
    //

  }

  @Test
  public void demo_single_plane() throws IOException,
                                  InterruptedException
  {

    final float euler_x = 0.0f;
    final float euler_y = 1.57f;
    final float euler_z = 0.0f;

    final float dx = 1.f / 1023;
    final float translate_x = -0 * dx;
    final float translate_y = 0 * 14 * dx;
    final float translate_z = 0 * 2 * dx;

    // build euler matrix
    final Vector3f lCenter = new Vector3f(0.5f, 0.5f, 0.5f);

    Matrix4f lMatrix = GeometryUtils.rotX(euler_x, lCenter);
    lMatrix.mul(GeometryUtils.rotY(euler_y, lCenter), lMatrix);
    lMatrix.mul(GeometryUtils.rotZ(euler_z, lCenter), lMatrix);

    // Stackgenerator lStackgenerator = new Stackgenerator(lMatrix);
    Stackgenerator lStackgenerator = new Stackgenerator();

    lStackgenerator.generate_view(1, 0, -.1f, null);
    lStackgenerator.generate_view(1, 0, 0.1f, null);
  }

  @Test
  public void demo_single_stack() throws IOException,
                                  InterruptedException
  {

    final float euler_x = 0.0f;
    final float euler_y = 1.57f;
    final float euler_z = 0.0f;

    final float dx = 1.f / 1023;
    final float translate_x = -0 * dx;
    final float translate_y = 0 * 14 * dx;
    final float translate_z = 0 * 2 * dx;

    // build euler matrix
    final Vector3f lCenter = new Vector3f(0.5f, 0.5f, 0.5f);

    Matrix4f lMatrix = GeometryUtils.rotX(euler_x, lCenter);
    lMatrix.mul(GeometryUtils.rotY(euler_y, lCenter), lMatrix);
    lMatrix.mul(GeometryUtils.rotZ(euler_z, lCenter), lMatrix);

    // Stackgenerator lStackgenerator = new Stackgenerator(lMatrix);
    Stackgenerator lStackgenerator = new Stackgenerator();

    lStackgenerator.generate_stack(1,
                                   0,
                                   64,
                                   System.getProperty("user.home")
                                       + "/Tmp/xscope");
  }

}
