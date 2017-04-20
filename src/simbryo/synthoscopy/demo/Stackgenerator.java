package simbryo.synthoscopy.demo;

import java.io.File;
import java.io.IOException;

import javax.vecmath.Matrix4f;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.io.RawWriter;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorXWing;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

public class Stackgenerator
{

  private static final float z_max = 0.25f;
  private static final int lMaxCameraResolution = 1024;
  private LightSheetMicroscopeSimulatorXWing lSimulator;
  private DrosophilaScatteringPhantom lDrosophilaScatteringPhantom;
  private DrosophilaHistoneFluorescence lDrosophilaFluorescencePhantom;
  private Drosophila lDrosophila;
  private ClearCLContext lContext;

  private static Matrix4f createIdentity()
  {

    final Matrix4f lMisalignmentCamera = new Matrix4f();
    lMisalignmentCamera.setIdentity();
    return lMisalignmentCamera;

  }

  public Stackgenerator()
  {
    this(createIdentity());
  }

  public Stackgenerator(final Matrix4f pMisalignmentCamera)
  {

    try
    {

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      ClearCLBackendInterface lBestBackend =
                                           ClearCLBackends.getBestBackend();

      try
      {
        ClearCL lClearCL = new ClearCL(lBestBackend);

        ClearCLDevice lFastestGPUDevice =
                                        lClearCL.getFastestGPUDeviceForImages();

        lContext = lFastestGPUDevice.createContext();
      }
      catch (Throwable e)
      {
        e.printStackTrace();
      }

      lDrosophila = Drosophila.getDeveloppedEmbryo(11);

      lDrosophilaFluorescencePhantom =
                                     new DrosophilaHistoneFluorescence(lContext,
                                                                       lDrosophila,
                                                                       lPhantomWidth,
                                                                       lPhantomHeight,
                                                                       lPhantomDepth);
      lDrosophilaFluorescencePhantom.render(true);

      lDrosophilaScatteringPhantom =
                                   new DrosophilaScatteringPhantom(lContext,
                                                                   lDrosophila,
                                                                   lDrosophilaFluorescencePhantom,
                                                                   lPhantomWidth / 2,
                                                                   lPhantomHeight / 2,
                                                                   lPhantomDepth / 2);

      lDrosophilaScatteringPhantom.render(true);

      lSimulator =
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

    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  public void generate_view(final int camera,
                            final int lightsheet,
                            final int n_planes,
                            final String outdir) throws IOException
  {

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

    for (int i = 0; i < 4; i++)
    {
      lSimulator.setNumberParameter(IlluminationParameter.Height,
                                    i,
                                    1.2f);
      lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                    i,
                                    0.f);

    }

    lSimulator.setNumberParameter(IlluminationParameter.Intensity,
                                  lightsheet,
                                  5.f);

    for (int counter = 0; counter < n_planes; counter++)
    {

      final float z =
                    -z_max + counter * 2.f * z_max / (n_planes - 1.f);

      System.out.println(z + " " + counter);
      for (int i = 0; i < 4; i++)
        lSimulator.setNumberParameter(IlluminationParameter.Z, i, z);

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
        lRawWriter.write(lSimulator.getCameraImage(camera), lRawFile);
      }
    }

  }

  @Override
  public void finalize() throws Exception
  {
    lSimulator.close();
    lDrosophilaScatteringPhantom.close();
    lDrosophilaFluorescencePhantom.close();

  }

}
