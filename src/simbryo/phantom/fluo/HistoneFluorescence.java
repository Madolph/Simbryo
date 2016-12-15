package simbryo.phantom.fluo;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLDevice;
import clearcl.ClearCLProgram;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;
import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.phantom.ClearCLPhantomRendererBase;
import simbryo.phantom.PhantomRendererInterface;

public class HistoneFluorescence extends ClearCLPhantomRendererBase
                                 implements PhantomRendererInterface
{
  private ClearCLBuffer mNeighboorsBuffer, mPositionsBuffer,
      mRadiiBuffer;
  private OffHeapMemory mNeighboorsMemory, mPositionsMemory,
      mRadiiMemory;

  public HistoneFluorescence(ClearCLDevice pDevice,
                             TissueDynamics pEmbryo,
                             long... pStackDimensions) throws IOException
  {
    super(pDevice, pEmbryo, pStackDimensions);

    ClearCLProgram lProgram =
                            mContext.createProgram(this.getClass(),
                                                   "kernel/HistoneFluoRender.cl");

    final int lMaxParticlesPerGridCell =
                                       pEmbryo.getNeighborhoodGrid()
                                              .getMaxParticlesPerGridCell();

    lProgram.addDefine("MAXNEI", "" + lMaxParticlesPerGridCell);
    lProgram.buildAndLog();

    mRenderKernel = lProgram.createKernel("gaussrender");

    final int lDimension = mEmbryo.getDimension();

    final int lNeighboorsArrayLength = pEmbryo.getNeighborhoodGrid()
                                              .getVolume()
                                       * lMaxParticlesPerGridCell;

    mNeighboorsBuffer =
                      mContext.createBuffer(HostAccessType.WriteOnly,
                                            KernelAccessType.ReadOnly,
                                            NativeTypeEnum.Int,
                                            lNeighboorsArrayLength);

    mPositionsBuffer =
                     mContext.createBuffer(HostAccessType.WriteOnly,
                                           KernelAccessType.ReadOnly,
                                           NativeTypeEnum.Float,
                                           lDimension * pEmbryo.getMaxNumberOfParticles());

    mRadiiBuffer =
                 mContext.createBuffer(HostAccessType.WriteOnly,
                                       KernelAccessType.ReadOnly,
                                       NativeTypeEnum.Float,
                                       pEmbryo.getMaxNumberOfParticles());

    mNeighboorsMemory =
                      OffHeapMemory.allocateInts(lNeighboorsArrayLength);

    mPositionsMemory =
                     OffHeapMemory.allocateFloats(lDimension
                                                  * pEmbryo.getMaxNumberOfParticles());
    mRadiiMemory =
                 OffHeapMemory.allocateFloats(pEmbryo.getMaxNumberOfParticles());

    mRenderKernel.setArgument("image", mImage);
    mRenderKernel.setArgument("neighboors", mNeighboorsBuffer);
    mRenderKernel.setArgument("positions", mPositionsBuffer);
    mRenderKernel.setArgument("radii", mRadiiBuffer);
    //mRenderKernel.setLocalMemoryArgument("localneighboorsP", NativeTypeEnum.Int, lMaxParticlesPerGridCell);

  }

  private void updateBuffers()
  {
    final int lDimension = mEmbryo.getDimension();
    final int lNumberOfCells = mEmbryo.getMaxNumberOfParticles();
    final int lMaximalNumberOfNeighboorsPerCell =
                                                mEmbryo.getMaxNumberOfParticlesPerGridCell();
    final int lGridVolume = mEmbryo.getNeighborhoodGrid().getVolume();

    mNeighboorsMemory.copyFrom(mEmbryo.getNeighborhoodGrid()
                                      .getArray());

    mRadiiMemory.copyFrom(mEmbryo.getRadii().getCurrentArray(),
                          0,
                          0,
                          lNumberOfCells);

    mPositionsMemory.copyFrom(mEmbryo.getPositions()
                                     .getCurrentArray(),
                              0,
                              0,
                              lDimension * lNumberOfCells);

    mNeighboorsBuffer.readFrom(mNeighboorsMemory,
                               0,
                               lGridVolume
                                  * lMaximalNumberOfNeighboorsPerCell,
                               false);

    mPositionsBuffer.readFrom(mPositionsMemory,
                              0,
                              lDimension * lNumberOfCells,
                              false);
    mRadiiBuffer.readFrom(mRadiiMemory, 0, lNumberOfCells, true);
  }

  @Override
  public void clear()
  {
    super.clear();
    updateBuffers();
  }

  @Override
  public boolean render(int pZPlaneIndex)
  {
    mRenderKernel.setArgument("num", mEmbryo.getNumberOfParticles());
    return super.render(pZPlaneIndex);
  }

  @Override
  public void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd)
  {
    mRenderKernel.setArgument("num", mEmbryo.getNumberOfParticles());
    super.render(pZPlaneIndexBegin, pZPlaneIndexEnd);
  }

}