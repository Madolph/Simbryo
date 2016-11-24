package embryosim.neighborhood;

import java.util.Arrays;

import embryosim.util.vectorinc.VectorInc;

/**
 * This data structure is an extremely fast way to keep track of the
 * neighborhood of particles in a particle system. The maximal number of
 * neighboors is fixed at construction time.
 *
 * @author royer
 */
public class NeighborhoodCellGrid
{
  private final static float cEpsilon = 1e-6f;

  private final int mDimension;
  private final int mGridSize;
  private final int mMaxParticlesPerGridCell;

  private final int[] mStride;

  private final int[] mNeighboorhoodArray;

  /**
   * Constructs an instance given the maximal number of particles per cell and
   * the grid dimensions.
   * 
   * @param pDimension
   *          dimension
   * @param pGridSize
   *          grid size along each dimension
   * @param pMaxParticlesPerCell
   *          max particles per cell
   */
  public NeighborhoodCellGrid(int pDimension,
                              int pGridSize,
                              int pMaxParticlesPerCell)
  {
    super();
    mDimension = pDimension;
    mGridSize = pGridSize;
    mMaxParticlesPerGridCell = pMaxParticlesPerCell;

    mNeighboorhoodArray = new int[getVolume() * pMaxParticlesPerCell];

    mStride = new int[pDimension];
    int lStride = mMaxParticlesPerGridCell;
    for (int d = 0; d < pDimension; d++)
    {
      mStride[d] = lStride;
      lStride *= mGridSize;
    }

  }

  /**
   * Returns dimension of the grid.
   * 
   * @return dimension
   */
  public int getDimension()
  {
    return mDimension;
  }

  /**
   * Return grid size along each dimension
   * 
   * @return gris size
   */
  public int getGridSize()
  {
    return mGridSize;
  }

  /**
   * Returns the 'volume' of the grid i.e. the number of cells.
   * 
   * @return number of cells
   */
  public int getVolume()
  {
    return (int) Math.pow(mGridSize, mDimension);
  }

  /**
   * Returns a '-1' terminated array of particle ids for a given cell.
   * 
   * @param pCellCoordinates
   *          cell coordinates
   * @return array with ids
   */
  public int[] getCellContents(int... pCellCoordinates)
  {
    return getCellContents(new int[mMaxParticlesPerGridCell],
                           pCellCoordinates);
  }

  /**
   * Returns a '-1' terminated array of particle ids for a given cell. An array
   * has to be provided to store the ids.
   * 
   * @param pNeighboors
   *          allocated array for storing the particle ids.
   * @param pCellCoordinates
   *          cell coordinates
   * @return the provided array filled with ids.
   */
  public int[] getCellContents(int[] pNeighboors,
                               int... pCellCoordinates)
  {
    int lCellIndex = getCellIndex(mDimension,
                                  mStride,
                                  mMaxParticlesPerGridCell,
                                  pCellCoordinates);

    System.arraycopy(mNeighboorhoodArray,
                     lCellIndex,
                     pNeighboors,
                     0,
                     mMaxParticlesPerGridCell);

    return pNeighboors;
  }

  /**
   * Returns a '-1' terminated array of particle ids for a given cell. An array
   * 
   * @param pTestPoint
   *          coordinates in particle space.
   * @return array of ids.
   */
  public final int[] getCellContentsAt(final float... pTestPoint)
  {
    return getCellContentsAt(new int[mMaxParticlesPerGridCell],
                             pTestPoint);
  }

  /**
   * Returns a '-1' terminated array of particle ids for a given cell. An array
   * 
   * @param pNeighboors
   *          preallocated array for storing ids.
   * @param pTestPoint
   *          coordinates in particle space.
   * @return array of ids.
   */
  public final int[] getCellContentsAt(int[] pNeighboors,
                                       final float... pTestPoint)
  {
    int lCellIndex = getCellIndexAtPoint(mDimension,
                                         mGridSize,
                                         mStride,
                                         mMaxParticlesPerGridCell,
                                         pTestPoint);

    System.arraycopy(mNeighboorhoodArray,
                     lCellIndex,
                     pNeighboors,
                     0,
                     mMaxParticlesPerGridCell);

    return pNeighboors;
  }

