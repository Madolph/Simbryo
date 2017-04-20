package simbryo.synthoscopy.microscope.aberration;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.MutablePair;

import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;

/**
 * Illumination misalignment
 *
 * @author royer
 */
public class IlluminationMisalignment extends AberrationBase
                                      implements AberrationInterface
{
  private volatile float mOffsetConstant = 1f;

  private HashMap<MutablePair<ParameterInterface<Number>, Integer>, Number> mOffsetMap =
                                                                                       new HashMap<>();

  /**
   * Instanciates an illumination misalignment
   */
  public IlluminationMisalignment()
  {
    super();
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {

  }

  @Override
  public Number transform(ParameterInterface<Number> pParameter,
                          int pIndex,
                          Number pNumber)
  {
    if (!(pParameter instanceof IlluminationParameter))
      return pNumber;

    Number lNumber = pNumber;
    MutablePair<ParameterInterface<Number>, Integer> lKey =
                                                          MutablePair.of(pParameter,
                                                                         pIndex);

    Number lOffset = mOffsetMap.get(lKey);

    if (lOffset == null)
    {
      float lOffsetConstant = adjustOffsetAmount(pParameter);

      lOffset = mOffsetConstant * lOffsetConstant; // * rand(-1, 1);

      mOffsetMap.put(lKey, lOffset);
    }

    lNumber = lNumber.floatValue() + lOffset.floatValue();

    // System.out.format("offset: %s[%d] -> %g
    // \n",pParameter,pIndex,lOffset.floatValue());

    return lNumber;
  }

  protected float adjustOffsetAmount(ParameterInterface<Number> pParameter)
  {
    float lOffsetConstant = 0;
    switch ((IlluminationParameter) pParameter)
    {
    case Alpha:
      lOffsetConstant = 5;
      break;
    case Beta:
      lOffsetConstant = 0;
      break;
    case Gamma:
      lOffsetConstant = 0;
      break;
    case Height:
      lOffsetConstant = 1;
      break;
    case Intensity:
      lOffsetConstant = 0.01f;
      break;
    case Theta:
      lOffsetConstant = 0;
      break;
    case Wavelength:
      lOffsetConstant = 0;
      break;
    case X:
      lOffsetConstant = 30;
      break;
    case Y:
      lOffsetConstant = 30;
      break;
    case Z:
      lOffsetConstant = 30;
      break;
    default:
      break;
    }
    return lOffsetConstant;
  }

}
