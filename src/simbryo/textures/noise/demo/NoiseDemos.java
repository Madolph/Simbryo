package simbryo.textures.noise.demo;

import org.junit.Test;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.ImageChannelDataType;
import clearcl.viewer.ClearCLImageViewer;
import simbryo.textures.noise.BSplineNoise;
import simbryo.textures.noise.SimplexNoise;

public class NoiseDemos
{

  @Test
  public void demoSimplexNoise2D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      SimplexNoise lSimplexNoise = new SimplexNoise(2);
      lSimplexNoise.setScale(0.1f, 0.1f, 0.1f);

      float[] lTexture = lSimplexNoise.generateTexture(128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }

  }

  @Test
  public void demoSimplexNoise3D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      SimplexNoise lSimplexNoise = new SimplexNoise(3);
      lSimplexNoise.setScale(0.1f, 0.1f, 0.1f);

      float[] lTexture = lSimplexNoise.generateTexture(128, 128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }

  }

  @Test
  public void demoBSplineNoise2D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      BSplineNoise lBSplineNoise = new BSplineNoise(2);
      lBSplineNoise.setScale(0.1f, 0.1f, 0.1f);

      float[] lTexture = lBSplineNoise.generateTexture(128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }
  }

  @Test
  public void demoBSplineNoise3D()
  {
    ClearCLBackendInterface lBestBackend =
                                         ClearCLBackends.getBestBackend();
    System.out.println("lBestBackend=" + lBestBackend);
    try (ClearCL lClearCL = new ClearCL(lBestBackend))
    {
      ClearCLDevice lFastestGPUDevice =
                                      lClearCL.getFastestGPUDeviceForImages();

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      BSplineNoise lBSplineNoise = new BSplineNoise(3);
      lBSplineNoise.setScale(0.1f, 0.1f, 0.1f);

      float[] lTexture = lBSplineNoise.generateTexture(128, 128, 128);

      ClearCLImage lClearCLImage =
                                 lContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                                   128,
                                                                   128,
                                                                   128);

      lClearCLImage.readFrom(lTexture, true);

      ClearCLImageViewer lView =
                               ClearCLImageViewer.view(lClearCLImage);

      lView.waitWhileShowing();

    }

  }

}