  /**
   * Returns a string describing the contents of this cell.
   * 
   * @param pCellCoordinates
   *          cell's coordinates
   * @return string describing the contents of this cell.
   */
  public String getCellInfoAt(int... pCellCoordinates)
  {

    int lCellIndex = getCellIndex(mDimension,
                                  mStride,
                                  mMaxParticlesPerGridCell,
                                  pCellCoordinates);

    StringBuilder lStringBuilder = new StringBuilder();
    lStringBuilder.append("Cell: " + Arrays.toString(pCellCoordinates)
                          + ": ");

    while (mNeighboorhoodArray[lCellIndex] != -1)
    {
      lStringBuilder.append("" + mNeighboorhoodArray[lCellIndex]
                            + ", ");
      lCellIndex++;
    }

    return lStringBuilder.toString();
  }

  /**
   * Returns all neighbors for a given particle and radius.
   * 
   * @param pNeighboors
   *          array in which to store the list of ids.
   * @param pNeighboorsTemp
   *          aorking array of same length as pNeighboors.
   * @param pPositions
   *          array of particle positions
   * @param pParticleId
   *          particle id
   * @param pRadius
   *          radius
   * @return number of particle ids written in array.
   */
  public final int getAllNeighborsForParticle(int[] pNeighboors,
                                              int[] pNeighboorsTemp,
                                              float[] pPositions,
                                              int pParticleId,
                                              float pRadius)
  {
    final int lDimension = getDimension();

    final float[] lCellCoord = new float[lDimension];
    final int[] lCellCoordMin = new int[lDimension];
    final int[] lCellCoordMax = new int[lDimension];
    final int[] lCellCoordCurrent = new int[lDimension];

    return getAllNeighborsForParticle(pNeighboors,
                                      pNeighboorsTemp,
                                      pPositions,
                                      pParticleId,
                                      pRadius,
                                      lCellCoord,
                                      lCellCoordMin,
                                      lCellCoordMax,
                                      lCellCoordCurrent);
  }

  /**
   * Returns all neighbors for a given particle and radius. This method
   * delegates the allocation of working arrays to the caller.
   * 
   * @param pNeighboors
   *          array in which to store the list of ids.
   * @param pNeighboors
   *          working array of the same size as pNeighboors, used internally.
   * @param pPositions
   *          array of particle positions
   * @param pParticleId
   *          particle id
   * @param pRadius
   *          radius
   * @param pCellCoord
   *          working array, length must be dimension
   * @param pCellCoordMin
   *          working array, length must be dimension
   * @param pCellCoordMax
   *          working array, length must be dimension
   * @param pCellCoordCurrent
   *          working array, length must be dimension
   * @return number of particle ids written in array.
   */
  public final int getAllNeighborsForParticle(int[] pNeighboors,
                                              int[] pNeighboorsTemp,
                                              float[] pPositions,
                                              int pParticleId,
                                              float pRadius,
                                              float[] pCellCoord,
                                              int[] pCellCoordMin,
                                              int[] pCellCoordMax,
                                              int[] pCellCoordCurrent)
  {
    int lNeighboorCounter = 0;

    final int lDimension = mDimension;
    final int lGridSideLength = mGridSize;
    final int[] lStride = mStride;
    final int lMaxParticlesPerGridCell = mMaxParticlesPerGridCell;
    final int[] lNeighboorhoodArray = mNeighboorhoodArray;
    final float[] lCellCoord = pCellCoord;

    boolean lFullyContainedInCell = true;

    for (int d = 0; d < lDimension; d++)
    {
      float lValue = getCellCoordForParticle(lDimension,
                                             lGridSideLength,
                                             pPositions,
                                             pParticleId,
                                             d);

      lCellCoord[d] = lValue;

      float lInCellCoord = (float) (lValue - Math.floor(lValue));
      float lScaledRadius = pRadius * lGridSideLength;

      lFullyContainedInCell &= (lInCellCoord - lScaledRadius >= 0)
                               && (lInCellCoord + lScaledRadius <= 1);

    }

    if (lFullyContainedInCell)
    {
      lNeighboorCounter = copyCellContentsTo(lNeighboorhoodArray,
                                             lDimension,
                                             lStride,
                                             lMaxParticlesPerGridCell,
                                             pNeighboors,
                                             lCellCoord);

    }
    else
    {
      initCellEnumeration(lDimension,
                          lGridSideLength,
                          lCellCoord,
                          pRadius,
                          pCellCoordMin,
                          pCellCoordMax,
                          pCellCoordCurrent);

      do
      {
        int lNeighboorListIndex = getCellIndex(lDimension,
                                               lStride,
                                               lMaxParticlesPerGridCell,
                                               pCellCoordCurrent);

        for (int i = 0; i < lMaxParticlesPerGridCell; i++)
        {
          int lNeighboorId = lNeighboorhoodArray[lNeighboorListIndex
                                                 + i];
          if (lNeighboorId == -1)
            break;

          if (!contains(pNeighboors, lNeighboorCounter, lNeighboorId))
          {
            pNeighboors[lNeighboorCounter++] = lNeighboorId;
          }

        }

      }
      while (VectorInc.increment(pCellCoordMin,
                                 pCellCoordMax,
                                 pCellCoordCurrent));

      pNeighboors[lNeighboorCounter] = -1;

    }

    return lNeighboorCounter;
  }

