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
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrthoNonregistered;
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

  public void simulate_single_beams2(final int side,
                                     final float angle,
                                     final int n_planes,
                                     final String outdir) throws IOException,
                                                          InterruptedException
  {

    try
    {

      int lNumberOfDetectionArms = 1;
      int lNumberOfIlluminationArms = 2;

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      final float z_max = 0.25f;

      boolean lWriteFile = (outdir != null);

      System.out.format("side =  %d, angle = %f, outdir = %s",
                        side,
                        angle,
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
         * mer ClearCLImageViewer lFluoPhantomViewer =
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
                                      1.2f);
        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      1,
                                      1.2f);

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      side,
                                      50f);
        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      1 - side,
                                      0f);

        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      side,
                                      angle);
        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      1 - side,
                                      0f);

        int counter = 0;

        for (float z =
                     -z_max; z <= z_max
                             && lCameraImageViewer.isShowing(); z +=
                                                                  2.f * z_max
                                                                     / (n_planes
                                                                        - 1.f))
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
            File lRawFile =
                          new File(lDesktopFolder,
                                   String.format("plane_%01d_%01d_%04d.raw",
                                                 side,
                                                 (angle < 0) ? 0 : 1,
                                                 counter++)); // lDrosophila.getTimeStepIndex()

            // System.out.println("Writting: " + lRawFile);
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

  @Test
  public void demo_single_arms() throws IOException,
                                 InterruptedException
  {

    // simulate_single_beams(1, 20.f, 51, null);

    for (int side = 0; side < 2; side++)
      for (int angle_mode = 0; angle_mode < 2; angle_mode++)
        simulate_single_beams2(side,
                               ((angle_mode < 1) ? -1.f : 1.f) * 20.f,
                               101,
                               System.getProperty("user.home")
                                    + "/Tmp/xscope");
  }

  public void simulate_single_view(final int camera,
                                   final int side,
                                   final float angle,
                                   final int n_planes,
                                   final String outdir) throws IOException,
                                                        InterruptedException
  {

    try
    {

      int lNumberOfDetectionArms = 2;
      int lNumberOfIlluminationArms = 2;

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      final float z_max = 0.25f;

      boolean lWriteFile = (outdir != null);

      System.out.format("side =  %d, camera = %d, angle = %f, outdir = %s",
                        side,
                        camera,
                        angle,
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
                                      1.2f);
        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      1,
                                      1.2f);

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      side,
                                      50f);
        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      1 - side,
                                      0f);

        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      side,
                                      angle);
        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      1 - side,
                                      0f);

        int counter = 0;

        for (float z =
                     -z_max; z <= z_max
                             && lCameraImageViewer.isShowing(); z +=
                                                                  2.f * z_max
                                                                     / (n_planes
                                                                        - 1.f))
        {

          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        0,
                                        z);
          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        1,
                                        z);

          lSimulator.setNumberParameter(DetectionParameter.Z, 0, z);
          lSimulator.setNumberParameter(DetectionParameter.Z, 1, -z);

          // lDrosophila.simulationSteps(10, 1);
          // lDrosophilaFluorescencePhantom.clear(false);
          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);
          //
          // if (lWriteFile) {
          // File lRawFile = new File(lDesktopFolder,
          // String.format("plane_%01d_%01d_%01d_%01d_%01d_%04d.raw",
          // lMaxCameraResolution,
          // lMaxCameraResolution, camera, side, (angle < 0) ? 0 : 1,
          // counter++)); // lDrosophila.getTimeStepIndex()
          //
          // // System.out.println("Writing: " + lRawFile);
          // lRawWriter.write(lSimulator.getCameraImage(camera), lRawFile);
          // }
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

  public void simulate_single_misaligned(final int camera,
                                         final int side,
                                         final float angle,
                                         final int n_planes,
                                         final String outdir,
                                         final float euler_a,
                                         final float euler_b,
                                         final float euler_c,
                                         final float translate_x,
                                         final float translate_y,
                                         final float translate_z) throws IOException,
                                                                  InterruptedException
  {

    try
    {

      Matrix4f lTransform = GeometryUtils.rotY((float) Math.PI,
                                               new Vector3f(0.5f,
                                                            0.5f,
                                                            0.5f));

      int lMaxCameraResolution = 1024;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      final float z_max = 0.25f;

      boolean lWriteFile = (outdir != null);

      System.out.format("side =  %d, camera = %d, angle = %f, outdir = %s",
                        side,
                        camera,
                        angle,
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

        LightSheetMicroscopeSimulatorOrthoNonregistered lSimulator =
                                                                   new LightSheetMicroscopeSimulatorOrthoNonregistered(lContext,
                                                                                                                       lTransform,
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

        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      0,
                                      1.2f);
        lSimulator.setNumberParameter(IlluminationParameter.Height,
                                      1,
                                      1.2f);

        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      side,
                                      50f);
        lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                      1 - side,
                                      0f);

        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      side,
                                      angle);
        lSimulator.setNumberParameter(IlluminationParameter.Gamma,
                                      1 - side,
                                      0f);

        int counter = 0;

        for (float z =
                     -z_max; z <= z_max
                             && lCameraImageViewer.isShowing(); z +=
                                                                  2.f * z_max
                                                                     / (n_planes
                                                                        - 1.f))
        {

          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        0,
                                        z);
          lSimulator.setNumberParameter(IlluminationParameter.Z,
                                        1,
                                        z);

          lSimulator.setNumberParameter(DetectionParameter.Z, 0, z);
          lSimulator.setNumberParameter(DetectionParameter.Z, 1, -z);

          // lDrosophila.simulationSteps(10, 1);
          // lDrosophilaFluorescencePhantom.clear(false);
          lDrosophilaFluorescencePhantom.render(false);

          lSimulator.render(true);

          if (lWriteFile)
          {
            File lRawFile =
                          new File(lDesktopFolder,
                                   String.format("plane_%01d_%01d_%01d_%01d_%01d_%04d.raw",
                                                 lMaxCameraResolution,
                                                 lMaxCameraResolution,
                                                 camera,
                                                 side,
                                                 (angle < 0) ? 0 : 1,
                                                 counter++)); // lDrosophila.getTimeStepIndex()

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
  public void demo_all_views() throws IOException,
                               InterruptedException
  {

    // simulate_single_beams(1, 20.f, 51, null);
    for (int camera = 0; camera < 2; camera++)
      for (int side = 0; side < 2; side++)
        for (int angle_mode = 0; angle_mode < 2; angle_mode++)
          simulate_single_view(camera,
                               side,
                               ((angle_mode < 1) ? -1.f : 1.f) * 20.f,
                               512,
                               System.getProperty("user.home")
                                    + "/Desktop/xscope/data");
  }

  @Test
  public void demo_single_view() throws IOException,
                                 InterruptedException
  {

    simulate_single_view(0,
                         0,
                         20.f,
                         21,
                         System.getProperty("user.home")
                             + "/Tmp/xscope");
  }

  @Test
  public void demo_single_misaligned() throws IOException,
                                       InterruptedException
  {

    final float euler_a = 0;
    final float euler_b = 0;
    final float euler_c = 0;
    final float translate_x = 0;
    final float translate_y = 0;
    final float translate_z = 0;

    simulate_single_misaligned(0,
                               0,
                               20.f,
                               21,
                               System.getProperty("user.home")
                                   + "/Tmp/xscope",
                               euler_a,
                               euler_b,
                               euler_c,
                               translate_x,
                               translate_y,
                               translate_z);

  }

}
