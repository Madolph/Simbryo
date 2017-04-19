
#include [OCLlib] "imageops/imageops.cl" 


__kernel void combine2(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __write_only   image3d_t  imagedest
                     )
{
  add2(image0,image1,1.0f,1.0f,imagedest);
}

__kernel void combine4(    __read_only    image3d_t  image0,
                           __read_only    image3d_t  image1,
                           __read_only    image3d_t  image2,
                           __read_only    image3d_t  image3,
                           __write_only   image3d_t  imagedest
                     )
{
  add4(image0,image1,image2,image3,1.0f,1.0f,1.0f,1.0f,imagedest);
}