  /**
   * Utility method that checks if a particle id is already present in a
   * neighborhood array.
   * 
   * @param pNeighboors
   *          neighborhood array
   * @param pNeighboorCounter
   *          number of neighbors in array
   * @param pNeighboorId
   *          Neighbor to test presence of.
   * @return true if present, false otherwise.s
   */
  private static boolean contains(int[] pNeighboors,
                                  int pNeighboorCounter,
                                  int pNeighboorId)
  {
    for (int i = 0; i < pNeighboorCounter; i++)
      if (pNeighboors[i] == pNeighboorId)
        return true;

    return false;
  }

  /**
   * Clears the ids for all cells
   */
  public final void clear()
  {
    Arrays.fill(mNeighboorhoodArray, -1);
  }

  /**
   * Updates the content of the cells.
   * 
   * @param pPositions
   *          particle position array
   * @param pRadii
   *          particle radii
   */
  public final void updateCells(float[] pPositions, float[] pRadii)
  {
    final int lDimension = getDimension();
    update(pPositions, pRadii, pPositions.length / lDimension);
  }

  /**
   * Updates the content of the cells.
   * 
   * @param pPositions
   *          particle position array
   * @param pRadii
   *          particle radii
   * @param pNumberOfParticles
   *          number of particles to consider in arrays.
   */
  public final void update(float[] pPositions,
                           float[] pRadii,
                           int pNumberOfParticles)
  {
    final int lDimension = getDimension();
    final int lGridSideLength = mGridSize;
    final int[] lStride = mStride;
    final int lMaxParticlesPerGridCell = mMaxParticlesPerGridCell;
    final int[] lNeighboorhoodArray = mNeighboorhoodArray;

    final float[] lCellCoord = new float[lDimension];
    final int[] lCellCoordMin = new int[lDimension];
    final int[] lCellCoordMax = new int[lDimension];
    final int[] lCellCoordCurrent = new int[lDimension];

    for (int id = 0; id < pNumberOfParticles; id++)
    {
      addParticleToCells(lNeighboorhoodArray,
                         lDimension,
                         lGridSideLength,
                         lStride,
                         lMaxParticlesPerGridCell,
                         pPositions,
                         pRadii,
                         id,
                         lCellCoord,
                         lCellCoordMin,
                         lCellCoordMax,
                         lCellCoordCurrent);
    }
  }

  /**
   * Adds a given particle to all cells that it touches.
   * 
   * @param pNeighboorhoodArray
   * @param pDimension
   *          dimension
   * @param pGridSize
   *          grid size
   * @param pStride
   *          precomputed strides
   * @param pMaxParticlesPerGridCell
   *          max particles per cell
   * @param pPositions
   *          positions array
   * @param pRadii
   *          radii array
   * @param pParticleId
   *          particle id
   * @param pCellCoord
   *          working array, length must be dimension
   * @param pCellCoordMin
   *          working array, length must be dimension
   * @param pCellCoordMax
   *          working array, length must be dimension
   * @param pCellCoordCurrent
   *          working array, length must be dimension
   */
  private static final void addParticleToCells(int[] pNeighboorhoodArray,
                                               int pDimension,
                                               int pGridSize,
                                               int[] pStride,
                                               int pMaxParticlesPerGridCell,
                                               float[] pPositions,
                                               float[] pRadii,
                                               int pParticleId,
                                               float[] pCellCoord,
                                               int[] pCellCoordMin,
                                               int[] pCellCoordMax,
                                               int[] pCellCoordCurrent)
  {

    final float lRadius = pRadii[pParticleId];
    float[] lCellCoord = pCellCoord;

    boolean lFullyContainedInCell = true;

    for (int d = 0; d < pDimension; d++)
    {
      float lValue = getCellCoordForParticle(pDimension,
                                             pGridSize,
                                             pPositions,
                                             pParticleId,
                                             d);

      lCellCoord[d] = lValue;

      float lInCellCoord = (float) (lValue - Math.floor(lValue));
      float lScaledRadius = lRadius * pGridSize;

      lFullyContainedInCell &= (lInCellCoord - lScaledRadius >= 0)
                               && (lInCellCoord
                                   + lScaledRadius <= 1);/**/

    }

    if (lFullyContainedInCell)
    {
      int lNeighboorListIndex = getCellIndexForParticle(pDimension,
                                                        pGridSize,
                                                        pStride,
                                                        pPositions,
                                                        pParticleId);
      addParticleToCell(pNeighboorhoodArray,
                        pMaxParticlesPerGridCell,
                        lNeighboorListIndex,
                        pParticleId);
    }
    else/**/
    {

      initCellEnumeration(pDimension,
                          pGridSize,
                          lCellCoord,
                          lRadius,
                          pCellCoordMin,
                          pCellCoordMax,
                          pCellCoordCurrent);

      do
      {
        int lNeighboorListIndex = getCellIndex(pDimension,
                                               pStride,
                                               pMaxParticlesPerGridCell,
                                               pCellCoordCurrent);
        addParticleToCell(pNeighboorhoodArray,
                          pMaxParticlesPerGridCell,
                          lNeighboorListIndex,
                          pParticleId);

      }
      while (VectorInc.increment(pCellCoordMin,
                                 pCellCoordMax,
                                 pCellCoordCurrent));
    }
  }

  /**
   * Initializes working arrays for traversing a region of the grid.
   * 
   * @param pDimension
   *          dimension
   * @param pGridSize
   *          grid size
   * @param pCellCoord
   *          cell coordinates
   * @param pRadius
   *          particle radius
   * @param pCellCoordMin
   *          working array, length must be dimension
   * @param pCellCoordMax
   *          working array, length must be dimension
   * @param pCellCoordCurrent
   *          working array, length must be dimension
   */
  private static final void initCellEnumeration(int pDimension,
                                                int pGridSize,
                                                float[] pCellCoord,
                                                final float pRadius,
                                                int[] pCellCoordMin,
                                                int[] pCellCoordMax,
                                                int[] pCellCoordCurrent)
  {

    for (int d = 0; d < pDimension; d++)
    {
      float lInfluenceRadius = pRadius * pGridSize;
      pCellCoordMin[d] =
                       (int) Math.max(0,
                                      Math.min(pGridSize - 1,
                                               (pCellCoord[d]
                                                - lInfluenceRadius)));
      pCellCoordMax[d] =
                       1 + (int) Math.max(0,
                                          Math.min(pGridSize - 1,
                                                   (pCellCoord[d]
                                                    + lInfluenceRadius)));
      pCellCoordCurrent[d] = pCellCoordMin[d];
    }
  }

  /**
   * Adds a given particle to a cell.
   * 
   * @param pNeighboorhoodArray
   *          neighborhood array
   * @param pMaxParticlesPerGridCell
   *          max particles per cell
   * @param pCellIndex
   *          cell index
   * @param pParticleId
   *          particle id
   * @return new index
   */
  private static final int addParticleToCell(int[] pNeighboorhoodArray,
                                             int pMaxParticlesPerGridCell,
                                             int pCellIndex,
                                             int pParticleId)
  {
    int k;
    for (k = 0; k < pMaxParticlesPerGridCell; k++)
      if (pNeighboorhoodArray[pCellIndex + k] == -1)
        break;

    if (k < pMaxParticlesPerGridCell)
      pNeighboorhoodArray[pCellIndex + k] = pParticleId;
    return k;
  }

  /**
   * Returns a particle's coordinate in cell space along a given dimension
   * index.
   * 
   * @param pDimension
   *          dimensions
   * @param pGridSize
   *          grid size
   * @param pPositions
   *          postions array
   * @param pParticleID
   *          particle id
   * @param pDimensionIndex
   *          dimension index
   * @return particle coordinate in cell space.
   */
  private static final float getCellCoordForParticle(int pDimension,
                                                     int pGridSize,
                                                     float[] pPositions,
                                                     int pParticleID,
                                                     int pDimensionIndex)
  {
    final int i = pParticleID * pDimension + pDimensionIndex;
    final float lParticleCoordinate = pPositions[i];
    return getParticleCoordinateInCell(pGridSize,
                                       pDimensionIndex,
                                       lParticleCoordinate);
  }

  /**
   * Converts cell coordinate from particle space to cell space.
   * 
   * @param pGridSize
   *          grid size
   * @param pDimensionIndex
   *          dimension index
   * @param pParticleCoordinate
   * @return particle coordinate in cell space.
   */
  private static final float getParticleCoordinateInCell(final int pGridSize,
                                                         final int pDimensionIndex,
                                                         final float pParticleCoordinate)
  {
    // cEpsilon is to make sure that we never see the value pGridSize as
    // coordinate...
    return pParticleCoordinate * (1.0f * pGridSize - cEpsilon);
  }

  /**
   * Copies this cell's ids to an array.
   * 
   * @param pNeighboorhoodArray
   *          neighborhood array
   * @param pDimension
   *          dimension
   * @param pStride
   *          precomputed strides
   * @param pMaxParticlesPerGridCell
   *          max particles per cell
   * @param pNeighboors
   *          array to copy to
   * @param pCellCoordinate
   *          cell's coordinates
   * @return
   */
  private static int copyCellContentsTo(int[] pNeighboorhoodArray,
                                        int pDimension,
                                        int[] pStride,
                                        int pMaxParticlesPerGridCell,
                                        int[] pNeighboors,
                                        float... pCellCoordinate)
  {
    int lCellIndex = getCellIndex(pDimension,
                                  pStride,
                                  pMaxParticlesPerGridCell,
                                  pCellCoordinate);

    for (int k = 0; k < pMaxParticlesPerGridCell; k++)
    {
      int lId = pNeighboorhoodArray[lCellIndex + k];
      if (lId == -1)
        return k;

      pNeighboors[k] = lId;
    }

    return pMaxParticlesPerGridCell;
  }

  /**
   * Returns a cell's index from its coordinates.
   * 
   * @param pDimension
   *          dimension
   * @param pStride
   *          precomputed strides
   * @param pMaxParticlesPerGridCell
   *          max particles per cell
   * @param pCellCoordinates
   *          cell's coordinates
   * @return
   */
  private static final int getCellIndex(int pDimension,
                                        int[] pStride,
                                        int pMaxParticlesPerGridCell,
                                        int[] pCellCoordinates)
  {
    int lIndex = 0;
    for (int d = 0; d < pDimension; d++)
      lIndex += pStride[d] * pCellCoordinates[d];

    return lIndex;
  }

  /**
   * Returns the cell index for a given particle.
   * 
   * @param pDimension
   *          dimension
   * @param pGridSize
   *          grid size
   * @param pStride
   *          precomputed strides
   * @param pPositions
   *          positions array
   * @param pParticleId
   *          particle id
   * @return cell index
   */
  private static final int getCellIndexForParticle(int pDimension,
                                                   int pGridSize,
                                                   int[] pStride,
                                                   final float[] pPositions,
                                                   int pParticleId)
  {

    int lIndex = 0;
    for (int d = 0; d < pDimension; d++)
    {
      int lComponent = (int) getCellCoordForParticle(pDimension,
                                                     pGridSize,
                                                     pPositions,
                                                     pParticleId,
                                                     d);
      lIndex += pStride[d] * lComponent;
    }
    return lIndex;
  }

  /**
   * Returns a cell's index from coordinates in cell space.
   * 
   * @param pDimension
   *          dimensions
   * @param pStride
   *          precomputed strides
   * @param pMaxParticlesPerGridCell
   *          max particles per cell
   * @param pCellCoordinate
   * @return
   */
  private static final int getCellIndex(int pDimension,
                                        int[] pStride,
                                        int pMaxParticlesPerGridCell,
                                        final float[] pCellCoordinate)
  {

    int lIndex = 0;
    for (int d = 0; d < pDimension; d++)
    {
      lIndex += pStride[d] * (int) pCellCoordinate[d];
    }
    return lIndex;
  }

  /**
   * @param pDimension
   *          dimesion
   * @param pGridSize
   *          grid size
   * @param pStride
   *          precomputd strides
   * @param pMaxParticlesPerGridCell
   *          max particles per cell
   * @param pCellCoordinate
   *          cell coordinates
   * @return
   */
  private static final int getCellIndexAtPoint(int pDimension,
                                               int pGridSize,
                                               int[] pStride,
                                               int pMaxParticlesPerGridCell,
                                               final float[] pCellCoordinate)
  {

    int lIndex = 0;
    for (int d = 0; d < pDimension; d++)
    {
      lIndex += pStride[d]
                * (int) (getParticleCoordinateInCell(pGridSize,
                                                     d,
                                                     pCellCoordinate[d]));
    }
    return lIndex;
  }

}
